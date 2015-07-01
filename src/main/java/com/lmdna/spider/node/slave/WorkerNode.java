package com.lmdna.spider.node.slave;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.JMException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.Pipeline;

import com.lmdna.spider.SpiderDAOServiceFacade;
import com.lmdna.spider.boot.SpiderProcess;
import com.lmdna.spider.dao.model.SpiderBiz;
import com.lmdna.spider.dao.model.SpiderFieldRule;
import com.lmdna.spider.mongodb.MongoDBTemplate;
import com.lmdna.spider.monitor.SpiderMonitor;
import com.lmdna.spider.monitor.SpiderStatusMXBean;
import com.lmdna.spider.node.SpiderContext;
import com.lmdna.spider.node.SpiderNode;
import com.lmdna.spider.pipeline.MongoDBPipeline;
import com.lmdna.spider.protocol.TaskFileParseProtocol;
import com.lmdna.spider.protocol.rpc.HeartBeatProtocol;
import com.lmdna.spider.protocol.rpc.utils.FileRequestObject;
import com.lmdna.spider.protocol.rpc.utils.HeartBeatData;
import com.lmdna.spider.protocol.rpc.utils.RemoteCmd;
import com.lmdna.spider.protocol.rpc.utils.SpiderStatusSerialization;
import com.lmdna.spider.utils.SpiderGlobalConfig;

/**
 * @author ayumiono
 *spider slave节点类
 *slave节点组件包括一个spider线程池、工作线程池（任务扫描线程、心跳发送线程）
 *version1.0.0:新增一个jetty server来处理远程控制请求
 */
@Deprecated
public class WorkerNode extends SpiderNode{
	
	private static final Logger logger = LoggerFactory.getLogger(SpiderProcess.class);
	private ExecutorService spider_pool;
	private ExecutorService worker;
	private SpiderMonitor monitor = SpiderMonitor.instance();
	private Map<String, Spider> spiders;
	private BlockingQueue<Request> reqQueue;
	private SpiderDAOServiceFacade facade;
	
	private Map<String,ClassLoader> classLoaderCache;//bizcode - classloader
	private Map<String,TaskFileParseProtocol> taskFileParserCache;//bizcode - taskFileParser
	private Map<String,String> taskFilePathCache;//taskid-taskfilepath
	private Map<String,String> jarFilePathCache;//bizcode-jarfilepath
	
	private HeartBeatProtocol heartBeatProtocol;//心跳接口
	
	private SpiderContext _config;
	
	public WorkerNode(SpiderContext _config){
		this._config = _config;
	}
	
	private void initComponent(){
		logger.info("Slave Node:start to initialize spider component...");
		try {
			this.spider_pool = Executors.newFixedThreadPool(Integer.parseInt(SpiderGlobalConfig.getValue(SpiderGlobalConfig.SPIDER_POOL_SIZE)),new SpiderFactory());//spider线程池
			this.spiders = new ConcurrentHashMap<String, Spider>();
			this.worker = Executors.newFixedThreadPool(Integer.parseInt(SpiderGlobalConfig.getValue("spider.readerpool.size")),new BatchReaderFactory());
			this.reqQueue = new LinkedBlockingQueue<Request>(Integer.parseInt(SpiderGlobalConfig.getValue(SpiderGlobalConfig.SPIDER_SLAVE_REQQUEUE_CEILING)));// 最大50万条请求，超出50万条等待 
			this.facade = SpiderDAOServiceFacade.getInstance();
			this.classLoaderCache = new HashMap<String,ClassLoader>();
			loadSpider();
			logger.info("Slave Node:components initialize completed.");
		} catch (Exception e) {
			logger.error("SpiderSlave:components initialize failed!", e);
		}
	}
	
	private void loadSpider(){
		if ("on".equals(SpiderGlobalConfig.getValue(SpiderGlobalConfig.SPIDER_AUTOLOAD_SWITCH))){
			SpiderBiz conditionBean = new SpiderBiz();
			conditionBean.setStatus(0);
			Map<String,Object> querymap = new HashMap<String,Object>();
			querymap.put("status", 0);
			//TODO
			List<SpiderBiz> bizList = facade.getBizList(querymap);
			logger.info("Slave Node:start to autoload spider...");
			for (SpiderBiz biz : bizList) {
				loadSpider(biz);
			}
		}else{
			logger.info("Slave Node:spider.autoload.switch turned off.");
		}
		logger.info(String.format("SpiderSlave:spider loading completed,容器中现共有%d个spider", spiders.size()));
	}
	
	public void run(){
		initComponent();
		this.worker.execute(new ReqQueueConsumeThread());//开启任务消费线程
		this.worker.execute(new HeartBeatThread());//开启定时心跳线程
	}
	
