package com.lmdna.spider.monitor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author code4crafer@gmail.com
 * @since 0.5.0
 */
public interface SpiderStatusMXBean extends Serializable{

    public String getName();

    public String getStatus();

    public int getThread();

    public long getTotalPageCount();

    public long getLeftPageCount();

    public long getSuccessPageCount();
    
    public long getMatchSuccessPageCount();

    public long getErrorPageCount();

    public void start();

    public void stop();

    public Date getStartTime();

    public int getPagePerSecond();
    
    //public int getPipelineTime();//页面持久化的时间
    
    public int getProxyPoolSize();
    
}
