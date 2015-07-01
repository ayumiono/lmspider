package com.lmdna.spider.apicrawl.tencent;

import com.lmdna.spider.apicrawl.ApiProxy;
import com.lmdna.spider.downloader.AccountProxy;
import com.lmdna.spider.proxy.LmdnaProxy;

public class TQQApiProxy extends ApiProxy {
	
	private TQQApi api;
	
	public TQQApiProxy(AccountProxy account, LmdnaProxy ip) {
		super(account, ip);
	}

	public String info(){
		return null;
	}	
}
