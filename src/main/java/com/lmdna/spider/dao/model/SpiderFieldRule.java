package com.lmdna.spider.dao.model;

import java.io.Serializable;
import java.util.Date;


public class SpiderFieldRule implements Serializable{
	private static final long serialVersionUID = -2436502418896477477L;
	private Integer id; //主键
	private String fieldName; //字段名
	private Integer bizId;
	private String rule; //匹配规则
	private Integer type;//0:正则,1:xpath,2:css
	private Integer allowEmpty;//允许为空
	private Integer additionRequest;//是否会产生新的任务0：不会，1：会
	private Integer additionDownload;//是否产生额外的下载请求0：不会1：会
	private Integer parentId;//父规则ID
	private Integer needPersistence;//是否需要存储0:不需要1：需要
	private String responseValidCheck;//该字段只针对additionalReq=1的规则有效
	private Date createTime; //创建时间
	private Date updateTime; //修改时间
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public Integer getBizId() {
		return bizId;
	}
	public void setBizId(Integer bizId) {
		this.bizId = bizId;
	}
	public String getRule() {
		return rule;
	}
	public void setRule(String rule) {
		this.rule = rule;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Integer getAllowEmpty() {
		return allowEmpty;
	}
	public void setAllowEmpty(Integer allowEmpty) {
		this.allowEmpty = allowEmpty;
	}
	public Integer getAdditionRequest() {
		return additionRequest;
	}
	public void setAdditionRequest(Integer additionRequest) {
		this.additionRequest = additionRequest;
	}
	public Integer getAdditionDownload() {
		return additionDownload;
	}
	public void setAdditionDownload(Integer additionDownload) {
		this.additionDownload = additionDownload;
	}
	public Integer getParentId() {
		return parentId;
	}
	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}
	public Integer getNeedPersistence() {
		return needPersistence;
	}
	public void setNeedPersistence(Integer needPersistence) {
		this.needPersistence = needPersistence;
	}
	public String getResponseValidCheck() {
		return responseValidCheck;
	}
	public void setResponseValidCheck(String responseValidCheck) {
		this.responseValidCheck = responseValidCheck;
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
	
}
