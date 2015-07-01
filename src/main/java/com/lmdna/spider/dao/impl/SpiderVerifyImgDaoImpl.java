package com.lmdna.spider.dao.impl;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.lmdna.spider.dao.SpiderVerifyImgDao;
import com.lmdna.spider.dao.model.SpiderVerifyImg;
import com.lmdna.spider.exception.BizException;

public class SpiderVerifyImgDaoImpl extends BaseDaoImpl implements
		SpiderVerifyImgDao {

	@Override
	public SpiderVerifyImg getVerifyImg(long id) throws BizException{
		try {
			return (SpiderVerifyImg) this.getSqlMapClient().queryForObject("spiderVerifyImg.selectVerifyImgByid", id);
		} catch (SQLException e) {
			throw new BizException("getVerifyImg ERROR", e);
		}
	}

	@Override
	public int addVerifyImg(Map<String, Object> parammap)throws BizException {
		try {
			return (Integer) this.getSqlMapClient().insert("spiderVerifyImg.addVerifyImg", parammap);
		} catch (SQLException e) {
			throw new BizException("addVerifyImg ERROR", e);
		}
	}

	@Override
	public void deleteVerifyImg(long id) throws BizException{
		try {
			this.getSqlMapClient().delete("spiderVerifyImg.deleteVerifyImg", id);
		} catch (SQLException e) {
			throw new BizException("deleteVerifyImg ERROR", e);
		}
	}

	@Override
	public List<SpiderVerifyImg>  getVerifyImgList()throws BizException {
		try {
			return this.getSqlMapClient().queryForList("spiderVerifyImg.selectVerifyImgs");
		} catch (SQLException e) {
			throw new BizException("getVerifyImgs ERROR", e);
		}
	}

	@Override
	public void submitVerifyCode(Map<String, Object> parammap) throws BizException {
		try {
			this.getSqlMapClient().update("spiderVerifyImg.submitVerifyCode", parammap);
		} catch (SQLException e) {
			throw new BizException("submitVerifyCode ERROR", e);
		}
	}

}
