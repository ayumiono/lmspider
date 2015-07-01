package com.lmdna.spider.protocol.rpc.utils;

import com.lmdna.spider.dao.model.SpiderBiz;

public class CommonSpiderLoadTask extends RemoteCmd{
	private static final long serialVersionUID = 8291305140783284214L;
	private SpiderBiz biz;//spider初始化config
	
	public CommonSpiderLoadTask(){
		this.setCmdType(CmdType.REMOTE_CMD_COMMON_SPIDER_LOAD);
	}
	
	public SpiderBiz getBiz() {
		return biz;
	}
	public void setBiz(SpiderBiz biz) {
		this.biz = biz;
	}
	public String getBizCode(){
		return this.biz.getBizCode();
	}
}
