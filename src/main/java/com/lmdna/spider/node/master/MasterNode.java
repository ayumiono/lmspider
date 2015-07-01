package com.lmdna.spider.node.master;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.ws.WebServiceException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.caucho.hessian.server.HessianServlet;
import com.lmdna.spider.SpiderDAOServiceFacade;
import com.lmdna.spider.berkeleydb.BdbUriUniqFilter;
import com.lmdna.spider.berkeleydb.Checkpoint;
import com.lmdna.spider.berkeleydb.CheckpointUtils;
import com.lmdna.spider.berkeleydb.Checkpointer;
import com.lmdna.spider.berkeleydb.FileUtils;
import com.lmdna.spider.berkeleydb.UriUniqFilter;
import com.lmdna.spider.dao.model.SpiderBiz;
import com.lmdna.spider.dao.model.SpiderProxyIp;
import com.lmdna.spider.dao.model.SpiderWebsiteAccount;
import com.lmdna.spider.http.HttpServer;
import com.lmdna.spider.jar.utils.TaskFileParseProxyFacotry;
import com.lmdna.spider.node.SpiderContext;
import com.lmdna.spider.node.SpiderNode;
import com.lmdna.spider.protocol.TaskFileParseProtocol;
import com.lmdna.spider.protocol.TaskStatusListener;
import com.lmdna.spider.protocol.rpc.AccountProtocol;
import com.lmdna.spider.protocol.rpc.HeartBeatProtocol;
import com.lmdna.spider.protocol.rpc.IpProtocol;
import com.lmdna.spider.protocol.rpc.VerifyImgProtocol;
import com.lmdna.spider.protocol.rpc.impl.AccountProtocolImpl;
import com.lmdna.spider.protocol.rpc.impl.HeartBeatProtocolImpl;
import com.lmdna.spider.protocol.rpc.impl.IpProtocolImpl;
import com.lmdna.spider.protocol.rpc.impl.SpiderRemoteControlPtotocolImpl;
import com.lmdna.spider.protocol.rpc.impl.VerifyImgProtocolImpl;
import com.lmdna.spider.protocol.rpc.utils.HeartBeatData;
import com.lmdna.spider.protocol.rpc.utils.RemoteCmd;
import com.lmdna.spider.utils.SpiderGlobalConfig;
import com.sleepycat.je.CheckpointConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.DbInternal;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.dbi.EnvironmentImpl;
import com.sleepycat.je.utilint.DbLsn;

/**
 * @author ayumiono
 * Master节点类
 * 节点组件包括
 * 一个HttpServer负责监听slave节点的心跳、处理web请求（包括任务提交、爬虫集群性能监控、资源监控、任务监控）
 * 一个FileServer负责接收slave请求，传输任务文件
 * 一个BdbUriUniqFilter负责对url判重处理
 */
public class MasterNode extends SpiderNode implements Serializable{
	
	private static final long serialVersionUID = 4811761593595506530L;
	private static final Logger logger = LoggerFactory.getLogger(MasterNode.class);
	private transient FileServer file_server;
	private transient HttpServer web_server;
	private transient SpiderContext _context;
	private transient ZooKeeper zk;
	private transient SpiderDAOServiceFacade facade = SpiderDAOServiceFacade.getInstance();
	
	public static final Object NASCENT = "NASCENT".intern();
    public static final Object RUNNING = "RUNNING".intern();
    public static final Object PAUSED = "PAUSED".intern();
    public static final Object PAUSING = "PAUSING".intern();
    public static final Object CHECKPOINTING = "CHECKPOINTING".intern();
    public static final Object STOPPING = "STOPPING".intern();
//    public static final Object FINISHED = "FINISHED".intern();
    public static final Object STARTED = "STARTED".intern();
//    public static final Object PREPARING = "PREPARING".intern();
    private transient Object state = NASCENT;
    
	private transient File stateDisk;
    private transient File checkpointsDisk;
	private transient Checkpointer checkpointer = null;
	private Checkpoint checkpointRecover = null;
	private transient Environment bdbUriFilterEnv = null;
	private transient UriUniqFilter uriUniqFilter = null;
	private static final String CHECKPOINT_SUFIX = "";
	
	//记录任务文件执行情况
	private static final AtomicLong _count = new AtomicLong(0);
	public Map<String,Task> taskProgressRecord;//key:taskId
	public List<Task> finishedTask;
	private transient Map<String,List<TaskStatusListener>> taskListeners = new HashMap<String,List<TaskStatusListener>>();
	
	//记录spider in operation
	private Map<String,SpiderBizInOperation> spidersInOperation;
	
	//slavenodes info cache
	private transient Map<String,HeartBeatData> slaveNodeHeartBeatCache = new ConcurrentHashMap<String,HeartBeatData>();//缓存slave nodes心跳信息key:machineid
	private transient Map<String,String> slaveZkNodesDataCache = new HashMap<String,String>();//key:zk_path,value:zk_data
	
	//有机器id标识的命令分发容器
	private transient Map<String,BlockingQueue<RemoteCmd>> singleSlaveCmd = new HashMap<String,BlockingQueue<RemoteCmd>>();
	
