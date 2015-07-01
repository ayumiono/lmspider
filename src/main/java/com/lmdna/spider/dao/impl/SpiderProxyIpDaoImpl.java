package com.lmdna.spider.dao.impl;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.lmdna.spider.dao.SpiderProxyIpDao;
import com.lmdna.spider.dao.model.SpiderProxyIp;
import com.lmdna.spider.dao.model.SpiderVerifyImg;
import com.lmdna.spider.exception.BizException;

public class SpiderProxyIpDaoImpl extends BaseDaoImpl implements SpiderProxyIpDao {

	@Override
	public List<SpiderProxyIp> getAvailableProxy(Map<String, Object> parammap) throws BizException{
		List<SpiderProxyIp> proxyIps;
		try {
			proxyIps = this.getSqlMapClient().queryForList("spiderProxyip.getProxyIps", parammap);
		} catch (SQLException e) {
			throw new BizException("getProxyIps ERROR", e);
		}
		return proxyIps;
	}

	@Override
	public int addProxyIp(SpiderProxyIp t) throws BizException{
		try {
			return (Integer) this.getSqlMapClient().insert("spiderProxyip.addProxyip", t);
		} catch (SQLException e) {
			throw new BizException("addProxyip ERROR", e);
		}
	}

	@Override
	public void deleteProxyIp(Map<String, Object> parammap) throws BizException{
		try {
			this.getSqlMapClient().delete("spiderProxyip.deleteInvalidProxyip", parammap);
		} catch (SQLException e) {
			throw new BizException("deleteInvalidProxyip ERROR", e);
		}
	}
	
	@Override
	public List<SpiderProxyIp> getAllProxyIp(Map<String,Object> parammap)throws BizException {
		try {
			return this.getSqlMapClient().queryForList("spiderProxyip.selectProxyip",parammap);
		} catch (SQLException e) {
			throw new BizException("getAllProxyIp ERROR", e);
		}
	}

	@Override
	public int getProxyIpCount(Map<String, Object> parammap)
			throws BizException {
		try {
			return (Integer) this.getSqlMapClient().queryForObject("spiderProxyip.getProxyipCount",parammap);
		} catch (SQLException e) {
			throw new BizException("getProxyipCount ERROR", e);
		}
	}
}
