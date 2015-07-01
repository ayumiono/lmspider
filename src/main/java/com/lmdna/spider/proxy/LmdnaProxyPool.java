package com.lmdna.spider.proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;

import org.apache.http.HttpHost;
import org.slf4j.LoggerFactory;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.ProxyPool;
import us.codecraft.webmagic.utils.LoggerUtil;
import us.codecraft.webmagic.utils.ProxyUtils;

import com.lmdna.spider.dao.model.SpiderBiz;
import com.lmdna.spider.dao.model.SpiderProxyIp;
import com.lmdna.spider.dao.model.SpiderWebsiteConfig;
import com.lmdna.spider.notify.mail.MailSender;
import com.lmdna.spider.protocol.rpc.IpProtocol;
import com.lmdna.spider.protocol.rpc.RPCProtocolFactory;
import com.lmdna.spider.utils.SpiderGlobalConfig;


/**
 * @author ayumi
 */
public class LmdnaProxyPool extends ProxyPool{
	
    private BlockingQueue<LmdnaProxy> proxyQueue = new DelayQueue<LmdnaProxy>();
    
    private BlockingQueue<LmdnaProxy> reviveProxyQueue = new DelayQueue<LmdnaProxy>();//增加额外的复活容器是为了使待复活的ip不会影响到checkProxyCountTask工作
    //key:ip:port
    private Map<String, LmdnaProxy> allProxy = new ConcurrentHashMap<String, LmdnaProxy>();
    
    private SpiderBiz biz;//标识业务种别
    private SpiderWebsiteConfig websiteConfig;//反监控规则

    //如果之后做成页面可控，这几个参数要确保线程安全
    private volatile int reuseInterval = 1000;// 反监控规则_请求间隔 ms
    private volatile int reviveTime = 60*60*1000;// 反监控规则_复活时间 d
    private volatile int saveProxyInterval = 60*1000;// 代理IP使用情况监控 h
    
    private static final int checkProxyCountInterval = 10*60*1000;
    
    private boolean isEnable = false;
    private Timer timer1 = new Timer(true);
    
    private String logName ="ProxyPool";
    //定期检查代理IP数量是否充足
    private TimerTask checkProxyCountTask = new TimerTask(){
		@Override
		public void run() {
			checkProxyCount();
		}
    	
    };
    
    public LmdnaProxyPool(SpiderBiz biz) {
    	this.websiteConfig = biz.getWebsiteConfigBO();
    	this.reuseInterval = biz.getWebsiteConfigBO().getIpReuseInterval();
    	this.reviveTime = biz.getWebsiteConfigBO().getIpReviveinTime()*60*60*1000;//小时转为毫秒
    	this.saveProxyInterval = biz.getWebsiteConfigBO().getIpStatReportInterval()*60*1000;//分钟转为毫秒
    	this.biz = biz;
    	LoggerUtil.info(biz.getBizCode(), getLogName(), "初始加载代理IP...");
    	enable(true);
    	loadProxy(true);
    	timer1.schedule(checkProxyCountTask, checkProxyCountInterval, checkProxyCountInterval);
    }

    private void checkProxyCount(){
    	LoggerUtil.info(biz.getBizCode(),getLogName(),"检查代理IP池数量");
    	if(allProxy.size()<websiteConfig.getProxyIpCount()){
    		LoggerUtil.info(biz.getBizCode(),getLogName(),String.format("可用代理IP还剩%d个，数量不足，加载更多的代理IP...", allProxy.size()));
    		loadProxy(false);
    	}
    }
    
    private Map<String, LmdnaProxy> resetAllProxy() {
        Map<String, LmdnaProxy> tmp = new HashMap<String, LmdnaProxy>();
        for (Entry<String, LmdnaProxy> e : allProxy.entrySet()) {
        	LmdnaProxy p = e.getValue();
            p.resetSuccessNum();
            p.resetBorrowNum();
            tmp.put(e.getKey(), p);
        }
        return tmp;
    }
    
