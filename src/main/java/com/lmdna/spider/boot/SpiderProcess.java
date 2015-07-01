package com.lmdna.spider.boot;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lmdna.spider.node.SpiderContext;
import com.lmdna.spider.node.SpiderNode;
import com.lmdna.spider.node.ZKNodeData;
import com.lmdna.spider.node.master.MasterNode;
import com.lmdna.spider.node.slave.FollowerNode;
import com.lmdna.spider.protocol.rpc.AccountProtocol;
import com.lmdna.spider.protocol.rpc.HeartBeatProtocol;
import com.lmdna.spider.protocol.rpc.IpProtocol;
import com.lmdna.spider.protocol.rpc.RPCProtocolFactory;
import com.lmdna.spider.protocol.rpc.VerifyImgProtocol;
import com.lmdna.spider.utils.SpiderGlobalConfig;

/**
 * lmdna_spider
 * @author ayumiono
 */
public class SpiderProcess{

	private static final Logger logger = LoggerFactory.getLogger(SpiderProcess.class);
	private SpiderNode node;
	private ZooKeeper zk;
	private String znode_path;
	private SpiderContext _context;
	
	public SpiderProcess(SpiderContext context) throws Exception{
		this._context = context;
		init();
	}
	
	/**
	 * 组件初始化
	 * @throws Exception
	 */
	private void init(){
		logger.info("Boot:lmdna-spider bootstrap start...");
		try {
			zk = new ZooKeeper(SpiderGlobalConfig.getValue("zookeeper.cluster.server"),30000,new MyWatcher());//2*ticktime~20*ticktime
			if(zk.exists(SpiderGlobalConfig.getValue("zookeeper.spider.root.path"), true)==null){
				zk.create(SpiderGlobalConfig.getValue("zookeeper.spider.root.path"), null, Ids.OPEN_ACL_UNSAFE , CreateMode.PERSISTENT);
			}
		} catch (Exception e1) {
			logger.error("connect to the zookeeper server{} failed！",SpiderGlobalConfig.getValue("zookeeper.cluster.server"),e1);
			System.exit(-1);
		}
		try {
			if(this._context.getNodeLevel() == SpiderNode.Level.master){
				if(zk.exists(SpiderGlobalConfig.getValue("zookeeper.spider.master.path"), false)==null){
					String zkNodeData = generateZKNodeData();
					znode_path = zk.create(SpiderGlobalConfig.getValue("zookeeper.spider.master.path"), zkNodeData.getBytes(), Ids.OPEN_ACL_UNSAFE , CreateMode.EPHEMERAL);
					_context.setParameter("zk", zk);
					this.node = new MasterNode(_context);
					logger.info("Boot:master node built successfully on zookeeper cluster.");
				}else{
					System.out.println("Boot:master node already exist on zookeeper cluster!");
					System.exit(-1);
				}
			}else if(this._context.getNodeLevel() == SpiderNode.Level.follower){
				if(zk.exists(SpiderGlobalConfig.getValue("zookeeper.spider.master.path"), false)==null){
					_context.setParameter("zk", zk);
					_context.setParameter(SpiderContext.NODE_LEVEL, SpiderNode.Level.master);
					String zkNodeData = generateZKNodeData();
					znode_path = zk.create(SpiderGlobalConfig.getValue("zookeeper.spider.master.path"), zkNodeData.getBytes(), Ids.OPEN_ACL_UNSAFE , CreateMode.EPHEMERAL);
					this.node = new MasterNode(_context);
					logger.info("Boot:master node built successfully on zookeeper cluster.");
				}else{
					zk.exists(SpiderGlobalConfig.getValue("zookeeper.spider.master.path"), true);
					updateMasterNodeData();
					String zkNodeData = generateZKNodeData();
					if(zk.exists(SpiderGlobalConfig.getValue("zookeeper.spider.slaves.path"), true)==null){
						zk.create(SpiderGlobalConfig.getValue("zookeeper.spider.slaves.path"), null, Ids.OPEN_ACL_UNSAFE , CreateMode.PERSISTENT);
					}
					znode_path = zk.create(SpiderGlobalConfig.getValue("zookeeper.spider.slaves.children.path"), zkNodeData.getBytes(), Ids.OPEN_ACL_UNSAFE , CreateMode.EPHEMERAL_SEQUENTIAL);
					logger.info("Boot:slave node built successfully on zookeeper cluster.");
					this.node = new FollowerNode(_context);
				}
			}else if(this._context.getNodeLevel() == SpiderNode.Level.worker){
				if(zk.exists(SpiderGlobalConfig.getValue("zookeeper.spider.master.path"), true)==null){
					logger.info("Boot Error:the worker node cannot find the master node!");
					return;
				}else{
					updateMasterNodeData();
					String zkNodeData = generateZKNodeData();
					if(zk.exists(SpiderGlobalConfig.getValue("zookeeper.spider.slaves.path"), true)==null){
						zk.create(SpiderGlobalConfig.getValue("zookeeper.spider.slaves.path"), null, Ids.OPEN_ACL_UNSAFE , CreateMode.PERSISTENT);
					}
					znode_path = zk.create(SpiderGlobalConfig.getValue("zookeeper.spider.slaves.children.path"), zkNodeData.getBytes(), Ids.OPEN_ACL_UNSAFE , CreateMode.EPHEMERAL_SEQUENTIAL);
					logger.info("Boot:slave node built successfully on zookeeper cluster.");
					this.node = new FollowerNode(_context);
				}
			}
		} catch (Exception e) {
			logger.error("Boot:spider node initiallization failed!",e);
			System.exit(-1);
		} 
		_context.setParameter("zk", zk);
	}
	