	//记录资源分发情况
	private Map<String,List<SpiderProxyIp>> biz_ip_map;//一个业务下被指派过的IP
	private Map<String,List<SpiderProxyIp>> machine_ip_map;//一个节点下被指派过的IP
	private Map<String,List<SpiderWebsiteAccount>> biz_account_map;//一个业务下被指派过的account
	private Map<String,List<SpiderWebsiteAccount>> machine_account_map;//一个节点下被指派过的account
	
	//jar包类加载器、文件解析器
	private transient Map<String,String> jarpathCache = new ConcurrentHashMap<String,String>();
	private transient Map<String,TaskFileParseProtocol> taskFileParserCache = new ConcurrentHashMap<String,TaskFileParseProtocol>();//bizcode - taskFileParser
	private transient Map<String,ClassLoader> classLoaderCache = new ConcurrentHashMap<String,ClassLoader>();//bizcode - classloader
	
	private transient Map<Integer,String> verifyImgInput = new ConcurrentHashMap<Integer, String>();//验证码输入缓存
	private static final AtomicInteger verify_img_id = new AtomicInteger(1);////验证码id生成器
	//缓存验证码
	private transient PriorityBlockingQueue<VerifyImgBean> verifyImgQueue = new PriorityBlockingQueue<VerifyImgBean>(100,new Comparator<VerifyImgBean>() {
		@Override
		public int compare(VerifyImgBean o1, VerifyImgBean o2) {
			long expire1 = o1.getExpire();
			long expire2 = o2.getExpire();
			long imgcreatetime1 = o1.getImgCreateTime().getTime();
			long imgcreatetime2 = o2.getImgCreateTime().getTime();
			long timenow = System.currentTimeMillis();
			long lefttime1 = expire1 - timenow + imgcreatetime1;
			long lefttime2 = expire2 - timenow + imgcreatetime2;
			int priority1 = o1.getPriority();
			int priority2 = o2.getPriority();
			if(priority1 == priority2){
				if(lefttime1 < lefttime2){
					return 1;
				}else if(lefttime1 > lefttime2){
					return -1;
				}else{
					return 0;
				}
			}else if(priority1 > priority2){
				return 1;
			}else{
				return -1;
			}
		}
	});
	
	public MasterNode(SpiderContext _context) throws Exception{
		this._context = _context;
		if(_context.getParameter("recover")!=null){
			this.checkpointRecover = new Checkpoint(new File(_context.getParameter("recover").toString()));
		}
		initialize();
	}
	
	/**
	 * setup web server
	 * @throws Exception
	 */
	private void setupWebServer() throws Exception{
		logger.info("Master Node: Start to Setup WebServer...");
		try{
			this.web_server = new HttpServer(_context.getJettyPort(),this);
		}catch(IOException e){
			logger.error("Master Node:error happened when initialize web server!",e);
			throw e;
		}
		//远程控制接口
		HessianServlet spiderRemoteServlet = new HessianServlet();
		spiderRemoteServlet.setHome(new SpiderRemoteControlPtotocolImpl(this));
		this.web_server.addInternalServlet("spiderRemoteService", SpiderGlobalConfig.getValue("spider.remote.servlet.path"), spiderRemoteServlet);
		//slave master通信接口
		HessianServlet heartBeatServlet = new HessianServlet();
		HeartBeatProtocol heartBeatServiceImpl = new HeartBeatProtocolImpl(this);
		heartBeatServlet.setHome(heartBeatServiceImpl);
		this.web_server.addInternalServlet("heartBeatService", SpiderGlobalConfig.getValue("spider.heartbeat.servlet.path"), heartBeatServlet);
		//资源管理接口
		HessianServlet ipServlet = new HessianServlet();
		IpProtocol ipServiceImpl = new IpProtocolImpl(this);
		ipServlet.setHome(ipServiceImpl);
		this.web_server.addInternalServlet("ipService", SpiderGlobalConfig.getValue("spider.ip.servlet.path"), ipServlet);
		HessianServlet accountServlet = new HessianServlet();
		AccountProtocol accountServiceImpl = new AccountProtocolImpl(this);
		accountServlet.setHome(accountServiceImpl);
		this.web_server.addInternalServlet("accountService", SpiderGlobalConfig.getValue("spider.account.servlet.path"), accountServlet);
		HessianServlet verifyImgServlet = new HessianServlet();
		VerifyImgProtocol verifyImgProtocol = new VerifyImgProtocolImpl(this);
		verifyImgServlet.setHome(verifyImgProtocol);
		this.web_server.addInternalServlet("verifyimgService", SpiderGlobalConfig.getValue("spider.verifyimg.servlet.path"), verifyImgServlet);
		logger.info("Master Node: WebServer Setup successed.");
	}
	
	private void setupDisk()throws IOException{
		this.checkpointsDisk = new File(SpiderGlobalConfig.getValue(SpiderGlobalConfig.SPIDER_BDB_CHECKPOINT_DIR));
		if(!checkpointsDisk.isDirectory() && !checkpointsDisk.mkdirs()){
			throw new IOException(SpiderGlobalConfig.getValue(SpiderGlobalConfig.SPIDER_BDB_CHECKPOINT_DIR)+" cannot be made!");
		}
		this.stateDisk = new File(SpiderGlobalConfig.getValue(SpiderGlobalConfig.SPIDER_BDB_ENV_PATH));
		if(!stateDisk.isDirectory() && !stateDisk.mkdirs()){
			throw new IOException(SpiderGlobalConfig.getValue(SpiderGlobalConfig.SPIDER_BDB_ENV_PATH)+" cannot be made!");
		}
	}
	
