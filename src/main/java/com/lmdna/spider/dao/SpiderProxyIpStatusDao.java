package com.lmdna.spider.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.lmdna.spider.dao.model.SpiderProxyIp;
import com.lmdna.spider.exception.BizException;


public interface SpiderProxyIpStatusDao {
	public int addProxyIpStatus(Map<String,Object> parammap)throws BizException;
	public void deleteProxyIpStatus(Map<String,Object> parammap)throws BizException;
	public List<SpiderProxyIp> getProxyIpStatus(Map<String,Object> parammap)throws BizException;
	public void updateProxyIpStatus(Map<String,Object> parammap)throws BizException;
}
