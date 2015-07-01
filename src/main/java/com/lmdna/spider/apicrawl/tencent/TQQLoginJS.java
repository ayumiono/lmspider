package com.lmdna.spider.apicrawl.tencent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import com.lmdna.spider.utils.JSExecute;


public final class TQQLoginJS extends JSExecute {

	public static final String TQQ_LOGIN_JS = "com/lmdna/spider/apicrawl/tencent/tqqLogin.js";

	public String getPassword(String uin, String oriPassword, String verifyCode) {

		String password = "";
		try {
			DefaultResourceLoader drl = new DefaultResourceLoader();
			Resource src1 = drl.getResource(TQQ_LOGIN_JS);

			runJavaScript(TQQ_LOGIN_JS, src1.getInputStream());

			Scriptable scope = getScope();

			Function md5 = (Function) scope.get("md5", scope);

			Function hexchar2bin = (Function) scope.get("hexchar2bin", scope);

			Function evalString = (Function) scope.get("evalString", scope);

			Object uidString = evalString.call(Context.getCurrentContext(), scope, evalString, new Object[] { uin });

			Object passwordTmp = md5.call(Context.getCurrentContext(), scope, md5, new Object[] { oriPassword });

			passwordTmp = hexchar2bin.call(Context.getCurrentContext(), scope, hexchar2bin, new Object[] { passwordTmp });

			password = Context.toString(passwordTmp);

			passwordTmp = md5.call(Context.getCurrentContext(), scope, md5, new Object[] { password + uidString });

			password = Context.toString(passwordTmp);

			passwordTmp = md5.call(Context.getCurrentContext(), scope, md5, new Object[] { password + verifyCode.toUpperCase() });

			password = Context.toString(passwordTmp);
		} catch (FileNotFoundException e) {
			//throw new BizException("tqqLogin.js不存在");
		} catch (IOException e) {
			//throw new BizException("tqqLogin.js读取错误");
		}
		return password;
	}

	public static void main(String[] args) throws Exception {

	}

	public static final String HEXSTRING = "0123456789ABCDEF";

	public static String md5(String originalText) throws Exception {
		byte buf[] = originalText.getBytes("ISO-8859-1");
		StringBuffer hexString = new StringBuffer();
		String result = "";
		String digit = "";

		try {
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(buf);

			byte[] digest = algorithm.digest();

			for (int i = 0; i < digest.length; i++) {
				digit = Integer.toHexString(0xFF & digest[i]);

				if (digit.length() == 1) {
					digit = "0" + digit;
				}

				hexString.append(digit);
			}
			result = hexString.toString();

			// result = new String(digest,"GBK");

		} catch (Exception ex) {
			result = "";
		}

		return result.toUpperCase();
	}

	public static String hexchar2bin(String md5str) throws UnsupportedEncodingException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(md5str.length() / 2);

		for (int i = 0; i < md5str.length(); i = i + 2) {
			System.out.println(md5str.charAt(i + 1));
			baos.write((HEXSTRING.indexOf(md5str.charAt(i)) << 4 | HEXSTRING.indexOf(md5str.charAt(i + 1))));
		}

		return new String(baos.toByteArray(), "ISO-8859-1");
	}

	/**
	 * 
	 * @param qq
	 *            http://check.ptlogin2.qq.com/check?uin={0}&appid=15000101&r={1
	 *            } 返回的第三个值
	 * @param password
	 *            QQ密码
	 * @param verifycode
	 *            验证码
	 * @return 加密后的密码
	 * @throws UnsupportedEncodingException
	 * @throws Exception
	 */
	public static String GetPassword(String qq, String password, String verifycode) throws Exception {

		// http://ptlogin2.qq.com/login?ptlang=2052&u=2409630976&p=143FFC34B9E0683032BE06C29792F48C&verifycode=pyhe&low_login_enable=1&low_login_hour=720&aid=46000101&u1=http%3A%2F%2Ft.qq.com&ptredirect=1&h=1&from_ui=1&dumy=&fp=loginerroralert&action=7-25-2477980&g=1&t=2&dummy=
		String P = hexchar2bin(md5(password));
		String U = md5(P + hexchar2bin(qq.replace("\\x", "").toUpperCase()));// hexchar2bin(qq.replace("\\x",
		// "").toUpperCase()));
		String V = md5(U + verifycode.toUpperCase());

		return V;
	}
}