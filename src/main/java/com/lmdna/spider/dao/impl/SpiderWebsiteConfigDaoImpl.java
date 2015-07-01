package com.lmdna.spider.dao.impl;

import java.sql.SQLException;
import java.util.Map;

import com.lmdna.spider.dao.SpiderWebsiteConfigDao;
import com.lmdna.spider.dao.model.SpiderWebsiteConfig;
import com.lmdna.spider.exception.BizException;

public class SpiderWebsiteConfigDaoImpl extends BaseDaoImpl implements SpiderWebsiteConfigDao {

	@Override
	public void deleteWebsiteConfig(Map<String, Object> parammap) throws BizException{
		// TODO Auto-generated method stub
		
	}

	@Override
	public SpiderWebsiteConfig getWebsiteConfig(Map<String, Object> parammap) throws BizException{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int addWebsiteConfig(Map<String, Object> parammap) throws BizException{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int addWebsiteConfig(SpiderWebsiteConfig t) throws BizException {
		try {
			return (Integer) this.getSqlMapClient().insert("spiderWebsiteConfig.addWebsiteConfig", t);
		} catch (SQLException e) {
			throw new BizException("addWebsiteConfig ERROR", e);
		}
	}
}
