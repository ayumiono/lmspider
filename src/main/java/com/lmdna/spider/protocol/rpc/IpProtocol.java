package com.lmdna.spider.protocol.rpc;

import java.util.List;
import java.util.Map;

import com.lmdna.spider.dao.model.SpiderProxyIp;

public interface IpProtocol {
	/**
	 * 获取IP
	 * @param bizCode 业务名
	 * @param machineId 机器识别（IP）
	 * @return
	 */
	public List<SpiderProxyIp> getIps(Map<String,Object> parammap);
	/**删除某个业务下被封的代理IP
	 * @param proxyip
	 */
	public int addBlackProxyIp(Map<String,Object> parammap);
	
	public void updateProxyIpStatus(Map<String,Object> parammap);
	
	public void delProxyIpStatus(Map<String,Object> parammap);
	
	public int addProxyIpStatus(Map<String,Object> parammap);
	
	public void delProxyIp(Map<String,Object> parammap);
}
