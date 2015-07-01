package com.lmdna.spider.apicrawl;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import com.lmdna.spider.downloader.AccountProxy;
import com.lmdna.spider.proxy.LmdnaProxy;

public class ApiProxy implements Delayed{
	
	private AccountProxy account;
	private LmdnaProxy ip;
	
	public ApiProxy(AccountProxy account,LmdnaProxy ip){
		this.account=account;
		this.ip=ip;
	}
	
	public boolean isValid(){
		if(account.getBorrowNum()<account.getMaxVisitTimesPerHour() 
				&& ip.getBorrowNum()<ip.getMaxVisitTimesPerHour()){
			return true;
		}else{
			return false;
		}
	}

	@Override
	public int compareTo(Delayed o) {
		ApiProxy that = (ApiProxy) o;
		if(account.getCanReuseTime()==that.account.getCanReuseTime()){
			if(ip.getCanReuseTime()==that.ip.getCanReuseTime()){
				return 0;
			}else if(ip.getCanReuseTime()>that.ip.getCanReuseTime()){
				return 1;
			}else{
				return -1;
			}
		}else if(account.getCanReuseTime()>that.account.getCanReuseTime()){
			return 1;
		}else{
			return -1;
		}
	}

	@Override
	public long getDelay(TimeUnit unit) {
		if(account.getDelay(unit)>ip.getDelay(unit)){
			return account.getDelay(unit);
		}else{
			return ip.getDelay(unit);
		}
	}

	public AccountProxy getAccount() {
		return account;
	}

	public void setAccount(AccountProxy account) {
		this.account = account;
	}

	public LmdnaProxy getIp() {
		return ip;
	}

	public void setIp(LmdnaProxy ip) {
		this.ip = ip;
	}

}
