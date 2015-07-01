package com.lmdna.spider.exception;

public class SpiderLoginException extends Exception {
	
	private static final long serialVersionUID = -4951327525232200772L;

	public SpiderLoginException(String message){
		super(message);
	}
	
	public SpiderLoginException(String message, Throwable cause){
		super(message,cause);
	}
}
