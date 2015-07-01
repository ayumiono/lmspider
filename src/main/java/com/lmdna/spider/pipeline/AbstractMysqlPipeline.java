package com.lmdna.spider.pipeline;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sql.DataSource;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.utils.LoggerUtil;

import com.lmdna.spider.node.SpiderContext;
import com.lmdna.spider.protocol.SpiderContextAware;

/**
 * 使用mysql批量存储
 * @author ayumiono
 *
 */
public abstract class AbstractMysqlPipeline implements SpiderContextAware, BatchPipeline, ReferenceablePipeline {
	protected SpiderContext _context;
	protected final Vector<Map<String,String>> recordCache = new Vector<Map<String,String>>();
	private volatile AtomicBoolean isFlushing = new AtomicBoolean(false);
	@Override
	public void setSpiderContext(SpiderContext _context) {
		this._context = _context;
	}
	public JdbcTemplate getJdbcTemplate(){
		return _context.getJdbcTemplate();
	}
	public PlatformTransactionManager getTransactionManager(){
		return _context.getTransactionManager();
	}
	public DataSource getDataSource(){
		return _context.getDataSource();
	}
	public abstract Map<String,String> processSingleRecord(Page page, Task task);
	public abstract String getSQL();
	@Override
	public void process(Page page, Task task) {
		Map<String,String> values = processSingleRecord(page,task);
		if(values==null){
			return;
		}
		if(recordCache.size()>=10000){
			synchronized (recordCache) {
				//double checkcd 
				if(recordCache.size()<10000){
					recordCache.add(values);
					return;
				}
				isFlushing.set(true);
				TransactionStatus status = null;
				LoggerUtil.info("AbstractMysqlPipeline",task.getUUID()+">>>开始批量插入数据库...");
				long startTime = System.currentTimeMillis();
				try{
					DefaultTransactionDefinition def = new DefaultTransactionDefinition();
					status = getTransactionManager().getTransaction(def);
					getJdbcTemplate().batchUpdate(getSQL(), new BatchPreparedStatementSetter() {
						@Override
						public void setValues(PreparedStatement ps, int i) throws SQLException {
							Map<String,String> record  = recordCache.get(i);
							if(record!=null){
								ps.setString(1, record.get("fingerprint"));
								ps.setString(2, record.get("url"));
								ps.setString(3, record.get("model"));
								ps.setString(4, record.get("tag"));
							}
						}
						@Override
						public int getBatchSize() {
							return recordCache.size();
						}
					});
					getTransactionManager().commit(status);
					long endTime = System.currentTimeMillis();
					recordCache.clear();
					LoggerUtil.info("AbstractMysqlPipeline",task.getUUID()+">>>批量插入数据库结束，耗时{}分钟.",new Object[]{(endTime-startTime)/60000});
				}catch(Exception e){
					LoggerUtil.info("AbstractMysqlPipeline",task.getUUID()+">>>批量插入出错"+e.getMessage());
					if(status!=null){
						getTransactionManager().rollback(status);
					}
				}
				isFlushing.set(false);
			}
		}else{
			recordCache.add(values);
		}
	}
	
	@Override
	public void flush(Task task) {
		if(isFlushing.get()){
			LoggerUtil.info("AbstractMysqlPipeline",task.getUUID()+">>>正在批量插入数据库.");
			return;
		}
		synchronized (recordCache) {
			DefaultTransactionDefinition def = new DefaultTransactionDefinition();
			TransactionStatus status = getTransactionManager().getTransaction(def);
			LoggerUtil.info("AbstractMysqlPipeline",task.getUUID()+">>>开始批量插入数据库...");
			try{
				getJdbcTemplate().batchUpdate(getSQL(), new BatchPreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						Map<String,String> record  = recordCache.get(i);
						if(record!=null){
							ps.setString(1, record.get("fingerprint"));
							ps.setString(2, record.get("url"));
							ps.setString(3, record.get("model"));
							ps.setString(4, record.get("tag"));
						}
					}
					@Override
					public int getBatchSize() {
						return recordCache.size();
					}
				});
				getTransactionManager().commit(status);
				recordCache.clear();
			}catch(Exception e){
				LoggerUtil.info("AbstractMysqlPipeline",task.getUUID()+">>>批量插入出错");
				getTransactionManager().rollback(status);
			}
		}
	}
}
