package com.lmdna.spider.dao;

import java.sql.SQLException;
import java.util.Map;

import com.lmdna.spider.dao.model.SpiderServer;
import com.lmdna.spider.exception.BizException;


public interface SpiderServerDao {
	public int addServer(Map<String,Object> parammap)throws BizException;
	public void deleteServer(Map<String,Object> parammap)throws BizException;
	public SpiderServer getServer(Map<String,Object> parammap)throws BizException;
}
