package com.lmdna.spider.apicrawl.sina;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.lmdna.spider.LoginProxy;
import com.lmdna.spider.exception.AuthorMatchException;
import com.lmdna.spider.exception.ProxyIpException;
import com.lmdna.spider.exception.SpiderLoginException;
import com.lmdna.spider.utils.HttpClientHelper;
import com.lmdna.spider.utils.SpiderConstant;

public class TSinaLogin implements LoginProxy {
	
	private String account;
	
	private String password;
	
	private HttpClientHelper httpclient;
	
	private static String preLoginUrl = "http://login.sina.com.cn/sso/prelogin.php?entry=weibo&callback=sinaSSOController.preloginCallBack&rsakt=mod&checkpin=1&client=ssologin.js(v1.4.5)";
	private static String loginUrl = "http://login.sina.com.cn/sso/login.php?client=ssologin.js(v1.4.2)";
	
	public TSinaLogin(String account,String password,HttpClientHelper httpclient){
		this.account = account;
		this.password = password;
		this.httpclient = httpclient;
	}

	@Override
	public void login() throws Exception{
		try{
			TSinaLoginJS tsinaLoginJS = new TSinaLoginJS();
			String su = tsinaLoginJS.getSU(account);
			String su2 = su.replaceAll("=", "%3D");
			String curPrefixLoginUrl = preLoginUrl + "&su=" + su2 + "&_=" + System.currentTimeMillis();
			Map<String,String> headers = new HashMap<String,String>();
			headers.put("Referer", "https://api.weibo.com/oauth2/authorize?client_id=4003670232&redirect_uri=http://wetui.com/&response_type=code");
			String preloginstr = httpclient.doGet(curPrefixLoginUrl, "utf-8", headers);
			boolean needVerifyCode = false;
			
			String showpin = StringUtils.substringBetween(preloginstr, "showpin\":", ",");
			needVerifyCode = null != showpin && showpin.equals("1");
			
			if(needVerifyCode){
				System.out.println("需要输入验证码...");
				return;
			}
			
			String nonce = StringUtils.substringBetween(preloginstr, "nonce\":\"", "\"");
			String pubkey = StringUtils.substringBetween(preloginstr, "pubkey\":\"", "\"");
			String rsakv = StringUtils.substringBetween(preloginstr, "rsakv\":\"", "\"");
			
			String strServerTime = StringUtils.substringBetween(preloginstr, "servertime\":", ",");
			String sp = tsinaLoginJS.getSP(pubkey, strServerTime, nonce, password);
			
			NameValuePair loginparams[] = { 
					new BasicNameValuePair("entry", "openapi"),
					new BasicNameValuePair("nonce", "nonce"),
					new BasicNameValuePair("sp", sp),
					new BasicNameValuePair("rsakv", rsakv),
					new BasicNameValuePair("su", su),
					new BasicNameValuePair("appkey", "6sfoNi"),
					new BasicNameValuePair("useticket", "1"),
					new BasicNameValuePair("vsnf", "1"),
					new BasicNameValuePair("servertime", strServerTime),
					new BasicNameValuePair("pwencode", "rsa2")
			};
			
			String loginstr = httpclient.doPost(loginUrl + "&_=" + System.currentTimeMillis(), "utf-8", loginparams, headers);
			
			String retCode_0 = "retcode=0";// 登录成功
            String retCode_5 = "retcode=5";// 登录名或密码错误
            String retCode_20 = "retcode=20";// 用户名为null
            String retCode_80 = "retcode=80";// 密码为null
            String retCode_101 = "retcode=101";// 用户名正确，密码错误
            String retCode_4057 = "retcode=4057";// 您的账号有异常，请验证身份
            String retCode_4049 = "retcode=4049";// 为了您的帐号安全，请输入验证码
            String retCode_4403 = "retcode=4403";// 抱歉！登录失败，请稍候再试
            String retcode_4069 = "retcode=4069";// http://weibo.com/ajaxlogin.php?framelogin=1&callback=parent.sinaSSOController.feedBackUrlCallBack&sudaref=weibo.com&retcode=4069&reason=帐号太久未登录，请<a href="http://login.sina.com.cn/member/testify/testify.php?entry=weibo&at=AT-MTkxMTgzOTg0MQ==-1363326474-xd-E0AFD6B9F0EDD4F0EF3A7031AFB52157&r=http%3A%2F%2Fweibo.com%2F" target="_blank">验证身份</a>|http://login.sina.com.cn/member/testify/testify.php?entry=weibo&at=AT-MTkxMTgzOTg0MQ==-1363326474-xd-E0AFD6B9F0EDD4F0EF3A7031AFB52157&r=http%3A%2F%2Fweibo.com%2F
            String retcode_4040 = "retcode=4040";// retcode=4040&reason=%B5%C7%C2%BC%B4%CE%CA%FD%B9%FD%B6%E0%A1%A3
            String retcode_2070 = "retcode=2070";// retcode=2070&reason=%CA%E4%C8%EB%B5%C4%D1%E9%D6%A4%C2%EB%B2%BB%D5%FD%C8%B7
			
            if (loginstr.indexOf(retCode_0) != -1)
            {
                String curAjaxLoginUrl = StringUtils.substringBetween(loginstr, "location.replace('", "'");
				if (StringUtils.isEmpty(curAjaxLoginUrl)) {
					curAjaxLoginUrl = StringUtils.substringBetween(loginstr, "location.replace(\"", "\"");
				}
				headers.put("Referer", "http://weibo.com/");
                String content = httpclient.doGet(curAjaxLoginUrl,"utf-8",headers);
               
                
                String result = StringUtils.substringBetween(content, "result\":", ",");
                if(!"true".equals(result)){
                	throw new SpiderLoginException("登录失败");
                }
                content = httpclient.doGet("http://weibo.com/","utf-8");
                if(content.indexOf("抱歉，您的帐号存在异常")!=-1){
                	throw new AuthorMatchException("抱歉，您的帐号存在异常，目前无法进行登录。");
                }
				if(content.indexOf("手机验证")!=-1 || content.indexOf("微博帐号解冻")!=-1){
					throw new AuthorMatchException("你当前使用的帐号异常，请完成手机验证，提升帐号安全。。");
				}
                if(content.indexOf("帐号存在高危风险")!=-1){
                	throw new AuthorMatchException("帐号安全系统检测到您的帐号存在高危风险，请先验证安全信息并修改密码，以保障您帐号安全！");
                }
                if(content.indexOf("您的帐号存在安全风险")!=-1){
                	throw new AuthorMatchException("抱歉，您的帐号存在异常，目前无法进行登录。");
                }
            }
            else if(loginstr.indexOf(retcode_2070) != -1){
            	throw new SpiderLoginException("输入的验证码不正确");
            }
            else if (loginstr.indexOf(retCode_5) != -1)
            {
                throw new SpiderLoginException("登录名或密码错误");
            }
            else if (loginstr.indexOf(retCode_20) != -1)
            {
                throw new SpiderLoginException("请输入正确的用户名");
            }
            else if (loginstr.indexOf(retCode_80) != -1)
            {
                throw new SpiderLoginException("请输入正确的密码");
            }
            else if (loginstr.indexOf(retCode_101) != -1)
            {
                throw new SpiderLoginException("请输入正确的密码");
            }
            else if(loginstr.indexOf(retcode_4069) != -1){
				throw new AuthorMatchException("帐号太久未登录，需验证身份");
			}
            else if(loginstr.indexOf(retCode_4057) != -1){
            	throw new AuthorMatchException("您的账号有异常，请验证身份");
            }
            else if(loginstr.indexOf(retCode_4049) != -1){
            	if(needVerifyCode){
            		throw new AuthorMatchException(SpiderConstant.AUTHORMATCH_VERIFYCODE,"为了您的帐号安全，请输入验证码");
            	}else{
            		throw new AuthorMatchException(SpiderConstant.AUTHORMATCH_VERIFYCODE2,"为了您的帐号安全，请输入验证码2");
            	}
            }
            else if(loginstr.indexOf(retCode_4403) != -1){
            	 throw new SpiderLoginException("抱歉！登录失败，请稍候再试");
            }
            else if(loginstr.indexOf(retcode_4040) != -1){
            	throw new AuthorMatchException(SpiderConstant.AUTHORMATCH_LOGIN_OFTEN,"账号登录次数过多");
            }
            else
            {
                throw new SpiderLoginException("访问新浪微博出现异常，请稍后再试" + loginstr);
            }
            
		}catch(Exception e){
			if(e instanceof IOException){
				throw new ProxyIpException(e.getMessage(), e);
			}
			throw e;
		}
		
	}

}
