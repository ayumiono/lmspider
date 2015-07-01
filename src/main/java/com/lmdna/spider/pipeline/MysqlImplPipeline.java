package com.lmdna.spider.pipeline;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

public class MysqlImplPipeline implements Pipeline {
	
	//批量插入
	private static final Vector<List<Object>> recordCache = new Vector<List<Object>>();
	
	private static volatile String sqlStr = null;
	
	private static final Logger logger = LoggerFactory.getLogger(MysqlImplPipeline.class);
	
	private JdbcTemplate jdbcTemplate;
	
	private String tableName;
	
	public MysqlImplPipeline(JdbcTemplate jdbcTemplate,String tableName){
		this.jdbcTemplate = jdbcTemplate;
		this.tableName = tableName;
	}

	@Override
	public void process(Page page, Task task) {
		ResultItems resultItems = page.getResultItems();
		if(sqlStr == null){
			StringBuilder sql = new StringBuilder("insert into "+tableName);
			List<String> columns=new ArrayList<String>();
			List<Object> values=new ArrayList<Object>();
			for(Entry<String,Object> e:resultItems.getAll().entrySet()){
				columns.add(e.getKey());
				values.add(e.getValue());
			}
			sql.append("(");
			sql.append(StringUtils.join(columns,','));
			sql.append(") values(");
			String[] paras=new String[values.size()];
			Arrays.fill(paras, "?");
			sql.append(StringUtils.join(paras,','));
			sql.append(")");
			logger.info(sql.toString()+","+values);
			sqlStr = sql.toString();
		}
		
		List<Object> values=new ArrayList<Object>();
		for(Entry<String,Object> e:resultItems.getAll().entrySet()){
			values.add(e.getValue());
		}
		recordCache.add(values);
		if(recordCache.size()>=10000){
			long begin = System.currentTimeMillis();
			logger.info("recordCache中目前有[]条数据，开始批量插入数据库...",recordCache.size());
			try{
				jdbcTemplate.batchUpdate(sqlStr, new BatchPreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						for(List<Object> record : recordCache){
							for(Object fieldValue : record){
								if(fieldValue!=null){
									if(fieldValue instanceof java.lang.Integer){
										ps.setInt(i+1, (Integer) fieldValue);
									}else if(fieldValue instanceof java.lang.Long){
										ps.setLong(i+1, (Long) fieldValue);
									}else if(fieldValue instanceof java.util.Date){
										ps.setDate(i+1, new java.sql.Date(((Date)fieldValue).getTime()));
										ps.setTimestamp(i+1, new java.sql.Timestamp(((Date)fieldValue).getTime()));
									}else if(fieldValue instanceof java.lang.String){
										ps.setString(i+1, fieldValue.toString());
									}else if(fieldValue instanceof java.lang.Double){
										ps.setDouble(i+1, (Double) fieldValue);
									}else if(fieldValue instanceof java.lang.Byte){
										ps.setByte(i+1, (Byte) fieldValue);
									}else if(fieldValue instanceof java.lang.Character){
										ps.setString(i+1, fieldValue.toString());
									}else if(fieldValue instanceof java.lang.Float){
										ps.setFloat(i+1, (Float) fieldValue);
									}else if(fieldValue instanceof java.lang.Boolean){
										ps.setBoolean(i+1, (Boolean) fieldValue);
									}else if(fieldValue instanceof java.lang.Short){
										ps.setShort(i+1, (Short) fieldValue);
									}else{
										ps.setObject(i+1, fieldValue);
									}
								}else{
									ps.setNull(i+1, Types.NULL);
								}
							}
						}
					}
					@Override
					public int getBatchSize() {
						return recordCache.size();
					}
				});
			}catch(Exception e){
				logger.error("批量插入出错",e);
			}finally{
				recordCache.clear();
			}
			long end = System.currentTimeMillis();
			logger.debug("批量插入结束.耗时{} >>> ",end-begin);
		}
	}

}
