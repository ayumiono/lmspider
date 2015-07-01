package com.lmdna.spider.protocol.rpc.utils;

import com.lmdna.spider.dao.model.SpiderBiz;

public class JarSpiderLoadTask extends RemoteCmd{
	private static final long serialVersionUID = 3236841933313004869L;
	private String jarFilePath;//spider初始化jar包
	private String jarFileName;
	private SpiderBiz biz;//spider初始化config
	
	public JarSpiderLoadTask(){
		this.setCmdType(CmdType.REMOTE_CMD_JAR_SPIDER_LOAD);
	}
	public String getJarFilePath() {
		return jarFilePath;
	}
	public void setJarFilePath(String jarFilePath) {
		this.jarFilePath = jarFilePath;
	}
	public String getJarFileName() {
		return jarFileName;
	}
	public void setJarFileName(String jarFileName) {
		this.jarFileName = jarFileName;
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
	public String toString(){
		return "jarFileName:"+this.jarFileName;
	}
}
