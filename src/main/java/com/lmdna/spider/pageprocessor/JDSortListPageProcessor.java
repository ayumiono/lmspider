package com.lmdna.spider.pageprocessor;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.exception.PageProcessException;
import us.codecraft.webmagic.processor.PageProcessor;

public class JDSortListPageProcessor implements PageProcessor {

	@Override
	public void process(Page page) throws PageProcessException {
		page.putField("parentid", page.getRequest().getExtra("parentid"));
		page.putField("taglist", page.getHtml().xpath("//div[@id=\"sortlist\"]//ul[1]/li/a/text()").all());
		page.putField("codelist", page.getHtml().xpath("//div[@id=\"sortlist\"]//ul[1]/li/a/@href").all());
		page.putField("lvl1tag",page.getRequest().getExtra("lvl1tag"));
		page.putField("lvl2tag", page.getRequest().getExtra("lvl2tag"));
	}
	// 部分一：抓取网站的相关配置，包括编码、抓取间隔、重试次数等
    private Site site = Site.me().setRetryTimes(3).setSleepTime(3000);
    
	@Override
	public Site getSite() {
		return site;
	}

}
