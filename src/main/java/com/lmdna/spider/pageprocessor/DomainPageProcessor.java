package com.lmdna.spider.pageprocessor;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

public class DomainPageProcessor implements PageProcessor{

	// 部分一：抓取网站的相关配置，包括编码、抓取间隔、重试次数等
    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000);
	
	public Site getSite() {
		return site;
	}

	// process是定制爬虫逻辑的核心接口，在这里编写抽取逻辑
	public void process(Page page) {
		// 部分二：定义如何抽取页面信息，并保存下来
		page.putField("domain", page.getUrl().get());
        page.putField("title", page.getHtml().regex("<title>(.*?)</title>").toString());
        page.putField("meta-keywords", page.getHtml().regex("<meta name=\"keywords\" content=\"(.*?)\" />").toString());
        page.putField("meta-description", page.getHtml().regex("<meta name=\"description\" content=\"(.*?)\" />").toString());
        if (page.getResultItems().get("title") == null && page.getResultItems().get("meta") == null) {
            //skip this page
            page.setSkip(true);
        }
        /*// 部分三：从页面发现后续的url地址来抓取
        page.addTargetRequests(page.getHtml().links().regex("(https://github\\.com/\\w+/\\w+)").all());*/
	}

}
