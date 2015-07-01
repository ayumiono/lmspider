package com.lmdna.spider.protocol.rpc;



public interface SpiderRemoteControlProtocol {
	public void submitTask(String bizCode,String filePath,String fileName,int rowperblock);
}
