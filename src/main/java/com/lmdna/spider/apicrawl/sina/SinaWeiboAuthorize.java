package com.lmdna.spider.apicrawl.sina;


import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import us.codecraft.webmagic.selector.Html;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lmdna.spider.apicrawl.sina.TSinaLoginJS;
import com.lmdna.spider.utils.HttpClientHelper;

public class SinaWeiboAuthorize{
	
	private static String preLoginUrl = "http://login.sina.com.cn/sso/prelogin.php?entry=weibo&callback=sinaSSOController.preloginCallBack&rsakt=mod&checkpin=1&client=ssologin.js(v1.4.5)";
	//example:https://login.sina.com.cn/sso/prelogin.php?entry=openapi&callback=sinaSSOController.preloginCallBack&su=YXl1bWlvbm8lNDAxNjMuY29t&rsakt=mod&checkpin=1&client=ssologin.js(v1.4.15)&_=1414559952657
	private static String loginUrl = "http://login.sina.com.cn/sso/login.php?client=ssologin.js(v1.4.2)";
	//example:https://login.sina.com.cn/sso/login.php?client=ssologin.js(v1.4.15)&_=1414559957228
	private static String authorizeUrl = "https://api.weibo.com/oauth2/authorize";
	
	public static void main(String arg[]) throws IOException{
		
		HttpClientHelper httphelper = HttpClientHelper.instance();
		String account="koo2345@163.com";
		String password="wwee08";
		TSinaLoginJS tsinaLoginJS = new TSinaLoginJS();
		String su = tsinaLoginJS.getSU(account);
		String su2 = su.replaceAll("=", "%3D");
		String curPrefixLoginUrl = preLoginUrl + "&su=" + su2 + "&_=" + System.currentTimeMillis();
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("Referer", "https://api.weibo.com/oauth2/authorize?client_id=3679272428&redirect_uri=http://www.imbugu.com/&response_type=code");
		String preloginstr = httphelper.doGet(curPrefixLoginUrl, "utf-8", headers);
		boolean needVerifyCode = false;
		
		//String pcid = StringUtils.substringBetween(preloginstr, "pcid\":\"", "\"");
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
				new BasicNameValuePair("appkey", "5VCUbi"),
				new BasicNameValuePair("useticket", "1"),
				new BasicNameValuePair("vsnf", "1"),
				new BasicNameValuePair("servertime", strServerTime),
				new BasicNameValuePair("pwencode", "rsa2")
		};
		
		String loginstr = httphelper.doPost(loginUrl + "&_=" + System.currentTimeMillis(), "utf-8", loginparams, headers);
		
		String returnJson = StringUtils.substringBetween(loginstr, "setCrossDomainUrlList(", ")");
		Map<?,?> returnMap = JSONObject.parseObject(returnJson);
		JSONArray array = (JSONArray) returnMap.get("arrURL");
		Object[] urlArr = array.toArray();
		String nessaryStepUrl = (String) urlArr[0];
		String ticket = StringUtils.substringBetween(URLDecoder.decode(nessaryStepUrl,"utf-8"),"ticket=","&ssosavestate=");
		
		String result = httphelper.doGet(nessaryStepUrl, "utf-8");
		
		String uid = StringUtils.substringBetween(result,"\"uniqueid\":\"","\",\"userid\"");
		
		
		NameValuePair verifyparams[] = {
				new BasicNameValuePair("action", "login"),
				new BasicNameValuePair("appkey62", "5VCUbi"),
				new BasicNameValuePair("client_id", "3679272428"),
				new BasicNameValuePair("redirect_uri", "http://www.imbugu.com/"),
				new BasicNameValuePair("response_type", "code"),
				new BasicNameValuePair("uid", uid),
				new BasicNameValuePair("ticket", ticket),
				new BasicNameValuePair("userId",account),
				new BasicNameValuePair("switchLogin","0"),
				new BasicNameValuePair("regCallback","https%3A%2F%2Fapi.weibo.com%2F2%2Foauth2%2Fauthorize%3Fclient_id%3D3679272428%26response_type%3Dcode%26display%3Ddefault%26redirect_uri%3Dhttp%3A%2F%2Fwww.imbugu.com%2F%26from%3D%26with_cookie%3D")
		};
		
		String verifystr = httphelper.doPost(authorizeUrl, "utf-8", verifyparams,headers);
		
		Html html = new Html(verifystr);
		String verifyToken = html.xpath("//input[@name='verifyToken']/@value").toString();
		
		NameValuePair authparams[] = {
				new BasicNameValuePair("action", "authorize"),
				new BasicNameValuePair("appkey62", "6sfoNi"),
				new BasicNameValuePair("client_id", "3679272428"),
				new BasicNameValuePair("redirect_uri", "http://www.imbugu.com/"),
				new BasicNameValuePair("response_type", "code"),
				new BasicNameValuePair("uid", uid),
				new BasicNameValuePair("verifyToken", verifyToken),
				new BasicNameValuePair("url", "	https://api.weibo.com/oauth2/authorize?client_id=3679272428&redirect_uri=http://www.imbugu.com/&response_type=code"),
				new BasicNameValuePair("regCallback","https%3A%2F%2Fapi.weibo.com%2F2%2Foauth2%2Fauthorize%3Fclient_id%3D3679272428%26response_type%3Dcode%26display%3Ddefault%26redirect_uri%3Dhttp%3A%2F%2Fwww.imbugu.com%2F%26from%3D%26with_cookie%3D")
		};
		headers.put("Referer", "https://api.weibo.com/oauth2/authorize");
		HttpResponse response = httphelper.doReq(authorizeUrl, "post",authparams,headers);
		Header[] location = response.getHeaders("Location");
		EntityUtils.consume(response.getEntity());
		String code = StringUtils.substringAfter(location[0].toString(), "code=");
		System.out.println(code);
		//清除access_token
//		NameValuePair params[] = { 
//				new BasicNameValuePair("access_token", "2.002PxzHE1Wzw3E6374c1f027rBBy6D"),
//		};
//		System.out.println(httphelper.doPost("https://api.weibo.com/oauth2/revokeoauth2", "utf-8", params));
	}
}