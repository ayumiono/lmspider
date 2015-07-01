package com.lmdna.spider.protocol.rpc;

import java.util.List;
import java.util.Map;

import com.lmdna.spider.dao.model.SpiderWebsiteAccount;

public interface AccountProtocol {
	public List<SpiderWebsiteAccount> getAccounts(Map<String,Object> parammap);
	public void removeInvalidAccount(long siteId,String account,String password,String machineId,String cause);
	public SpiderWebsiteAccount getAccount(Map<String,Object> parammap);
}
