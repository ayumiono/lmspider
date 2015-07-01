package com.lmdna.spider.node.slave;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.JMException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.lmdna.spider.berkeleydb.FileUtils;
import com.lmdna.spider.dao.model.SpiderBiz;
import com.lmdna.spider.downloader.StatusFulDownloader;
import com.lmdna.spider.mongodb.MongoDBTemplate;
import com.lmdna.spider.monitor.SpiderMonitor;
import com.lmdna.spider.monitor.SpiderStatusMXBean;
import com.lmdna.spider.node.SpiderContext;
import com.lmdna.spider.node.SpiderNode;
import com.lmdna.spider.pipeline.AbstractFilePipeline;
import com.lmdna.spider.pipeline.MongoDBPipeline;
import com.lmdna.spider.pipeline.MysqlImplPipeline;
import com.lmdna.spider.protocol.rpc.HeartBeatProtocol;
import com.lmdna.spider.protocol.rpc.RPCProtocolFactory;
import com.lmdna.spider.protocol.rpc.utils.CheckpointCmd;
import com.lmdna.spider.protocol.rpc.utils.CmdType;
import com.lmdna.spider.protocol.rpc.utils.CommonSpiderLoadTask;
import com.lmdna.spider.protocol.rpc.utils.CrawlTask;
import com.lmdna.spider.protocol.rpc.utils.FileRequestObject;
import com.lmdna.spider.protocol.rpc.utils.FileUploadObject;
import com.lmdna.spider.protocol.rpc.utils.HeartBeatData;
import com.lmdna.spider.protocol.rpc.utils.JarSpiderLoadTask;
import com.lmdna.spider.protocol.rpc.utils.MixedCrawlTask;
import com.lmdna.spider.protocol.rpc.utils.RemoteCmd;
import com.lmdna.spider.protocol.rpc.utils.SpiderRestartCmd;
import com.lmdna.spider.protocol.rpc.utils.SpiderStatusSerialization;
import com.lmdna.spider.protocol.rpc.utils.TaskFinishedMsg;
import com.lmdna.spider.utils.SpiderCommonTool;
import com.lmdna.spider.utils.SpiderGlobalConfig;

/**
 * @author ayumiono
 *spider slave节点类
 *slave节点组件包括一个spider线程池、工作线程池（任务扫描线程、心跳发送线程）
 */
public class FollowerNode extends SpiderNode{
	
	private static final Logger logger = LoggerFactory.getLogger(FollowerNode.class);
	private ExecutorService spider_pool;
	private SpiderMonitor monitor = SpiderMonitor.instance();
	private Map<String, Spider> spiders;
	private Map<String,Spider> spiders4Reg;
	private Map<String, Spider> shutdownSpiders;
	
	private BlockingQueue<Request> reqQueue;
	
	private Map<String,ClassLoader> classLoaderCache;//bizcode - classloader
	private Map<String,String> jarFilePathCache;//bizcode-jarfilepath
	private Map<String,String> taskFilePathCache;
	private HeartBeatProtocol heartBeatProtocol;//心跳接口
	
//	private LMSpiderDFSServer dfsServer;
	private Thread heartBeatThread;
	
	public FollowerNode(SpiderContext context){
		this._context = context;
	}
	
	private void initComponent(){
		logger.info("Slave Node:start to initialize spider component...");
		try {
			this.spider_pool = Executors.newFixedThreadPool(Integer.parseInt(SpiderGlobalConfig.getValue(SpiderGlobalConfig.SPIDER_POOL_SIZE)),new SpiderFactory());//spider线程池
			
			this.spiders = new ConcurrentHashMap<String, Spider>();
			this.shutdownSpiders = new ConcurrentHashMap<String, Spider>();
			this.reqQueue = new LinkedBlockingQueue<Request>(Integer.parseInt(SpiderGlobalConfig.getValue(SpiderGlobalConfig.SPIDER_SLAVE_REQQUEUE_CEILING)));// 最大50万条请求，超出50万条等待 
			
			this.classLoaderCache = new ConcurrentHashMap<String,ClassLoader>();
			this.jarFilePathCache = new ConcurrentHashMap<String,String>();
			this.taskFilePathCache = new ConcurrentHashMap<String,String>();
//			ServerSocket ss = new ServerSocket();
//			ss.bind(new InetSocketAddress(_context.getFileServerPort()));
//			dfsServer = new LMSpiderDFSServer(ss);
			heartBeatThread = new Thread(new HeartBeatThread(),"HeartBeatThread");
			logger.info("Slave Node:components initialize completed.");
		} catch (Exception e) {
			logger.error("SpiderSlave:components initialize failed!", e);
		}
	}
	
