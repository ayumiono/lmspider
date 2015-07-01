package com.lmdna.spider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmdna.spider.dao.SpiderBizDao;
import com.lmdna.spider.dao.SpiderBlackProxyIpDao;
import com.lmdna.spider.dao.SpiderFieldRuleDao;
import com.lmdna.spider.dao.SpiderProxyIpDao;
import com.lmdna.spider.dao.SpiderProxyIpStatusDao;
import com.lmdna.spider.dao.SpiderSiteCharsetDao;
import com.lmdna.spider.dao.SpiderVerifyImgDao;
import com.lmdna.spider.dao.SpiderWebsiteAccountDao;
import com.lmdna.spider.dao.SpiderWebsiteConfigDao;
import com.lmdna.spider.dao.SpiderWebsiteDao;
import com.lmdna.spider.dao.impl.SpiderBizDaoImpl;
import com.lmdna.spider.dao.impl.SpiderBlackProxyIpDaoImpl;
import com.lmdna.spider.dao.impl.SpiderFieldRuleDaoImpl;
import com.lmdna.spider.dao.impl.SpiderProxyIpDaoImpl;
import com.lmdna.spider.dao.impl.SpiderProxyIpStatusDaoImpl;
import com.lmdna.spider.dao.impl.SpiderSiteCharsetDaoImpl;
import com.lmdna.spider.dao.impl.SpiderVerifyImgDaoImpl;
import com.lmdna.spider.dao.impl.SpiderWebsiteAccountDaoImpl;
import com.lmdna.spider.dao.impl.SpiderWebsiteConfigDaoImpl;
import com.lmdna.spider.dao.impl.SpiderWebsiteDaoImpl;
import com.lmdna.spider.dao.model.SpiderBiz;
import com.lmdna.spider.dao.model.SpiderFieldRule;
import com.lmdna.spider.dao.model.SpiderProxyIp;
import com.lmdna.spider.dao.model.SpiderSiteCharset;
import com.lmdna.spider.dao.model.SpiderVerifyImg;
import com.lmdna.spider.dao.model.SpiderWebsite;
import com.lmdna.spider.dao.model.SpiderWebsiteAccount;
import com.lmdna.spider.dao.model.SpiderWebsiteConfig;
import com.lmdna.spider.exception.BizException;


/**
 * 数据层访问的门面类
 * @author ayumi
 *
 */
public class SpiderDAOServiceFacade {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private SpiderProxyIpDao spiderProxyIpDao;

	private SpiderBizDao spiderBizDao;

	private SpiderFieldRuleDao spiderFieldRuleDao;

	private SpiderProxyIpStatusDao proxyIpStatusDao;

	private SpiderWebsiteConfigDao spiderWebsiteConfigDao;
	
	private SpiderBlackProxyIpDao blackProxyIpDao;
	
	private SpiderWebsiteAccountDao spiderWebsiteAccountDao;
	
	private SpiderWebsiteDao spiderWebsiteDao;
	
	private SpiderSiteCharsetDao spiderSiteCharsetDao;
	
	private SpiderVerifyImgDao spiderVerifyImgDao;
	
	private static final SpiderDAOServiceFacade instance = new SpiderDAOServiceFacade();
	
	private SpiderDAOServiceFacade(){
		spiderProxyIpDao = new SpiderProxyIpDaoImpl();
		spiderBizDao = new SpiderBizDaoImpl();
		spiderFieldRuleDao = new SpiderFieldRuleDaoImpl();
		proxyIpStatusDao = new SpiderProxyIpStatusDaoImpl();
		spiderWebsiteConfigDao = new SpiderWebsiteConfigDaoImpl();
		blackProxyIpDao = new SpiderBlackProxyIpDaoImpl();
		spiderWebsiteAccountDao = new SpiderWebsiteAccountDaoImpl();
		spiderWebsiteDao = new SpiderWebsiteDaoImpl();
		spiderSiteCharsetDao = new SpiderSiteCharsetDaoImpl();
		spiderVerifyImgDao = new SpiderVerifyImgDaoImpl();
	}
	
