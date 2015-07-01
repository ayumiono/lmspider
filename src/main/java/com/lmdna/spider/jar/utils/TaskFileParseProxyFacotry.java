package com.lmdna.spider.jar.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import us.codecraft.webmagic.Request;

import com.lmdna.spider.berkeleydb.BdbUriUniqFilter;
import com.lmdna.spider.berkeleydb.UriUniqFilter;
import com.lmdna.spider.protocol.TaskFileParseProtocol;

/**
 * 任务文件解析代理类
 * @author ayumiono
 */
public class TaskFileParseProxyFacotry {
	
	public static Object create(){
		return create(new DefaultTaskFileParseProtocolImpl(),null);
	}
	public static Object create(UriUniqFilter filter){
		return create(new DefaultTaskFileParseProtocolImpl(),filter);
	}
	public static Object create(TaskFileParseProtocol impl){
		return create(impl,null);
	}
	/**
	 * @param impl 任务解析实现类
	 * @param filter	URL过滤器
	 * @return
	 */
	public static Object create(TaskFileParseProtocol impl,UriUniqFilter filter){
		return Proxy.newProxyInstance(TaskFileParseProtocol.class.getClassLoader(), impl.getClass().getInterfaces(), new DefaultTaskFileParseProxy(impl,filter));
	}
	static class DefaultTaskFileParseProxy implements InvocationHandler{
		
		private TaskFileParseProtocol proxyObj;
		private UriUniqFilter filter;
		
		public DefaultTaskFileParseProxy(TaskFileParseProtocol proxyObj,UriUniqFilter filter){
			this.proxyObj = proxyObj;
			this.filter = filter;
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			List<Request> reqList = (List<Request>) method.invoke(proxyObj, args);
			if(filter!=null){
				Iterator<Request> iterator = reqList.iterator();
				while(iterator.hasNext()){
					Request request = iterator.next();
					String url = request.getUrl();
					if(filter.add(url)){
					}else{
						iterator.remove();
					}
				}
			}
			return reqList;
		}
		
	}
	
	static class DefaultTaskFileParseProtocolImpl implements TaskFileParseProtocol{
		@Override
		public List<Request> parse(InputStream in) throws IOException {
			List<Request> reqList = new ArrayList<Request>();
			FileInputStream fs = (FileInputStream)in;
			BufferedInputStream bis = new BufferedInputStream(fs);
			BufferedReader br = new BufferedReader(new InputStreamReader(bis, "UTF-8"),1024*1024);
			String line = "";
			while((line = br.readLine())!=null){
				if(StringUtils.isNotBlank(line)){
					Request request = new Request(line);
					request.putExtra("fingerPrint", BdbUriUniqFilter.createKey(line));
					reqList.add(request);
				}
			}
			br.close();
			return reqList;
		}
		
	}
}