	private void setupFileServer(){
		logger.info("Master Node: FileServer setup...");
		this.file_server = new FileServer(_context.getFileServerPort(),this);
		logger.info("Master Node: FileServer setup finished.");
	}
	
	private void initialComponent(){
		this.taskProgressRecord = new ConcurrentHashMap<String,Task>();//key:taskId
		this.finishedTask = new ArrayList<Task>();
		this.spidersInOperation = new HashMap<String,SpiderBizInOperation>();
		this.biz_ip_map = new HashMap<String,List<SpiderProxyIp>>();//一个业务下被指派过的IP
		this.machine_ip_map = new HashMap<String,List<SpiderProxyIp>>();//一个节点下被指派过的IP
		this.biz_account_map = new HashMap<String,List<SpiderWebsiteAccount>>();//一个业务下被指派过的account
		this.machine_account_map = new HashMap<String,List<SpiderWebsiteAccount>>();//一个节点下被指派过的account
		this.jarpathCache = new ConcurrentHashMap<String,String>();
	}
	
	/**
	 * set up uri uniq filter
	 * @throws Exception
	 */
	private void setupUriUniqFilter() throws Exception{
		try{
	        EnvironmentConfig envConfig = new EnvironmentConfig();
	        envConfig.setAllowCreate(true);
	        envConfig.setSharedCache(true);
	        envConfig.setConfigParam("je.cleaner.expunge", "true");
	        envConfig.setLockTimeout(5000000,TimeUnit.MICROSECONDS); // 5 seconds
	        envConfig.setCachePercent(30);//30%*jvm-xmx size,because URL-FingerPrint is rarely reused in JE,WE should set the JE cache small
//	        envConfig.setConfigParam(EnvironmentConfig.LOG_FILE_MAX, "50000000");//log size 50M
	        this.bdbUriFilterEnv = new Environment(stateDisk, envConfig);
			this.uriUniqFilter = new BdbUriUniqFilter(bdbUriFilterEnv);
			logger.info("Master Node: BdbUriUniqFilter setup successed.");
		}catch(Exception e){
			throw e;
		}
	}
	
	protected void setupCheckpointRecover()
		    throws IOException {
//		        long started = System.currentTimeMillis();
//		        if (LOGGER.isLoggable(Level.FINE)) {
//		            LOGGER.fine("Starting recovery setup -- copying into place " +
//		                "bdbje log files -- for checkpoint named " +
//		                this.checkpointRecover.getDisplayName());
//		        }
		        // Mark context we're in a recovery.
		        this.checkpointer.recover(this);
//		        this.progressStats.info("CHECKPOINT RECOVER " + this.checkpointRecover.getDisplayName());
		        // Copy the bdb log files to the state dir so we don't damage
		        // old checkpoint.  If thousands of log files, can take
		        // tens of minutes (1000 logs takes ~5 minutes to java copy,
		        // dependent upon hardware).  If log file already exists over in the
		        // target state directory, we do not overwrite -- we assume the log
		        // file in the target same as one we'd copy from the checkpoint dir.
		        File bdbSubDir = CheckpointUtils.getBdbSubDirectory(this.checkpointRecover.getDirectory());
		        List<IOException> errs = new ArrayList<IOException>();
		        FileUtils.copyFiles(bdbSubDir, CheckpointUtils.getJeLogsFilter(),getStateDisk(), true, false, errs);
//		        for (IOException ioe : errs) {
//		            LOGGER.log(Level.SEVERE, "Problem copying checkpoint files: "
//		                    +"checkpoint may be corrupt",ioe);
//		        }
//		        if (LOGGER.isLoggable(Level.INFO)) {
//		            LOGGER.info("Finished recovery setup for checkpoint named " +
//		                this.checkpointRecover.getDisplayName() + " in " +
//		                (System.currentTimeMillis() - started) + "ms.");
//		        }
		    }
	
	private void initialize() throws Exception{
		setupDisk();
		setupWebServer();
		setupFileServer();
		if(this.checkpointRecover!=null){
			logger.info("recover MasterNode from "+ this.checkpointRecover.getDirectory().getPath());
			File bdbSubDir = CheckpointUtils.getBdbSubDirectory(this.checkpointRecover.getDirectory());
			List<IOException> errs = new ArrayList<IOException>();
			FileUtils.copyFiles(bdbSubDir, CheckpointUtils.getJeLogsFilter(),getStateDisk(), true, false, errs);
			try{
				MasterNode rmn = CheckpointUtils.readObjectFromFile(MasterNode.class, this.checkpointRecover.getDirectory());
				this.taskProgressRecord = rmn.taskProgressRecord;
				this.finishedTask = rmn.finishedTask;
				this.spidersInOperation = rmn.spidersInOperation;
				this.biz_ip_map = rmn.biz_ip_map;
				this.machine_ip_map = rmn.machine_ip_map;
				this.biz_account_map = rmn.biz_account_map;
				this.machine_account_map = rmn.machine_account_map;
				this.jarpathCache = rmn.jarpathCache;
			}catch(Exception e){
				logger.info("recover MasterNode from serialize file failed."+e.getMessage());
				initialComponent();
			}
			logger.info("recover MasterNode finished.");
		}else{
			initialComponent();
		}
		this.zk = (ZooKeeper) _context.getParameter("zk");
		this.checkpointer = new Checkpointer(this, CHECKPOINT_SUFIX);
		setupUriUniqFilter();
		autoLoadSpider();
	}
	
