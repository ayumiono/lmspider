package com.lmdna.spider.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.lmdna.spider.dao.model.SpiderWebsite;
import com.lmdna.spider.exception.BizException;


public interface SpiderWebsiteDao {
	public void deleteWebsite(Map<String,Object> parammap)throws BizException;
	public int addWebsite(SpiderWebsite t)throws BizException;
	public SpiderWebsite getWebsite(int id)throws BizException;
	public List<SpiderWebsite> getAllWebsite()throws BizException;
	public SpiderWebsite getWebsite(SpiderWebsite t)throws BizException;
}
