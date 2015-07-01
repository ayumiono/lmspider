package com.lmdna.spider.node;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import us.codecraft.webmagic.utils.LoggerUtil;

import com.lmdna.spider.node.SpiderNode.Level;
import com.lmdna.spider.utils.SpiderGlobalConfig;

/**
 * SpiderNode上下文
 * @author ayumiono
 */
public class SpiderContext {
	
	private static final Logger logger = LoggerFactory.getLogger(SpiderContext.class);
	
	public static final String JETTY_PORT = "jetty.port";
	public static final String FILE_SERVER_PORT = "file.server.port";
	public static final String LOG_DIR = "log.dir";
	public static final String PERSISTENCE_METHOD = "persistence.method";
	public static final String JDBCTEMPLATE = "jdbctemplate";
	public static final String TRANSACTION_MANAGER = "transactionmanager";
	public static final String DATASOURCE = "datasource";
	public static final String BIND_IP = "bindip";
	public static final String NODE_LEVEL = "nodelevel"; 
	
	public static final int MYSQL_PERSISTENCE = 0;
	public static final int MONGODB_PERSISTENCE = 1;
	public static final int FILE_PERSISTENCE = 2;
	
	private Map<String,Object> map;
	
	SpiderContext(){
		this.map = new HashMap<String,Object>();
	}
	
	public void setParameter(String key,Object value){
		map.put(key, value);
	}
	
	public <T> T getParameter(String key){
		Object t = map.get(key);
		if(t == null){
			return null;
		}
		return (T) map.get(key);
	}
	
	public JdbcTemplate getJdbcTemplate(){
		return getParameter(JDBCTEMPLATE);
	}
	
	public PlatformTransactionManager getTransactionManager(){
		return getParameter(TRANSACTION_MANAGER);
	}
	
	public DataSource getDataSource(){
		return getParameter(DATASOURCE);
	}
	
	public int getJettyPort(){
		return getParameter(JETTY_PORT);
	}
	
	public String getBindIp(){
		return getParameter(BIND_IP);
	}
	
	public int getFileServerPort(){
		return getParameter(FILE_SERVER_PORT);
	}
	
	public int getPersistenceMethod(){
		return getParameter(PERSISTENCE_METHOD);
	}
	
	public Level getNodeLevel(){
		return getParameter(NODE_LEVEL);
	}
	
	public static SpiderContext.SpiderContextBuilder custom(){
		return new SpiderContextBuilder();
	}
	
	public static class SpiderContextBuilder{
		private int jettyPort;
		private int fileServerPort;
		private String logDir;
		private int persistenceMethod;
		private String bindIp;
		private Level level;
		
		public SpiderContextBuilder setJettyPort(int port){
			this.jettyPort = port;
			return this;
		}
		
		public SpiderContextBuilder setFileServerPort(int port){
			this.fileServerPort = port;
			return this;
		}
		
		public SpiderContextBuilder setLogDir(String dir){
			this.logDir = dir;
			return this;
		}
		
		public SpiderContextBuilder setPersistenceMethod(int persistenceMethod){
			this.persistenceMethod = persistenceMethod;
			return this;
		}
		
		public SpiderContextBuilder setBindIp(String bindIp){
			this.bindIp = bindIp;
			return this;
		}
		
		public SpiderContextBuilder setLevel(Level level){
			this.level = level;
			return this;
		}
		
		public SpiderContext build()throws Exception{
			SpiderContext _context = new SpiderContext();
			_context.setParameter(SpiderContext.JETTY_PORT, jettyPort);
			_context.setParameter(SpiderContext.FILE_SERVER_PORT, fileServerPort);
			_context.setParameter(SpiderContext.LOG_DIR, logDir);
			_context.setParameter(SpiderContext.PERSISTENCE_METHOD, persistenceMethod);
			_context.setParameter(SpiderContext.BIND_IP, bindIp);
			_context.setParameter(SpiderContext.NODE_LEVEL, level);
			try{
				logger.info("SpiderContext: initialize jdbc...");
				JdbcTemplate jdbcTemplate = new JdbcTemplate();
				BasicDataSource dataSource = new BasicDataSource();
				dataSource.setDriverClassName(SpiderGlobalConfig.getValue("jdbc.driverClassName"));
				dataSource.setUrl(SpiderGlobalConfig.getValue("jdbc.url"));
				dataSource.setUsername(SpiderGlobalConfig.getValue("jdbc.username"));
				dataSource.setPassword(SpiderGlobalConfig.getValue("jdbc.password"));
				dataSource.setMaxActive(100);
				dataSource.setMaxIdle(30);
				dataSource.setMaxWait(5000);
				dataSource.setTestOnBorrow(true);
				dataSource.setTestWhileIdle(true);
				dataSource.setValidationQuery("select 1 from dual");
				_context.setParameter(SpiderContext.DATASOURCE, dataSource);
				jdbcTemplate.setDataSource(dataSource);
				_context.setParameter(SpiderContext.JDBCTEMPLATE, jdbcTemplate);
				DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
				_context.setParameter(SpiderContext.TRANSACTION_MANAGER, transactionManager);
				logger.info("SpiderContext: jdbc initialize finished.");
			}catch(Exception e){
				logger.error("SpiderContext: jdbc initialize failed.",e);
				throw new Exception("SpiderContext: jdbc initialize failed.",e);
			}
			return _context;
		}
	}
	public static void main(String[] args){
		final BlockingQueue<Map<String,String>> recordCache = new LinkedBlockingQueue<Map<String,String>>();
		for(int i = 10001 ;i>=0;i--){
			Map<String,String> values=new HashMap<String,String>();
			values.put("fingerprint", "fingerprint"+i);
			values.put("url", "url"+i);
			values.put("model", "model"+i);
			values.put("tag", "tag"+i);
			recordCache.add(values);
		}
		final String sqlStr = "insert into lmdna_carbiz(fingerprint,url,model,tag) values(?,?,?,?)";
		JdbcTemplate jdbcTemplate = new JdbcTemplate();
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(SpiderGlobalConfig.getValue("jdbc.driverClassName"));
		dataSource.setUrl(SpiderGlobalConfig.getValue("jdbc.url"));
		dataSource.setUsername(SpiderGlobalConfig.getValue("jdbc.username"));
		dataSource.setPassword(SpiderGlobalConfig.getValue("jdbc.password"));
		dataSource.setMaxActive(100);
		dataSource.setMaxIdle(30);
		dataSource.setMaxWait(5000);
		dataSource.setTestOnBorrow(true);
		dataSource.setTestWhileIdle(true);
		dataSource.setValidationQuery("select 1 from dual");
		dataSource.setDefaultAutoCommit(false);
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		TransactionStatus status = transactionManager.getTransaction(def);
		jdbcTemplate.setDataSource(dataSource);
		final List<Map<String,String>> ds = new ArrayList<Map<String,String>>();
		ds.addAll(recordCache);
		try{
			jdbcTemplate.batchUpdate(sqlStr, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					ps.setString(1, ds.get(i).get("fingerprint"));
					ps.setString(2, ds.get(i).get("url"));
					ps.setString(3, ds.get(i).get("model"));
					ps.setString(4, ds.get(i).get("tag"));
				}
				@Override
				public int getBatchSize() {
					return ds.size();
				}
			});
			transactionManager.commit(status);
		}catch(Exception e){
			LoggerUtil.error("批量插入出错",e);
		}
	}
}
