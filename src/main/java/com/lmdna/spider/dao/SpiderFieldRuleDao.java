package com.lmdna.spider.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.lmdna.spider.dao.model.SpiderFieldRule;
import com.lmdna.spider.exception.BizException;


public interface SpiderFieldRuleDao {
	public List<SpiderFieldRule> getSpiderFieldRules(int bizId)throws BizException;
	public int addSpiderFieldRule(Map<String,Object> parammap)throws BizException;
	public void deleteSpiderFieldRule(Map<String,Object> parammap)throws BizException;
	public int addSpiderFieldRule(SpiderFieldRule t)throws BizException;
}
