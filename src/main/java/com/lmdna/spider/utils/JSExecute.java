package com.lmdna.spider.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class JSExecute {
	protected Context cx;
	protected Scriptable scope;

	public JSExecute() {
		this.cx = Context.enter();
		this.scope = cx.initStandardObjects();
	}

	public Object runJavaScript(String filename) {
		String jsContent = this.getJsContent(filename);
		Object result = cx.evaluateString(scope, jsContent, filename, 1, null);
		return result;
	}

	public Object runJavaScript(String filename, InputStream is) {
		String jsContent = this.getJsContent(is);
		Object result = cx.evaluateString(scope, jsContent, filename, 1, null);
		return result;
	}

	public String getJsContent(InputStream is) {
		BufferedReader buf1 = new BufferedReader(new InputStreamReader(is));

		LineNumberReader reader1;
		try {
			reader1 = new LineNumberReader(buf1);
			String s = null;
			StringBuffer sb = new StringBuffer();
			while ((s = reader1.readLine()) != null) {
				sb.append(s).append("\n");
			}
			return sb.toString();
		} catch (Exception e) {
			return null;
		}
	}

	private String getJsContent(String filename) {
		LineNumberReader reader;
		try {
			reader = new LineNumberReader(new FileReader(filename));
			String s = null;
			StringBuffer sb = new StringBuffer();
			while ((s = reader.readLine()) != null) {
				sb.append(s).append("\n");
			}
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Scriptable getScope() {
		return scope;
	}
}
