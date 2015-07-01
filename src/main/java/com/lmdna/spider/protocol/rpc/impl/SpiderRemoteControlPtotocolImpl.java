package com.lmdna.spider.protocol.rpc.impl;

import com.lmdna.spider.node.master.MasterNode;
import com.lmdna.spider.protocol.rpc.SpiderRemoteControlProtocol;

public class SpiderRemoteControlPtotocolImpl implements SpiderRemoteControlProtocol {
	
	private MasterNode master;
	
	public SpiderRemoteControlPtotocolImpl(MasterNode master){
		this.master = master;
	}

	@Override
	public void submitTask(String bizCode,String filePath,String fileName,int rowperblock) {
		try {
			master.submitTask(bizCode, filePath, fileName, rowperblock);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
