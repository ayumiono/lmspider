package com.lmdna.spider.dao;

import java.sql.SQLException;
import java.util.Map;

import com.lmdna.spider.dao.model.SpiderWebsiteConfig;
import com.lmdna.spider.exception.BizException;


public interface SpiderWebsiteConfigDao {
	public void deleteWebsiteConfig(Map<String,Object> parammap)throws BizException;
	public SpiderWebsiteConfig getWebsiteConfig(Map<String,Object> parammap)throws BizException;
	public int addWebsiteConfig(Map<String,Object> parammap)throws BizException;
	public int addWebsiteConfig(SpiderWebsiteConfig t)throws BizException;
}
