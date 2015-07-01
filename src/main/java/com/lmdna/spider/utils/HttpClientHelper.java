package com.lmdna.spider.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import us.codecraft.webmagic.utils.HttpConstant;


/**
 * HTTP请求工具类
 * @author ayumi
 *
 */
public class HttpClientHelper {
	
	private HttpClientConnectionManager cm;
	private static HttpClientHelper instance;
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:32.0) Gecko/20100101 Firefox/32.0";
	private static final String ACCEPT_ENCODING = "gzip, deflate";
	private static final Pattern patternForCharset = Pattern.compile("charset\\s*=\\s*['\"]*([^\\s;'\"]*)");
	
	private CloseableHttpClient httpClient;
	private CookieStore cookieStore = new BasicCookieStore();
	private HttpHost proxy;
	
	public HttpClientHelper(boolean pooling){
		if(pooling){
			cm = new PoolingHttpClientConnectionManager();
			((PoolingHttpClientConnectionManager) cm).setDefaultMaxPerRoute(200);
			((PoolingHttpClientConnectionManager) cm).setMaxTotal(200);
			SocketConfig sconfig = SocketConfig.custom().setSoTimeout(2000).setSoKeepAlive(true).setTcpNoDelay(true).build();
			((PoolingHttpClientConnectionManager) cm).setDefaultSocketConfig(sconfig);
		}else{
			cm = new BasicHttpClientConnectionManager();
		}
	}
	
	
	/**
	 * 工具实例
	 * @return
	 */
	public static HttpClientHelper instance(){
		if(instance == null){
			instance = new HttpClientHelper(true);
		}
		return instance;
	}
	
	private CloseableHttpClient getClient(){
		if (httpClient == null) {
        	synchronized (this) {
                if (httpClient == null) {
                    httpClient = getClient(USER_AGENT,ACCEPT_ENCODING);
                }
            }
        }
        return httpClient;
	}
	
	private CloseableHttpClient getClient(String userAgent,final String acceptEncoding){
		HttpClientBuilder httpClientBuilder = HttpClients.custom().setConnectionManager(cm);
		httpClientBuilder.setUserAgent(userAgent);
		httpClientBuilder.addInterceptorFirst(new HttpRequestInterceptor() {
			@Override
            public void process(
                    final HttpRequest request,
                    final HttpContext context) throws HttpException, IOException {
                if (!request.containsHeader("Accept-Encoding")) {
                    request.addHeader("Accept-Encoding", acceptEncoding);
                }

            }
        });
        SocketConfig socketConfig = SocketConfig.custom().setSoKeepAlive(true).setTcpNoDelay(true).build();
        httpClientBuilder.setDefaultSocketConfig(socketConfig);
        httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(2, true));
        httpClientBuilder.setDefaultCookieStore(cookieStore);
        if(getProxy()!=null){
        	httpClientBuilder.setProxy(getProxy());
        }
        return httpClientBuilder.build();
	}
	
	private HttpResponse doRequest(String url,String method,NameValuePair[] nameValuePair,Map<String,String> headers,HttpHost proxy,int requestTimeOut,int socketTimeout,int connectionTimeout) throws IOException{
		RequestBuilder requestBuilder = selectRequestMethod(method,nameValuePair).setUri(url);
        if (headers != null) {
            for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
                requestBuilder.addHeader(headerEntry.getKey(), headerEntry.getValue());
            }
        }
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
                .setConnectionRequestTimeout(requestTimeOut)
                .setSocketTimeout(socketTimeout)
                .setConnectTimeout(connectionTimeout)
                .setCookieSpec(CookieSpecs.BEST_MATCH);
        if(proxy!=null){
        	requestConfigBuilder.setProxy(proxy);
        }
        requestBuilder.setConfig(requestConfigBuilder.build());
        HttpUriRequest request = requestBuilder.build();
        CloseableHttpClient httpClient = getClient();
        CloseableHttpResponse response = null;
        try{
        	response = httpClient.execute(request);
        	return response;
        }catch(Exception e){
        	throw new IOException(e);
        }
	}
	
	public String getCookie(String key){
		List<Cookie> cookies = this.cookieStore.getCookies();
		for(Cookie cookie : cookies){
			if(cookie.getName().equals(key)){
				return cookie.getValue();
			}
		}
		return null;
	}
	
	
	/**
	 * 注意：调用的目标方法需要把流关闭
	 * @param url
	 * @param headers
	 * @return
	 * @throws IOException
	 */
	public HttpResponse doReq(String url,String method,NameValuePair[] nameValuePair,Map<String,String> headers) throws IOException{
		return doRequest(url, method, nameValuePair, headers, null,3000, 3000, 3000);
	}
	
	public HttpResponse doReq(String url,String method,NameValuePair[] nameValuePair,Map<String,String> headers,HttpHost proxy) throws IOException{
		return doRequest(url, method, nameValuePair, headers, proxy,3000, 3000, 3000);
	}
	
	public String doGet(String url,String charset) throws IOException{
		return getContent(charset,doRequest(url,"get",null,null,proxy,3000,3000,3000));
	}
	
	public String doGet(String url,String charset,int requesttimeout,int sockettimeout,int connectiontimeout) throws IOException{
		return getContent(charset,doRequest(url,"get",null,null,proxy,requesttimeout,sockettimeout,connectiontimeout));
	}
	
	public String doGet(String url,String charset,Map<String,String> headers) throws IOException{
		return getContent(charset,doRequest(url,"get",null,headers,proxy,3000,3000,3000));
	}
	
	public String doGet(String url,String charset,Map<String,String> headers,HttpHost proxy) throws IOException{
		return getContent(charset,doRequest(url,"get",null,headers,proxy,3000,3000,3000));
	}
	
	public String doGet(String url,String charset,Map<String,String> headers,HttpHost proxy,int requesttimeout,int sockettimeout,int connectiontimeout) throws IOException{
		return getContent(charset,doRequest(url,"get",null,headers,proxy,requesttimeout,sockettimeout,connectiontimeout));
	}
	
	public String doGet(String url,String charset,Map<String,String> headers,int requesttimeout,int sockettimeout,int connectiontimeout) throws IOException{
		return getContent(charset,doRequest(url,"get",null,headers,proxy,requesttimeout,sockettimeout,connectiontimeout));
	}
	
	public String doPost(String url,String charset,NameValuePair[] nameValuePair) throws IOException{
		return getContent(charset,doRequest(url,"post",nameValuePair,null,proxy,3000,3000,3000));
	}
	
	public String doPost(String url,String charset,NameValuePair[] nameValuePair,HttpHost proxy) throws IOException{
		return getContent(charset,doRequest(url,"post",nameValuePair,null,proxy,3000,3000,3000));
	}
	
	public String doPost(String url,String charset,NameValuePair[] nameValuePair,Map<String,String> headers) throws IOException{
		return getContent(charset,doRequest(url,"post",nameValuePair,headers,proxy,3000,3000,3000));
	}
	
	public String doPost(String url,String charset,NameValuePair[] nameValuePair,Map<String,String> headers,HttpHost proxy) throws IOException{
		return getContent(charset,doRequest(url,"post",nameValuePair,headers,proxy,3000,3000,3000));
	}
	
	private String getContent(String charset, HttpResponse httpResponse) throws IOException {
        try{
        	if (StringUtils.isEmpty(charset)) {
                byte[] contentBytes = IOUtils.toByteArray(httpResponse.getEntity().getContent());
                String htmlCharset = getHtmlCharset(httpResponse, contentBytes);
                if (htmlCharset != null) {
                    return new String(contentBytes, htmlCharset);
                } else {
                    return new String(contentBytes);
                }
            } else {
                return IOUtils.toString(httpResponse.getEntity().getContent(), charset);
            }
        }finally{
        	if(httpResponse!=null){
        		EntityUtils.consume(httpResponse.getEntity());
        	}
        }
    }
	
	private String getHtmlCharset(HttpResponse httpResponse, byte[] contentBytes) throws IOException {
        String charset;
        // charset
        // 1、encoding in http header Content-Type
        String value = httpResponse.getEntity().getContentType().getValue();
        charset = getCharset(value);
        if (StringUtils.isNotBlank(charset)) {
            return charset;
        }
        // use default charset to decode first time
        Charset defaultCharset = Charset.defaultCharset();
        String content = new String(contentBytes, defaultCharset.name());
        // 2、charset in meta
        if (StringUtils.isNotEmpty(content)) {
            Document document = Jsoup.parse(content);
            Elements links = document.select("meta");
            for (Element link : links) {
                // 2.1、html4.01 <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
                String metaContent = link.attr("content");
                String metaCharset = link.attr("charset");
                if (metaContent.indexOf("charset") != -1) {
                    metaContent = metaContent.substring(metaContent.indexOf("charset"), metaContent.length());
                    charset = metaContent.split("=")[1];
                    break;
                }
                // 2.2、html5 <meta charset="UTF-8" />
                else if (StringUtils.isNotEmpty(metaCharset)) {
                    charset = metaCharset;
                    break;
                }
            }
        }
        // 3、todo use tools as cpdetector for content decode
        return charset;
    }
	
	private RequestBuilder selectRequestMethod(String method,NameValuePair[] nameValuePair){
        if (method == null || method.equalsIgnoreCase(HttpConstant.Method.GET)) {
            //default get
            return RequestBuilder.get();
        } else if (method.equalsIgnoreCase(HttpConstant.Method.POST)) {
            RequestBuilder requestBuilder = RequestBuilder.post();
            if (nameValuePair.length > 0) {
                requestBuilder.addParameters(nameValuePair);
            }
            return requestBuilder;
        } else if (method.equalsIgnoreCase(HttpConstant.Method.HEAD)) {
            return RequestBuilder.head();
        } else if (method.equalsIgnoreCase(HttpConstant.Method.PUT)) {
            return RequestBuilder.put();
        } else if (method.equalsIgnoreCase(HttpConstant.Method.DELETE)) {
            return RequestBuilder.delete();
        } else if (method.equalsIgnoreCase(HttpConstant.Method.TRACE)) {
            return RequestBuilder.trace();
        }
        throw new IllegalArgumentException("Illegal HTTP Method " + method);
	}
	
	private String getCharset(String contentType) {
        Matcher matcher = patternForCharset.matcher(contentType);
        if (matcher.find()) {
            String charset = matcher.group(1);
            if (Charset.isSupported(charset)) {
                return charset;
            }
        }
        return null;
    }
	
	public HttpHost getProxy() {
		return proxy;
	}


	public void setProxy(HttpHost proxy) {
		this.proxy = proxy;
	}


	public static void main(String args[]) throws IOException{
		String url = "http://manager.yundmp.com";
		HttpClientHelper helper = new HttpClientHelper(true);
		helper.setProxy(new HttpHost("115.238.225.26",80));
		
		String content = helper.doGet(url, "");
		content = helper.doGet(url, "");
		System.out.println(content);
//		Html html = new Html(content);
//		List<String> meta_title = html.xpath("//title/text()").all();
//		List<String> meta_description = html.xpath("//meta[@name=\"description\"]/@content").all();
//		FileWriter fw = new FileWriter(new File("E:\\charset"),true);
//		for(String item : meta_title){
//			fw.write(item+"\r\n");
//		}
//		for(String item : meta_description){
//			fw.write(item+"\r\n");
//		}
//		fw.flush();
//		fw.close();
	}
}
