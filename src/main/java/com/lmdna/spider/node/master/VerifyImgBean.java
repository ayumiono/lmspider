package com.lmdna.spider.node.master;

import java.util.Date;

public class VerifyImgBean {
	private Integer id;
	private String img_name;
	private String staticFileURL;
	private String verifyCode; //验证码
	private Integer expire;//验证码过期时间
	private String from;//来源
	private Date imgCreateTime;//验证码生成时间
	private Integer priority;
	private Date createTime; //创建时间
	private String host;//由哪个slave node产生的
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getImg_name() {
		return img_name;
	}
	public void setImg_name(String img_name) {
		this.img_name = img_name;
	}
	public String getStaticFileURL() {
		return staticFileURL;
	}
	public void setStaticFileURL(String staticFileURL) {
		this.staticFileURL = staticFileURL;
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
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
}
