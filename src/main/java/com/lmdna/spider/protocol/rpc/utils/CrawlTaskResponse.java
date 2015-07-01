package com.lmdna.spider.protocol.rpc.utils;

import java.io.Serializable;

public class CrawlTaskResponse implements Serializable{
	private static final long serialVersionUID = -5241572098327946445L;
	private String taskId;
	private String bizCode;
	
	private String taskFilePath;
	private String taskFileName;
	private int start;
	private int end;
	
	private String msg;
}
