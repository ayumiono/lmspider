package com.lmdna.spider.protocol.rpc.utils;

import java.io.Serializable;

public class SpiderStatusSerialization implements Serializable{
	private static final long serialVersionUID = 1L;
	private String name;
	private String status;
	private int thread;
	private long totalPageCount;
	private long leftPageCount;
	private long successPageCount;
	private long matchSuccessPageCount;
	private long errorPageCount;
	private int pagePerSecond;
	private int proxyPoolSize;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getThread() {
		return thread;
	}
	public void setThread(int thread) {
		this.thread = thread;
	}
	public long getTotalPageCount() {
		return totalPageCount;
	}
	public void setTotalPageCount(long totalPageCount) {
		this.totalPageCount = totalPageCount;
	}
	public long getLeftPageCount() {
		return leftPageCount;
	}
	public void setLeftPageCount(long leftPageCount) {
		this.leftPageCount = leftPageCount;
	}
	public long getSuccessPageCount() {
		return successPageCount;
	}
	public void setSuccessPageCount(long successPageCount) {
		this.successPageCount = successPageCount;
	}
	public long getMatchSuccessPageCount() {
		return matchSuccessPageCount;
	}
	public void setMatchSuccessPageCount(long matchSuccessPageCount) {
		this.matchSuccessPageCount = matchSuccessPageCount;
	}
	public long getErrorPageCount() {
		return errorPageCount;
	}
	public void setErrorPageCount(long errorPageCount) {
		this.errorPageCount = errorPageCount;
	}
	public int getPagePerSecond() {
		return pagePerSecond;
	}
	public void setPagePerSecond(int pagePerSecond) {
		this.pagePerSecond = pagePerSecond;
	}
	public int getProxyPoolSize() {
		return proxyPoolSize;
	}
	public void setProxyPoolSize(int proxyPoolSize) {
		this.proxyPoolSize = proxyPoolSize;
	}
	public String toString(){
		return String.format("name:%20s >> status:%s >> thread:%3d >> totalpagecount:%8d >> leftpagecount:%8d >> successpagecount:%8d >> matchsuccesspageCount:%8d >> errorpagecount:%8d >> pagepersecond:%3d >> proxypoolsize:%4d", 
				this.name,this.status,this.thread,this.totalPageCount,this.leftPageCount,this.successPageCount,this.matchSuccessPageCount,this.errorPageCount,this.pagePerSecond,this.proxyPoolSize);
	}
	public static void main(String[] args){
		SpiderStatusSerialization test = new SpiderStatusSerialization();
		test.setName("estest");
		test.setStatus("run");
		test.setThread(120);
		test.setTotalPageCount(19888880L);
		test.setLeftPageCount(2000000L);
		test.setSuccessPageCount(2000000L);
		test.setMatchSuccessPageCount(1999999L);
		test.setErrorPageCount(12121212L);
		test.setPagePerSecond(10);
		test.setProxyPoolSize(50);
		System.out.println(test.toString());
		SpiderStatusSerialization test2 = new SpiderStatusSerialization();
		test2.setName("estest12121212123");
		test2.setStatus("run");
		test2.setThread(120);
		test2.setTotalPageCount(19880L);
		test2.setLeftPageCount(2000L);
		test2.setSuccessPageCount(2000L);
		test2.setMatchSuccessPageCount(199L);
		test2.setErrorPageCount(121212L);
		test2.setPagePerSecond(10);
		test2.setProxyPoolSize(50);
		System.out.println(test2.toString());
	}
}
