package com.lmdna.spider.pageprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.exception.PageProcessException;
import us.codecraft.webmagic.processor.PageProcessor;

public class CtripAllHotelsPageProcessor implements PageProcessor {
	
    private Site site = Site.me().setRetryTimes(2).setSleepTime(2000);
    
    private static Map<String,Integer> priceMap = new HashMap<String,Integer>();
    
    static{
    	priceMap.put("p_h4_0", 0);
    	priceMap.put("p_h4_5", 1);
    	priceMap.put("p_h4_2", 2);
    	priceMap.put("p_h4_9", 3);
    	priceMap.put("p_h4_3", 4);
    	priceMap.put("p_h4_6", 5);
    	priceMap.put("p_h4_4", 6);
    	priceMap.put("p_h4_7", 7);
    	priceMap.put("p_h4_1", 8);
    	priceMap.put("p_h4_8", 9);
    }

	@Override
	public void process(Page page) throws PageProcessException {
//		List<String> cityhrefs = page.getHtml().xpath("//dl[@class='pinyin_filter_detail layoutfix']//a/@href").all();
//		List<String> citynames = page.getHtml().xpath("//dl[@class='pinyin_filter_detail layoutfix']//a/text()").all(); 
//		for(int i=0;i<citynames.size();i++){
//			page.putField(citynames.get(i), cityhrefs.get(i));
//		}
//		String city = page.getRequest().getExtra("city").toString();
//		page.putField("city",city);
//		List<String> pagenos = page.getHtml().links().all();
//		List<String> hrefs = page.getHtml().xpath("//div[@id='page_info']/div/a/@href").all();
//		int biggestpageno = 0;
//		for(int i=0;i<pagenos.size();i++){
//			if(Integer.valueOf(pagenos.get(i))>biggestpageno){
//				biggestpageno = Integer.valueOf(pagenos.get(i));
//			}
//		}
//		String href="";
//		if(hrefs.size()>1){
//			href = hrefs.get(1);
//			href = StringUtils.substringBeforeLast(href, "p");
//			page.putField("page1", page.getRequest().getUrl());
//			for(int i =2;i<=biggestpageno;i++){
//				page.putField("page"+i, href + "p" + String.valueOf(i));
//			}
//		}else{
//			href=page.getRequest().getUrl();
//			page.putField("page1", href);
//		}
		List<String> hoteldivlist = page.getHtml().xpath("//div[@id='hotel_list']/div").all();
		List<String> hotelhreflist = new ArrayList<String>();
		for(int i=1; i<=hoteldivlist.size();i++){
			String childXpath = "//div[@id='hotel_list']/div[" + i + "]/ul/li[@class='searchresult_info_name']/h2[@class='searchresult_name']/a/@href";
			String href = page.getHtml().xpath(childXpath).toString();
			if(StringUtils.isNotEmpty(href)){
				hotelhreflist.add(href);
			}
		}
		page.putField("hotelhreflist", hotelhreflist);
	}

	@Override
	public Site getSite() {
		return site;
	}

}
