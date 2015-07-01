package com.lmdna.spider.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.lmdna.spider.dao.model.SpiderProxyIp;
import com.lmdna.spider.exception.BizException;



public interface SpiderProxyIpDao {
	/**
	 * 关联spider_proxyip和spider_proxyip_status表，根据bizid取不存在于spider_proxyip_status表中的代理IP
	 * @param paraMap
	 * @return
	 */
	public List<SpiderProxyIp> getAvailableProxy(Map<String,Object> parammap)throws BizException;
	public int addProxyIp(SpiderProxyIp t)throws BizException;
	public void deleteProxyIp(Map<String,Object> parammap)throws BizException;
	public List<SpiderProxyIp> getAllProxyIp(Map<String,Object> parammap)throws BizException;
	public int getProxyIpCount(Map<String,Object> parammap)throws BizException;
}
