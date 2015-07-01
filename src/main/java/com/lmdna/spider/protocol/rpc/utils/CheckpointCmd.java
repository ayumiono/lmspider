package com.lmdna.spider.protocol.rpc.utils;

public class CheckpointCmd extends RemoteCmd {

	private static final long serialVersionUID = 8904764265247922135L;
	private String checkpointDir;
	public CheckpointCmd(String checkpointDir){
		this.setCmdType(CmdType.REMOTE_CMD_CHECKPOINT);
		this.setCheckpointDir(checkpointDir);
	}
	public String getCheckpointDir() {
		return checkpointDir;
	}
	public void setCheckpointDir(String checkpointDir) {
		this.checkpointDir = checkpointDir;
	}
}