	public void run(){
		initComponent();
		heartBeatThread.start();
//		dfsServer.start();
	}
	
	private void loadSpider(SpiderBiz biz){
		try {
			Spider spider = fixSpider(biz);
			Pipeline pipeline = null;
			if(_context.getPersistenceMethod() == SpiderContext.FILE_PERSISTENCE){
			}else if(_context.getPersistenceMethod() == SpiderContext.MYSQL_PERSISTENCE){
				JdbcTemplate jdbcTemplate = null;
				jdbcTemplate = _context.getParameter(SpiderContext.JDBCTEMPLATE);
				pipeline = new MysqlImplPipeline(jdbcTemplate,biz.getPersistenceTable());
			}else if(_context.getPersistenceMethod() == SpiderContext.MONGODB_PERSISTENCE){
				pipeline = new MongoDBPipeline(MongoDBTemplate.Default(), biz.getPersistenceTable());
			}
			spider.setExitWhenComplete(false)
				  .thread(biz.getThreadCount())
				  .addPipeline(pipeline)
				  .setUUID(biz.getBizCode());
			spider.setEmptySleepTime(30000);
			spider.addPipeline(new AbstractFilePipeline() {

				@Override
				public String processSinglePage(Page page) throws Exception {
					ResultItems resultItems = page.getResultItems();
					Map<String,Object> fields = resultItems.getAll();
					Gson gson = new Gson();
					return gson.toJson(fields);
				}
				
			});
			spiders.put(biz.getBizCode(), spider);
			try {
				monitor.register(spider);
			} catch (JMException e) {
				logger.info("lmdna-spider:a new spider failed to register to the jmx！");
				logger.error("lmdna-spider:a new spider failed to register to the jmx！",e);
			}
			spider_pool.execute(spider);
			logger.info("lmdna-spider:a new spider initiallization completed.");
		} catch (Exception e) {
			return;
		}
	}
	
