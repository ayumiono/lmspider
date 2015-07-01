package com.lmdna.spider.apicrawl.tencent;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import us.codecraft.webmagic.utils.LoggerUtil;

import com.lmdna.spider.LoginProxy;
import com.lmdna.spider.exception.AuthorMatchException;
import com.lmdna.spider.exception.ProxyIpException;
import com.lmdna.spider.exception.SpiderLoginException;
import com.lmdna.spider.utils.HttpClientHelper;
import com.lmdna.spider.utils.VerifyCodeFactory;
import com.lmdna.spider.utils.VerifyDialog;

public class TQQLogin implements LoginProxy {
	
	private static final String biz_name = "tqq";
	
	private String account;
	
	private String password;
	
	private HttpClientHelper httpclient;
	
	public TQQLogin(String account,String password,HttpClientHelper httpclient){
		this.account = account;
		this.password = password;
		this.httpclient = httpclient;
	}

	@Override
	public void login() throws Exception {
		try{
			String u1 = "";
			u1 = URLEncoder.encode("http://t.qq.com/", "UTF-8");
			String loginSigUIURL = "http://ui.ptlogin2.qq.com/cgi-bin/login?appid=716027609&style=13&lang=&low_login=1&hide_title_bar=1&hide_close_icon=1&self_regurl=http%3A//reg.t.qq.com/index.php&s_url="
					+ u1 + "&daid=6";
			String login_sig = httpclient.doGet(loginSigUIURL,"utf-8");
			login_sig = StringUtils.substringBetween(login_sig, "login_sig:\"", "\"");
			if(login_sig==null){
				throw new SpiderLoginException("tqq登录时，访问ui.ptlogin2.qq.com时发生错误！");
			}
			String verifyURL = "http://check.ptlogin2.qq.com/check?regmaster=&uin=" + account + "&appid=716027609&js_ver=10060&js_type=1&login_sig=" + login_sig + "&u1=" + u1 + "&r=" + Math.random();
			String loginURL = "http://ptlogin2.qq.com/login?";
			String verifyImageUrl = "http://captcha.qq.com/getimage?aid=716027609";
			String encodeUin = "";
			String curVerifyCode = "";
			int verifyCodeRetry = 3;
			int retryCount = 0;
			String br = "";
			String cap_cd="";
			while (retryCount <= verifyCodeRetry) {
				br = httpclient.doGet(verifyURL,"utf-8");
				String retCode = br.substring(14, 15);
				if (!retCode.equals("1")) {
					curVerifyCode = br.substring(18, 22);
					encodeUin = StringUtils.substringBetween(br, curVerifyCode+"','", "'");
					break;
				}else{
					cap_cd = StringUtils.substringBetween(br, "ptui_checkVC('1','", "'");
					String temp = StringUtils.substringBetween(br,"ptui_checkVC(",");");
					encodeUin = StringUtils.substringBetween(temp.split(",")[2].trim(),"'","'");
//					encodeUin = StringUtils.substringBetween(br, cap_cd+"','", "'");
				}
				String curVerifyImageUrl = verifyImageUrl + "&uin=" + account + "&cap_cd=" + cap_cd;
				curVerifyCode = VerifyCodeFactory.getInstance().getVerifyCode(biz_name, httpclient, curVerifyImageUrl,"");
//				CloseableHttpResponse response = (CloseableHttpResponse) httpclient.doReq(curVerifyImageUrl, "get", null, null);
//				byte[] image = EntityUtils.toByteArray(response.getEntity());
//		        VerifyDialog dialog = new VerifyDialog(image);
//		        dialog.setVisible(true);
//				curVerifyCode = dialog.waitOK();
				if (!StringUtils.isEmpty(curVerifyCode)) {
					break;
				}
				retryCount++;
				if (retryCount >= verifyCodeRetry) {
					break;
				}
			}
			
			TQQLoginJS tqqLoginJS = new TQQLoginJS();
			String passwd = tqqLoginJS.getPassword(encodeUin, password, curVerifyCode);
			String urlEncodeUin = URLEncoder.encode(account, "UTF-8");
			String curLoginURL = loginURL
					+ "u="
					+ urlEncodeUin
					+ "&p="
					+ passwd
					+ "&verifycode="
					+ curVerifyCode
					+ "&aid=716027609&u1="
					+ u1
					+ "&h=1&ptredirect=1&ptlang=2052&daid=6&from_ui=1&dumy=&low_login_enable=1&low_login_hour=720&regmaster=&fp=loginerroralert&action=25-31-1386760113562&mibao_css=&t=1&g=1&js_ver=10060&js_type=1&login_sig="
					+ login_sig + "&pt_rsa=0";
			Map<String,String> headers = new HashMap<String,String>();
			headers.put("Referer", "http://t.qq.com/");
			String responseStr = httpclient.doGet(curLoginURL,"utf-8",headers);
			String retCode2 = StringUtils.substringBetween(responseStr, "ptuiCB('", "'");
//			 ptuiCB('10','0','','0','您输入的帐号不正确，请重试。(3449815910)');
//			 ptuiCB('3','0','','0','您输入的密码有误，请重试。');
//			 ptuiCB('0','0','http://t.qq.com','0','登录成功！');
//			 ptuiCB('4','0','','0','您输入的验证码有误，请重试。');
//			 ptuiCB('64','0','','0','由于您所在的网络环境存在异常，需要再次输入验证码');
//			 ptuiCB('7','0','','0','很遗憾，网络连接出现异常，请您稍后再试。(1490027014)');
			int loginSuccessRetCode = 0;// 登录成功
			int loginFailedRetCode = 3;// 帐号或密码错误
			int userNameErrorRetCode = 10;// 帐号不正确
			int verifyCodeErrorRetCode = 4;// 验证码有误
			int netErrorRetCode = 64;// 网络环境存在异常，需要重新输入验证码
			int netConnErrorRetCode = 7;// 很遗憾，网络连接出现异常，请您稍后再试。(4042260970)
			int userLimitRetCode = 19;// 您的号码已被冻结
			int iRetCode = -1;
			try {
				iRetCode = Integer.valueOf(retCode2);
			} catch (NumberFormatException e) {
				iRetCode = -1;
			}
			if (iRetCode == loginSuccessRetCode) {
				String finalJumpUrl = StringUtils.substringBetween(responseStr, "ptuiCB('0','0','", "'");
				String html = httpclient.doGet(finalJumpUrl, "utf-8");
				if (validUserError(html)) {
					LoggerUtil.debug("tqq", account + " 登录腾讯出现错误！！！！" + html);
					throw new AuthorMatchException(account + " 登录腾讯出现错误");
				}
			}else if (iRetCode == verifyCodeErrorRetCode) {
				LoggerUtil.debug(account + " 验证码错误，重新输入");
				LoggerUtil.debug("tqq", account + " 验证码错误，重新输入");
			} else if (iRetCode == netErrorRetCode) {
				throw new SpiderLoginException("网络环境存在异常");
			} else if (iRetCode == loginFailedRetCode) {
				LoggerUtil.debug("tqq", account + " 登录名或密码错误");
				throw new AuthorMatchException(account + " 登录名或密码错误");
			} else if (iRetCode == userNameErrorRetCode) {
				LoggerUtil.debug("tqq", account + " 请输入正确的用户名");
				throw new AuthorMatchException(account + " 请输入正确的用户名");
			} else if (iRetCode == userLimitRetCode) {
				LoggerUtil.info("tqq", account + " 您的号码已被冻结");
				throw new AuthorMatchException(account + " 您的号码已被冻结");
			} else if (iRetCode == netConnErrorRetCode) {
				LoggerUtil.debug("tqq", account + " 访问腾讯微博出现异常，请稍后再试" + responseStr);
				throw new SpiderLoginException(account + " 访问腾讯微博出现异常，请稍后再试" + responseStr);
			} else {
				LoggerUtil.debug("tqq", account + " 访问腾讯微博出现异常，请稍后再试" + responseStr);
				throw new SpiderLoginException(account + " 访问腾讯微博出现异常，请稍后再试" + responseStr);
			}
		}catch(Exception e){
			if(e instanceof IOException){
				throw new ProxyIpException(e.getMessage(),e);
			}
			throw e;
		}

	}
	
	public boolean validUserError(String html) {
		boolean ret = false;
		// 行为被认为有风险
		if (html.indexOf("mb/images/tipsAccess3.jpg") != -1 && html.indexOf("class=\"shensu") != -1  && ((html.indexOf("我要申诉") != -1 && html.indexOf("MI.complainVerify") != -1) || (html.indexOf("申诉处理中") != -1  ) )) {
			ret = true;
		}
		return ret;
	}
	
	public static void main(String[] args) throws Exception{
		TQQLogin login = new TQQLogin("844736871","ja165774",HttpClientHelper.instance());
		login.login();
	}

}
