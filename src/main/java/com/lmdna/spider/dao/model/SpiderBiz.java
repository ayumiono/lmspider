package com.lmdna.spider.dao.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class SpiderBiz implements Serializable{
	private static final long serialVersionUID = -4808886606357607040L;
	private Integer id; //主键
	private String bizCode; //业务代号
	private String bizName; //业务名
	private String urlRule; //url匹配规则
	private String persistenceTable;//抓取结果存储表
	private Integer websiteConfig;//网页规则
	private Integer editFlag;//0:没有修改过 1：修改过
	private Integer status;//0:没有加载	1：正在运行	 2：被停止	 3：被移除 
	private Integer threadCount;//线程数
	private String responseValidCheck;//网页有效性验证字段
	private String taskProcessClass;
	private Date createTime; //创建时间
	private Date updateTime; //修改时间
	
	private SpiderWebsiteConfig websiteConfigBO;
	private List<SpiderFieldRule> fieldRules;
	
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
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
	public String getUrlRule() {
		return urlRule;
	}
	public void setUrlRule(String urlRule) {
		this.urlRule = urlRule;
	}
	public String getPersistenceTable() {
		return persistenceTable;
	}
	public void setPersistenceTable(String persistenceTable) {
		this.persistenceTable = persistenceTable;
	}
	public Integer getWebsiteConfig() {
		return websiteConfig;
	}
	public void setWebsiteConfig(Integer websiteConfig) {
		this.websiteConfig = websiteConfig;
	}
	public Integer getEditFlag() {
		return editFlag;
	}
	public void setEditFlag(Integer editFlag) {
		this.editFlag = editFlag;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Integer getThreadCount() {
		return threadCount;
	}
	public void setThreadCount(Integer threadCount) {
		this.threadCount = threadCount;
	}
	public String getResponseValidCheck() {
		return responseValidCheck;
	}
	public void setResponseValidCheck(String responseValidCheck) {
		this.responseValidCheck = responseValidCheck;
	}
	public String getTaskProcessClass() {
		return taskProcessClass;
	}
	public void setTaskProcessClass(String taskProcessClass) {
		this.taskProcessClass = taskProcessClass;
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
	public SpiderWebsiteConfig getWebsiteConfigBO() {
		return websiteConfigBO;
	}
	public void setWebsiteConfigBO(SpiderWebsiteConfig websiteConfigBO) {
		this.websiteConfigBO = websiteConfigBO;
	}
	public List<SpiderFieldRule> getFieldRules() {
		return fieldRules;
	}
	public void setFieldRules(List<SpiderFieldRule> fieldRules) {
		this.fieldRules = fieldRules;
	}
}
