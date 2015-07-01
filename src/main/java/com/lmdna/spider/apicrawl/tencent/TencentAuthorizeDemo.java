package com.lmdna.spider.apicrawl.tencent;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.lmdna.spider.utils.HttpClientHelper;
import com.lmdna.spider.utils.SpiderCommonTool;

public class TencentAuthorizeDemo {
	
	private static String loginUrl = "";
	
	private static String authUrl = "	http://openapi.qzone.qq.com/oauth/show?which=Login&display=pc&response_type=code&"
			+ "client_id=#appid#&"
			+ "redirect_uri=#redirect_url#&"
			+ "state=#state#";
	
	private static String xuiloginUrl = "http://xui.ptlogin2.qq.com/cgi-bin/xlogin?"
			+ "appid=716027609&"
			+ "style=23&"
			+ "login_text=%E6%8E%88%E6%9D%83%E5%B9%B6%E7%99%BB%E5%BD%95&"//授权并登录
			+ "hide_title_bar=1&"
			+ "hide_border=1&"
			+ "target=self&"
			+ "s_url=http%3A%2F%2Fopenapi.qzone.qq.com%2Foauth%2Flogin_jump&"
			+ "pt_3rd_aid=#self_appid#&"//app
			+ "pt_feedback_link=#pt_feedback_link#";//http://support.qq.com/write.shtml?fid=780&SSTAG=imbugu.com.appid100274996
	
	private static String verifyUrl = "https://ssl.ptlogin2.qq.com/check?regmaster=&uin=#uin#&appid=716027609&js_ver=10099&js_type=1&login_sig=#login_sig#&u1=http%3A%2F%2Fopenapi.qzone.qq.com%2Foauth%2Flogin_jump&r=" + Math.random();
	
	private static String verifyImgUrl = "http://captcha.qq.com/getimage?"
			+ "uin=#uin#"
			+ "&aid=716027609"
			+ "&cap_cd=#cap_cd#&"
			+ Math.random();
	
	public static void main(String arg[]) throws IOException{
		HttpClientHelper httphelper = HttpClientHelper.instance();
		String account = "844736871";
		String password = "ja165774";
		String redirect_url = "http://www.imbugu.com/";
		String appid="100274996";
		authUrl = authUrl.replace("appid", appid).replace("redirect_url", redirect_url).replace("#state#", String.valueOf(System.currentTimeMillis()));
		String pt_feedback_link = "http://support.qq.com/write.shtml?fid=780&SSTAG="+"imbugu.com"+".appid"+appid;
		try {
			pt_feedback_link = URLEncoder.encode(pt_feedback_link,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		xuiloginUrl = xuiloginUrl.replace("#pt_feedback_link#", pt_feedback_link).replace("#self_appid#", appid);
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("Referer", authUrl);
		String login_sig = httphelper.doGet(xuiloginUrl, "utf-8", headers);
		login_sig = StringUtils.substringBetween(login_sig, "login_sig:\"", "\",clientip:");
		verifyUrl = verifyUrl.replace("#uin#", account).replace("#login_sig#", login_sig);
		headers.put("Referer", URLEncoder.encode(xuiloginUrl, "utf-8"));
		String verifyStr = httphelper.doGet(verifyUrl, "utf-8", headers);
		String checkVCCode = StringUtils.substringBetween(verifyStr, "ptui_checkVC('", "'");
		String cap_cd="";
		if("1".equals(checkVCCode)){
			//需要输入验证码
			cap_cd = StringUtils.substringBetween(verifyStr,"'1','","'");
		}else if("0".equals(checkVCCode)){
			
		}
		verifyImgUrl = verifyImgUrl.replace("#uin#", account).replace("#cap_cd#", cap_cd);
		SpiderCommonTool.downloadVerifyImg("D:/workspace/lmdna/lmdna-spider/src/main/webapp/verifyimages"+System.currentTimeMillis()+".jpg", verifyImgUrl, httphelper);
	}
}
