package com.lmdna.spider.protocol.rpc.utils;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileRequestObject implements Serializable{
	private static final long serialVersionUID = 5936546828510528424L;
	private String bizCode;
	private String taskId;
	private String filePath;
	private String fileName;
	private String type;
	private AtomicBoolean downloaded = new AtomicBoolean(false);
	private AtomicBoolean failed = new AtomicBoolean(false);
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
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void finished(){
		this.downloaded = new AtomicBoolean(true);
	}
	public boolean isFinished(){
		return downloaded.get();
	}
	public boolean isFailed() {
		return failed.get();
	}
	public void failed() {
		this.failed = new AtomicBoolean(true);
	}
}
