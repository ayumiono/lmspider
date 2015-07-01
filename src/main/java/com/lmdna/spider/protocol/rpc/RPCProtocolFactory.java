package com.lmdna.spider.protocol.rpc;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import com.caucho.hessian.client.HessianProxyFactory;

public class RPCProtocolFactory {
	
	private static Map<String,Object> _protocols = new HashMap<String,Object>();
	private static HessianProxyFactory _factory = new HessianProxyFactory();
	
	public static synchronized Object getOrCreate(Class<?> api, String urlName) throws MalformedURLException{
		Object protocol =  _protocols.get(api.getName());
		if(protocol == null){
			protocol = create(api,urlName);
		}
		return protocol;
	}
	
	public static synchronized Object create(Class<?> api, String urlName) throws MalformedURLException{
		Object protocol =  _factory.create(api, urlName);
		_protocols.put(api.getName(), protocol);
		return protocol;
	}
	
	public static synchronized <T> T get(Class<T> api){
		return (T) _protocols.get(api.getName());
	}
}
