package com.lmdna.spider.apicrawl.sina;

import java.util.concurrent.DelayQueue;

import weibo4j.model.User;
import weibo4j.model.WeiboException;


public class SinaWeiboApiCrawlFacade {
	
	private DelayQueue<SinaWeiboApiProxy> pool = new DelayQueue<SinaWeiboApiProxy>();

	private SinaWeiboApiProxy currentApiProxy;
	
	public User showUserById(String uid){
		try {
			return currentApiProxy.showUserById(uid);
		} catch (WeiboException e) {
			return null;
		}
	}
	
	public User showUserByScreenName(String screen_name){
		try {
			return currentApiProxy.showUserByScreenName(screen_name);
		} catch (WeiboException e) {
			return null;
		}
	}
	public User showUserByDomain(String domain){
		try {
			return currentApiProxy.showUserByDomain(domain);
		} catch (WeiboException e) {
			return null;
		}
	}
}
