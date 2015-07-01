package com.lmdna.spider.temptask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.lmdna.spider.apicrawl.tencent.TQQLoginJS;

import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.downloader.HttpClientGenerator;


public class Test {
	public static void main(String arg[]) throws ClientProtocolException, IOException{
		Site site = new Site();
		site.setUserAgent("Mozilla/5.0 (Windows NT 6.1; rv:30.0) Gecko/20100101 Firefox/30.0");
		HttpClientGenerator generator = new HttpClientGenerator();
		CloseableHttpClient httpClient = generator.getClient(site);

		String account="844736871";
		String password = "ja165774";

		String u1 = URLEncoder.encode("http://t.qq.com/", "UTF-8");

		//http://xui.ptlogin2.qq.com/cgi-bin/xlogin?appid=716027609&style=23&login_text=%E6%8E%88%E6%9D%83%E5%B9%B6%E7%99%BB%E5%BD%95&hide_title_bar=1&hide_border=1&target=self&s_url=http%3A%2F%2Fopenapi.qzone.qq.com%2Foauth%2Flogin_jump&pt_3rd_aid=100274996&pt_feedback_link=http%3A%2F%2Fsupport.qq.com%2Fwrite.shtml%3Ffid%3D780%26SSTAG%3Dimbugu.com.appid100274996
		String loginSigUIURL = "http://ui.ptlogin2.qq.com/cgi-bin/login?appid=716027609&style=13&lang=&low_login=1&hide_title_bar=1&hide_close_icon=1&self_regurl=http%3A//reg.t.qq.com/index.php&s_url="
				+ u1 + "&daid=6";
		
		HttpGet loginsigReq = new HttpGet(loginSigUIURL);

		CloseableHttpResponse response = httpClient.execute(loginsigReq);
		
		String login_sig = EntityUtils.toString(response.getEntity());
		
		login_sig = StringUtils.substringBetween(login_sig, "login_sig:\"", "\"");

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
			HttpGet verifycodeGet = new HttpGet(verifyURL);
			response = httpClient.execute(verifycodeGet);
			br = EntityUtils.toString(response.getEntity());
			String retCode = br.substring(14, 15);
			if (!retCode.equals("1")) {
				curVerifyCode = br.substring(18, 22);
				break;
			}else{
				cap_cd = StringUtils.substringBetween(br, "ptui_checkVC('1','", "'");
			}
			String curVerifyImageUrl = verifyImageUrl + "&uin=" + account + "&cap_cd=" + cap_cd;
			HttpGet verifyimageGet = new HttpGet(curVerifyImageUrl);
			response = httpClient.execute(verifyimageGet);
            byte[] image = EntityUtils.toByteArray(response.getEntity());
            File file = new File("d:\\verifyimage.jpg");
            FileOutputStream fs;
            fs = new FileOutputStream(file);
            fs.write(image);
            fs.flush();
            fs.close();
            BufferedReader br1 = new BufferedReader(new InputStreamReader(System.in));
            curVerifyCode = String.valueOf(br1.readLine().toString());
            file.delete();
			if (!StringUtils.isEmpty(curVerifyCode)) {
				break;
			}
			retryCount++;
			if (retryCount >= verifyCodeRetry) {
				break;
			}
		}
		System.out.println(br);
//		BufferedReader br1 = new BufferedReader(new InputStreamReader(System.in));
//		encodeUin = String.valueOf(br1.readLine().toString());
		br = StringUtils.substringBetween(br, "ptui_checkVC(", ")");
		String[] arr = br.split(",");
		encodeUin = arr[2];
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
		HttpGet curloginGet = new HttpGet(curLoginURL);
		curloginGet.addHeader("Referer", "http://t.qq.com/");
		response = httpClient.execute(curloginGet);
		String responseStr = EntityUtils.toString(response.getEntity());
		String retCode2 = StringUtils.substringBetween(responseStr, "ptuiCB('", "'");
//		 ptuiCB('10','0','','0','您输入的帐号不正确，请重试。(3449815910)');
//		 ptuiCB('3','0','','0','您输入的密码有误，请重试。');
//		 ptuiCB('0','0','http://t.qq.com','0','登录成功！');
//		 ptuiCB('4','0','','0','您输入的验证码有误，请重试。');
//		 ptuiCB('64','0','','0','由于您所在的网络环境存在异常，需要再次输入验证码');
//		 ptuiCB('7','0','','0','很遗憾，网络连接出现异常，请您稍后再试。(1490027014)');
		int loginSuccessRetCode = 0;// 登录成功
//		int loginFailedRetCode = 3;// 帐号或密码错误
//		int userNameErrorRetCode = 10;// 帐号不正确
//		int verifyCodeErrorRetCode = 4;// 验证码有误
//		int netErrorRetCode = 64;// 网络环境存在异常，需要重新输入验证码
//		int netConnErrorRetCode = 7;// 很遗憾，网络连接出现异常，请您稍后再试。(4042260970)
//		int userLimitRetCode = 19;// 您的号码已被冻结

		int iRetCode = -1;
		try {
			iRetCode = Integer.valueOf(retCode2);
		} catch (NumberFormatException e) {
			iRetCode = -1;
		}
		String html = "";
		if (iRetCode == loginSuccessRetCode) {
			String finalJumpUrl = StringUtils.substringBetween(responseStr, "ptuiCB('0','0','", "'");
//			HttpGet finalJumpGet = new HttpGet(finalJumpUrl);
//			response = httpClient.execute(finalJumpGet);
			//ownerProfileSummary
			HttpGet qzone = new HttpGet("http://user.qzone.qq.com/47909772/main");
			response = httpClient.execute(qzone);
			html = EntityUtils.toString(response.getEntity());
			System.out.println(StringUtils.substringBetween(html.replaceAll("([\\r\\t\\n]+)", ""), "ownerProfileSummary=", ",g_isOFP="));
			qzone = new HttpGet("http://user.qzone.qq.com/11558115/main");
			response = httpClient.execute(qzone);
			html = EntityUtils.toString(response.getEntity());
			System.out.println(StringUtils.substringBetween(html.replaceAll("([\\r\\t\\n]+)", ""), "ownerProfileSummary=", ",g_isOFP="));
		}
	}
}
