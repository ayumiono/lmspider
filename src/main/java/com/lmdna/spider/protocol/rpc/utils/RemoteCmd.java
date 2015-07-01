package com.lmdna.spider.protocol.rpc.utils;

import java.io.Serializable;

public abstract class RemoteCmd implements Serializable{
	private static final long serialVersionUID = -5906895844219787594L;
	private String cmdType;
	public RemoteCmd(){
		
	}
	public RemoteCmd(String cmdType){
		this.cmdType = cmdType;
	}
	public String getCmdType() {
		return cmdType;
	}
	public void setCmdType(String cmdType) {
		this.cmdType = cmdType;
	}
}
