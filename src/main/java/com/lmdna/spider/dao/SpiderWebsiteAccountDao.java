package com.lmdna.spider.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.lmdna.spider.dao.model.SpiderWebsiteAccount;
import com.lmdna.spider.exception.BizException;

public interface SpiderWebsiteAccountDao {
	public List<SpiderWebsiteAccount> getWebsiteAccounts(Map<String, Object> parammap)throws BizException;
	public int invalidWebsiteAccount(Map<String, Object> parammap)throws BizException;
	public int addWebsiteAccount(Map<String, Object> parammap)throws BizException;
}
