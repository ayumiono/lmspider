package com.lmdna.spider.dao.model;

import java.io.Serializable;
import java.util.Date;


public class SpiderProxyIp implements Serializable{
	private static final long serialVersionUID = 2831566341833538542L;
	private Integer id; //主键
	private String ip; //ip
	private Integer port; //端口号
	private Date createTime; //创建时间
	private Date updateTime; //修改时间
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
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
		SpiderProxyIp that = (SpiderProxyIp)o;
		if(this.getIp().equals(that.getIp()) && this.getPort().equals(that.getPort())){
			return true;
		}else{
			return false;
		}
	}
}
