package com.lmdna.spider.dao.impl;

import java.sql.SQLException;
import java.util.Map;

import com.lmdna.spider.dao.SpiderBlackProxyIpDao;
import com.lmdna.spider.dao.model.SpiderBlackProxyIp;
import com.lmdna.spider.exception.BizException;

public class SpiderBlackProxyIpDaoImpl extends BaseDaoImpl implements SpiderBlackProxyIpDao {

	@Override
	public SpiderBlackProxyIp getBlackProxyIps(Map<String, Object> parammap) throws BizException{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int addBlackProxyIp(Map<String, Object> parammap) throws BizException{
		Integer id = null;
		try {
			id = (Integer) this.getSqlMapClient().insert("spiderProxyip.addBlackProxyip", parammap);
		} catch (SQLException e) {
			throw new BizException("addBlackProxyIp ERROR",e);
		}
		return id;
	}

	@Override
	public void deleteBlackProxyIp(Map<String, Object> parammap){
	}
}
