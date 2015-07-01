package com.lmdna.spider.dao.model;

import java.io.Serializable;
import java.util.Date;


public class SpiderWebsiteConfig implements Serializable{
	private static final long serialVersionUID = 4382947674369693180L;
	private Integer id; //主键
	private Integer website; //网站id
	private Integer needProxy;//是否需要使用代理0：需要，1：不需要
	private Integer ipReuseInterval; //代理IP使用间隔(毫秒)
	private Integer ipStatReportInterval; //代理IP提交报告周期(小时)
	private Integer ipReviveinTime;//失效IP复活时间
	private Integer failedTimes;//ip失效多少次后移除
	private Integer retryTimes;//抓取失败重试次数
	private Integer cycleRetryTimes;//抓取失败重试次数(biz),默认0为无限重试
	private Integer proxyIpCount;//代理IP数量的最低要求，低于该值通知
	private Integer proxyIpLoadCount;//每次加载多少个代理IP
	private Integer deadTimes;//代理IP失效次数
	private Integer sleepTime;//网页抓取间隔时间，如果使用了代理IP策略，最好将其设为0
	private Integer needLogin;//是否需要登入0：不需要1：需要
	private String loginClass;//指定登入类
	private Integer accountCount;//针对需要账号的业务，最低账号个数
	private Integer accountLoadCount;//每次加载账号的个数
	private Integer accountReuseInterval;
	private Integer maxVisitPerAccount;//同一账号访问总量限制
	private Integer maxVisitPerIp;//同一IP访问总量限制
	private Date createTime; //创建时间
	private Date updateTime; //修改时间
	
	private SpiderWebsite websiteBO;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getWebsite() {
		return website;
	}
	public void setWebsite(Integer website) {
		this.website = website;
	}
	public Integer getNeedProxy() {
		return needProxy;
	}
	public void setNeedProxy(Integer needProxy) {
		this.needProxy = needProxy;
	}
	public Integer getIpReuseInterval() {
		return ipReuseInterval;
	}
	public void setIpReuseInterval(Integer ipReuseInterval) {
		this.ipReuseInterval = ipReuseInterval;
	}
	public Integer getIpStatReportInterval() {
		return ipStatReportInterval;
	}
	public void setIpStatReportInterval(Integer ipStatReportInterval) {
		this.ipStatReportInterval = ipStatReportInterval;
	}
	public Integer getIpReviveinTime() {
		return ipReviveinTime;
	}
	public void setIpReviveinTime(Integer ipReviveinTime) {
		this.ipReviveinTime = ipReviveinTime;
	}
	public Integer getFailedTimes() {
		return failedTimes;
	}
	public void setFailedTimes(Integer failedTimes) {
		this.failedTimes = failedTimes;
	}
	public Integer getRetryTimes() {
		return retryTimes;
	}
	public void setRetryTimes(Integer retryTimes) {
		this.retryTimes = retryTimes;
	}
	public Integer getCycleRetryTimes() {
		return cycleRetryTimes;
	}
	public void setCycleRetryTimes(Integer cycleRetryTimes) {
		this.cycleRetryTimes = cycleRetryTimes;
	}
	public Integer getProxyIpCount() {
		return proxyIpCount;
	}
	public void setProxyIpCount(Integer proxyIpCount) {
		this.proxyIpCount = proxyIpCount;
	}
	public Integer getProxyIpLoadCount() {
		return proxyIpLoadCount;
	}
	public void setProxyIpLoadCount(Integer proxyIpLoadCount) {
		this.proxyIpLoadCount = proxyIpLoadCount;
	}
	public Integer getDeadTimes() {
		return deadTimes;
	}
	public void setDeadTimes(Integer deadTimes) {
		this.deadTimes = deadTimes;
	}
	public Integer getSleepTime() {
		return sleepTime;
	}
	public void setSleepTime(Integer sleepTime) {
		this.sleepTime = sleepTime;
	}
	public Integer getNeedLogin() {
		return needLogin;
	}
	public void setNeedLogin(Integer needLogin) {
		this.needLogin = needLogin;
	}
	public String getLoginClass() {
		return loginClass;
	}
	public void setLoginClass(String loginClass) {
		this.loginClass = loginClass;
	}
	public Integer getAccountCount() {
		return accountCount;
	}
	public void setAccountCount(Integer accountCount) {
		this.accountCount = accountCount;
	}
	public Integer getAccountLoadCount() {
		return accountLoadCount;
	}
	public void setAccountLoadCount(Integer accountLoadCount) {
		this.accountLoadCount = accountLoadCount;
	}
	public Integer getAccountReuseInterval() {
		return accountReuseInterval;
	}
	public void setAccountReuseInterval(Integer accountReuseInterval) {
		this.accountReuseInterval = accountReuseInterval;
	}
	public Integer getMaxVisitPerAccount() {
		return maxVisitPerAccount;
	}
	public void setMaxVisitPerAccount(Integer maxVisitPerAccount) {
		this.maxVisitPerAccount = maxVisitPerAccount;
	}
	public Integer getMaxVisitPerIp() {
		return maxVisitPerIp;
	}
	public void setMaxVisitPerIp(Integer maxVisitPerIp) {
		this.maxVisitPerIp = maxVisitPerIp;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	public SpiderWebsite getWebsiteBO() {
		return websiteBO;
	}
	public void setWebsiteBO(SpiderWebsite websiteBO) {
		this.websiteBO = websiteBO;
	}
	
}

