package com.lmdna.spider.pipeline;

import java.io.IOException;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.ResultItems;

/**
 * 备份网页抓取内容
 * 文件结构如下：
 * —— 一级域名：
 * 		—— 二级域名1
 * 			—— taskid1.txt
 * 				url_fingerprint  	url		content
 * 			—— taskid2.txt
 *  		—— taskid3.txt
 *  	—— 二级域名2
 * 			—— taskid1.txt
 * 			—— taskid2.txt
 *  		—— taskid3.txt
 * 调用模式识别接口，记算出模式，存到mysql
 * 	url_fingerprint		tags
 * @author ayumiono
 */
public class LmdnaFilePipeline extends AbstractFilePipeline {
	
	public LmdnaFilePipeline() throws IOException {
		super();
	}

	@Override
	public String processSinglePage(Page page) {
		ResultItems resultItems = page.getResultItems();
		long fingerPrint = resultItems.get("fingerPrint");
		String url = resultItems.get("url");
		String content = resultItems.get("content");
		StringBuffer sb = new StringBuffer();
		sb.append(url+"\t"+fingerPrint+"\t"+content);
		return sb.toString();
	}
}
