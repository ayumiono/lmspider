package com.lmdna.spider.protocol.rpc.utils;

public class CrawlResultCollect extends RemoteCmd {
	private String filePath;
	public CrawlResultCollect(){
		this.setCmdType(CmdType.REMOTE_CMD_CRAWL_RESULT_UPLOAD);
	}
}
