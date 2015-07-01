package com.lmdna.spider.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;

import com.lmdna.spider.protocol.rpc.RPCProtocolFactory;
import com.lmdna.spider.protocol.rpc.VerifyImgProtocol;


public class VerifyCodeFactory {

	private VerifyImgProtocol verifyImgProtocol;
	
	private static VerifyCodeFactory instance;
	
	public static synchronized VerifyCodeFactory getInstance(){
		if(null == instance){
			instance = new VerifyCodeFactory();
		}
		return instance;
	}
	
	private VerifyCodeFactory(){
		verifyImgProtocol = RPCProtocolFactory.get(VerifyImgProtocol.class);
	}
	
	public String getVerifyCode(String from,HttpClientHelper httpclient,String verifyCodeUrl, String referer){
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("Referer", referer);
		HttpResponse response;
		try {
			response = httpclient.doReq(verifyCodeUrl, "get", null, headers);
		} catch (IOException e) {
			return "";
		}
		InputStream data;
		try {
			data = response.getEntity().getContent();
		} catch (IllegalStateException e) {
			return "";
		} catch (IOException e) {
			return "";
		}
		return verifyImgProtocol.getVerifyCode(SpiderCommonTool.getLocalIP(), from, data);
	}
}