	public void run(){
		try {
			this.web_server.start();
			logger.info("Master Node: web server started successed.");
		} catch (Exception e) {
			logger.error("Master Node:web server start failed!",e);
		}
		new Thread(this.file_server).start();
		logger.info("Master Node:file server started successed.");
		listenSlaveNodesStatus();
	}

	public List<Task> getTaskProgressRecord() {
		List<Task> result = new ArrayList<Task>();
		for(Entry<String,Task> entry : taskProgressRecord.entrySet()){
			result.add(entry.getValue());
		}
		return result;
	}
	
	public String reportTaskProgress(){
		StringBuffer sb = new StringBuffer("taskProgress view:>>>>\n");
		for(Entry<String,Task> entry : taskProgressRecord.entrySet()){
			if(!entry.getValue().isOver()){
				sb.append(entry.getValue().toString()+"\n");
			}
		}
		return sb.toString();
	}
	
	/**@see VerifyImgProtocolImpl,获取验证码
	 * @param id
	 * @return
	 */
	public String getVerifyCode(int id){
		Set<Entry<Integer,String>> entryset = verifyImgInput.entrySet();
		Iterator<Entry<Integer, String>> itrator = entryset.iterator();
		while(itrator.hasNext()){
			Entry<Integer,String> entry = itrator.next();
			if(entry.getKey() == id){
				String code = entry.getValue();
				itrator.remove();
				return code;
			}
		}
		return null;
	}
	
	/**接收验证码
	 * @param id
	 * @param code
	 */
	public void submitVerifyCode(int id,String code){
		verifyImgInput.put(id, code);
	}
	
	/**接收验证码图片
	 * @param img
	 * @return
	 */
	public int submitVerifyImg(VerifyImgBean img){
		int id = verify_img_id.getAndIncrement();
		img.setId(id);//附上唯一标识id
		verifyImgQueue.add(img);
		return id;
	}
	
	public VerifyImgBean displayOneVerifyImg(){
		return verifyImgQueue.poll();
	}
	
	/**提交任务文件
	 * @param task
	 * @throws IOException
	 */
	public synchronized Task submitTask(String bizCode,String filePath,String fileName,Integer rowperblock) throws Exception{
		Task task = new Task(this,slaveZkNodesDataCache.size(),bizCode,filePath,fileName,rowperblock);
		//如果超出50个，则移除已经完成的任务记录
		if(taskProgressRecord.size()>=50){
			Set<Entry<String,Task>> entryset = taskProgressRecord.entrySet();
			Iterator<Entry<String, Task>> iterator = entryset.iterator();
			while(iterator.hasNext()){
				Entry<String, Task> entry = iterator.next();
				if(entry.getValue().isOver()){
					iterator.remove();
				}
			}
		}
		taskProgressRecord.put(task.getTaskId(), task);
		//限定超出100000行新记录时需要备份
		if(_count.addAndGet(task.getTotalRow())>=100000){
			requestCrawlCheckpoint();
			_count.set(0L);
		}
		return task;
	}
	
	/**@see HeartBeatProtocolImpl，保存节点心跳信息
	 * @param data
	 */
	public void saveHeartBeatData(HeartBeatData data){
		this.slaveNodeHeartBeatCache.put(data.getMachineId(), data);
	}
	
	/**页面调用
	 * @return
	 */
	public List<HeartBeatData> getSlaveNodeHeartBeatInfo(){
		List<HeartBeatData> result = new ArrayList<HeartBeatData>();
		for(Entry<String,HeartBeatData> entry : slaveNodeHeartBeatCache.entrySet()){
			result.add(entry.getValue());
		}
		return result;
	}

	/**
	 * 从数据库中加载spider
	 * @param bizId
	 * @throws Exception
	 */
	public void loadSpider(int bizId) throws Exception{
		SpiderBiz biz = this.facade.getBiz(bizId);
		if(biz != null){
			SpiderBizInOperation newSpiderInOperation = new SpiderBizInOperation(biz);
			this.spidersInOperation.put(biz.getBizCode(), newSpiderInOperation);
		}else{
			throw new Exception("no biz exist!");
		}
	}
	
