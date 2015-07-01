package com.lmdna.spider.dao.model;

import java.io.Serializable;
import java.util.Date;


public class SpiderWebsiteAccount implements Serializable{
	private static final long serialVersionUID = 8288484815083463262L;
	private Integer id;
	private String account;
	private String password;
	private String site;
	private Integer valid;
	private Date createTime;
	private Date updateTime;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
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
	public String getSite() {
		return site;
	}
	public void setSite(String site) {
		this.site = site;
	}
	public Integer getValid() {
		return valid;
	}
	public void setValid(Integer valid) {
		this.valid = valid;
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
	
	public boolean equals(Object o){
		SpiderWebsiteAccount that = (SpiderWebsiteAccount)o;
		if(this.getAccount().equals(that.getAccount()) && this.getSite().equals(that.getSite()) && this.getPassword().equals(that.getPassword())){
			return true;
		}else{
			return false;
		}
	}
}
