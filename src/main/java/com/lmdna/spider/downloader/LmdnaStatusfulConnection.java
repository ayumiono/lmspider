package com.lmdna.spider.downloader;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;

import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.statusful.StatusfulConnection;

import com.lmdna.spider.utils.HttpClientHelper;

public class LmdnaStatusfulConnection extends StatusfulConnection{
	
	private AccountProxy account;
	
	private HttpClientHelper httphelper;
	
	public LmdnaStatusfulConnection(AccountProxy account){
		this.setAccount(account);
	}
	
	public LmdnaStatusfulConnection(){
		
	}
	
	/* 
	 * 连接使用的优先级，参考顺序：账号可用时间》代理IP可用使用间隔
	 */
	@Override
	public int compareTo(Delayed o) {
		LmdnaStatusfulConnection that = (LmdnaStatusfulConnection) o;
		if(account.getCanReuseTime()==that.account.getCanReuseTime()){
			return 0;
		}else if(account.getCanReuseTime()>that.account.getCanReuseTime()){
			return 1;
		}else{
			return -1;
		}
	}

	@Override
	public long getDelay(TimeUnit unit) {
		return account.getDelay(unit);
	}

	public HttpClientHelper getHttphelper() {
		return httphelper;
	}

	public void setHttphelper(HttpClientHelper httphelper) {
		this.httphelper = httphelper;
	}

	public AccountProxy getAccount() {
		return account;
	}

	public void setAccount(AccountProxy account) {
		this.account = account;
	}

	/**
	 * 账号，代理IP一小时内访问总量限制
	 * @return
	 */
	public boolean isValid(){
		if(account.getBorrowNum()<account.getMaxVisitTimesPerHour()){
			return true;
		}else{
			return false;
		}
	}
	
	public HttpResponse doReq(String url,Map<String,String> headers,Proxy proxy) throws IOException{
		if(proxy!=null){
			return this.httphelper.doReq(url, "get", null, headers,proxy.getHttpHost());
		}else{
			return this.httphelper.doReq(url, "get", null, headers,null);
		}
		
	}
}
