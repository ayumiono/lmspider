package com.lmdna.spider.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.lmdna.spider.dao.model.SpiderBiz;
import com.lmdna.spider.exception.BizException;


public interface SpiderBizDao{
	public SpiderBiz getBizbyId(int id)throws BizException;
	public List<SpiderBiz> getBizs(Map<String,Object> parammap)throws BizException;
	public List<SpiderBiz> getAllBiz()throws BizException;
	public int getBizCount(Map<String, Object> parammap)throws BizException;
	public int addBiz(SpiderBiz biz)throws BizException;
}