	/**
	 * 收集心跳提交数据
	 * @return
	 * @throws UnknownHostException 
	 */
	private HeartBeatData gatherHeartBeatData() throws UnknownHostException{
		HeartBeatData heartBeatData = new HeartBeatData();
		heartBeatData.setSpiderCounts(spiders.size());
		heartBeatData.setLevel(SpiderNode.Level.follower);
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
	
	/**
	 * 提供http控制spiders接口：reset=remove + load
	 * 
	 * @param spiderId
	 * @param bizId
	 * @throws Exception
	 */
	public void reset(String bizcode) throws Exception {
		Spider spider = spiders.get(bizcode);
		if (spider == null) {
			throw new Exception("[reset]there is no spider named "+bizcode+"found!");
		}
		spider.stop();
		spiders.remove(bizcode);
	}

	/**
	 * 提供http控制spiders接口：shutdown
	 * 
	 * @param spiderId
	 * @throws Exception
	 */
	public void shutdown(String bizcode) throws Exception {
		
		Spider spider = spiders.get(bizcode);
		if (spider == null) {
			throw new Exception("[shutdown]there is no spider named "+bizcode+"found!");
		}
		monitor.getSpiderStatusMap().get(bizcode).stop();
		shutdownSpiders.put(bizcode, spider);
		spiders.remove(bizcode);
	}

	/**
	 * 提供http控制spiders接口：start
	 * 
	 * @param spiderId
	 * @throws Exception
	 */
	public void start(String bizcode) throws Exception {
		Spider spider = shutdownSpiders.get(bizcode);
		if (spider == null) {
			throw new Exception("[start]there is no spider named "+bizcode+"found!");
		}
		spiders.put(bizcode, spider);
		spider_pool.execute(spider);
		shutdownSpiders.remove(bizcode);
	}

	public void remove(String bizcode) throws Exception {
		Spider spider = spiders.get(bizcode);
		Spider spiderCopy = shutdownSpiders.get(bizcode);
		if (spider == null && spiderCopy == null) {
			throw new Exception("[remove]there is no spider named "+bizcode+"found!");
		}
		spider.stop();
		spiders.remove(bizcode);
		shutdownSpiders.remove(bizcode);
	}

	public SpiderMonitor getMonitor(){
		return monitor;
	}
	
	/**
	 * 文件上传（以文件形式存储的抓取结果的上传）
	 * @author ayumiono
	 */
	class FileUploadClient implements Runnable {
		private FileUploadObject uploadFile;
		private String fileServerHost;
		private int fileServerPort;
		
		public FileUploadClient(String fileServerHost, int fileServerPort, FileUploadObject uploadFile) {
			this.fileServerHost = fileServerHost;
			this.fileServerPort = fileServerPort;
			this.uploadFile = uploadFile;
		}
		
		@Override
		public void run() {
			try{
				Selector selector = null;
				selector = Selector.open();
				SocketChannel channel = SocketChannel.open();   
				channel.configureBlocking(false);   
				channel.connect(new InetSocketAddress(fileServerHost, fileServerPort));   
				channel.register(selector, SelectionKey.OP_CONNECT);
				while(true){
					selector.select();
					Iterator<SelectionKey> it = selector.selectedKeys().iterator();
					while(it.hasNext()){
						SelectionKey readyKey = it.next();
						it.remove();
						if(readyKey.isConnectable()){
							SocketChannel socketChannel = (SocketChannel) readyKey.channel();  
						    if(socketChannel.isConnectionPending())  
						        socketChannel.finishConnect();
							ByteBuffer bytebuffer = ByteBuffer.allocate(20+uploadFile.getContents().length+uploadFile.getFileName().length+uploadFile.getFilePath().length);
						    bytebuffer.putInt(1);//upload
							bytebuffer.putInt(uploadFile.getNameLength());
							bytebuffer.put(ByteBuffer.wrap(uploadFile.getFileName()));
						    bytebuffer.putInt(uploadFile.getFilePathLength());
						    bytebuffer.put(ByteBuffer.wrap(uploadFile.getFilePath()));
						    bytebuffer.putLong(uploadFile.getContentLength());
						    bytebuffer.put(ByteBuffer.wrap(uploadFile.getContents()));
						    bytebuffer.flip();
						    socketChannel.write(bytebuffer);  
						    socketChannel.register(selector, SelectionKey.OP_READ);
						}else if (readyKey.isReadable()) {
//							SocketChannel socketChannel = (SocketChannel) readyKey.channel();
							//读取服务器返回信息
						}
					}
				}
			}catch(Exception e){
				
			}
			
		}
		
		private void write(File file,SocketChannel socketChannel) throws IOException{
			String fileName = file.getName();
			int fileNameLength = fileName.getBytes().length;
			if(file.isDirectory()){
				try{
					ByteBuffer write_buffer = ByteBuffer.allocate(1024);
					write_buffer.put(new byte[]{(byte)2},0,1);//cmd:2 文件夹名
					write_buffer.put(SpiderCommonTool.intToBytes(fileNameLength));
					write_buffer.put(fileName.getBytes(), 0, fileNameLength);
					write_buffer.flip();
					socketChannel.write(write_buffer);
					write_buffer.clear();
					File[] files = file.listFiles();
					for(File f : files){
						write(f,socketChannel);
					}
				}catch(Exception e){
					
				}
			}else{
				FileInputStream fis = new FileInputStream(file);
				FileChannel fc = fis.getChannel();
				try {
					ByteBuffer write_buffer = ByteBuffer.allocate(1024*1024);
					write_buffer.put(new byte[] { (byte) 1 },0,1);// 1：文件名
					write_buffer.put(SpiderCommonTool.intToBytes(fileNameLength));
					write_buffer.put(fileName.getBytes(), 0, fileNameLength);
					write_buffer.flip();
					socketChannel.write(write_buffer);
					write_buffer.clear();
					write_buffer.put(new byte[] { (byte) 0 }, 0, 1);
					write_buffer.put(SpiderCommonTool.longToBytes(fis.available()));
					write_buffer.flip();
					socketChannel.write(write_buffer);
					write_buffer.clear();
					int size = 0;
					while ((size = fc.read(write_buffer)) != -1) {
						write_buffer.rewind();
						write_buffer.limit(size);
						//等效于write_buffer.flip();
						while(write_buffer.hasRemaining()){
							socketChannel.write(write_buffer);
						}
						write_buffer.clear();
					}
				}catch(Exception e){
					
				} finally {
					try {
						fc.close();
					} catch (Exception ex) {
					}
					try {
						fis.close();
					} catch (Exception ex) {
					}
					try {
						socketChannel.close();
					} catch (IOException e) {
						
					}
				}
			}
		}
		
	}
	
	/**
	 * 文件下载请求
	 * @author ayumiono
	 */
	class FileDownloadClient implements Runnable {
		
		private FileRequestObject fileRequest;
		FileOutputStream out = null;
		long count = 0;// 当前命令是否完成数据的读取，如果完成读取则进行下一条命令
		int cmd = -1;
		int bsl = 0;

		public FileDownloadClient(FileRequestObject fileRequest) {
			this.fileRequest = fileRequest;
		}

		@Override
		public void run() {
			try {
				Selector selector = null;
				selector = Selector.open();
				SocketChannel channel = SocketChannel.open();   
				channel.configureBlocking(false);   
				channel.connect(new InetSocketAddress((String)_context.getParameter("master.fileserver.host"), (Integer)_context.getParameter("master.fileserver.port")));   
				channel.register(selector, SelectionKey.OP_CONNECT);
				while (true) {
					if(!selector.isOpen()){
						break;
					}
					selector.select();
					Iterator<SelectionKey> it = selector.selectedKeys().iterator();
					while (it.hasNext()) {
						SelectionKey readyKey = it.next();
						it.remove();
						if (readyKey.isConnectable()) {
							SocketChannel socketChannel = (SocketChannel) readyKey.channel();  
						    if(socketChannel.isConnectionPending())  
						        socketChannel.finishConnect();
						    ByteBuffer bytebuffer = ByteBuffer.allocate(12+fileRequest.getFilePath().getBytes().length+fileRequest.getFileName().getBytes().length);
						    bytebuffer.putInt(0);
						    bytebuffer.putInt(fileRequest.getFilePath().getBytes().length);
						    bytebuffer.put(ByteBuffer.wrap(fileRequest.getFilePath().getBytes()));
						    bytebuffer.putInt(fileRequest.getFileName().getBytes().length);
						    bytebuffer.put(ByteBuffer.wrap(fileRequest.getFileName().getBytes()));
						    bytebuffer.flip();
						    socketChannel.write(bytebuffer);
						    socketChannel.register(selector, SelectionKey.OP_READ);
						} else if (readyKey.isReadable()) {
							SocketChannel socketChannel = (SocketChannel) readyKey.channel();
							try{
								if("jar".equals(fileRequest.getType())){
									File downloadJar = new File(SpiderGlobalConfig.getValue(SpiderGlobalConfig.SPIDER_JAR_DOWNLOAD_DIR));
									
									ensureDirectory(downloadJar);
									ByteBuffer buf = ByteBuffer.allocate(1024*1024);
									int length = 0;
									byte[] bs = new byte[1024*1024];
									while((length = socketChannel.read(buf))>=0 || count!=0){
										buf.flip();
										byte[] bytes = buf.array();
										writeData(bytes, 0, length, downloadJar.getPath(), bs);
										buf.clear();
							        }
									jarFilePathCache.put(fileRequest.getBizCode(), downloadJar.getPath()+File.separator+fileRequest.getFileName());
									fileRequest.finished();
								}else if("task".equals(fileRequest.getType())){
									SimpleDateFormat dateFormat = new SimpleDateFormat();
									dateFormat.applyPattern("yyyyMMdd");
									File taskFile = new File(SpiderGlobalConfig.getValue(SpiderGlobalConfig.SPIDER_TASKFILE_DOWNLOAD_DIR)+dateFormat.format(new Date())+File.separator);
									ensureDirectory(taskFile);
									byte[] bs = new byte[1024*1024];
									int length = 0;
									ByteBuffer buf = ByteBuffer.allocate(1024*1024);
									while((length = socketChannel.read(buf))>=0 || count!=0){
										buf.flip();
										byte[] bytes = buf.array();
										writeData(bytes, 0, length, taskFile.getPath(), bs);
										buf.clear();
							        }
									taskFilePathCache.put(fileRequest.getTaskId(), taskFile.getPath()+File.separator+fileRequest.getFileName());
									fileRequest.finished();
								}else if("checkpoint".equals(fileRequest.getType())){
									File checkpoint = new File(SpiderGlobalConfig.getValue(SpiderGlobalConfig.SPIDER_BDB_CHECKPOINT_DIR));
									//备份目录下只保留一个备份文件
									if(checkpoint.exists() && checkpoint.isDirectory()){
										FileUtils.deleteDir(checkpoint);
									}
									ensureDirectory(checkpoint);
									ByteBuffer buf = ByteBuffer.allocate(1024*1024);
									int length = 0;
									byte[] bs = new byte[1024*1024];
									while((length = socketChannel.read(buf))>=0 || count!=0){
										buf.flip();
										byte[] bytes = buf.array();
										writeData(bytes, 0, length, checkpoint.getPath(), bs);
										buf.clear();
							        }
								}
							}catch(Exception e){
								break;
							}finally{
								socketChannel.close();
								selector.close();
							}
						}
					}
				}
			} catch (IOException e) {
				logger.error("File Client:文件"+fileRequest.getFilePath()+" download failed!",e);
				fileRequest.failed();
			}
		}
		
		private void ensureDirectory(File dir) throws IOException {
			if (!dir.mkdirs() && !dir.isDirectory()) {
				throw new IOException("Mkdirs failed to create " + dir.toString());
			}
		}
		
		private void writeData(byte[] buf,int off, int len,String savePath, byte[] bs) throws IOException{
			if (len - off == 0)
				return;
			int i = off;
			if (count == 0l) {
				cmd = buf[i++];
				count = -1l;
				if (len - i == 0)
					return;
				writeData(buf, i, len, savePath, bs);
			} else if (count == -1l) {
				switch (cmd) {
				case 0:
					if (len - i + bsl < 8) {
						System.arraycopy(buf, i, bs, bsl, len - i);
						bsl = len - i;
						i = 0;
						return;
					}
					System.arraycopy(buf, i, bs, bsl, 8 - bsl);
					count = SpiderCommonTool.bytesToLong(bs, 0);
					i += 8 - bsl;
					bsl = 0;
					writeData(buf, i, len, savePath, bs);
					break;
				case 1:
				case 2:
					if (len - i + bsl < 4) {
						System.arraycopy(buf, i, bs, bsl, len - i);
						bsl = len - i;
						i = 0;
						return;
					}
					System.arraycopy(buf, i, bs, bsl, 4 - bsl);
					count = SpiderCommonTool.bytesToInt(bs, 0);
					i += 4 - bsl;
					bsl = 0;
					writeData(buf, i, len, savePath, bs);
					break;
				}
			} else {
				switch (cmd) {
				case 0:
					if (len - i - count >= 0) {
						try {
							logger.info("写入完成");
							out.write(buf, i, (int) count);
							i += count;
							count = 0;
							out.flush();
						} finally {
							if (out != null)
								out.close();
						}
						writeData(buf, i, len, savePath, bs);
					} else {
						out.write(buf, i, len - i);
						count -= len - i;
						i = 0;
					}
					break;
				case 1:
					if (len - i - count < 0) {
						System.arraycopy(buf, i, bs, bsl, len - i);
						bsl += len - i;
						count -= bsl;
						i = 0;
						return;
					} else {
						System.arraycopy(buf, i, bs, bsl, (int) count);
						String name = new String(bs, 0, (int) count + bsl);
						logger.info("文件：" + savePath + File.separator + name);
						out = new FileOutputStream(new File(savePath,name));
						bsl = 0;
						i += count;
						count = 0;
						writeData(buf, i, len, savePath, bs);
					}
					break;
				case 2:
					if (len - i - count < 0) {
						System.arraycopy(buf, i, bs, bsl, len - i);
						bsl += len - i;
						count -= bsl;
						i = 0;
						return;
					} else {
						System.arraycopy(buf, i, bs, bsl, (int) count);
						String name = new String(bs, 0, bsl + (int) count);
						File file = new File(savePath,name);
						bsl = 0;
						i += count;
						count = 0;
						if (!file.exists()) {
							file.mkdirs();
						}
						logger.info("文件夹：" + savePath + File.separator+ name);
						writeData(buf, i, len, savePath, bs);
					}
					break;
				}
			}
		}
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
					String bizcode = (String) req.getExtra("bizcode");
					try{
						if(spiders.containsKey(bizcode)){
							spiders.get(bizcode).addRequest(req);
						}else{
							//不做任何处理
						}
					}catch(IllegalStateException e){
						if(e.getMessage().equalsIgnoreCase("queue full")){
							logger.warn("spider>>>{}---process request failed as request queue is overflow!",bizcode);
							try{
								reqQueue.add(req);//如果Spider中任务队列溢出，默认是Integer.Max，则归还任务.
							}catch(IllegalStateException e2){
								if(e.getMessage().equalsIgnoreCase("queue full")){
									continue;//如果连reqQueue也满了，那只能放弃该任务
								}
							}
						}else{
							logger.error("spider>>>{}---process request failed!",bizcode);
						}
					}
				} catch (InterruptedException e) {
					logger.info("ReqQueueConsumeThread Interrupted!");
					logger.error("ReqQueueConsumeThread Interrupted!");
				} catch(Exception e){
					logger.error(e.getMessage(),e);
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
			while(!Thread.currentThread().isInterrupted()){
				try{
					Thread.sleep(Long.parseLong(SpiderGlobalConfig.getValue(SpiderGlobalConfig.SPIDER_SLAVE_HEARTBEAT_INTERVAL)));
					logger.info("Heartbeat Sent...");
					heartBeatProtocol = RPCProtocolFactory.get(HeartBeatProtocol.class);
					if(heartBeatProtocol != null){
						List<RemoteCmd> acceptTasks = heartBeatProtocol.handleHeartBeat(gatherHeartBeatData());
						for(RemoteCmd task : acceptTasks){
							String cmdType = task.getCmdType();
							if(CmdType.REMOTE_CMD_NEWTASK.equals(cmdType)){
								CrawlTask acceptTask = (CrawlTask)task;
								logger.info("CmdType:REMOTE_CMD_NEWTASK "+acceptTask.toString());
								List<FileRequestObject> downloadFileWatcher = new ArrayList<FileRequestObject>();
								//没有对应taskid的任务文件路径，则说明这是第一次执行该任务文件，需要下载
								if(taskFilePathCache.get(acceptTask.getTaskId()) == null){
									FileRequestObject fileRequestObject = new FileRequestObject();
									fileRequestObject.setType("task");
									fileRequestObject.setTaskId(acceptTask.getTaskId());
									fileRequestObject.setBizCode(acceptTask.getBizCode());
									fileRequestObject.setFilePath(acceptTask.getTaskFilePath());
									fileRequestObject.setFileName(acceptTask.getTaskFileName());
									new Thread(new FileDownloadClient(fileRequestObject)).start();
									downloadFileWatcher.add(fileRequestObject);
								}
								new Thread(new CrawlTaskParseThread(acceptTask,downloadFileWatcher.toArray(new FileRequestObject[0]))).start();
							}else if(CmdType.REMOTE_CMD_MIXEDTASK.equals(cmdType)){
								MixedCrawlTask acceptTask = (MixedCrawlTask)task;
								if(acceptTask.getReqs()!=null){
									for(Request req : acceptTask.getReqs()){
										String url = req.getUrl();
										for(Entry<String,Spider> entry : spiders4Reg.entrySet()){
											String url_rule = entry.getKey();
											Spider spider = entry.getValue();
											Pattern p = Pattern.compile(url_rule, Pattern.CASE_INSENSITIVE);
											Matcher m = p.matcher(url);
											if(m.find()){
												spider.addRequest(req);
												break;
											}
										}
									}
								}
							}else if(CmdType.REMOTE_CMD_COMMON_SPIDER_LOAD.equals(cmdType)){
								logger.info("CmdType:REMOTE_CMD_COMMON_SPIDER_LOAD ");
								CommonSpiderLoadTask acceptTask = (CommonSpiderLoadTask)task;
								loadSpider(acceptTask.getBiz());
							}else if(CmdType.REMOTE_CMD_JAR_SPIDER_LOAD.equals(cmdType)){
								List<FileRequestObject> downloadFileWatcher = new ArrayList<FileRequestObject>();
								JarSpiderLoadTask acceptTask = (JarSpiderLoadTask)task;
								logger.info("CmdType:REMOTE_CMD_JAR_SPIDER_LOAD "+acceptTask.toString());
								FileRequestObject fileRequestObject = new FileRequestObject();
								fileRequestObject.setType("jar");
								fileRequestObject.setBizCode(acceptTask.getBizCode());
								fileRequestObject.setFilePath(acceptTask.getJarFilePath());
								fileRequestObject.setFileName(acceptTask.getJarFileName());
								new Thread(new FileDownloadClient(fileRequestObject)).start();
								downloadFileWatcher.add(fileRequestObject);
								new Thread(new JarSpiderLoadThread(acceptTask.getBiz(),downloadFileWatcher.toArray(new FileRequestObject[0]))).start();
							}else if(CmdType.REMOTE_CMD_TASK_FINISHED.equals(cmdType)){
								TaskFinishedMsg msg = (TaskFinishedMsg)task;
								String taskId = msg.getTaskId();
								String bizCode = msg.getBizCode();
								logger.info("REMOTE_CMD_TASK_FINISHED {}",taskId);
								Spider myspider = spiders.get(bizCode);
								if(myspider!=null){
									for(Pipeline pipeline : myspider.getPipelines()){
										Class pipeline_class = pipeline.getClass();
										Class current_class = pipeline_class;
										while(true){
											try{
												Method flush = current_class.getMethod("flush",Task.class);
												logger.info(pipeline.getClass().getName()+"@"+myspider.getUUID()+" start to invoke flush method.");
												flush.invoke(pipeline,myspider);
												break;
											}catch(Exception e){
												current_class = current_class.getSuperclass();
												if(current_class==null){
													break;
												}
												continue;
											}
										}
									}
								}
							}else if(CmdType.REMOTE_CMD_CHECKPOINT.equals(cmdType)){
								CheckpointCmd checkpointCmd = (CheckpointCmd)task;
								logger.info("CmdType:REMOTE_CMD_CHECKPOINT "+checkpointCmd.getCheckpointDir());
								//下载checkpoint文件夹
								FileRequestObject fileRequestObject = new FileRequestObject();
								fileRequestObject.setType("checkpoint");
								fileRequestObject.setFilePath(checkpointCmd.getCheckpointDir());
								new Thread(new FileDownloadClient(fileRequestObject)).start();
							}else if(CmdType.REMOTE_CMD_SPIDER_TERMINATE.equals(cmdType)){
								//TODO
							}else if(CmdType.REMOTE_CMD_SPIDER_REMOVE.equals(cmdType)){
								//TODO
							}else if(CmdType.REMOTE_CMD_SPIDER_RESTART.equals(cmdType)){
								//TODO
								SpiderRestartCmd restartCmd = (SpiderRestartCmd)task;
							}
							
						}
					}
				}catch(InterruptedException e){
					logger.info("Slave Node: HeartBeatThread Terminated!");
					break;//
				}catch(Exception e){
					logger.error("Slave Node: HeartBeatThread ERROR",e);
				}
			}
		}
	}
	
