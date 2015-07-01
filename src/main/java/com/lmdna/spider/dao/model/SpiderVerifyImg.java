package com.lmdna.spider.dao.model;

import java.util.Date;


public class SpiderVerifyImg {
	private Integer id; //主键
	private String host; //爬虫机器IP
	private String url; //请求爬虫机器上的验证码的URL
	private String verifyCode; //验证码
	private Integer expire;//验证码过期时间
	private String from;//来源
	private Date imgCreateTime;//验证码生成时间
	private Integer priority;
	private Date createTime; //创建时间
	private Date updateTime; //修改时间
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getVerifyCode() {
		return verifyCode;
	}
	public void setVerifyCode(String verifyCode) {
		this.verifyCode = verifyCode;
	}
	public Integer getExpire() {
		return expire;
	}
	public void setExpire(Integer expire) {
		this.expire = expire;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public Date getImgCreateTime() {
		return imgCreateTime;
	}
	public void setImgCreateTime(Date imgCreateTime) {
		this.imgCreateTime = imgCreateTime;
	}
	public Integer getPriority() {
		return priority;
	}
	public void setPriority(Integer priority) {
		this.priority = priority;
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
