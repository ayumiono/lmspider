package com.lmdna.spider.protocol.rpc.utils;

public class TaskFinishedMsg extends RemoteCmd {
	private static final long serialVersionUID = 4073145154797178651L;
	private String taskId;
	private String bizCode;
	public TaskFinishedMsg(String taskId,String bizCode){
		this.setCmdType(CmdType.REMOTE_CMD_TASK_FINISHED);
		this.taskId = taskId;
		this.bizCode = bizCode;
	}
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public String getBizCode() {
		return bizCode;
	}
	public void setBizCode(String bizCode) {
		this.bizCode = bizCode;
	}

}