	public static SpiderDAOServiceFacade getInstance(){
		return instance;
	}
	
	public List<SpiderWebsiteAccount> getWebsiteAccount(Map<String,Object> parammap){
		List<SpiderWebsiteAccount> result = null;
		try {
			result = spiderWebsiteAccountDao.getWebsiteAccounts(parammap);
		} catch (BizException e) {
			logger.error("[query]获取账号失败！",e);
		}
		return result;
	}
	
	public void removeInvalidAccount(Map<String,Object> parammap){
		try {
			spiderWebsiteAccountDao.invalidWebsiteAccount(parammap);
		} catch (BizException e) {
			logger.error("[delete]删除账号失败！",e);
		}
	}
	
	public List<SpiderBiz> getBizList(Map<String,Object> parammap){
		List<SpiderBiz> bizList = new ArrayList<SpiderBiz>();
		try{
			bizList = spiderBizDao.getBizs(parammap);
			logger.debug("[query]爬虫业务配置信息获取成功！共有{}条记录",bizList.size());
		}catch(Exception e){
			logger.error("爬虫业务配置信息获取失败！",e);
		}
		return bizList;
	}
	
	public int getBizCount(Map<String,Object> parammap){
		int count = 0;
		try {
			count = spiderBizDao.getBizCount(parammap);
		} catch (BizException e) {
			logger.error("爬虫业务数量获取失败！",e);
		}
		return count;
	}
	
	public SpiderBiz getBiz(int id){
		SpiderBiz biz = null;
		try {
			biz = spiderBizDao.getBizbyId(id);
		} catch (BizException e) {
			logger.error("[query]爬虫业务信息获取失败！",e);
		}
		return biz;
	}
	
	public void updateProxyIpStatus(Map<String,Object> paramMap){
		try{
			proxyIpStatusDao.updateProxyIpStatus(paramMap);
			logger.debug("[update]更新代理IP使用状态成功。");
		}catch(BizException e){
			logger.error("更新代理IP使用状态出错！",e);
		}
	}
	
	public List<SpiderProxyIp> getProxyIps(Map<String,Object> paramMap){
		List<SpiderProxyIp> proxyIpList = new ArrayList<SpiderProxyIp>();
		try{
			proxyIpList = spiderProxyIpDao.getAvailableProxy(paramMap);
			logger.debug("[query]从数据库中获取代理IP成功，共获取到{}个代理IP。",proxyIpList.size());
		}catch (Exception e){
			logger.error("从数据库中获取代理IP失败！",e);
		}
		return proxyIpList;
	}
	
	public void delProxyIpStatus(Map<String,Object> paramMap){
		try{
			proxyIpStatusDao.deleteProxyIpStatus(paramMap);
			logger.debug("[delete]删除spider_proxyip_status记录成功。");
		}catch(BizException e){
			logger.error("删除spider_proxyip_status记录失败！",e);
		}
	}
	
	public int addProxyIpStatus(Map<String,Object> parammap){
		try{
			logger.debug("[insert]添加代理IP监控记录成功 ");
			return proxyIpStatusDao.addProxyIpStatus(parammap);
		}catch(BizException e){
			logger.error("添加代理IP监控记录失败",e);
		}
		return 0;
	}
	
	public List<SpiderFieldRule> getFieldRuleByBizId(int bizId){
		List<SpiderFieldRule> fieldList = null;
		try{
			fieldList = spiderFieldRuleDao.getSpiderFieldRules(bizId);
			logger.debug("[query]查询业务ID为{}网页解析规则成功。");
		}catch(Exception e){
			logger.error("查询业务ID为网页解析规则失败！",e);
		}
		return fieldList;
	}
	
	public int addFieldRule(SpiderFieldRule t){
		Integer id = null;
		try {
			id =  this.spiderFieldRuleDao.addSpiderFieldRule(t);
		} catch (BizException e) {
			logger.error("添加fieldrule失败！",e);
		}
		return id;
	}
	
