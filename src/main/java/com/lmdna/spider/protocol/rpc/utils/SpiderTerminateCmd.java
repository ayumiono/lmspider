package com.lmdna.spider.protocol.rpc.utils;

public class SpiderTerminateCmd extends RemoteCmd {

	private static final long serialVersionUID = -2814521816804699473L;
	
	private String bizCode;

	public SpiderTerminateCmd(String bizCode){
		this.setBizCode(bizCode);
		this.setCmdType(CmdType.REMOTE_CMD_SPIDER_TERMINATE);
	}

	public String getBizCode() {
		return bizCode;
	}

	public void setBizCode(String bizCode) {
		this.bizCode = bizCode;
	}
}
