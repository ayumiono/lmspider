<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
	<div class="box">
		<div class="title">
			<h2>Code Editor</h2>
		</div>
		<div class="content pages">
			<div id="editor">/**
 * 你可以用code editor 编写自定义的spider流程，自定义spider必须继承自Spider类！模版如下：
 */
package com.lmdna.spider.jar.userdefine;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.exception.PageProcessException;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.utils.HtmlCleaner;

import com.lmdna.spider.berkeleydb.BdbUriUniqFilter;
import com.lmdna.spider.node.SpiderContext;
import com.lmdna.spider.pipeline.AbstractFilePipeline;

public class MySpider extends Spider {
	
	public LmdnaCarBizTask() throws IOException{
		this.setPageProcessor(new MyFilePageProcessor());
		this.addPipeline(new MyFilePipeLine("LMDNAcarbiz"));
	}
	
	class MyFilePageProcessor implements PageProcessor{
		private Site site = Site.me().setDomain("LMDNAcarbiz").setSleepTime(1500);
		@Override
		public void process(Page page) throws PageProcessException {
			//TODO
		}
		@Override
		public Site getSite() {
			return site;
		}
	}
	
	class MyFilePipeLine extends AbstractFilePipeline{
		
		public MyFilePipeLine(String subDir) throws IOException {
			super(subDir);
		}

		@Override
		public String processSinglePage(Page page) {
			//TODO
		}
	}

}
			</div>
		</div>
	</div>

<script>
	$(document).ready(function(){
		var editor = ace.edit("editor");
		// editor.setTheme("ace/theme/github");
		editor.getSession().setMode("ace/mode/java");
	});
</script>