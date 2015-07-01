package com.lmdna.spider.downloader;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.selector.PlainText;
import us.codecraft.webmagic.utils.LoggerUtil;
import us.codecraft.webmagic.utils.UrlUtils;

import com.google.common.collect.Sets;

public class StatusFulDownloader extends AbstractStatusfulDownloader {
	
	private static String logName = "StatusFulDownloader";
	
	@Override
	public Page download(Request request, Task task) {
		Site site = null;
        if (task != null) {
            site = task.getSite();
        }
        Set<Integer> acceptStatCode;
        String charset = null;
        Map<String, String> headers = null;
        if (site != null) {
            acceptStatCode = site.getAcceptStatCode();
            charset = site.getCharset();
            headers = site.getHeaders();
            //用request中的headers覆盖site中的headers
            Map<String,String> independentheaders = request.getHeaders();
            if(independentheaders!=null){
            	for(Entry<String,String> entry : independentheaders.entrySet()){
            		headers.put(entry.getKey(), entry.getValue());
            	}
            }
        } else {
            acceptStatCode = Sets.newHashSet(200);
            headers = request.getHeaders();
        }
        LoggerUtil.info(logName,"downloading page {}", new Object[]{request.getUrl()});
        CloseableHttpResponse httpResponse = null;
        int statusCode=0;
        LmdnaStatusfulConnection conn = null;
        if(request.getStatusfulConn()==null){
        	conn = (LmdnaStatusfulConnection) site.getStatusfulConnFromPool();
        	request.setStatusfulConn(conn);//当前request持有该连接,在一个processPage周期内要保证request始终持有同一个conn
        }else{
        	conn = (LmdnaStatusfulConnection) request.getStatusfulConn();
        }
        try {
        	if (site.getHttpProxyPool() != null && site.getHttpProxyPool().isEnable()) {
            	if(request.getProxy() == null){
            		Proxy proxy = site.getHttpProxyFromPool();
        			//细化到每个request级别,更好的控制代理IP资源
        			request.setProxy(proxy);
            	}
    		}
            httpResponse = (CloseableHttpResponse) conn.doReq(request.getUrl(),headers,request.getProxy());
            statusCode = httpResponse.getStatusLine().getStatusCode();
            request.setStatusCode(statusCode);
            if (statusAccept(acceptStatCode, statusCode)) {
                Page page = handleResponse(request, charset, httpResponse, conn);
                //用户登录状态过期检测
                if(!task.validExpire(page)){
                	request.setStatusCode(Proxy.SUCCESS);
                	request.setStatusfulConnCode(LmdnaStatusfulConnection.AUTH_EXPIRE);
                	return addToCycleRetry(request, site);
                }
                //用户有效性检测
                if(!task.validUser(page)){
                	request.setStatusCode(Proxy.SUCCESS);
                	request.setStatusfulConnCode(LmdnaStatusfulConnection.INVALID_ACCOUNT);
                	return addToCycleRetry(request, site);
                }
                //用户行为异常检测
                if(!task.validUserAction(page)){
                	request.setStatusCode(Proxy.SUCCESS);
                	request.setStatusfulConnCode(LmdnaStatusfulConnection.ACCOUNT_TOO_OFFTEN);
                	return addToCycleRetry(request, site);
                }
                //ip行为异常检测
                if(!task.validProxyIpSafe(page)){
                	request.setStatusCode(Proxy.ERROR_DEFAULT);
                	request.setStatusfulConnCode(LmdnaStatusfulConnection.SUCCESS);
                	return addToCycleRetry(request, site);
                }
                String validRule = site.getValidCheck(request.getFieldRuleId() == null ? 0 :request.getFieldRuleId());
                if(StringUtils.isNotEmpty(validRule)){
                	if(task.validPageContent(page)){
                    	request.setStatusCode(Proxy.ERROR_DEFAULT);//定义成10009错误（代理IP级别错误）
                    	request.setStatusfulConnCode(LmdnaStatusfulConnection.SUCCESS);
                    	return addToCycleRetry(request, site);
                    }
                }
                onSuccess(request);
                request.setStatusfulConnCode(LmdnaStatusfulConnection.SUCCESS);
                return page;	
            } else {
            	//500(server error),403(ip forbidden)--->(代理IP级错误)
            	if(statusCode == Proxy.ERROR_403 || statusCode == Proxy.ERROR_500){
            		request.setStatusfulConnCode(LmdnaStatusfulConnection.SUCCESS);
                	return addToCycleRetry(request, site);
                }
            	//404(page not found)
                LoggerUtil.warn(logName,"code error " + statusCode + "\t" + request.getUrl());
                return null;
            }
        } catch(HttpHostConnectException e){
        	LoggerUtil.warn(logName,"download page "+request.getUrl()+" error! Proxy:"+request.getProxy().getHttpHost().toString(),e);
        	//由于是代理IP本身的原因，请求要重新放回，更换代理IP再试
        	request.setStatusfulConnCode(LmdnaStatusfulConnection.SUCCESS);
        	request.putExtra(Request.STATUS_CODE, Proxy.ERROR_Proxy);
        	return addToCycleRetry(request, site);
        } catch(ConnectTimeoutException e){
        	LoggerUtil.warn(logName,"download page "+request.getUrl()+" error! Proxy:"+request.getProxy().getHttpHost().toString(),e);
        	//由于是代理IP本身的原因，请求要重新放回，更换代理IP再试
        	request.setStatusfulConnCode(LmdnaStatusfulConnection.SUCCESS);
        	request.putExtra(Request.STATUS_CODE, Proxy.ERROR_PROXY_TIME_OUT);
        	return addToCycleRetry(request, site);
        } catch(ClientProtocolException e){
        	LoggerUtil.warn(logName,"download page "+request.getUrl()+" error! Proxy:"+request.getProxy().getHttpHost().toString(),e);
        	request.setStatusfulConnCode(LmdnaStatusfulConnection.SUCCESS);
        	request.putExtra(Request.STATUS_CODE, Request.ERROR_ILLEGAL_REQUEST_URI);
            onError(request);
            return null;
        }catch(SocketTimeoutException e){
        	LoggerUtil.warn(logName,"download page "+request.getUrl()+" error! Proxy:"+request.getProxy().getHttpHost().toString(),e);
        	request.setStatusfulConnCode(LmdnaStatusfulConnection.SUCCESS);
        	request.putExtra(Request.STATUS_CODE, Proxy.ERROR_SOCKET_READ_TIME_OUT);
        	//代理IP速度太慢，导致读response超时
        	return addToCycleRetry(request, site);
        }catch (IOException e) {
        	LoggerUtil.warn(logName,"download page "+request.getUrl()+" error! Proxy:"+request.getProxy().getHttpHost().toString(),e);
        	request.setStatusfulConnCode(LmdnaStatusfulConnection.SUCCESS);
        	request.putExtra(Request.STATUS_CODE, Proxy.ERROR_DEFAULT);
            onError(request);
            return addToCycleRetry(request, site);
        }finally {
            try {
                if (httpResponse != null) {
                    EntityUtils.consume(httpResponse.getEntity());
                }
            } catch (IOException e) {
            	LoggerUtil.warn(logName,"close response fail", e);
            }
        }
	}
	