	private void loadSpider(SpiderBiz biz){
		final List<SpiderFieldRule> fieldRules = facade.getFieldRuleByBizId(biz.getId());
		try {
			Spider spider = fixSpider(biz);
			//mongodb存储方式
			Pipeline pipeline = new MongoDBPipeline(MongoDBTemplate.Default(), biz.getPersistenceTable());
			spider.setExitWhenComplete(false)
				  .thread(biz.getThreadCount())
				  .addPipeline(pipeline)
				  .setUUID(biz.getBizCode());
			spider.setEmptySleepTime(30000);
			spiders.put(biz.getBizCode(), spider);
			logger.info("lmdna-spider:实例新的spider,成功!");
			try {
				monitor.register(spider);
			} catch (JMException e) {
				logger.info("新的spider注册到jmx平台时失败！");
				logger.error("新的spider注册到jmx平台时失败！");
			}
			spider_pool.execute(spider);
		} catch (Exception e) {
			return;
		}
	}
	
	/**
	 * 收集心跳提交数据
	 * @return
	 */
	private HeartBeatData gatherHeartBeatData(){
		HeartBeatData heartBeatData = new HeartBeatData();
		heartBeatData.setSpiderCounts(spiders.size());
		SpiderMonitor monitor = this.getMonitor();
		List<SpiderStatusSerialization> statusSerializations = new ArrayList<SpiderStatusSerialization>();
		int totalThreadCount = 0;
		for(Entry<String,SpiderStatusMXBean> entry : monitor.getSpiderStatusMap().entrySet()){
			SpiderStatusMXBean statusBean = entry.getValue();
			SpiderStatusSerialization statusSerialization = new SpiderStatusSerialization();
			statusSerialization.setErrorPageCount(statusBean.getErrorPageCount());
			statusSerialization.setLeftPageCount(statusBean.getLeftPageCount());
			statusSerialization.setMatchSuccessPageCount(statusBean.getMatchSuccessPageCount());
			statusSerialization.setName(statusBean.getName());
			statusSerialization.setPagePerSecond(statusBean.getPagePerSecond());
			statusSerialization.setProxyPoolSize(statusBean.getProxyPoolSize());
			statusSerialization.setStatus(statusBean.getStatus());
			statusSerialization.setSuccessPageCount(statusBean.getSuccessPageCount());
			statusSerialization.setThread(statusBean.getThread());
			statusSerialization.setTotalPageCount(statusBean.getTotalPageCount());
			statusSerializations.add(statusSerialization);
			totalThreadCount = totalThreadCount + statusBean.getThread();
		}
		heartBeatData.setSpiderInfos(statusSerializations);
		heartBeatData.setActiveThreadCounts(totalThreadCount);
		heartBeatData.setMachineId(SpiderGlobalConfig.getValue(SpiderGlobalConfig.MACHINE_ID));
		return heartBeatData;
	}
	
	public SpiderMonitor getMonitor(){
		return monitor;
	}
	
	/**
	 * 任务队列消费线程
	 */
	class ReqQueueConsumeThread implements Runnable{
		@Override
		public void run() {
			for(;;){
				Request req = null;
				try {
					req = reqQueue.take();
				} catch (InterruptedException e) {
					logger.info("爬虫任务消费线程异常中断！");
					logger.error("爬虫任务消费线程异常中断！");
				}
				String bizcode = (String) req.getExtra("bizcode");
				try{
					if(spiders.containsKey(bizcode)){
						spiders.get(bizcode).addRequest(req);
					}else{
						//不做任何处理
					}
				}catch(IllegalStateException e){
					if(e.getMessage().equalsIgnoreCase("queue full")){
						logger.warn("spider>>>{}---任务处理失败，任务池已满!",bizcode);
						try{
							reqQueue.add(req);//如果Spider中任务队列溢出，默认是Integer.Max，则归还任务.
						}catch(IllegalStateException e2){
							if(e.getMessage().equalsIgnoreCase("queue full")){
								continue;//如果连reqQueue也满了，那只能放弃该任务
							}
						}
					}else{
						logger.error("spider>>>{}---任务提交失败！",bizcode);
					}
				}
			}
		}
	}
	
