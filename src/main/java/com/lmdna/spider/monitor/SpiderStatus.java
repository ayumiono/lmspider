package com.lmdna.spider.monitor;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.scheduler.MonitorableScheduler;

/**
 * @author code4crafer@gmail.com
 * @since 0.5.0
 */
public class SpiderStatus implements SpiderStatusMXBean {

	private static final long serialVersionUID = -2623053766042439517L;

	protected final Spider spider;

    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected final SpiderMonitor.MonitorSpiderListener monitorSpiderListener;

    public SpiderStatus(Spider spider, SpiderMonitor.MonitorSpiderListener monitorSpiderListener) {
        this.spider = spider;
        this.monitorSpiderListener = monitorSpiderListener;
    }

    public String getName() {
        return spider.getUUID();
    }

    public long getLeftPageCount() {
        if (spider.getScheduler() instanceof MonitorableScheduler) {
            return ((MonitorableScheduler) spider.getScheduler()).getLeftRequestsCount(spider);
        }
        logger.warn("Get leftPageCount fail, try to use a Scheduler implement MonitorableScheduler for monitor count!");
        return -1;
    }

    public long getTotalPageCount() {
    	return monitorSpiderListener.getTotalPageCount();
    }

    @Override
    public long getSuccessPageCount() {
        return monitorSpiderListener.getSuccessCount().get();
    }

    @Override
    public long getErrorPageCount() {
        return monitorSpiderListener.getErrorCount().get();
    }

    @Override
    public String getStatus() {
        return spider.getStatus().name();
    }

    @Override
    public int getThread() {
        return spider.getThreadAlive();
    }

    public void start() {
        spider.start();
    }

    public void stop() {
        spider.stop();
    }

    @Override
    public Date getStartTime() {
        return spider.getStartTime();
    }

    @Override
    public int getPagePerSecond() {
        int runSeconds = (int) (System.currentTimeMillis() - getStartTime().getTime()) / 1000;
        return (int) (getSuccessPageCount() / runSeconds);
    }
    
    @Override
    public int getProxyPoolSize(){
    	if(spider.getSite().getHttpProxyPool() != null)
    		return spider.getSite().getHttpProxyPool().getIdleNum();
//    	logger.warn("No proxy ip pool found in this Spider");
    	return -1;
    }

	@Override
	public long getMatchSuccessPageCount() {
		 return monitorSpiderListener.getMatchSuccessCount().get();
	}

}
