package com.lmdna.spider.dao;

import java.sql.SQLException;
import java.util.Map;

import com.lmdna.spider.dao.model.SpiderBlackProxyIp;
import com.lmdna.spider.exception.BizException;


public interface SpiderBlackProxyIpDao {
	public SpiderBlackProxyIp getBlackProxyIps(Map<String,Object> parammap)throws BizException;
	public int addBlackProxyIp(Map<String,Object> parammap)throws BizException;
	public void deleteBlackProxyIp(Map<String,Object> parammap)throws BizException;
}