	/**
	 * 从上传的jar包中加载spider
	 * @param jarFilePath
	 * @param jarName
	 * @throws Throwable
	 */
	public void loadSpider(String jarFilePath,String jarName) throws Throwable{
		ClassLoader loader = newClassLoader(jarFilePath);
		SpiderBiz biz = SpiderNode.parseBizBeanFromJar(jarFilePath);
		classLoaderCache.put(biz.getBizCode(), loader);
		jarpathCache.put(biz.getBizCode(), jarFilePath);
		TaskFileParseProtocol fileParser = null;
		String taskFilePaserClassName = getTaskFileParseClassFromJarName(jarFilePath);
		SpiderBizInOperation newSpiderInOperation = new SpiderBizInOperation(biz,jarFilePath,jarName);
		//开始解析任务文件（可以自定义任务文件解析器）
		if(taskFilePaserClassName!=null){
			fileParser = (TaskFileParseProtocol)TaskFileParseProxyFacotry.create((TaskFileParseProtocol)Class.forName(taskFilePaserClassName,true,classLoaderCache.get(biz.getBizCode())).getConstructor().newInstance(),this.uriUniqFilter);
		}
		taskFileParserCache.put(biz.getBizCode(), fileParser);
		this.spidersInOperation.put(biz.getBizCode(), newSpiderInOperation);
	}
	
	public void removeSpider(String bizCode){
		spidersInOperation.remove(bizCode);
	}
	
	public void terminateSpider(){
	}
	
