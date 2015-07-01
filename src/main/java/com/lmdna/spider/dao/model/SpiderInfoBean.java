package com.lmdna.spider.dao.model;

import java.io.Serializable;

public class SpiderInfoBean implements Serializable{
	
	private static final long serialVersionUID = -241799481071173052L;
	
	private String bizCode;
	private String bizName;
	private Long requestCount;
	private Integer proxyPoolSize;
	private Integer activeThreadSize;
	private String status;
	private Double downloadRate;
	private Double pageProcessRate;
	private Integer errorCount;
	private Integer successCount;
	private Integer matchSuccessCount;
	public String getBizCode() {
		return bizCode;
	}
	public void setBizCode(String bizCode) {
		this.bizCode = bizCode;
	}
	public String getBizName() {
		return bizName;
	}
	public void setBizName(String bizName) {
		this.bizName = bizName;
	}
	public Long getRequestCount() {
		return requestCount;
	}
	public void setRequestCount(Long requestCount) {
		this.requestCount = requestCount;
	}
	public Integer getProxyPoolSize() {
		return proxyPoolSize;
	}
	public void setProxyPoolSize(Integer proxyPoolSize) {
		this.proxyPoolSize = proxyPoolSize;
	}
	public Integer getActiveThreadSize() {
		return activeThreadSize;
	}
	public void setActiveThreadSize(Integer activeThreadSize) {
		this.activeThreadSize = activeThreadSize;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Double getDownloadRate() {
		return downloadRate;
	}
	public void setDownloadRate(Double downloadRate) {
		this.downloadRate = downloadRate;
	}
	public Double getPageProcessRate() {
		return pageProcessRate;
	}
	public void setPageProcessRate(Double pageProcessRate) {
		this.pageProcessRate = pageProcessRate;
	}
	public Integer getErrorCount() {
		return errorCount;
	}
	public void setErrorCount(Integer errorCount) {
		this.errorCount = errorCount;
	}
	public Integer getSuccessCount() {
		return successCount;
	}
	public void setSuccessCount(Integer successCount) {
		this.successCount = successCount;
	}
	public Integer getMatchSuccessCount() {
		return matchSuccessCount;
	}
	public void setMatchSuccessCount(Integer matchSuccessCount) {
		this.matchSuccessCount = matchSuccessCount;
	}
}
