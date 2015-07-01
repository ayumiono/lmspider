package com.lmdna.spider.apicrawl.sina;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import com.lmdna.spider.utils.JSExecute;


public final class TSinaLoginJS extends JSExecute {

	public static final String TSINA_LOGIN_JS = "com/lmdna/spider/apicrawl/sina/tsinaLogin.js";
	public static final String TSINA_PASSWORD_JS = "com/lmdna/spider/apicrawl/sina/tsinaPassword.js";

	public String getSU(String userName){
		String su = "";
		try {
			DefaultResourceLoader drl = new DefaultResourceLoader();
			Resource src1 = drl.getResource(TSINA_LOGIN_JS);

			runJavaScript(TSINA_LOGIN_JS, src1.getInputStream());

			Scriptable scope = getScope();

			Function getSU = (Function) scope.get("getSU", scope);
			
			Object suTmp = getSU.call(Context.getCurrentContext(), scope, getSU, new Object[] { userName });
			
			su = Context.toString(suTmp);
			
		} catch (FileNotFoundException e) {

			//throw new BizException("tsinaLogin.js 不存在");

		} catch (IOException e) {
			//throw new BizException("tsinaLogin.js 读取错误");
		}
		return su;
	}
	
	public String getSP(String rsaPubkey, String servertime, String nonce, String password) {

		String sp = "";
		try {
			DefaultResourceLoader drl = new DefaultResourceLoader();
			Resource src1 = drl.getResource(TSINA_LOGIN_JS);

			runJavaScript(TSINA_LOGIN_JS, src1.getInputStream());

			Scriptable scope = getScope();

			Function getSP = (Function) scope.get("getSP", scope);

			Object spTmp = getSP.call(Context.getCurrentContext(), scope, getSP, new Object[] {rsaPubkey, servertime ,nonce,password});

			sp = Context.toString(spTmp);
		} catch (FileNotFoundException e) {

			//throw new BizException("tsinaLogin.js 不存在");

		} catch (IOException e) {
			//throw new BizException("tsinaLogin.js 读取错误");
		}
		return sp;
	}
	
	public String encrypt(String rsaPubkey, String password){
		String sp = "";
		try {
			DefaultResourceLoader drl = new DefaultResourceLoader();
			Resource src1 = drl.getResource(TSINA_PASSWORD_JS);

			runJavaScript(TSINA_PASSWORD_JS, src1.getInputStream());

			Scriptable scope = getScope();

			Function encrypt = (Function) scope.get("encrypt", scope);

			Object spTmp = encrypt.call(Context.getCurrentContext(), scope, encrypt, new Object[] {rsaPubkey, password});

			sp = Context.toString(spTmp);
		} catch (FileNotFoundException e) {

			//throw new BizException("tsinaPassword.js 不存在");

		} catch (IOException e) {
			//throw new BizException("tsinaPassword.js 读取错误");
		}
		return sp;
	}

	public static void main(String[] args) throws Exception {

		TSinaLoginJS tsinaLoginJS = new TSinaLoginJS();
		String su = tsinaLoginJS.getSU("liuke240@163.com");
		System.out.println(su);
	}
}