package com.lmdna.spider.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.lmdna.spider.dao.model.SpiderSiteCharset;
import com.lmdna.spider.exception.BizException;


public interface SpiderSiteCharsetDao {
	public List<SpiderSiteCharset> getCharset(Map<String,Object> parammap)throws BizException;
}
