package com.lmdna.spider;

import java.util.HashMap;
import java.util.Map;


public class SpiderApplicationContext {
	private static Map<String,Object> _bean = new HashMap<String,Object>();
	public static <T> Class<T> getBean(T clazz){
		return (Class<T>) _bean.get(clazz.getClass().getName());
	}
	public static void addBean(Class clazz, Object o){
		_bean.put(clazz.getName(), o);
	}
}
