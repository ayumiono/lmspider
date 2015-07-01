package com.lmdna.spider.proxy;

import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;

import us.codecraft.webmagic.proxy.Proxy;


public class LmdnaProxy extends Proxy {

	private static final long serialVersionUID = -4970183653493180323L;

	private final HttpHost httpHost;
	
//	private final int id;//对应于spider_proxyip_status表中的唯一id
	
	private final int proxyipid;//关联spider_proxyip表中的id
	
	private Long maxVisitTimesPerHour = 0L;
	
	public Long getMaxVisitTimesPerHour() {
		return maxVisitTimesPerHour;
	}

	public void setMaxVisitTimesPerHour(Long maxVisitTimesPerHour) {
		this.maxVisitTimesPerHour = maxVisitTimesPerHour;
	}

	private int deadNum;//失效次数

	public LmdnaProxy(HttpHost httpHost,int proxyipid) {
		this.httpHost = httpHost;
//		this.id = id;
		this.proxyipid = proxyipid;
		this.canReuseTime = System.nanoTime() + TimeUnit.NANOSECONDS.convert(reuseTimeInterval, TimeUnit.MILLISECONDS);
	}

	public LmdnaProxy(HttpHost httpHost, int proxyipid, int reuseInterval) {
		this.httpHost = httpHost;
//		this.id = id;
		this.proxyipid = proxyipid;
		this.reuseTimeInterval = reuseInterval;
		this.canReuseTime = System.nanoTime() + TimeUnit.NANOSECONDS.convert(reuseInterval, TimeUnit.MILLISECONDS);
	}
	
	public void resetDeadNum(){
		deadNum = 0;
	}
	
	public int getDeadNum() {
		return deadNum;
	}

	public void dead(){
		this.deadNum+=1;
	}

//	public int getId() {
//		return id;
//	}
	
	public int getProxyIpId(){
		return proxyipid;
	}

	@Override
	public String toString() {

		String re = String.format(
				"host: %21s >> "
				+ "speed: %5dms >> "
				+ "successrate: %-3.2f%% >> "
				+ "success: %4d >> "
				+ "borrow: %4d >> "
				+ "reusetimeinterval:%5d >> "
				+ "failtimes:%2d >> "
				+ "deadtimes:%2d", 
				httpHost.getHostName()+":"+httpHost.getPort(), //ip：port
				responseTime,//速度
				borrowNum == 0 ? 0 : successNum * 100.0 / borrowNum, //成功率
				successNum,
				borrowNum,//使用次数
				reuseTimeInterval,//使用间隔
				failedNum,//连续失败次数
				deadNum);//失效次数
		return re;

	}
}
