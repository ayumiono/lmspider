package com.lmdna.spider.http.servlet;

public class FieldRule {
	private Integer id;
	private Integer bizId;
	private String name;
	private String rule;
	private Integer type;
	private Integer allowEmpty;
	private Integer additionalReq;
	private Integer additionalDownload;
	private String parent;
	private Integer needPersistence;
	private String responseValidCheck;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getBizId() {
		return bizId;
	}

	public void setBizId(Integer bizId) {
		this.bizId = bizId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public Integer getAdditionalReq() {
		return additionalReq;
	}

	public void setAdditionalReq(Integer additionalReq) {
		this.additionalReq = additionalReq;
	}

	public Integer getAdditionalDownload() {
		return additionalDownload;
	}

	public void setAdditionalDownload(Integer additionalDownload) {
		this.additionalDownload = additionalDownload;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
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
}
