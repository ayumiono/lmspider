package com.lmdna.spider.jar;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.utils.LoggerUtil;

import com.alibaba.fastjson.JSONObject;
import com.lmdna.spider.apicrawl.tencent.TencentHttpLoginDemo;
import com.lmdna.spider.downloader.LmdnaStatusfulConnection;

public class TencentIndividualInfoCrawlTask extends Spider {
	
	//重写processRequest方法
	public void processRequest(Request request) {
		Page page = downloader.download(request, this);
		if (page == null) {
			sleep(getSite().getSleepTime());
			onError(request);
			return;
		}
		if (page.isNeedCycleRetry()) {
			extractAndAddRequests(page, true);
			return;
		}
		String uin = (String) page.getRequest().getExtra("uin");
		LmdnaStatusfulConnection conn = (LmdnaStatusfulConnection) page.getConn();
		String account = conn.getAccount().getAccount();
		String skey = conn.getHttphelper().getCookie("skey");
		int g_tk = TencentHttpLoginDemo.getACSRFToken(skey);
		String userProfile_url = "http://base.s11.qzone.qq.com/cgi-bin/user/cgi_userinfo_get_all?uin="
				+ uin
				+ "&vuin="
				+ account
				+ "&fupdate=1&rd="
				+ Math.random()
				+ "&g_tk=" + g_tk;
		Request request2 = new Request(userProfile_url);
		request2.setStatusfulConn(request.getStatusfulConn());
		page = downloader.download(request2, this);
		String home = "";
		int marriage = 0;
		String marriageStr = "";
		String career = "";
		String company = "";
		String companyaddress = "";
		String nickname = "";
		int sex = 0;
		String sexStr = "";
		int age = 0;
		String birthday = "";
		int birthyear = 0;
		String location = "";
		String g_userProfile = page.getRawText();
		if (!StringUtils.isEmpty(g_userProfile)) {
			String data = StringUtils.substringBetween(g_userProfile,"_Callback(", ");");
			Map<String, Object> json = JSONObject.parseObject(data);
			int code = (Integer) json.get("code");
			String message = (String) json.get("message");
			if (code == 0 && "获取成功".equals(message)) {
				Map<String, Object> dataMap = (Map<String, Object>) json.get("data");
				uin = dataMap.get("uin").toString();
				nickname = (String) dataMap.get("nickname");
				sex = (Integer) dataMap.get("sex");
				if (sex == 1) {
					sexStr = "男";
				} else if (sex == 2) {
					sexStr = "女";
				} else {
					sexStr = "";
				}
				age = (Integer) dataMap.get("age");
				birthyear = (Integer) dataMap.get("birthyear");
				birthday = (String) dataMap.get("birthday");
				birthday = birthyear + "-" + birthday;
				String country = (String) dataMap.get("country");
				String province = (String) dataMap.get("province");
				String city = (String) dataMap.get("city");
				String hco = (String) dataMap.get("hco");
				String hp = (String) dataMap.get("hp");
				String hc = (String) dataMap.get("hc");
				home = hco + "@" + hp + "@" + hc;
				location = country + "@" + province + "@" + city;
				marriage = (Integer) dataMap.get("marriage");
				if (marriage == 1) {
					marriageStr = "单身";
				} else if (marriage == 2) {
					marriageStr = "已婚";
				} else {
					marriageStr = "";
				}
				career = (String) dataMap.get("career");
				company = (String) dataMap.get("company");
				companyaddress = (String) dataMap.get("cb");
			} else {
				LoggerUtil.info(message);
			}
		} else {
			LoggerUtil.info("腾讯数据获取失败！");
		}
		page.putField("uin", uin);
		page.putField("nickname", nickname);
		page.putField("sex", sexStr);
		page.putField("age", age);
		page.putField("birthday", birthday);
		page.putField("location", location);
		page.putField("home", home);
		page.putField("marriage", marriageStr);
		page.putField("career", career);
		page.putField("company", company);
		page.putField("companyaddress", companyaddress);
		if (!page.getResultItems().isSkip()) {
			for (Pipeline pipeline : pipelines) {
				pipeline.process(page, this);
			}
		}
		request.putExtra(Request.STATUS_CODE, page.getStatusCode());
		sleep(getSite().getSleepTime());
	}

	@Override
	public boolean validProxyIpSafe(Page page) {
		return true;
	}

	@Override
	public boolean validExpire(Page page) {
		return true;
	}

	@Override
	public boolean validUser(Page page) {
		return true;
	}
	
	@Override
	public boolean validPageContent(Page page) {
		Site site = getSite();
		Request request = page.getRequest();
		String validRule = site.getValidCheck(request.getFieldRuleId() == null ? 0 : request.getFieldRuleId());
		return StringUtils.isEmpty(page.getHtml().xpath(validRule).toString())
				&& StringUtils.isEmpty(page.getHtml().regex(validRule).toString());
	}

	@Override
	public boolean validUserAction(Page page) {
		return true;
	}
}
