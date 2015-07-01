package com.lmdna.spider.dao.impl;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.lmdna.spider.dao.SpiderBizDao;
import com.lmdna.spider.dao.model.SpiderBiz;
import com.lmdna.spider.exception.BizException;

public class SpiderBizDaoImpl extends BaseDaoImpl implements SpiderBizDao {

	@Override
	public SpiderBiz getBizbyId(int id) throws BizException{
		SpiderBiz biz;
		try {
			biz = (SpiderBiz) this.getSqlMapClient().queryForObject("spiderBiz.selectBizById", id);
		} catch (SQLException e) {
			throw new BizException("selectBizById ERROR", e);
		}
		return biz;
	}

	@Override
	public List<SpiderBiz> getBizs(Map<String, Object> parammap) throws BizException{
		List<SpiderBiz> bizlist;
		try {
			bizlist = this.getSqlMapClient().queryForList("spiderBiz.selectBizs", parammap);
		} catch (SQLException e) {
			throw new BizException("getBizs ERROR", e);
		}
		return bizlist;
	}

	@Override
	public List<SpiderBiz> getAllBiz() throws BizException{
		List<SpiderBiz> bizlist;
		try {
			bizlist = this.getSqlMapClient().queryForList("spiderBiz.selectBizs");
		} catch (SQLException e) {
			throw new BizException("getAllBizs ERROR", e);
		}
		return bizlist;
	}

	@Override
	public int getBizCount(Map<String, Object> parammap) throws BizException {
		try {
			return (Integer) this.getSqlMapClient().queryForObject("spiderBiz.getBizCount", parammap);
		} catch (SQLException e) {
			throw new BizException("getBizCount ERROR", e);
		}
	}

	@Override
	public int addBiz(SpiderBiz biz) throws BizException {
		try {
			return (Integer) this.getSqlMapClient().insert("spiderBiz.addBiz", biz);
		} catch (SQLException e) {
			throw new BizException("addBiz ERROR", e);
		}
	}
	
}
