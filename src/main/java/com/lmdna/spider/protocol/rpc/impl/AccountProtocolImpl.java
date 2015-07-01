package com.lmdna.spider.protocol.rpc.impl;

import java.util.List;
import java.util.Map;

import com.lmdna.spider.dao.model.SpiderWebsiteAccount;
import com.lmdna.spider.node.master.MasterNode;
import com.lmdna.spider.protocol.rpc.AccountProtocol;


public class AccountProtocolImpl implements AccountProtocol{
	
	private MasterNode masterNode;
	
	public AccountProtocolImpl(MasterNode masterNode){
		this.masterNode = masterNode;
	}
	
	@Override
	public synchronized List<SpiderWebsiteAccount> getAccounts(Map<String,Object> parammap) {
		String bizCode = (String) parammap.get("bizCode");
		int loadcount = (Integer) parammap.get("loadCount");
		Integer site = (Integer)parammap.get("site");
		String machineId = (String) parammap.get("machineId");
		return masterNode.distributeAccount(machineId,bizCode, site, loadcount);
	}

	@Override
	public void removeInvalidAccount(long siteId, String account,String password, String machineId, String cause) {
		
	}

	@Override
	public SpiderWebsiteAccount getAccount(Map<String, Object> parammap) {
		String bizCode = (String) parammap.get("bizCode");
		Integer site = (Integer)parammap.get("site");
		String machineId = (String) parammap.get("machineId");
		List<SpiderWebsiteAccount> list = masterNode.distributeAccount(machineId,bizCode,site,1);
		if(list.size()>0){
			return list.get(0);
		}
		return null;
	}
}
