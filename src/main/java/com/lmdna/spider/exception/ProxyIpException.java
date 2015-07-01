package com.lmdna.spider.exception;

public class ProxyIpException extends Exception{
	private static final long serialVersionUID = -4596160436441207111L;

	public ProxyIpException(String msg) {
		super(msg);
	}

	public ProxyIpException(String msg, Throwable e) {
		super(msg, e);
	}
}
