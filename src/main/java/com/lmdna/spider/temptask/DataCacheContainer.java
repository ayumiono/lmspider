package com.lmdna.spider.temptask;

import java.util.Vector;

public class DataCacheContainer {
	
	private Vector<DomainBean> dataList;
	private static volatile DataCacheContainer instance;
	
	private DataCacheContainer(){
		dataList = new Vector<DomainBean>();
	}
	public static DataCacheContainer getInstance(){
		if(instance == null){
			synchronized (DataCacheContainer.class) {
				if(instance == null){
					instance = new DataCacheContainer();
				}
			}
		}
		return instance;
	}
	public void addData(DomainBean data){
		dataList.add(data);
	}
	public Vector<DomainBean> getData(){
		return this.dataList;
	}
}