    /**
     * 读取代理IP资源
     * 1.池初始化时调用
     * 2.池中没有可用代理IP里调用
     */
    private void loadProxy(boolean firstLoad) {
        try{
        	
        	if(allProxy.size()<websiteConfig.getProxyIpCount()){
        		LoggerUtil.info(biz.getBizCode(),getLogName(),"需要补充代理IP，开始加载代理IP...");
        		Map<String, LmdnaProxy> httpProxyMap = new HashMap<String,LmdnaProxy>();
        		//先从reviveProxyQueue中拿 
        		LmdnaProxy reviveProxy = null;
        		while(true){
        			reviveProxy = reviveProxyQueue.poll();
            		if(reviveProxy!=null){
            			reviveProxy.setReuseTimeInterval(reuseInterval);
            			httpProxyMap.put(reviveProxy.getHttpHost().toHostString(),reviveProxy);
            			LoggerUtil.info(biz.getBizCode(),getLogName(),String.format("失效代理IP：{} 被重新激活。", reviveProxy.getHttpHost().toHostString()));
            		}else{
            			break;
            		}
        		}
        		int loadcount = 0;
        		if(firstLoad){
        			loadcount = websiteConfig.getProxyIpCount();
        		}else{
        			loadcount = websiteConfig.getProxyIpLoadCount();
        		}
        		
        		List<SpiderProxyIp> proxyIpList = new ArrayList<SpiderProxyIp>();
        		Map<String,Object> querymap = new HashMap<String,Object>();
        		while(loadcount>0){
        			querymap.put("bizCode", biz.getBizCode());
            		querymap.put("loadCount", loadcount);
            		querymap.put("machineId", SpiderGlobalConfig.getValue(SpiderGlobalConfig.MACHINE_ID));
            		proxyIpList = RPCProtocolFactory.get(IpProtocol.class).getIps(querymap);
            		if(proxyIpList.size()==0){
                		break;
                	}
                	for(SpiderProxyIp proxyIp : proxyIpList){
                		HttpHost host = new HttpHost(proxyIp.getIp(),proxyIp.getPort());
                		String key = host.toHostString();
                		if(allProxy.containsKey(key)){
                			continue;
                		}else{
//                			int id = addProxyIpStatus(proxyIp.getId(),biz.getId(),proxyIp.getIp(),proxyIp.getPort(),InetAddress.getLocalHost().getHostAddress(),reuseInterval);
                			LmdnaProxy proxy = new LmdnaProxy(host,Integer.valueOf(proxyIp.getId()),reuseInterval);
                			httpProxyMap.put(key, proxy);
                			--loadcount;
                    		if(loadcount == 0){
                    			break;
                    		}
                		}
                	}
        		}
            	addProxy(httpProxyMap);
            	if(loadcount>0){
            		LoggerUtil.info(biz.getBizCode(),getLogName(),"没有新的代理IP可供添加！");
            		MailSender.getInstance().sendMail(String.format("业务%s  <span style='color:red'>代理IP不足！</span><br>"
            				+ "当前可用代理IP数为<span style='color:red'>%d</span>个，当前业务允许最低代理IP数为<span style='color:green'>%d</span>个<br>"
            				+ "请及时添加代理IP", biz.getBizName(),allProxy.size(),websiteConfig.getProxyIpCount()), "chenxuelong@alphaun.com", "柠檬爬虫系统异常（代理IP不足！）");
            	}
            	LoggerUtil.info(biz.getBizCode(),getLogName(),String.format("加载代理IP完成！代理IP池规模:%d", allProxy.size()));
        	}
        	
        }catch(Exception e){
        	LoggerUtil.info(biz.getBizCode(),getLogName(),"加载代理IP失败！");
        	LoggerFactory.getLogger(LmdnaProxyPool.class).error(biz.getBizName()+"加载代理IP失败！",e);
        }
    }

    private void addProxy(Map<String, LmdnaProxy> httpProxyMap) {
        for (Entry<String, LmdnaProxy> entry : httpProxyMap.entrySet()) {
            try {
                if (allProxy.containsKey(entry.getKey())) {
                    continue;
                }else{
                	proxyQueue.add(entry.getValue());
                    allProxy.put(entry.getKey(), entry.getValue());
                }
            } catch (NumberFormatException e) {
            	LoggerFactory.getLogger(LmdnaProxyPool.class).error("HttpHost init error:", e);
            }
        }
    }
    
