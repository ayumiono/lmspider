package com.lmdna.spider.downloader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import us.codecraft.webmagic.Site;

import com.lmdna.spider.dao.model.SpiderBiz;

/**
 * 有状态抓取连接池管理类
 * @author ayumi
 *
 */
public class LmdnaStatusfulConnectionPoolManager {
	
	private volatile static LmdnaStatusfulConnectionPoolManager instance = null; 
	
	private final String logName = "ConnectionPoolManager";
	
	private Map<String,LmdnaStatusfulConnectionPool> pools = new ConcurrentHashMap<String,LmdnaStatusfulConnectionPool>();
	
	public LmdnaStatusfulConnectionPool getPool(SpiderBiz biz){
		String poolId = biz.getWebsiteConfigBO().getWebsiteBO().getSiteEnName();
		return pools.get(poolId);
	}
	
	private LmdnaStatusfulConnectionPoolManager(){
	}
	
	public static LmdnaStatusfulConnectionPoolManager instance(){
		if(instance == null){
			synchronized (LmdnaStatusfulConnectionPoolManager.class) {
				if(instance == null){
					instance = new LmdnaStatusfulConnectionPoolManager();
				}
			}
		}
		return instance;
	}
	
	public LmdnaStatusfulConnectionPool createPool(SpiderBiz biz,Site site){
		LmdnaStatusfulConnectionPool pool = new LmdnaStatusfulConnectionPool(biz,site);
		pools.put(biz.getWebsiteConfigBO().getWebsiteBO().getSiteEnName(), pool);
		return pool;
	}
}