	protected boolean statusAccept(Set<Integer> acceptStatCode, int statusCode) {
        return acceptStatCode.contains(statusCode);
    }
	
	protected Page handleResponse(Request request, String charset, HttpResponse httpResponse, LmdnaStatusfulConnection conn) throws IOException {
        String content = getContent(charset, httpResponse);
        Page page = new Page();
        //将从上个request请求中继承下来的fields保存到page中
        Map<String,Object> inheritFields = request.getInheritFields();
        if(inheritFields!=null){
        	for(Entry<String,Object> entry : inheritFields.entrySet()){
            	page.putField(entry.getKey(), entry.getValue());
            }
        }
        page.setRawText(content);
        page.setUrl(new PlainText(request.getUrl()));
        page.setRequest(request);
        page.setStatusCode(httpResponse.getStatusLine().getStatusCode());
        page.setConn(conn);
        //将cookie保存到page中
        return page;
    }
	
	protected String getContent(String charset, HttpResponse httpResponse) throws IOException {
        if (StringUtils.isEmpty(charset)) {
            byte[] contentBytes = IOUtils.toByteArray(httpResponse.getEntity().getContent());
            String htmlCharset = getHtmlCharset(httpResponse, contentBytes);
            if (htmlCharset != null) {
                return new String(contentBytes, htmlCharset);
            } else {
                LoggerUtil.warn(logName,"Charset autodetect failed, use {} as charset. Please specify charset in Site.setCharset()", new Object[]{Charset.defaultCharset()});
                return new String(contentBytes);
            }
        } else {
            return IOUtils.toString(httpResponse.getEntity().getContent(), charset);
        }
    }
	
	protected String getHtmlCharset(HttpResponse httpResponse, byte[] contentBytes) throws IOException {
        String charset;
        String value = httpResponse.getEntity().getContentType().getValue();
        charset = UrlUtils.getCharset(value);
        if (StringUtils.isNotBlank(charset)) {
            return charset;
        }
        Charset defaultCharset = Charset.defaultCharset();
        String content = new String(contentBytes, defaultCharset.name());
        if (StringUtils.isNotEmpty(content)) {
            Document document = Jsoup.parse(content);
            Elements links = document.select("meta");
            for (Element link : links) {
                String metaContent = link.attr("content");
                String metaCharset = link.attr("charset");
                if (metaContent.indexOf("charset") != -1) {
                    metaContent = metaContent.substring(metaContent.indexOf("charset"), metaContent.length());
                    charset = metaContent.split("=")[1];
                    break;
                }
                else if (StringUtils.isNotEmpty(metaCharset)) {
                    charset = metaCharset;
                    break;
                }
            }
        }
        return charset;
    }
	
	private Page addToCycleRetry(Request request, Site site) {
    	Page page = new Page();
        Object cycleTriedTimesObject = request.getExtra(Request.CYCLE_TRIED_TIMES);//存储的次数公对site级别的错误有效，因为代理IP级别的错误是无限次重试的
    	//根据request的statusCode来确定处理流程
    	Integer statusCode = (Integer)request.getExtra(Request.STATUS_CODE);
    	//代理IP级别的错误，无限重试，直到正常或不是代理IP级别错误
    	if(Proxy.PROXY_ERROR_CODE_SET.contains(statusCode)){
    		page.addTargetRequest(request.setPriority(0));
    	}
    	
    	//如果是site级别的可接受的statusCode，则重试site.cycleRetryTimes次
    	if(site.getAcceptStatCode().contains(statusCode)){
    		if (cycleTriedTimesObject == null) {
                page.addTargetRequest(request.setPriority(0).putExtra(Request.CYCLE_TRIED_TIMES, 1));
            } else {
                int cycleTriedTimes = (Integer) cycleTriedTimesObject;
                cycleTriedTimes++;
                if(site.getCycleRetryTimes() == 0){
            		page.addTargetRequest(request.setPriority(0).putExtra(Request.CYCLE_TRIED_TIMES, cycleTriedTimes));
            	}else{
            		if (cycleTriedTimes >= site.getCycleRetryTimes()) {
                        return null;
                    }
            	}
            }
    	}
    	
        page.setNeedCycleRetry(true);
        return page;
    }
	
	@Override
	public void setThread(int threadNum) {
	}
}
