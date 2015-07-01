package com.lmdna.spider.protocol.rpc.utils;

public class SpiderRemoveCmd extends RemoteCmd {

	private static final long serialVersionUID = 4280659399187044451L;
	
	private RemoteCmd resetCmd;//CommonSpiderLoad
	
	public SpiderRemoveCmd(RemoteCmd resetCmd){
		this.setCmdType(CmdType.REMOTE_CMD_SPIDER_REMOVE);
		this.resetCmd = resetCmd;
	}

}
