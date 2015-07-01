package com.lmdna.spider.node.master;

import java.io.Serializable;

import com.lmdna.spider.dao.model.SpiderBiz;

public class SpiderBizInOperation implements Serializable{
	
	private static final long serialVersionUID = -1190294941286811981L;
	public static final String COMMON = "common";
	public static final String JAR = "jar";
	
	private SpiderBiz spiderConfig;
	private String jarPath;
	private String jarName;
	private String type;
	
	public SpiderBizInOperation(SpiderBiz spiderConfig){
		this.spiderConfig = spiderConfig;
		this.type = COMMON;
	}
	
	public SpiderBizInOperation(SpiderBiz spiderConfig,String jarPath,String jarName){
		this.spiderConfig = spiderConfig;
		this.jarPath = jarPath;
		this.jarName = jarName;
		this.type = JAR;
	}
	
	public SpiderBiz getSpiderConfig() {
		return spiderConfig;
	}
	public String getJarPath() {
		return jarPath;
	}
	public String getJarName() {
		return jarName;
	}

	public String getBizCode() {
		return spiderConfig.getBizCode();
	}
	public boolean isCommon(){
		if(type.equals(COMMON)){
			return true;
		}
		return false;
	}
	public boolean isJar(){
		if(type.equals(JAR)){
			return true;
		}
		return false;
	}
}
