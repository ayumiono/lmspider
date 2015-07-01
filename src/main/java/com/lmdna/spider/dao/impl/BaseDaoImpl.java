package com.lmdna.spider.dao.impl;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import com.lmdna.spider.exception.BizException;

public class BaseDaoImpl {
	
	private SqlMapClient sqlMapClient;
	
	public BaseDaoImpl(){
		this.sqlMapClient = IbatisSQLMapConfig.getSqlMapClient();
	}
	
	public SqlMapClient getSqlMapClient(){
		return this.sqlMapClient;
	}
	
	private static class IbatisSQLMapConfig{
		private static SqlMapClient sqlMapClient = null;
		static {
			try {
				Reader reader = Resources.getResourceAsReader("SqlMapConfig.xml");
				sqlMapClient = SqlMapClientBuilder.buildSqlMapClient(reader);
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("Error initializing MyAppSqlConfig class. Cause: " + e);
			}
		}
		public static SqlMapClient getSqlMapClient(){
			return sqlMapClient;
		}
	}
	
	public static void main(String[] args){
		SpiderProxyIpStatusDaoImpl test = new SpiderProxyIpStatusDaoImpl();
		Map<String,Object> parammap = new HashMap<String,Object>();
		parammap.put("id", 1);
		parammap.put("failednum", 10);
		parammap.put("successnum", 0);
		parammap.put("borrownum", 0);
		parammap.put("deadnum", 0);
		parammap.put("reuseinterval", 1500);
		try {
			test.updateProxyIpStatus(parammap);
		} catch (BizException e) {
			e.printStackTrace();
		}
	}
}