	public void run(){
		this.node.run();
	}
	
	private void startToElectionContest(){
		try{
			String zkNodeData = generateZKNodeData();
			zk.create(SpiderGlobalConfig.getValue("zookeeper.spider.master.path"), zkNodeData.getBytes(), Ids.OPEN_ACL_UNSAFE , CreateMode.EPHEMERAL);
			zk.delete(znode_path, -1);
			//zk.getData(SpiderGlobalConfig.getValue("zookeeper.spider.master.path"), true, null);//重新注册zk监听(本身是masternode不需要监听自身节点)
			this.node.close();
			_context.setParameter("recover", SpiderGlobalConfig.getValue(SpiderGlobalConfig.SPIDER_BDB_CHECKPOINT_DIR));
			_context.setParameter("zk", zk);
			try {
				this.node = new MasterNode(_context);
				this.node.run();
			} catch (Exception e) {
				logger.error("Master Node Initialize Failed!",e);
			}
		} catch (KeeperException e) {
			if(e instanceof NodeExistsException){
				logger.info("Master Node has been elected already.");
				try {
					Thread.sleep(30000);//30seconds after,reload masternode data
					updateMasterNodeData();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void updateMasterNodeData() throws Exception{
		try {
			byte[] masterDataBytes = zk.getData(SpiderGlobalConfig.getValue("zookeeper.spider.master.path"), true, null);
			String jsonString = new String(masterDataBytes,"utf-8");
			JSONObject obj = JSONObject.parseObject(jsonString);
			String jetty_server_address = obj.getString("jetty_server_address");
			String file_server_address = obj.getString("file_server_address");
			String fileServerHost = file_server_address.split(":")[0];
			int fileServerPort = Integer.parseInt(file_server_address.split(":")[1]);
			String heartBeatUrl = jetty_server_address+SpiderGlobalConfig.getValue("spider.heartbeat.service.url");
			try {
				RPCProtocolFactory.create(HeartBeatProtocol.class, heartBeatUrl);
			} catch (MalformedURLException e) {
				logger.error("Boot:heartBeatProtocol initiallization failed!",e);
				throw e;
			}
			String ipServiceUrl = jetty_server_address+SpiderGlobalConfig.getValue("spider.ip.service.url");
			try{
				RPCProtocolFactory.create(IpProtocol.class, ipServiceUrl);
			} catch (MalformedURLException e) {
				logger.error("Boot:ipProtocol initiallization failed!",e);
				throw e;
			}
			String accountServiceUrl = jetty_server_address+SpiderGlobalConfig.getValue("spider.account.service.url");
			try{
				RPCProtocolFactory.create(AccountProtocol.class, accountServiceUrl);
			} catch (MalformedURLException e) {
				logger.error("Boot:accountProtocol initiallization failed!",e);
				throw e;
			}
			String verifyimgServiceUrl = jetty_server_address+SpiderGlobalConfig.getValue("spider.verifyimg.service.url");
			try{
				RPCProtocolFactory.create(VerifyImgProtocol.class, verifyimgServiceUrl);
			} catch (MalformedURLException e) {
				logger.error("Boot:accountProtocol initiallization failed!",e);
				throw e;
			}
			_context.setParameter("master.fileserver.host", fileServerHost);
			_context.setParameter("master.fileserver.port", fileServerPort);
		} catch (KeeperException e) {
			logger.error("Boot:get data from master zknode failed!",e);
			throw e;
		} catch (InterruptedException e) {
			logger.error("Boot:get data from master zknode failed!",e);
			throw e;
		}
	}
	
	/**专为规则配置正确性检测用，跑临时spider
	 * @param biz
	 * @param antiMonitorPolicy
	 * @param fieldRules
	 * @throws UnknownHostException 
	 */
//	public Map<String,Object> runTmpSpider(SpiderBiz biz,List<SpiderFieldRule> fieldRules,String...urls){
//		final Map<String,Object> resultMap = new HashMap<String,Object>();
//		try {
//			Spider spider = fixSpider(biz,fieldRules);
//			spider.setExitWhenComplete(true)
//			  .thread(1)
//			  .addUrl(urls)
//			  .addPipeline(new Pipeline() {
//				  @Override
//				  public void process(ResultItems resultItems, Task task) {
//					  resultMap.putAll(resultItems.getAll());
//				  }
//			  });
//		spider.run();
//		return resultMap;
//		} catch (Exception e) {
//			return null;
//		}
//		
//	}
	
	private String generateZKNodeData(){
		ZKNodeData zkNodeData = new ZKNodeData();
		zkNodeData.setFile_server_address(_context.getBindIp()+":"+_context.getFileServerPort());
		zkNodeData.setJetty_server_address("http://"+_context.getBindIp()+":"+_context.getJettyPort());
		zkNodeData.setMachineId(SpiderGlobalConfig.getValue(SpiderGlobalConfig.MACHINE_ID));
		zkNodeData.setLevel(_context.getNodeLevel());
		return JSON.toJSONString(zkNodeData);
	}
	
	class MyWatcher implements Watcher{
		@Override
		public void process(WatchedEvent event) {
			//如果检测到会话超时
			if(event.getState() == KeeperState.Expired){
				logger.info("zk session expired");
				if(_context.getNodeLevel()==SpiderNode.Level.follower){
					if(zk!=null){
						try {
							zk.close();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					try {
						zk = new ZooKeeper(SpiderGlobalConfig.getValue("zookeeper.cluster.server"),30000,new MyWatcher());
						updateMasterNodeData();
						String zkNodeData = generateZKNodeData();
						if(zk.exists(SpiderGlobalConfig.getValue("zookeeper.spider.slaves.path"), true)==null){
							zk.create(SpiderGlobalConfig.getValue("zookeeper.spider.slaves.path"), null, Ids.OPEN_ACL_UNSAFE , CreateMode.PERSISTENT);
						}
						if(zk.exists(znode_path, false)==null){
							znode_path = zk.create(SpiderGlobalConfig.getValue("zookeeper.spider.slaves.children.path"), zkNodeData.getBytes(), Ids.OPEN_ACL_UNSAFE , CreateMode.EPHEMERAL_SEQUENTIAL);
						}
						//TODO:重新注册监听masternode
						zk.getData(SpiderGlobalConfig.getValue("zookeeper.spider.master.path"), true, null);
						zk.exists(SpiderGlobalConfig.getValue("zookeeper.spider.master.path"), true);
						logger.info("reconnect to zk server success.");
					} catch (IOException e) {
						logger.error("reconnect to zk server failed.",e);
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
						logger.error("reconnect to zk server failed.",e);
					}
				}else if(_context.getNodeLevel()==SpiderNode.Level.master){
					logger.info("zk session expired");
					//如果元master节点是因为过期而导致节点删除，具有优先权
					if(zk!=null){
						try {
							zk.close();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					logger.info("Boot:master node detected be deleted on zk for session expired, start to rebuilt master node on zk...");
					try {
						zk = new ZooKeeper(SpiderGlobalConfig.getValue("zookeeper.cluster.server"),30000,new MyWatcher());
						String zkNodeData = generateZKNodeData();
						if(zk.exists(SpiderGlobalConfig.getValue("zookeeper.spider.master.path"), true)==null){
							znode_path = zk.create(SpiderGlobalConfig.getValue("zookeeper.spider.master.path"), zkNodeData.getBytes(), Ids.OPEN_ACL_UNSAFE , CreateMode.EPHEMERAL);
							_context.setParameter("zk", zk);
							logger.info("Boot:master node rebuilt successfully on zookeeper cluster.");
						}else{
							zk.setData(SpiderGlobalConfig.getValue("zookeeper.spider.master.path"), zkNodeData.getBytes(),-1);
						}
						//TODO重新注册watcher
						MasterNode mnode = (MasterNode)node;
						mnode.setZk(zk);
						mnode.listenSlaveNodesStatus();
						logger.info("reconnect to zk server success.");
					} catch (KeeperException | InterruptedException | IOException e) {
						e.printStackTrace();
						logger.error("reconnect to zk server failed.",e);
					} 
				}
			}
			
			//如果发生master节点下的节点删除事件，则选出新的master节点
			if(event.getType() == EventType.NodeDeleted && SpiderGlobalConfig.getValue("zookeeper.spider.master.path").equals(event.getPath())){
				if(_context.getNodeLevel()==SpiderNode.Level.follower){
					if(event.getState() == KeeperState.SyncConnected){
						logger.info("notice the master node failed, start master node election");
						//wait for 10seconds, if still cannot find masternode on zk,then start master election
						try {
							Thread.sleep(30000);
							startToElectionContest();
						} catch (Exception e) {
							logger.error("master node election failed!",e);
						} 
					}
				}
			}
			//如果发生master节点下的节点数据更改事件，则获取最新的节点数据
			if(event.getType() == EventType.NodeDataChanged && SpiderGlobalConfig.getValue("zookeeper.spider.master.path").equals(event.getPath())){
				logger.info("notice the master node data changed,reset the master node data cache");
				try {
					updateMasterNodeData();
				} catch (Exception e) {
					
				}
			}
		}
	}
	
    public static void main(String[] args){
    	try{
    		CommandLineParser parser = new BasicParser();
        	Options options = new Options();
        	options.addOption("follower", "follower", false, "Print this usage information");
        	options.addOption("worker", "worker", false, "Print this usage information");
        	options.addOption("master", "master", false, "Print this usage information");
        	options.addOption("jsp", "jettyhttpport", true, "Print this usage information");
        	options.addOption("fsp", "fileserverport", true, "Print this usage information");
//        	options.addOption("task", "taskfiledir", true, "Print this usage information");
//        	options.addOption("unjar", "jarfiledir", true, "Print this usage information");
        	options.addOption("file","filepersistence",false,"Print this usage information");
        	options.addOption("mongo", "mongodb",false,"Print this usage information");
        	options.addOption("sql", "mysql", false, "Print this usage information");
        	options.addOption("recover","recover",true,"Print this usage information");
        	options.addOption("bindip","bindip",true,"Print this usage information");
        	options.addOption("clusterid","clusterid",true,"Print this usage information");
        	options.addOption("conf","config file",true,"Print this usage information");
        	CommandLine commandLine = parser.parse( options, args );
        	SpiderNode.Level level = null;
        	int jettyhttpport = 0;
        	int fileserverport = 0;
        	int persistenceMethod = 0;
        	String recoverDir = "";
        	String confFile = "";
//        	String tdir = "";
//        	String ldir = "";
//        	String jdir = "";
        	String bindip = "";
        	String clusterid = "";
        	if(commandLine.hasOption("follower")){
        		level = SpiderNode.Level.follower;
        	}else if(commandLine.hasOption("worker")){
        		level = SpiderNode.Level.worker;
        	}else if(commandLine.hasOption("master")){
        		level = SpiderNode.Level.master;
        	}
        	if(commandLine.hasOption("jsp")){
        		jettyhttpport = Integer.valueOf(commandLine.getOptionValue("jsp"));
        	}else{
        		if(level == SpiderNode.Level.follower){
        			System.out.println("Spider Error:the follower node may elected to be master node,you must assign the jetty server port!");
        			System.exit(-1);
            	}
        	}
        	if(commandLine.hasOption("bindip")){
        		bindip = String.valueOf(commandLine.getOptionValue("bindip"));
        	}else{
        		if(level == SpiderNode.Level.follower){
        			System.out.println("Spider Error:the follower node may elected to be master node,you must assign the bind ip!");
        			System.exit(-1);
            	}
        	}
        	if(commandLine.hasOption("clusterid")){
        		clusterid = String.valueOf(commandLine.getOptionValue("clusterid"));
        		SpiderGlobalConfig.putValue(SpiderGlobalConfig.MACHINE_ID, clusterid);
        	}else{
        		if(level == SpiderNode.Level.follower){
        			System.out.println("Spider Error:the follower node may elected to be master node,you must assign the clusterid!");
        			System.exit(-1);
            	}
        	}
        	if(commandLine.hasOption("fsp")){
        		fileserverport = Integer.valueOf(commandLine.getOptionValue("fsp"));
        	}else{
        		if(level == SpiderNode.Level.follower){
        			System.out.println("Spider Error:the follower node may elected to be master node,you must assign the file server port!");
        			System.exit(-1);
        		}
        	}
        	
        	if(commandLine.hasOption("conf")){
        		confFile = String.valueOf(commandLine.getOptionValue("conf"));
        		FileInputStream confis = new FileInputStream(confFile);
        		SpiderGlobalConfig.reLoad(confis);
        	}
        	//2015-04-22 将这两项交给外部配置文件spider_global_config.xml配置
//        	if(commandLine.hasOption("task")){
//        		tdir = String.valueOf(commandLine.getOptionValue("task"));
//        		SpiderGlobalConfig.putValue(SpiderGlobalConfig.SPIDER_TASKFILE_DIR, tdir);
//        	}
//        	if(commandLine.hasOption("unjar")){
//        		jdir = String.valueOf(commandLine.getOptionValue("unjar"));
//        		SpiderGlobalConfig.putValue(SpiderGlobalConfig.SPIDER_UNJAR_DIR, jdir);
//        	}
        	if(commandLine.hasOption("file")){
        		persistenceMethod = SpiderContext.FILE_PERSISTENCE;
        	}else if(commandLine.hasOption("mongo")){
        		persistenceMethod = SpiderContext.MONGODB_PERSISTENCE;
        	}else if(commandLine.hasOption("sql")){
        		persistenceMethod = SpiderContext.MYSQL_PERSISTENCE;
        	}
    		SpiderContext _context = SpiderContext.custom().setFileServerPort(fileserverport).setBindIp(bindip).setJettyPort(jettyhttpport).setPersistenceMethod(persistenceMethod).setLevel(level).build();
    		if(commandLine.hasOption("recover")){
    			recoverDir = commandLine.getOptionValue("recover");
    			_context.setParameter("recover", recoverDir);
    		}
    		SpiderProcess process = new SpiderProcess(_context);
    		process.run();
    	}catch(Exception e){
    		System.out.println(e.getMessage());
    		System.exit(-1);
    	}
    	
	}
}
