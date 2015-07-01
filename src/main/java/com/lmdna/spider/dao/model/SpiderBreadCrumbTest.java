package com.lmdna.spider.dao.model;

import java.util.Date;


public class SpiderBreadCrumbTest {
	private Integer id; //主键
	private String breadCrumb;//面包屑
	private Date createTime; //创建时间
	private Date updateTime; //修改时间
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getBreadCrumb() {
		return breadCrumb;
	}
	public void setBreadCrumb(String breadCrumb) {
		this.breadCrumb = breadCrumb;
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
