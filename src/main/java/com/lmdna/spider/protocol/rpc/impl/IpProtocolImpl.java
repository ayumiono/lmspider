package com.lmdna.spider.protocol.rpc.impl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmdna.spider.dao.model.SpiderProxyIp;
import com.lmdna.spider.node.master.MasterNode;
import com.lmdna.spider.protocol.rpc.IpProtocol;

public class IpProtocolImpl implements IpProtocol{
	
	private static final Logger logger = LoggerFactory.getLogger(IpProtocolImpl.class);
	private MasterNode masterNode;
	
	public IpProtocolImpl(MasterNode masterNode){
		this.masterNode = masterNode;
	}
	@Override
	public synchronized List<SpiderProxyIp> getIps(Map<String,Object> parammap) {
		String bizCode = (String) parammap.get("bizCode");
		int loadcount = (Integer) parammap.get("loadCount");
		String machineId = (String) parammap.get("machineId");
		return masterNode.distributeProxyIp(machineId,bizCode, loadcount);
	}

	@Override
	public synchronized int addBlackProxyIp(Map<String,Object> parammap) {
		return masterNode.getFacade().addBlackProxyIp(parammap);
	}
	
	@Override
	public void updateProxyIpStatus(Map<String, Object> parammap) {
		this.masterNode.getFacade().updateProxyIpStatus(parammap);
	}

	@Override
	public void delProxyIpStatus(Map<String, Object> parammap) {
		this.masterNode.getFacade().delProxyIpStatus(parammap);
	}

	@Override
	public int addProxyIpStatus(Map<String, Object> parammap) {
		return masterNode.getFacade().addProxyIpStatus(parammap);
	}

	@Override
	public void delProxyIp(Map<String, Object> parammap) {
		masterNode.getFacade().delProxyIp(parammap);
	}

}