	/**
	 * 启动时自动加载spider biz信息
	 */
	private void autoLoadSpider(){
		if ("on".equals(SpiderGlobalConfig.getValue(SpiderGlobalConfig.SPIDER_AUTOLOAD_SWITCH))){
			SpiderBiz conditionBean = new SpiderBiz();
			conditionBean.setStatus(0);
			Map<String,Object> querymap = new HashMap<String,Object>();
			querymap.put("status", 0);
			List<SpiderBiz> bizList = facade.getBizList(querymap);
			logger.info("Master Node:start to autoload spider...");
			if(bizList!=null){
				for(SpiderBiz biz : bizList){
					SpiderBizInOperation spiderBizInOperation = new SpiderBizInOperation(biz);
					spidersInOperation.put(biz.getBizCode(),spiderBizInOperation);
				}
			}
			
			String upload_jar_dir = SpiderGlobalConfig.getValue(SpiderGlobalConfig.SPIDER_JAR_UPLOAD_DIR);
			File dir = new File(upload_jar_dir);
			if(dir.exists()){
				for(File file : dir.listFiles()){
					if(file.getName().endsWith(".jar") && !file.isDirectory()){
						try {
							logger.info("start to load spider form "+file.getName());
							loadSpider(file.getPath(), file.getName());
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
				}
			}
		}else{
			logger.info("Slave Node:spider.autoload.switch turned off.");
		}
	}
	
	/**
	 * 分发代理IP
	 * IP资源获取原则：
	 * 同一个bizcode下，必须保证一个IP不会被指派给多个机器
	 * 同一个bizcode下，必须保证一个IP不会被重复指派给某台机器
	 * @param machineId
	 * @param bizCode
	 * @param needCount
	 * @return
	 */
	public synchronized List<SpiderProxyIp> distributeProxyIp(String machineId,String bizCode,int needCount){
		List<SpiderProxyIp> result = new ArrayList<SpiderProxyIp>();
		List<SpiderProxyIp> bizusedipcache = biz_ip_map.get(bizCode);
		List<SpiderProxyIp> machineusedipcache = machine_ip_map.get(machineId);
		if(bizusedipcache == null){
			bizusedipcache = new ArrayList<SpiderProxyIp>();
		}
		if(machineusedipcache == null){
			machineusedipcache = new ArrayList<SpiderProxyIp>();
		}
		int count = needCount;
		Map<String,Object> queryMap = new HashMap<String,Object>();
		List<SpiderProxyIp> proxyIpList = new ArrayList<SpiderProxyIp>();
		proxyIpList = facade.getProxyIps(queryMap);
		for(SpiderProxyIp ip : proxyIpList){
			if(count<=0){
				break;
			}
			if(!bizusedipcache.contains(ip)){
				result.add(ip);
				bizusedipcache.add(ip);
				count--;
				if(!machineusedipcache.contains(ip)){
					machineusedipcache.add(ip);
				}
			}
		}
		biz_ip_map.put(bizCode, bizusedipcache);
		machine_ip_map.put(machineId, machineusedipcache);
		return result;
	}
	
	/**
	 * 分发账号
	 * @param machineId
	 * @param bizCode
	 * @param site
	 * @param needCount
	 * @return
	 */
	public synchronized List<SpiderWebsiteAccount> distributeAccount(String machineId,String bizCode,int site,int needCount){
		List<SpiderWebsiteAccount> result = new ArrayList<SpiderWebsiteAccount>();
		List<SpiderWebsiteAccount> bizusedaccountcache = biz_account_map.get(bizCode);
		List<SpiderWebsiteAccount> machineusedaccountcache = machine_account_map.get(machineId);
		if(bizusedaccountcache == null){
			bizusedaccountcache = new ArrayList<SpiderWebsiteAccount>();
		}
		if(machineusedaccountcache == null){
			machineusedaccountcache = new ArrayList<SpiderWebsiteAccount>();
		}
		int count = needCount;
		Map<String,Object> queryMap = new HashMap<String,Object>();
		queryMap.put("site", site);
		queryMap.put("valid", 0);
		List<SpiderWebsiteAccount> accountList = new ArrayList<SpiderWebsiteAccount>();
		accountList = facade.getWebsiteAccount(queryMap);
		for(SpiderWebsiteAccount account : accountList){
			if(count<=0){
				break;
			}
			if(!bizusedaccountcache.contains(account)){
				result.add(account);
				bizusedaccountcache.add(account);
				count--;
				if(!machineusedaccountcache.contains(account)){
					machineusedaccountcache.add(account);
				}
			}
		}
		biz_account_map.put(bizCode, bizusedaccountcache);
		machine_account_map.put(machineId, machineusedaccountcache);
		return result;
	}
	
	/**
	 * 释放死亡节点资源
	 * @param machineId
	 */
	private void releaseResource(String machineId){
		logger.info("Master Node:start to release the resources occupied by {} ...",machineId);
		List<SpiderProxyIp> ips = machine_ip_map.get(machineId);
		if(ips!=null)
			for(SpiderProxyIp ip : ips){
				for(Entry<String,List<SpiderProxyIp>> entry : biz_ip_map.entrySet()){
					List<SpiderProxyIp> i = entry.getValue();
					i.remove(ip);
				}
			}
		List<SpiderWebsiteAccount> accounts = machine_account_map.get(machineId);
		if(accounts!=null)
			for(SpiderWebsiteAccount account : accounts){
				for(Entry<String,List<SpiderWebsiteAccount>> entry : biz_account_map.entrySet()){
					List<SpiderWebsiteAccount> i = entry.getValue();
					i.remove(account);
				}
			}
		logger.info("Master Node:resources release finished");
		for(Entry<String,Task> entryset : taskProgressRecord.entrySet()){
			entryset.getValue().returnCrawlTask(machineId);
		}
		singleSlaveCmd.remove(machineId);
	}
	
	/**
	 * 监听slave nodes状态，有新节点添加进来还需要重新排列task
	 */
	public void listenSlaveNodesStatus(){
		try {
			if(zk.exists(SpiderGlobalConfig.getValue("zookeeper.spider.slaves.path"), false)==null){
				zk.create(SpiderGlobalConfig.getValue("zookeeper.spider.slaves.path"), null, Ids.OPEN_ACL_UNSAFE , CreateMode.PERSISTENT);
			}
			zk.getChildren(SpiderGlobalConfig.getValue("zookeeper.spider.slaves.path"), new Watcher(){
				@Override
				public void process(WatchedEvent event) {
					if(event.getType()==EventType.NodeDeleted){
						logger.error("Master Node:the path of the parent of slavenodes has been deleted! please check.");
					}else if(event.getType() == EventType.NodeChildrenChanged){
						try {
							List<String> chilerenPath = zk.getChildren(SpiderGlobalConfig.getValue("zookeeper.spider.slaves.path"), null);
							Iterator<Entry<String,String>> iterator = slaveZkNodesDataCache.entrySet().iterator();
							while(iterator.hasNext()){
								Entry<String,String> entry = iterator.next();
								if(!chilerenPath.contains(entry.getKey())){
									//处理删除节点
									logger.info("Master Node:one slavenode has been deleted.");
									String data = entry.getValue();
									JSONObject obj = (JSONObject) JSONObject.parse(data);
									String machineId = obj.getString("machineId");
									releaseResource(machineId);
									iterator.remove();
								}
							}
							for(String s : chilerenPath){
								if(!slaveZkNodesDataCache.containsKey(s)){
									String realPath = SpiderGlobalConfig.getValue("zookeeper.spider.slaves.path")+"/"+s;
									//处理新增节点
									slaveZkNodesDataCache.put(s, new String(zk.getData(realPath, null, null),"utf-8"));
									logger.info("Master Node:one slavenode has been added.");
								}
							}
						} catch (KeeperException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						} catch (Exception e){
							e.printStackTrace();
						}
					}
					listenSlaveNodesStatus();
				}});
		} catch (KeeperException e) {
			if(e instanceof NoNodeException){
				logger.info("Master Node:"+e.getMessage());
			}else{
				logger.error(e.getMessage(),e);
			}
		} catch (InterruptedException e) {
			logger.error(e.getMessage(),e);
		}
	}
	
	/**
	 * 页面调用
	 * @return
	 */
	public List<SpiderBizInOperation> getSpidersInOperation() {
		List<SpiderBizInOperation> list = new ArrayList<SpiderBizInOperation>();
		Iterator<String> it = spidersInOperation.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			list.add(spidersInOperation.get(key));
		}
		return list;
	}
	
	public SpiderBizInOperation getSpiderBizInOperation(String bizCode){
		return spidersInOperation.get(bizCode);
	}
	
	public boolean spiderExist(String bizCode){
		if(spidersInOperation.get(bizCode) == null){
			return false;
		}else{
			return true;
		}
	}
	
	public SpiderDAOServiceFacade getFacade(){
		return this.facade;
	}
	
	public int getHttpServerPort(){
		return _context.getJettyPort();
	}

	@Override
	public void close() throws WebServiceException {
		
	}
	
	/**
	 * 请求备份数据，一般情况下当提交task做全量备份（bdb备分和self备份）,分发完crawltask后做self备份
	 * @throws IllegalStateException
	 */
	public void requestCrawlCheckpoint()throws IllegalStateException {
        if (this.checkpointer == null) {
            return;
        }
        if (this.checkpointer.isCheckpointing()) {
            throw new IllegalStateException("Checkpoint already running.");
        }
        this.checkpointer.checkpoint();
//        RemoteCmd cmd = new CheckpointCmd(this.checkpointer.getCheckpointInProgressDirectory().getPath());
        //需要给follower级别节点发送备份文件下载命令
//        addMyCmd(cmd);
    }   
	
	public void addMyCmd(RemoteCmd cmd){
		for(Entry<String,String> slave : slaveZkNodesDataCache.entrySet()){
        	String data = slave.getValue();
        	JSONObject slaveObj = (JSONObject) JSONObject.parse(data);
        	String level = slaveObj.getString("level");
        	if("follower".equals(level)){
        		String machineId = slaveObj.getString("machineId");
        		BlockingQueue<RemoteCmd> cmdqueue = singleSlaveCmd.get(machineId);
        		if(cmdqueue == null){
        			LinkedBlockingQueue<RemoteCmd> queue = new LinkedBlockingQueue<RemoteCmd>();
        			queue.add(cmd);
        			singleSlaveCmd.put(machineId, queue);
        		}else{
        			cmdqueue.add(cmd);
        		}
        	}
        }
	}
	
	public RemoteCmd getMyCmd(String machineId){
		if(singleSlaveCmd.get(machineId)!=null){
			return singleSlaveCmd.get(machineId).poll();
		}else{
			return null;
		}
	}
	
	public void checkpoint()throws Exception{
		logger.info("CheckpointDir:"+this.checkpointer.getCheckpointInProgressDirectory().getPath());
        checkpointBdb(this.checkpointer.getCheckpointInProgressDirectory());
        checkpointSelf(this.checkpointer.getCheckpointInProgressDirectory());
	}
	
	/**
     * Stop the crawl temporarly.
     */
    public synchronized void requestCrawlPause() {
        if (state == PAUSING || state == PAUSED) {
            // Already about to pause
            return;
        }
        state = PAUSED;
        notifyAll();
    }
	
    /**
     * Resume crawl from paused state
     */
    public synchronized void requestCrawlResume() {
        if (state != PAUSING && state != PAUSED && state != CHECKPOINTING) {
            // Can't resume if not been told to pause or if we're in middle of
            // a checkpoint.
            return;
        }
        state = RUNNING;
//        multiThreadMode();
    }
    
    public synchronized void completePause() {
        // Send a notifyAll. At least checkpointing thread may be waiting on a
        // complete pause.
        notifyAll();
    }
    
	/**
     * Tell if the controller is paused
     * @return true if paused
     */
    public boolean isPaused() {
        return state == PAUSED;
    }
    
    public boolean isPausing() {
        return state == PAUSING;
    }
    
    public boolean isRunning() {
        return state == RUNNING;
    }
    
    /**
     * @return True if checkpointing.
     */
    public boolean isCheckpointing() {
        return this.state == CHECKPOINTING;
    }
    
    public Object getState(){
    	return state;
    }
	
	protected void setBdbjeBkgrdThreads(final EnvironmentConfig config,
			final List threads, final String setting) {
		for (final Iterator i = threads.iterator(); i.hasNext();) {
			config.setConfigParam((String) i.next(), setting);
		}
	}

	protected String getBdbLogFileName(final long index) {
		String lastBdbLogFileHex = Long.toHexString(index);
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < (8 - lastBdbLogFileHex.length()); i++) {
			buffer.append('0');
		}
		buffer.append(lastBdbLogFileHex);
		buffer.append(".jdb");
		return buffer.toString();
	}
	
