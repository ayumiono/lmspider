package com.lmdna.spider.protocol.rpc.utils;

import java.io.Serializable;

public class TaskParseFinishedMsg implements Serializable{
	private static final long serialVersionUID = 1723526294985197079L;
	private String blockID;
	private String filePipelinePath;//该blockid对应的结果路径
	private String bizCode;
	private String taskId;
	private Integer parseRequestCount;//解析出多少个request
	private String machineId;//由哪台机器执行
	public String getBlockID() {
		return blockID;
	}
	public void setBlockID(String blockID) {
		this.blockID = blockID;
	}
	public String getFilePipelinePath() {
		return filePipelinePath;
	}
	public void setFilePipelinePath(String filePipelinePath) {
		this.filePipelinePath = filePipelinePath;
	}
	public String getBizCode() {
		return bizCode;
	}
	public void setBizCode(String bizCode) {
		this.bizCode = bizCode;
	}
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public Integer getParseRequestCount() {
		return parseRequestCount;
	}
	public void setParseRequestCount(Integer parseRequestCount) {
		this.parseRequestCount = parseRequestCount;
	}
	public String getMachineId() {
		return machineId;
	}
	public void setMachineId(String machineId) {
		this.machineId = machineId;
	}
}
