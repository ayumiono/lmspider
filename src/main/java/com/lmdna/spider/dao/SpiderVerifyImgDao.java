package com.lmdna.spider.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.lmdna.spider.dao.model.SpiderVerifyImg;
import com.lmdna.spider.exception.BizException;

public interface SpiderVerifyImgDao {
	public SpiderVerifyImg getVerifyImg(long id)throws BizException;
	public int addVerifyImg(Map<String,Object> parammap)throws BizException;
	public void deleteVerifyImg(long id)throws BizException;
	public List<SpiderVerifyImg> getVerifyImgList()throws BizException;
	public void submitVerifyCode(Map<String, Object> parammap)throws BizException;
}