	public File getCheckpointsDisk() {
        return this.checkpointsDisk;
    }

	protected void processBdbLogs(final File checkpointDir,
			final String lastBdbCheckpointLog) throws IOException {
		File bdbDir = CheckpointUtils.getBdbSubDirectory(checkpointDir);
		if (!bdbDir.exists()) {
			bdbDir.mkdir();
		}
		PrintWriter pw = new PrintWriter(new FileOutputStream(new File(
				checkpointDir, "bdbje-logs-manifest.txt")));
		try {
			// Don't copy any beyond the last bdb log file (bdbje can keep
			// writing logs after checkpoint).
			boolean pastLastLogFile = false;
			Set<String> srcFilenames = null;
			// final boolean copyFiles = getCheckpointCopyBdbjeLogs();
			final boolean copyFiles = true;
			do {
				FilenameFilter filter = CheckpointUtils.getJeLogsFilter();
				srcFilenames = new HashSet<String>(Arrays.asList(getStateDisk()
						.list(filter)));
				List tgtFilenames = Arrays.asList(bdbDir.list(filter));
				if (tgtFilenames != null && tgtFilenames.size() > 0) {
					srcFilenames.removeAll(tgtFilenames);
				}
				if (srcFilenames.size() > 0) {
					// Sort files.
					srcFilenames = new TreeSet<String>(srcFilenames);
					int count = 0;
					for (final Iterator i = srcFilenames.iterator(); i
							.hasNext() && !pastLastLogFile;) {
						String name = (String) i.next();
						if (copyFiles) {
							FileUtils.copyFiles(new File(getStateDisk(), name),
									new File(bdbDir, name));
						}
						pw.println(name);
						if (name.equals(lastBdbCheckpointLog)) {
							// We're done.
							pastLastLogFile = true;
						}
						count++;
					}
					// if (LOGGER.isLoggable(Level.FINE)) {
					// LOGGER.fine("Copied " + count);
					// }
				}
			} while (!pastLastLogFile && srcFilenames != null
					&& srcFilenames.size() > 0);
		} finally {
			pw.close();
		}
	}
	
