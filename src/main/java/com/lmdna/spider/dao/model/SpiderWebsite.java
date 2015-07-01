package com.lmdna.spider.dao.model;

import java.io.Serializable;


public class SpiderWebsite implements Serializable{
	private static final long serialVersionUID = 1122881704218681248L;
	private Integer id;
	private String siteEnName;
	private String siteChnName;
	private String domain;
	private String charset;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getSiteEnName() {
		return siteEnName;
	}
	public void setSiteEnName(String siteEnName) {
		this.siteEnName = siteEnName;
	}
	public String getSiteChnName() {
		return siteChnName;
	}
	public void setSiteChnName(String siteChnName) {
		this.siteChnName = siteChnName;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getCharset() {
		return charset;
	}
	public void setCharset(String charset) {
		this.charset = charset;
	}
}