	/**
	 * jar包解析
	 * @author ayumiono
	 *
	 */
	class JarSpiderLoadThread implements Runnable{
		private SpiderBiz biz;
		private List<FileRequestObject> locks = new ArrayList<FileRequestObject>();

		public JarSpiderLoadThread(SpiderBiz biz,FileRequestObject... o){
			this.biz = biz;
			if(o!=null && o.length>0){
				locks = Arrays.asList(o);
			}
		}
		
		@Override
		public void run() {
			for(FileRequestObject watcher : locks){
				for(;;){
					if(watcher.isFailed() || watcher.isFinished()){
						break;
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			//如果需要从jar包中加载spider
			String jarfilepath = jarFilePathCache.get(biz.getBizCode());
			if(StringUtils.isEmpty(jarfilepath)){
				return;
			}
			logger.info("start to load spider from jar...");
			try {
				ClassLoader loader = newClassLoader(jarfilepath);
				classLoaderCache.put(biz.getBizCode(), loader);
				String spider_process_name = getSpiderClassFromJar(jarfilepath);
				String spider_pipeline_name = getPipelineClassFromJar(jarfilepath);//新增pipeline支持
				Thread.currentThread().setContextClassLoader(loader);
			    Site site = fixSite(biz);
			    Constructor<?> c;
				c = Class.forName(spider_process_name,true,loader).getConstructor();
				Spider spider = (Spider) c.newInstance();
				spider.setUUID(biz.getBizCode());
				spider.setSite(site);
				if(spider_pipeline_name != null){
					Class pipeline_clazz = Class.forName(spider_pipeline_name,true,loader);
					c = pipeline_clazz.getConstructor();
					Pipeline pipeline = (Pipeline) c.newInstance();
					List<Class> interfacelist = new ArrayList<Class>();
					interfacelist.addAll(Arrays.asList(pipeline_clazz.getInterfaces()));
					Class current_class = pipeline_clazz;
					while(true){
						Class parent = current_class.getSuperclass();
						if(parent!=null){
							interfacelist.addAll(Arrays.asList(parent.getInterfaces()));
							current_class = parent;
						}else{
							break;
						}
					}
					//如果继承了SpiderContextAware接口，则调用setSpiderContext方法，将_context注入到对象中
					for(Class inter : interfacelist){
						if(("com.lmdna.spider.protocol.SpiderContextAware").equals(inter.getName())){
							logger.info("inject spidercontext bean into Pipeline.");
							Method m = pipeline_clazz.getMethod("setSpiderContext", SpiderContext.class);
							m.invoke(pipeline, _context);
						}
					}
					spider.addPipeline(pipeline);
				}
				if(biz.getWebsiteConfigBO().getNeedLogin() == 0){
					spider.setDownloader(new StatusFulDownloader());
				}
				spider.setExitWhenComplete(false).thread(biz.getThreadCount()).setUUID(biz.getBizCode());
				spider.addPipeline(new AbstractFilePipeline() {

					@Override
					public String processSinglePage(Page page) throws Exception {
						ResultItems resultItems = page.getResultItems();
						Map<String,Object> fields = resultItems.getAll();
						Gson gson = new Gson();
						return gson.toJson(fields);
					}
				});
				spiders.put(biz.getBizCode(), spider);
				try {
					monitor.register(spider);
				} catch (JMException e) {
					logger.info("lmdna-spider:a new spider failed to register to the jmx！");
					logger.error("lmdna-spider:a new spider failed to register to the jmx！",e);
				}
				spider_pool.execute(spider);
				logger.info("LmdnaSpider: New Spider Initiallization From JAR  Finished!");
			} catch (Throwable e) {
				RPCProtocolFactory.get(HeartBeatProtocol.class).handleJarParseError();
				logger.error(e.getMessage(),e);
			}
		}
	}
	
	class CrawlTaskParseThread implements Runnable{

		private CrawlTask crawlTask;
		
		private List<FileRequestObject> locks = new ArrayList<FileRequestObject>();
		
		public CrawlTaskParseThread(CrawlTask crawlTask,FileRequestObject... o){
			this.crawlTask = crawlTask;
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
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			String taskfilepath = taskFilePathCache.get(crawlTask.getTaskId());
			Spider spider = spiders.get(crawlTask.getBizCode());
			if(spider == null){
				return;
			}
			int startRow = crawlTask.getStart();
			int endRow = crawlTask.getEnd();
			try {
				List<Request> reqs = readSerializeReqFile(new FileInputStream(new File(taskfilepath)), startRow, endRow);
				logger.info("read SerializeReq from File size>>>"+reqs.size());
				spider.addRequest(reqs.toArray(new Request[1]));
			} catch (Throwable e) {
				logger.error("TaskFile Parse Failed!",e);
				RPCProtocolFactory.get(HeartBeatProtocol.class).handleCrawlTaskParseError(crawlTask);
			}
		}
		
		private List<Request> readSerializeReqFile(InputStream in,int startRow,int endRow) throws IOException{
			List<Request> result = new ArrayList<Request>();
			FileInputStream fs = (FileInputStream)in;
			BufferedInputStream bis = new BufferedInputStream(fs);
			BufferedReader br = new BufferedReader(new InputStreamReader(bis, "UTF-8"),1024*1024);
			int currentlineno = 0;
			String line = "";
			while((line = br.readLine())!=null){
				currentlineno = currentlineno + 1;
				if(currentlineno>=startRow && currentlineno<=endRow){
					JSONObject json = JSONObject.parseObject(line);
					Request request = JSONObject.toJavaObject(json, Request.class);
					result.add(request);
	        	}
				if(currentlineno==endRow){
					break;
				}
			}
			br.close();
			return result;
		}
	}
	
	/**
	 * 接收hessian远程任务
	 * @param reqJSON
	 * @return
	 */
	public String acceptRequest(String reqJSON){
		Request req;
		try{
			req = JSON.parseObject(reqJSON,Request.class);
		}catch(Exception e){
			return String.format("{'code':'402','msg':'参数格式不正确！','error':%s}", e.getMessage());
		}
		try{
			reqQueue.add(req);
			return  String.format("{'code':'200','msg':'抓取请求提交成功！'}");
		}catch(IllegalStateException e){
			if(e.getMessage().equalsIgnoreCase("Queue Full")){
				return String.format("{'code':'401','msg':'抓取队列已满(%d)！为了避免后台内存泄露请稍后重试！','error':%s}",reqQueue.size(),e.getMessage());
			}else{
				return String.format("{'code':'400','msg':'抓取请求提交失败！','error':%s}",e.getMessage());
			}
		}catch(Exception e){
			return String.format("{'code':'400','msg':'抓取请求提交失败！','error':%s}",e.getMessage());
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
    
	@Override
	public void close() {
		try{
			if(spider_pool!=null){
				spider_pool.shutdownNow();
			}
			if(heartBeatThread!=null){
				heartBeatThread.interrupt();
			}
//			if(dfsServer!=null){
//				dfsServer.interrupt();
//			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
}
