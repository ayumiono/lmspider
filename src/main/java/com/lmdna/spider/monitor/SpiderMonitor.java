package com.lmdna.spider.monitor;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.commons.lang3.StringUtils;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.SpiderListener;
import us.codecraft.webmagic.scheduler.MonitorableScheduler;
import us.codecraft.webmagic.utils.Experimental;

import com.lmdna.spider.notify.mail.MailSender;

/**
 * @author code4crafer@gmail.com
 * @since 0.5.0
 */
@Experimental
public class SpiderMonitor {

    private static SpiderMonitor INSTANCE = new SpiderMonitor();

    private MBeanServer mbeanServer;

    private String jmxServerName;

    private ConcurrentHashMap<String,SpiderStatusMXBean> spiderStatusMap = new ConcurrentHashMap<String,SpiderStatusMXBean>();

    protected SpiderMonitor() {
        jmxServerName = "LMSpider";
        mbeanServer = ManagementFactory.getPlatformMBeanServer();
    }

    /**
     * Register spider for monitor.
     *
     * @param spiders
     * @return
     */
    public synchronized SpiderMonitor register(Spider... spiders) throws JMException {
        for (Spider spider : spiders) {
            MonitorSpiderListener monitorSpiderListener = new MonitorSpiderListener();
            if (spider.getScheduler() instanceof MonitorableScheduler) {
            	monitorSpiderListener.setTotalPageCount(((MonitorableScheduler) spider.getScheduler()).getTotalRequestsCount(spider));
            }
            if (spider.getSpiderListeners() == null) {
                List<SpiderListener> spiderListeners = new ArrayList<SpiderListener>();
                spiderListeners.add(monitorSpiderListener);
                spider.setSpiderListeners(spiderListeners);
            } else {
                spider.getSpiderListeners().add(monitorSpiderListener);
            }
            SpiderStatusMXBean spiderStatusMBean = getSpiderStatusMBean(spider, monitorSpiderListener);
            registerMBean(spiderStatusMBean);
            spiderStatusMap.put(spider.getUUID(), spiderStatusMBean);
        }
        return this;
    }
    
    public synchronized void unregister(Spider... spiders) throws JMException{
    	for(Spider spider : spiders){
    		SpiderStatusMXBean spiderStatus = spiderStatusMap.get(spider.getUUID());
    		unregisterMBean(spiderStatus);
    		spiderStatusMap.remove(spider.getUUID());
    	}
    }

    protected SpiderStatusMXBean getSpiderStatusMBean(Spider spider, MonitorSpiderListener monitorSpiderListener) {
        return new SpiderStatus(spider, monitorSpiderListener);
    }

    public static SpiderMonitor instance() {
        return INSTANCE;
    }

    public class MonitorSpiderListener implements SpiderListener {

        private final AtomicInteger successCount = new AtomicInteger(0);//能顺利download的页面数

        private final AtomicInteger errorCount = new AtomicInteger(0);
        
        private final AtomicInteger successMatchCount = new AtomicInteger(0);//抓取字段能正常匹配的页面数
        
        private final AtomicInteger errorMatchCount = new AtomicInteger(0);
        
        private long totalpageCount = -1;
        
        private final ConcurrentHashMap<String,Integer> errors = new ConcurrentHashMap<String,Integer>();
        
        public void setTotalPageCount(long totalpageCount){
        	this.totalpageCount = totalpageCount;
        }
        
        public long getTotalPageCount(){
        	return this.totalpageCount;
        }
        
        @Override
        public void onSuccess(Request request) {
            successCount.incrementAndGet();
        }

        @Override
        public void onError(Request request) {
            errorCount.incrementAndGet();
        }
        
        public void onMatchSuccess(Request request){
        	successMatchCount.incrementAndGet();
        }

        public AtomicInteger getSuccessCount() {
            return successCount;
        }

        public AtomicInteger getErrorCount() {
            return errorCount;
        }
        
        public AtomicInteger getMatchSuccessCount(){
        	return successMatchCount;
        }
        
		@Override
		public void onMatchError(Request request) {
			
			String matchErrMsg = request.getMatchErrMsg();
			//{fieldname:@#%s@#,fieldrule:@#%s@#,
			String fieldname = StringUtils.substringBetween(matchErrMsg, "fieldname:@#", "@#");
			String fieldrule = StringUtils.substringBetween(matchErrMsg, "fieldrule:@#", "@#");
			String key = fieldname+":"+fieldrule;
			if(errors.get(key) == null){
				errors.put(key, 1);
			}else{
				errors.put(key, errors.get(key)+1);
			}
			errorMatchCount.incrementAndGet();
			
			double errRate = 0.0;
			
			for(Entry<String,Integer> entry: errors.entrySet()){
				synchronized (errors) {
					errRate = totalpageCount <= 0 ? 0 : entry.getValue() * 100.0/totalpageCount;
					if(errRate >= 10){
						String mailcontent = String.format(
								"错误信息：业务%s网页规则解析错误率达到<span style='color:red'>%-3.2f%%</span>!<br>"
								+ "详细信息：<br>"
								+"字段： "+entry.getKey()+" 错误次数: <span style='color:red'>"+entry.getValue()+"</span><br>"
								+ "请检测网页规则是否有变动!", request.getBizcode(),errRate);
						errors.put(entry.getKey(), 0);
						sendMail(mailcontent);
					}
				}
			}
		}
		
		private void sendMail(String mailcontent){
			MailSender.getInstance().sendMail(mailcontent,"chenxuelong@alphaun.com","柠檬爬虫系统异常（页面解析规则出错！）");
		}
    }

    protected void registerMBean(SpiderStatusMXBean spiderStatus) throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        ObjectName objName = new ObjectName(jmxServerName + ":name=" + spiderStatus.getName());
        mbeanServer.registerMBean(spiderStatus, objName);
    }
    
    protected void unregisterMBean(SpiderStatusMXBean spiderStatus) throws MalformedObjectNameException, NullPointerException, MBeanRegistrationException, InstanceNotFoundException{
    	ObjectName objName = new ObjectName(jmxServerName + ":name=" + spiderStatus.getName());
    	mbeanServer.unregisterMBean(objName);
    }
    
    public ConcurrentHashMap<String,SpiderStatusMXBean> getSpiderStatusMap(){
    	return spiderStatusMap;
    } 

}
