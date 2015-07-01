package com.lmdna.spider.protocol.rpc.utils;

import java.util.List;

import us.codecraft.webmagic.Request;

public class MixedCrawlTask extends RemoteCmd {

	private static final long serialVersionUID = 7781022439443461166L;
	
	private String taskId;//taskId必须与task_file_path一一对应
	private String blockId;
	private List<Request> reqs;

	public MixedCrawlTask(){
		this.setCmdType(CmdType.REMOTE_CMD_MIXEDTASK);
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getBlockId() {
		return blockId;
	}

	public void setBlockId(String blockId) {
		this.blockId = blockId;
	}

	public List<Request> getReqs() {
		return reqs;
	}

	public void setReqs(List<Request> reqs) {
		this.reqs = reqs;
	}
}
