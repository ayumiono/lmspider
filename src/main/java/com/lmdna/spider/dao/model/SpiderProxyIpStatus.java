package com.lmdna.spider.dao.model;



public class SpiderProxyIpStatus {
	private Integer id; //主键
	private Integer proxyIpId;
	private String ip; //ip
	private Integer port; //端口号
	private Integer bizId; //业务ID
	private Integer failedNum;//连续失败次数
	private Integer successNum;//成功次数
	private Integer borrowNum;//使用次数
	private Integer deadNum;//失效次数
	private Integer reuseInterval;
	private Integer speed;
	private String machineId;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getProxyIpId() {
		return proxyIpId;
	}
	public void setProxyIpId(Integer proxyIpId) {
		this.proxyIpId = proxyIpId;
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
	public Integer getFailedNum() {
		return failedNum;
	}
	public void setFailedNum(Integer failedNum) {
		this.failedNum = failedNum;
	}
	public Integer getSuccessNum() {
		return successNum;
	}
	public void setSuccessNum(Integer successNum) {
		this.successNum = successNum;
	}
	public Integer getBorrowNum() {
		return borrowNum;
	}
	public void setBorrowNum(Integer borrowNum) {
		this.borrowNum = borrowNum;
	}
	public Integer getDeadNum() {
		return deadNum;
	}
	public void setDeadNum(Integer deadNum) {
		this.deadNum = deadNum;
	}
	public Integer getReuseInterval() {
		return reuseInterval;
	}
	public void setReuseInterval(Integer reuseInterval) {
		this.reuseInterval = reuseInterval;
	}
	public Integer getSpeed() {
		return speed;
	}
	public void setSpeed(Integer speed) {
		this.speed = speed;
	}
	public Integer getBizId() {
		return bizId;
	}
	public void setBizId(Integer bizId) {
		this.bizId = bizId;
	}
	public String getMachineId() {
		return machineId;
	}
	public void setMachineId(String machineId) {
		this.machineId = machineId;
	}
	public String toString(){
		return String.format("bizId:%s ip:%s port:%d failedNum:%d successNum:%d borrowNum:%d deadNum:%d", bizId,ip,port,failedNum,successNum,borrowNum,deadNum);
	}
}

