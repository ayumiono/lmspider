package com.lmdna.spider.dao.impl;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.lmdna.spider.dao.SpiderWebsiteAccountDao;
import com.lmdna.spider.dao.model.SpiderWebsiteAccount;
import com.lmdna.spider.exception.BizException;

public class SpiderWebsiteAccountDaoImpl extends BaseDaoImpl implements SpiderWebsiteAccountDao {

	@Override
	public List<SpiderWebsiteAccount> getWebsiteAccounts(
			Map<String, Object> parammap) throws BizException{
		List<SpiderWebsiteAccount> result;
		try {
			result = this.getSqlMapClient().queryForList("spiderWebsiteAccount.selectWebsiteAccounts", parammap);
		} catch (SQLException e) {
			throw new BizException("getWebsiteAccounts ERROR", e);
		}
		return result;
	}

	@Override
	public int invalidWebsiteAccount(Map<String, Object> parammap) throws BizException{
		try {
			return this.getSqlMapClient().update("spiderWebsite.invalidWebsiteAccounts", parammap);
		} catch (SQLException e) {
			throw new BizException("invalidWebsiteAccounts ERROR", e);
		}
	}

	@Override
	public int addWebsiteAccount(Map<String, Object> parammap) throws BizException{
		try {
			return (Integer) this.getSqlMapClient().insert("spiderWebsite.addWebsiteAccount", parammap);
		} catch (SQLException e) {
			throw new BizException("addWebsiteAccount ERROR", e);
		}
	}
}
