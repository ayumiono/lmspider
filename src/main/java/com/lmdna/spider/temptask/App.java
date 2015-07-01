package com.lmdna.spider.temptask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;

import com.lmdna.spider.pageprocessor.CtripAllHotelsPageProcessor;
import com.lmdna.spider.pipeline.MysqlImplCtripHotel;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException
    {
    	CtripAllHotelsPageProcessor pageProcessor = new CtripAllHotelsPageProcessor();
    	List<String[]> proxyips = new ArrayList<String[]>();
    	proxyips.add(new String[]{"124.88.67.19","80"});
    	proxyips.add(new String[]{"221.10.40.237","80"});
    	proxyips.add(new String[]{"211.143.146.239","80"});
    	proxyips.add(new String[]{"218.108.168.70","80"});
    	proxyips.add(new String[]{"183.224.1.30","80"});
    	proxyips.add(new String[]{"218.240.156.82","80"});
    	proxyips.add(new String[]{"119.4.95.136","80"});
    	proxyips.add(new String[]{"202.108.23.247","80"});
    	proxyips.add(new String[]{"211.143.146.239","80"});
    	proxyips.add(new String[]{"59.56.173.75","8585"});
    	proxyips.add(new String[]{"60.191.39.252","80"});
    	proxyips.add(new String[]{"119.6.136.126","80"});
    	proxyips.add(new String[]{"124.88.67.19","80"});
    	proxyips.add(new String[]{"119.6.136.126","80"});
    	proxyips.add(new String[]{"60.190.138.151","80"});
    	proxyips.add(new String[]{"183.203.13.135","80"});
    	proxyips.add(new String[]{"218.108.232.99","80"});
    	proxyips.add(new String[]{"202.108.23.247","80"});
    	proxyips.add(new String[]{"183.224.1.30","80"});
    	proxyips.add(new String[]{"121.40.72.148","80"});
    	proxyips.add(new String[]{"119.6.136.126","80"});
    	proxyips.add(new String[]{"111.13.12.202","80"});
    	proxyips.add(new String[]{"111.13.12.216","80"});
    	proxyips.add(new String[]{"221.10.40.237","80"});
    	proxyips.add(new String[]{"112.245.191.186","8585"});
    	proxyips.add(new String[]{"119.145.200.50","8585"});
    	proxyips.add(new String[]{"221.130.178.78","8585"});
    	pageProcessor.getSite().setHttpProxyPool(proxyips);
    	pageProcessor.getSite().setCharset("gbk");
    	pageProcessor.getSite().getHttpProxyPool().enable(true);
    	pageProcessor.getSite().setProxyReuseInterval(500);
    	pageProcessor.getSite().putValidCheck(0, "//div[@id='hotel_list']");
    	Spider spider = Spider.create(pageProcessor);
    	File curImportFile = new File("D:\\ctripcityhotelpagehref\\hotels.ctrip.com");
		File[] directoryFiles = curImportFile.listFiles();
		for (File file : directoryFiles) {
			String city = file.getName();
			city = StringUtils.substringBefore(city, ".");
			BufferedReader br = null;
			BufferedInputStream bis = null;
			bis = new BufferedInputStream(new FileInputStream(file));
			br = new BufferedReader(new InputStreamReader(bis, "UTF-8"),1024);
	    	String line = "";
	    	while((line = br.readLine())!=null){
	    		String[] lin = line.split("\t");
	    		if(lin[1].startsWith("http")){
	    			Request request = new Request(lin[1]);
	    			request.putExtra("city", city);
		    		spider.addRequest(request);
	    		}
	    	}
	    	br.close();
		}
		MysqlImplCtripHotel pipeline = new MysqlImplCtripHotel();
    	spider.thread(30);
    	spider.addPipeline(pipeline);
    	spider.run();
    }
}
