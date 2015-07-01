package com.lmdna.spider.dao.impl;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.lmdna.spider.dao.SpiderProxyIpStatusDao;
import com.lmdna.spider.dao.model.SpiderProxyIp;
import com.lmdna.spider.exception.BizException;

public class SpiderProxyIpStatusDaoImpl extends BaseDaoImpl implements SpiderProxyIpStatusDao {

	@Override
	public int addProxyIpStatus(Map<String, Object> parammap) throws BizException{
		try {
			return (Integer) this.getSqlMapClient().insert("spiderProxyip.addProxyipStatus", parammap);
		} catch (SQLException e) {
			throw new BizException("addProxyipStatus ERROR", e);
		}
	}

	@Override
	public void deleteProxyIpStatus(Map<String, Object> parammap) throws BizException{
		try {
			this.getSqlMapClient().delete("spiderProxyip.deleteProxyipStatus", parammap);
		} catch (SQLException e) {
			throw new BizException("addProxyipStatus ERROR", e);
		}
	}

	@Override
	public List<SpiderProxyIp> getProxyIpStatus(Map<String, Object> parammap) throws BizException{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateProxyIpStatus(Map<String, Object> parammap) throws BizException{
		try {
			this.getSqlMapClient().update("spiderProxyip.updateProxyipStatus", parammap);
		} catch (SQLException e) {
			throw new BizException("updateProxyipStatus ERROR", e);
		}
	}
}
