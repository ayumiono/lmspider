package com.lmdna.spider.dao.impl;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.lmdna.spider.dao.SpiderFieldRuleDao;
import com.lmdna.spider.dao.model.SpiderFieldRule;
import com.lmdna.spider.exception.BizException;

public class SpiderFieldRuleDaoImpl extends BaseDaoImpl implements SpiderFieldRuleDao {

	@Override
	public List<SpiderFieldRule> getSpiderFieldRules(int bizId) throws BizException{
		List<SpiderFieldRule> fieldRules;
		try {
			fieldRules = this.getSqlMapClient().queryForList("spiderFieldRule.getFieldRuleBybizID", bizId);
		} catch (SQLException e) {
			throw new BizException("getSpiderFieldRules ERROR", e);
		}
		return fieldRules;
	}

	@Override
	public int addSpiderFieldRule(Map<String, Object> parammap){
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void deleteSpiderFieldRule(Map<String, Object> parammap){
		// TODO Auto-generated method stub
		
	}

	@Override
	public int addSpiderFieldRule(SpiderFieldRule t) throws BizException {
		try {
			return (Integer) this.getSqlMapClient().insert("spiderFieldRule.addFieldRule", t);
		} catch (SQLException e) {
			throw new BizException("addFieldRule ERROR", e);
		}
	}
}