    public LmdnaProxy getProxy() {
    	LmdnaProxy proxy = null;
        try {
            Long time = System.currentTimeMillis();
            LoggerUtil.debug(biz.getBizCode(),getLogName(),String.format("%s taking proxy ip form proxypool, current size:%d", Thread.currentThread().getName(),proxyQueue.size()));
            proxy = proxyQueue.take();
            LoggerUtil.debug(biz.getBizCode(),getLogName(),String.format("%s take proxy ip form proxypool success: %s",Thread.currentThread().getName(),proxy.getHttpHost().toHostString()));
            double costTime = (System.currentTimeMillis() - time) / 1000.0;
            if (costTime > reuseInterval) {
            	LoggerUtil.info(biz.getBizCode(),getLogName(),String.format("get proxy time: %d", costTime));
            }
            LmdnaProxy p = allProxy.get(proxy.getHttpHost().toHostString());
            p.setLastBorrowTime(System.currentTimeMillis());
            p.borrowNumIncrement(1);
        } catch (InterruptedException e) {
        	LoggerFactory.getLogger(LmdnaProxyPool.class).error("get proxy error", e);
        }
        if (proxy == null) {
            throw new NoSuchElementException();
        }
        return proxy;
    }

    public void returnProxy(Proxy proxy, int statusCode) throws Exception{
    	try{
    		HttpHost host = proxy.getHttpHost();
        	LmdnaProxy p = allProxy.get(host.toHostString());
            if (p == null) {
            	LoggerUtil.info(String.format("归还代理IP: %s 时,没有找到归还对象副本！！！！！！！！", host.toHostString()));
                return;
            }
            switch (statusCode) {
                case LmdnaProxy.SUCCESS:
                    p.setReuseTimeInterval(reuseInterval);
                    p.resetFailedNum();
                    p.resetDeadNum();
                    p.setFailedErrorType(new ArrayList<Integer>());
                    p.recordResponse();
                    p.successNumIncrement(1);
                    break;
                case LmdnaProxy.ERROR_403:
                    p.fail(LmdnaProxy.ERROR_403);
                    p.setReuseTimeInterval(reuseInterval * p.getFailedNum());
                    LoggerUtil.info(biz.getBizCode(),getLogName(),"{} >>>> 错误码为403。>>>> 当前失败次数{},改变使用间隔为{}秒",new Object[]{host.toHostString(),p.getFailedNum(),p.getReuseTimeInterval() / 1000.0});
                    break;
                case LmdnaProxy.ERROR_500:
                	p.fail(LmdnaProxy.ERROR_500);
                    p.setReuseTimeInterval(reuseInterval * p.getFailedNum());
                    LoggerUtil.info(biz.getBizCode(),getLogName(),"{} >>>> 错误码为500。>>>> 当前失败次数{},改变使用间隔为{}秒",new Object[]{host.toHostString(),p.getFailedNum(),p.getReuseTimeInterval() / 1000.0});
                    break;
                case LmdnaProxy.ERROR_BANNED:
                    p.dead();
                    p.setReuseTimeInterval(reviveTime);
                    allProxy.remove(host.toHostString());
                    reviveProxyQueue.add(p);
                    LoggerUtil.info(biz.getBizCode(),getLogName(),"{} 被列为濒危代理IP,将在一段时间后复活。  >>>> 错误记录：{} >>> 前一个周期内成功次数为{}次，连续失败次数为{}次！ 目前可用代理IP还剩{}个",new Object[]{host.toHostString(),p.getFailedType(),p.getSuccessNum(),p.getFailedNum(),allProxy.size()});
                    return;
                case LmdnaProxy.ERROR_404:
                    p.fail(LmdnaProxy.ERROR_404);
                    p.setReuseTimeInterval(reuseInterval * p.getFailedNum());
                    LoggerUtil.info(biz.getBizCode(),getLogName(),"{} >>>> 错误码为404。>>>> 当前失败次数{},改变使用间隔为{}秒",new Object[]{host.toHostString(),p.getFailedNum(),p.getReuseTimeInterval() / 1000.0});
                    break;
                case LmdnaProxy.ERROR_Proxy://代理IP异常HttpHostConnectException
                	if(!ProxyUtils.validateProxy(host,3,p.getSocketTimeout())){
                		//称除无效的代理IP
                    	allProxy.remove(host.toHostString());
                		Map<String,Object> parammap = new HashMap<String,Object>();
                		parammap.put("id", p.getProxyIpId());
                		RPCProtocolFactory.get(IpProtocol.class).delProxyIp(parammap);
                    	parammap.clear();
                    	LoggerUtil.info(biz.getBizCode(),getLogName(),"{} 被ProxyUtils检测为无效代理IP,将被移除！前一个周期内成功次数为{}次，连续失败次数为{}次！目前可用代理IP还剩{}个",new Object[]{host.toHostString(),p.getSuccessNum(),p.getFailedNum(),allProxy.size()});
                    	return;
                	}else{
                		p.fail(LmdnaProxy.ERROR_Proxy);
                		p.setReuseTimeInterval(reuseInterval * p.getFailedNum());
                		LoggerUtil.info(biz.getBizCode(),getLogName(),"{} >>>> 错误码为10001(HttpHostConnectException)。>>>> 当前失败次数{},改变使用间隔为{}秒",new Object[]{host.toHostString(),p.getFailedNum(),p.getReuseTimeInterval() / 1000.0});
                		break;
                	}
                case LmdnaProxy.ERROR_PROXY_TIME_OUT://代理IP异常ConnectTimeoutException
                	if(!ProxyUtils.validateProxy(host,3,p.getSocketTimeout())){
                		//称除无效的代理IP
                    	allProxy.remove(host.toHostString());
                    	Map<String,Object> parammap = new HashMap<String,Object>();
                		parammap.put("id", p.getProxyIpId());
                		RPCProtocolFactory.get(IpProtocol.class).delProxyIp(parammap);
                    	parammap.clear();
                        LoggerUtil.info(biz.getBizCode(),getLogName(),"{} 被检测为无效代理IP,将被移除！前一个周期内成功次数为{}次，连续失败次数为{}次！目前可用代理IP还剩{}个",new Object[]{host.toHostString(),p.getSuccessNum(),p.getFailedNum(),allProxy.size()});
                        return;
                	}else{
                		p.fail(LmdnaProxy.ERROR_PROXY_TIME_OUT);
                    	p.setSocketTimeout(p.getSocketTimeout() + p.getFailedNum()*300);
                    	LoggerUtil.info(biz.getBizCode(),getLogName(),"{} >>>错误码为10003(ConnectTimeoutException) >>> 改变conntimeout为{}秒",new Object[]{host.toHostString(),p.getSocketTimeout() / 1000.0});
                    	break;
                	}
                case LmdnaProxy.ERROR_SOCKET_READ_TIME_OUT://代理IP异常SocketTimeoutException
                	if(!ProxyUtils.validateProxy(host,3,p.getSocketTimeout())){
                    	allProxy.remove(host.toHostString());
                    	Map<String,Object> parammap = new HashMap<String,Object>();
                		parammap.put("id", p.getProxyIpId());
                		RPCProtocolFactory.get(IpProtocol.class).delProxyIp(parammap);
                    	parammap.clear();
                    	LoggerUtil.info(biz.getBizCode(),getLogName(),"{} 被检测为无效代理IP,将被移除！前一个周期内成功次数为{}次，连续失败次数为{}次！目前可用代理IP还剩{}个",new Object[]{host.toHostString(),p.getSuccessNum(),p.getFailedNum(),allProxy.size()});
                    	return;
                	}else{
                		p.fail(LmdnaProxy.ERROR_SOCKET_READ_TIME_OUT);
                    	p.setSocketTimeout(p.getSocketTimeout() + p.getFailedNum()*300);
                    	LoggerUtil.info(biz.getBizCode(),getLogName(),"{} >>>错误码为10005(SocketTimeoutException) >>> 改变sockettimeout为{}秒",new Object[]{host.toHostString(),p.getSocketTimeout() / 1000.0});
                    	break;
                	}
                case Request.ERROR_ILLEGAL_REQUEST_URI://url格式错误，不做任何处理
                	break;
                default:
                    p.fail(statusCode);
                    p.setReuseTimeInterval(reuseInterval * p.getFailedNum());
                    LoggerUtil.info(biz.getBizCode(),getLogName(),"{} >>>> 错误码为10009(DefaultError)。>>>> 目前失败次数{},改变使用间隔为{}秒",new Object[]{host.toHostString(),p.getFailedNum(),p.getReuseTimeInterval() / 1000.0});
                    break;
            }
            if (p.getFailedNum() > websiteConfig.getFailedTimes()) {
        		//没有通过检测 /超出失效次数阀值
            	if(!ProxyUtils.validateProxy(host,3,p.getSocketTimeout())){
            		//称除无效的代理IP
                	allProxy.remove(host.toHostString());
                	Map<String,Object> parammap = new HashMap<String,Object>();
            		parammap.put("id", p.getProxyIpId());
            		RPCProtocolFactory.get(IpProtocol.class).delProxyIp(parammap);
                	parammap.clear();
                	LoggerUtil.info(biz.getBizCode(),getLogName(),"{} 被检测为无效代理IP,将被移除！前一个周期内成功次数为{}次，连续失败次数为{}次！目前可用代理IP还剩{}个",new Object[]{host.toHostString(),p.getSuccessNum(),p.getFailedNum(),allProxy.size()});
                	return;
            	}else if(p.getDeadNum() < websiteConfig.getDeadTimes()){
            		p.dead();
                    p.setReuseTimeInterval(reviveTime);
                    allProxy.remove(host.toHostString());
                    reviveProxyQueue.add(p);
                    LoggerUtil.info(biz.getBizCode(),getLogName(),"{} 被列为濒危(dead)代理IP,将在一段时间后复活。  >>>> 错误记录{} >>> 目前可用代理IP还剩{}个",new Object[]{host.toHostString(),p.getFailedType(),allProxy.size()});
                    return;
            	}else{
                	allProxy.remove(host.toHostString());
                	Map<String,Object> parammap = new HashMap<String,Object>();
                	parammap.put("ip", host.getHostName());
                	parammap.put("port", host.getPort());
                	parammap.put("reason", p.getFailedType());
                	parammap.put("bizid", biz.getId());
                	RPCProtocolFactory.get(IpProtocol.class).addBlackProxyIp(parammap);
                	LoggerUtil.info(biz.getBizCode(),getLogName(),"{} 被封了！将被放入黑名单。 >>> 错误记录{} >>> 目前可用代理IP还剩{}个",new Object[]{host.toHostString(),p.getFailedType(),allProxy.size()});
                	return;
            	}
            }
            try {
                proxyQueue.put(p);
                LoggerUtil.debug(biz.getBizCode(),getLogName(),"代理IP{}回收成功。",new Object[]{host.toHostString()});
            } catch (InterruptedException e) {
            	LoggerFactory.getLogger(LmdnaProxyPool.class).error(biz.getBizCode()+"proxyQueue return proxy error", e);
            }
    	}catch(Exception e){
    		LoggerFactory.getLogger(LmdnaProxyPool.class).error(biz.getBizCode()+"proxyQueue return proxy error", e);
    		throw e;
    	}finally{
    		
    	}
    }

    public String allProxyStatus() {
        String re = "all proxy info >>>> \r\n";
        for (Entry<String, LmdnaProxy> entry : allProxy.entrySet()) {
            re += entry.getValue().toString() + "\r\n";
        }
        return re;
    }
    
    public int getIdleNum() {
        return proxyQueue.size();
    }
    
    public int getTotalNum(){
    	return allProxy.size();
    }

    public int getReuseInterval() {
        return reuseInterval;
    }

    public void setReuseInterval(int reuseInterval) {
        this.reuseInterval = reuseInterval;
    }

    public void enable(boolean isEnable) {
        this.isEnable = isEnable;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public int getReviveTime() {
        return reviveTime;
    }

    public void setReviveTime(int reviveTime) {
        this.reviveTime = reviveTime;
    }

    public int getSaveProxyInterval() {
        return saveProxyInterval;
    }

    public void setSaveProxyInterval(int saveProxyInterval) {
        this.saveProxyInterval = saveProxyInterval;
    }
    
    public String getLogName(){
    	return logName;
    }
    
    public static void main(String arg[]){
    	HttpHost host = new HttpHost("121.12.12.12",80);
    	System.out.println(host.getHostName());
    }
    
}
