package com.lmdna.spider.temptask;

import java.util.ArrayList;
import java.util.List;

public class DomainBean {
	private List<String> titleList = new ArrayList<String>();
	private List<Object> dataList = new ArrayList<Object>();
	public List<String> getTitleList() {
		return titleList;
	}
	public void setTitleList(List<String> titleList) {
		this.titleList = titleList;
	}
	public List<Object> getDataList() {
		return dataList;
	}
	public void setDataList(List<Object> dataList) {
		this.dataList = dataList;
	}
	
}
