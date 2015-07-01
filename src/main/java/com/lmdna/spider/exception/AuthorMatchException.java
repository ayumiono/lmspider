package com.lmdna.spider.exception;

public class AuthorMatchException extends Exception {

	private static final long serialVersionUID = 5189838472580965120L;
	
	private int errorCode;

	public AuthorMatchException(String msg) {
		super(msg);
	}

	public AuthorMatchException(String msg, Throwable e) {
		super(msg, e);
	}
	
	public AuthorMatchException(int errorCode, String msg) {
		super(msg);
		this.setErrorCode(errorCode);
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

}
