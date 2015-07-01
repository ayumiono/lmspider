package com.lmdna.spider.apicrawl;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;

import com.lmdna.spider.SpiderDAOServiceFacade;
import com.lmdna.spider.dao.model.SpiderBiz;
import com.lmdna.spider.dao.model.SpiderWebsiteConfig;

public class ApiOauthPool<T extends ApiProxy> {
	
	private BlockingQueue<T> oauthQueue = new DelayQueue<T>();
	private SpiderBiz biz;
	private SpiderWebsiteConfig websiteConfig;
	private SpiderDAOServiceFacade facade;
	private String poolId;
	private Timer timer = new Timer(true);
	private TimerTask refreshExpireToken = new TimerTask() {
        @Override
        public void run() {
        	
        }
    };
	
	public ApiOauthPool(SpiderBiz biz){
		this.biz = biz;
		this.websiteConfig = biz.getWebsiteConfigBO();
		this.facade = SpiderDAOServiceFacade.getInstance();
		this.poolId = websiteConfig.getWebsiteBO().getSiteEnName();
		timer.schedule(refreshExpireToken, 0, 60*60*1000);
	}
	
	private void load(){
		
	}
}