	/**
	 * check master node self,mainly taskrecord,ip distribute infos,account distribute infos
	 * @param checkpointDir
	 * @throws DatabaseException
	 * @throws IOException
	 * @throws RuntimeException
	 */
	protected void checkpointSelf(File checkpointDir)throws DatabaseException, IOException, RuntimeException{
		logger.info("Self checkpoint...");
		CheckpointUtils.writeObjectToFile(this, null, checkpointDir);
		logger.info("Self checkpoint finished");
	}

	protected void checkpointBdb(File checkpointDir)
			throws DatabaseException, IOException, RuntimeException {
		logger.info("Bdb environment checkpoint...");
		EnvironmentConfig envConfig = this.bdbUriFilterEnv.getConfig();
		final List bkgrdThreads = Arrays.asList(new String[] {
				"je.env.runCheckpointer", "je.env.runCleaner","je.env.runINCompressor" });
		try {
			// Disable background threads
			setBdbjeBkgrdThreads(envConfig, bkgrdThreads, "false");
			// Do a force checkpoint. Thats what a sync does (i.e. doSync).
			CheckpointConfig chkptConfig = new CheckpointConfig();
			chkptConfig.setForce(true);
			/* Mark Hayes of sleepycat says:
			 "The default for this property is false, which gives the current
			 behavior (allow deltas). If this property is true, deltas are
			 prohibited -- full versions of internal nodes are always logged
			 during the checkpoint. When a full version of an internal node
			 is logged during a checkpoint, recovery does not need to process
			 it at all. It is only fetched if needed by the application,
			 during normal DB operations after recovery. When a delta of an
			 internal node is logged during a checkpoint, recovery must
			 process it by fetching the full version of the node from earlier
			 in the log, and then applying the delta to it. This can be
			 pretty slow, since it is potentially a large amount of
			 random I/O."
			*/
			chkptConfig.setMinimizeRecoveryTime(true);
			this.bdbUriFilterEnv.checkpoint(chkptConfig);
//			  File    Size (KB)  % Used
//			  --------  ---------  ------
//			  000009f0       9764       6
//			  00000a4c       9765      12
//			  000009f8       9764      16
//			  000009ec       9764      19
//			  000009f4       9764      19
//			  000009fe       9764      20
//			  000009f2       9764      21
//			  000009f5       9764      21
//			  000009e9       9765      22
//			  000009d0       9764      23
//			  000009d1       9765      23
//			  000009f9       9765      23
//			  000009c4       9765      24
//			  000009ce       9765      24
//			  000009cf       9763      24
//			  000009ff       9764      24
//			  000009d6       9765      25
//			  000009f1       9764      25
//			  00000a01       9764      25
//			  00000990       9765      26
//			  000009ba       9765      26
//			  000009bb       9764      26
			this.bdbUriFilterEnv.cleanLog();// the utilized rate is so low
			// LOGGER.fine("Finished bdb checkpoint.");
			// From the sleepycat folks: A trick for flipping db logs.
			EnvironmentImpl envImpl = DbInternal.getEnvironmentImpl(this.bdbUriFilterEnv);
			long firstFileInNextSet = DbLsn.getFileNumber(envImpl
					.forceLogFileFlip());
			// So the last file in the checkpoint is firstFileInNextSet - 1.
			// Write manifest of all log files into the bdb directory.
			final String lastBdbCheckpointLog = getBdbLogFileName(firstFileInNextSet - 1);
			processBdbLogs(checkpointDir, lastBdbCheckpointLog);
			// LOGGER.fine("Finished processing bdb log files.");
			logger.info("Bdb environment checkpoint finished");
		} finally {
			// Restore background threads.
			setBdbjeBkgrdThreads(envConfig, bkgrdThreads, "true");
		}
	}
	
	public void addTaskListener(String taskId,TaskStatusListener listener){
		if(this.taskListeners.get(taskId)!=null){
			this.taskListeners.get(taskId).add(listener);
		}else{
			this.taskListeners.put(taskId, Arrays.asList(new TaskStatusListener[]{listener}));
		}
	}
	
	public void notifyTaskListener(String taskId){
		if(taskListeners.get(taskId)!=null){
			for(TaskStatusListener listener : taskListeners.get(taskId)){
				listener.finishNotify();
			}
			taskListeners.remove(taskId);
		}
	}
	
	public UriUniqFilter getUriUniqFilter(){
		return this.uriUniqFilter;
	}
	
	public Map<String,ClassLoader> getClassLoaderCache(){
		return this.classLoaderCache;
	}
	
	public Map<String,TaskFileParseProtocol> getTaskFileParserCache(){
		return this.taskFileParserCache;
	}
	
	private File getStateDisk() {
		return stateDisk;
	}

	public ZooKeeper getZk() {
		return zk;
	}

	public void setZk(ZooKeeper zk) {
		this.zk = zk;
	}
}
