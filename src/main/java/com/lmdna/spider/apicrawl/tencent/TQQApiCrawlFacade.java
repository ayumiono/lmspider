package com.lmdna.spider.apicrawl.tencent;


import com.lmdna.spider.apicrawl.ApiOauthPool;

public class TQQApiCrawlFacade {
	
	private ApiOauthPool<TQQApiProxy> pool;
	public static TQQApiCrawlFacade instance;
	
	public TQQApiCrawlFacade getInstance(){
		return instance;
	}
	
	public String otherInfo(){
		return null;
	}
	
	public String info(){
		return null;
	}
	
	public String infos(){
		return null;
	}
}
