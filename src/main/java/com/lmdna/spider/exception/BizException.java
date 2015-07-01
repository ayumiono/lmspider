package com.lmdna.spider.exception;

public class BizException extends Exception{
	public BizException(String msg){
		super(msg);
	}
	public BizException(String msg, Throwable e) {
		super(msg, e);
	}
}
