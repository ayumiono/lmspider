package com.lmdna.spider.dao.impl;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.lmdna.spider.dao.SpiderWebsiteDao;
import com.lmdna.spider.dao.model.SpiderWebsite;
import com.lmdna.spider.exception.BizException;

public class SpiderWebsiteDaoImpl extends BaseDaoImpl implements SpiderWebsiteDao {

	@Override
	public void deleteWebsite(Map<String, Object> parammap) throws BizException{
		// TODO Auto-generated method stub
		
	}

	@Override
	public int addWebsite(SpiderWebsite t) throws BizException{
		try {
			return (Integer) this.getSqlMapClient().insert("spiderWebsite.addWebsite", t);
		} catch (SQLException e) {
			throw new BizException("addWebsite ERROR", e);
		}
	}

	@Override
	public SpiderWebsite getWebsite(int id) throws BizException{
		try {
			return (SpiderWebsite) this.getSqlMapClient().queryForObject("spiderWebsite.getWebsiteById", id);
		} catch (SQLException e) {
			throw new BizException("getWebsite ERROR", e);
		}
	}

	@Override
	public List<SpiderWebsite> getAllWebsite() throws BizException {
		try {
			return (List<SpiderWebsite>) this.getSqlMapClient().queryForList("spiderWebsite.getAllWebsite");
		} catch (SQLException e) {
			throw new BizException("getAllWebsite ERROR", e);
		}
	}

	@Override
	public SpiderWebsite getWebsite(SpiderWebsite t) throws BizException {
		try {
			return (SpiderWebsite) this.getSqlMapClient().queryForObject("spiderWebsite.getWebsite",t);
		} catch (SQLException e) {
			throw new BizException("getWebsite ERROR", e);
		}
	}
}