	/**
	 * 心跳线程
	 */
	class HeartBeatThread implements Runnable{
		@Override
		public void run() {
			for(;;){
				List<RemoteCmd> acceptTasks = heartBeatProtocol.handleHeartBeat(gatherHeartBeatData());
				if(acceptTasks!=null && acceptTasks.size()>0){
					for(RemoteCmd acceptTask : acceptTasks){
						
					}
				}
				try {
					Thread.sleep(Long.parseLong(SpiderGlobalConfig.getValue(SpiderGlobalConfig.SPIDER_SLAVE_HEARTBEAT_INTERVAL)));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	class LocalCrawlTaskParseThread implements Runnable{

		private LocalCrawlTask localCrawlTask;
		
		private List<FileRequestObject> locks;
		
		public LocalCrawlTaskParseThread(LocalCrawlTask localCrawlTask,FileRequestObject... o){
			this.localCrawlTask = localCrawlTask;
			if(o!=null && o.length>0){
				locks = Arrays.asList(o);
			}
		}
		
		@Override
		public void run() {
			for(FileRequestObject watcher : locks){
				for(;;){
					if(!watcher.isFailed() && watcher.isFinished()){
						break;
					}
				}
			}
			//如果需要从jar包中加载spider
			String jarfilepath = jarFilePathCache.get(localCrawlTask.getBizCode());
			String taskfilepath = taskFilePathCache.get(localCrawlTask.getTaskId());
//			if(localCrawlTask.isNeedload()){
//				try {
//					ClassLoader loader = newClassLoader(jarfilepath);
//					classLoaderCache.put(localCrawlTask.getBizCode(), loader);
//					String spider_process_name = getSpiderClassFromJarName(jarfilepath);
//					SpiderBiz biz = SpiderNode.parseBizBeanFromJar(jarfilepath);
//					Thread.currentThread().setContextClassLoader(loader);
//				    Site site = SpiderNode.fixSite(biz);
//				    Constructor<?> c;
//					c = Class.forName(spider_process_name,true,loader).getConstructor();
//					Spider spider = (Spider) c.newInstance();
//					spider.setSite(site);
//					if(biz.getWebsiteConfigBO().getNeedLogin() == 1){
//						spider.setDownloader(new StatusFulDownloader());
//					}
//					spider.setExitWhenComplete(false).thread(biz.getThreadCount()).setBizcode(biz.getBizCode());
//					spider.setEmptySleepTime(30000);
//					spiders.put(localCrawlTask.getBizCode(), spider);
//					logger.info("lmdna-spider:实例新的spider,成功!");
//					try {
//						monitor.register(spider);
//					} catch (JMException e) {
//						logger.info("新的spider注册到jmx平台时失败！");
//						logger.error("新的spider注册到jmx平台时失败！");
//					}
//					spider_pool.execute(spider);
//				} catch (Throwable e) {
//					e.printStackTrace();
//				}
//				try{
//					TaskFileParseProtocol fileParser = null;
//					String taskFilePaserClassName = getTaskFileParseClassFromJarName(jarfilepath);
//					//开始解析任务文件（可以自定义任务文件解析器）
//					if(taskFilePaserClassName!=null){
//						TaskFileParseProxyFacotry fileParseFactory = new TaskFileParseProxyFacotry(classLoaderCache.get(localCrawlTask.getBizCode()));
//						fileParser = (TaskFileParseProtocol)fileParseFactory.create((TaskFileParseProtocol)Class.forName(taskFilePaserClassName,true,classLoaderCache.get(localCrawlTask.getBizCode())).getConstructor().newInstance(), reqQueue);
//					}else{
//						TaskFileParseProxyFacotry fileParseFactory = new TaskFileParseProxyFacotry();
//						fileParser = (TaskFileParseProtocol) fileParseFactory.create(reqQueue);
//					}
//					taskFileParserCache.put(localCrawlTask.getBizCode(), fileParser);
//				}catch(IOException e){
//					logger.error("任务文件解析失败！",e);
//				} catch (Throwable e) {
//					e.printStackTrace();
//				}
//			}
			//解析任务文件
			TaskFileParseProtocol fileParser = taskFileParserCache.get(localCrawlTask.getBizCode());
			try {
				fileParser.parse(new FileInputStream(new File(taskfilepath)));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
     * The default thread factory
     */
    static class SpiderFactory implements ThreadFactory {
        static final AtomicInteger poolNumber = new AtomicInteger(1);
        final ThreadGroup group;
        final AtomicInteger threadNumber = new AtomicInteger(1);
        final String namePrefix;

        SpiderFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null)? s.getThreadGroup() :
                                 Thread.currentThread().getThreadGroup();
            namePrefix = "spiderpool-" +
                          poolNumber.getAndIncrement() +
                         "-spider";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                                  namePrefix + threadNumber.getAndIncrement(),
                                  0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
    
    /**
     * The default thread factory
     */
    static class BatchReaderFactory implements ThreadFactory {
        static final AtomicInteger poolNumber = new AtomicInteger(1);
        final ThreadGroup group;
        final AtomicInteger threadNumber = new AtomicInteger(1);
        final String namePrefix;

        BatchReaderFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null)? s.getThreadGroup() :
                                 Thread.currentThread().getThreadGroup();
            namePrefix = "readerpool-" +
                          poolNumber.getAndIncrement() +
                         "-reader-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                                  namePrefix + threadNumber.getAndIncrement(),
                                  0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

	@Override
	public void close() {
		if(spider_pool!=null){
			spider_pool.shutdownNow();
		}
		if(worker!=null){
			worker.shutdownNow();
		}
	}

}
