package com.lmdna.spider.downloader;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AccountProxy implements Delayed{
	
	private String account;
	private String password;
	
	private Long canReuseTime = 0L;
	private int reuseTimeInterval = 1000;
	
	private long lastBorrowTime = System.currentTimeMillis();
	private long lastReqTimeStamp = 0L;
	private long lastLoginTimeStamp = 0L;
	
	private int maxVisitTimesPerHour = 0;
	private int loginIntervalStart = 0;
	private int loginIntervalEnd = 0;
	
	private AtomicInteger failedNum = new AtomicInteger(0);
	private AtomicInteger successNum = new AtomicInteger(0);
	private AtomicInteger borrowNum = new AtomicInteger(0);
	
	public AccountProxy(String account,String password,int reuseTimeInterval,int maxVisitTimesPerHour){
		this.account = account;
		this.password = password;
		this.reuseTimeInterval = reuseTimeInterval;
		this.maxVisitTimesPerHour = maxVisitTimesPerHour;
		this.canReuseTime = System.nanoTime() + TimeUnit.NANOSECONDS.convert(reuseTimeInterval, TimeUnit.MILLISECONDS);
	}
	
	public AccountProxy(String account,String password){
		this.account = account;
		this.password = password;
		this.canReuseTime = System.nanoTime() + TimeUnit.NANOSECONDS.convert(reuseTimeInterval, TimeUnit.MILLISECONDS);
	}
	
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Long getCanReuseTime() {
		return canReuseTime;
	}
	public void setCanReuseTime(Long canReuseTime) {
		this.canReuseTime = canReuseTime;
	}
	
	public long getLastReqTimeStamp() {
		return lastReqTimeStamp;
	}

	public void setLastReqTimeStamp(long lastReqTimeStamp) {
		this.lastReqTimeStamp = lastReqTimeStamp;
	}

	public long getLastLoginTimeStamp() {
		return lastLoginTimeStamp;
	}

	public void setLastLoginTimeStamp(long lastLoginTimeStamp) {
		this.lastLoginTimeStamp = lastLoginTimeStamp;
	}

	public int getMaxVisitTimesPerHour() {
		return maxVisitTimesPerHour;
	}

	public void setMaxVisitTimesPerHour(int maxVisitTimesPerHour) {
		this.maxVisitTimesPerHour = maxVisitTimesPerHour;
	}

	public int getLoginIntervalStart() {
		return loginIntervalStart;
	}

	public void setLoginIntervalStart(int loginIntervalStart) {
		this.loginIntervalStart = loginIntervalStart;
	}

	public int getLoginIntervalEnd() {
		return loginIntervalEnd;
	}

	public void setLoginIntervalEnd(int loginIntervalEnd) {
		this.loginIntervalEnd = loginIntervalEnd;
	}
	
	public void resetFailedNum(){
		failedNum = new AtomicInteger(0);
	}
	
	public void resetSuccessNum(){
		successNum = new AtomicInteger(0);
	}
	
	public void resetBorrowNum(){
		borrowNum = new AtomicInteger(0);
	}
	
	public void successNumIncrement(int increment) {
		this.successNum.addAndGet(increment);
	}
	
	public void borrow() {
		this.borrowNum.addAndGet(1);
	}

	public int getFailedNum() {
		return failedNum.get();
	}
	
	public int getSuccessNum() {
		return successNum.get();
	}
	
	public int getBorrowNum() {
		return borrowNum.get();
	}
	
	public Long getLastBorrowTime() {
		return lastBorrowTime;
	}
	public void setLastBorrowTime(Long lastBorrowTime) {
		this.lastBorrowTime = lastBorrowTime;
	}
	public void setReuseTimeInterval(int reuseTimeInterval) {
		this.reuseTimeInterval = reuseTimeInterval;
		this.canReuseTime = System.nanoTime() + TimeUnit.NANOSECONDS.convert(reuseTimeInterval, TimeUnit.MILLISECONDS);
	}

	@Override
	public int compareTo(Delayed o) {
		return 0;
	}

	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(canReuseTime - System.nanoTime(), TimeUnit.NANOSECONDS);
	}
}