	public int addBlackProxyIp(Map<String,Object> parammap){
		Integer id = null;
		try{
			id = blackProxyIpDao.addBlackProxyIp(parammap);
			logger.debug("[insert]添加代理IP黑名单成功。");
		}catch(BizException e){
			logger.warn("添加代理IP黑名单失败！",e);
		}
		return id;
	}
	
	public void delProxyIp(Map<String,Object> parammap){
		try{
			this.spiderProxyIpDao.deleteProxyIp(parammap);;
			logger.debug("[delete]删除无效代理IP成功。");
		}catch(BizException e){
			logger.error("删除无效代理IP成功！",e);
		}
	}
	
	public List<SpiderSiteCharset> getSiteCharset(Map<String,Object> parammap){
		List<SpiderSiteCharset> charsetList = null;
		try {
			this.spiderSiteCharsetDao.getCharset(parammap);
		} catch (BizException e) {
			logger.error("获取site charset失败!",e);
		}
		return charsetList;
	}
	
	public List<SpiderVerifyImg> getVerifyImgList(){
		List<SpiderVerifyImg> verifyimgList = null;
		try {
			verifyimgList = this.spiderVerifyImgDao.getVerifyImgList();
		} catch (BizException e) {
			logger.error("获取验证码列表失败!",e);
		}
		return verifyimgList;
	}
	
	public void submitVerifyCode(Map<String,Object> parammap){
		try {
			this.spiderVerifyImgDao.submitVerifyCode(parammap);
		} catch (BizException e) {
			logger.error("验证码提交失败！",e);
		}
	}
	
	public List<SpiderProxyIp> getAllProxyIp(Map<String,Object> parammap){
		List<SpiderProxyIp> result = null;
		try {
			result = this.spiderProxyIpDao.getAllProxyIp(parammap);
		} catch (BizException e) {
			logger.error("获取全量代理IP失败！",e);
		}
		return result;
	}
	
	public int getProxyipCount(Map<String,Object> parammap){
		Integer count = null;
		try {
			count = this.spiderProxyIpDao.getProxyIpCount(parammap);
		} catch (BizException e) {
			logger.error("获取代理IP数量失败！",e);
		}
		return count;
	}
	
	public int addProxyIp(SpiderProxyIp t){
		Integer id = null;
		try {
			id = this.spiderProxyIpDao.addProxyIp(t);
		} catch (BizException e) {
			logger.error("添加代理IP失败！",e);
		}
		return id;
	}
	
	public int addWebsite(SpiderWebsite t){
		Integer id = null;
		try {
			id = this.spiderWebsiteDao.addWebsite(t);
		} catch (Exception e) {
			logger.error("添加website失败！",e);
		}
		return id;
	}
	
	public SpiderWebsite getWebsite(Map<String,Object> parammap){
		SpiderWebsite t = null;
		
		return null;
	}
	
	public SpiderWebsite getWebsite(int id){
		SpiderWebsite t = null;
		try {
			t = this.spiderWebsiteDao.getWebsite(id);
		} catch (BizException e) {
			logger.error("添加website失败！",e);
		}
		return t;
	}
	
	public SpiderWebsite getWebsite(SpiderWebsite t){
		try {
			t = this.spiderWebsiteDao.getWebsite(t);
		} catch (BizException e) {
			logger.error("获取website失败！",e);
		}
		return t;
	}
	
	public List<SpiderWebsite> getAllWebsite(){
		List<SpiderWebsite> t = null;
		try {
			t = this.spiderWebsiteDao.getAllWebsite();
		} catch (BizException e) {
			logger.error("获取website失败！",e);
		}
		return t;
	}
	
	public int addWebsiteConfig(SpiderWebsiteConfig t){
		Integer id = null;
		try {
			id =  this.spiderWebsiteConfigDao.addWebsiteConfig(t);
		} catch (BizException e) {
			logger.error("添加websiteconfig失败！",e);
		}
		return id;
	}
	
	public int addBiz(SpiderBiz biz){
		Integer id = null;
		try {
			id =  this.spiderBizDao.addBiz(biz);
		} catch (BizException e) {
			logger.error("添加biz失败！",e);
		}
		return id;
	}
}
