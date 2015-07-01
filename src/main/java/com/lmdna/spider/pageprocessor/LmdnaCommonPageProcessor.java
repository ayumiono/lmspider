package com.lmdna.spider.pageprocessor;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.exception.PageProcessException;
import us.codecraft.webmagic.processor.PageProcessor;

import com.lmdna.spider.dao.model.SpiderFieldRule;

/**
 * 柠檬抓取业务通用页面解析类
 * @author ayumi
 *
 */
public class LmdnaCommonPageProcessor implements PageProcessor{
	
	public List<SpiderFieldRule> fieldRules;
	
	public Site site;
	
	public LmdnaCommonPageProcessor(List<SpiderFieldRule> fieldRules,Site site){
		this.fieldRules = fieldRules;
		this.site = site;
	}

	@Override
	public void process(Page page) throws PageProcessException {
		for (SpiderFieldRule fieldRule : fieldRules) {
			List<String> results;
			StringBuilder sb; 
			switch (fieldRule.getType()) {
			case 0:
				results = page.getHtml().regex(fieldRule.getRule()).all();
				if(results.size() == 0){
					if(fieldRule.getAllowEmpty() == 1){
						throw new PageProcessException(String.format("{fieldname:@#%s@#,fieldrule:@#%s@#,parentId:@#%d@#,type:@#regex@#}",fieldRule.getFieldName(),fieldRule.getRule(),fieldRule.getParentId()));
					}
				}
				sb = new StringBuilder();
				for (String result : results) {
					sb.append(result + ",");
				}
				page.putField(fieldRule.getFieldName(), StringUtils.substringBeforeLast(sb.toString().trim(), ","));
				break;
			case 1:
				results = page.getHtml().xpath(fieldRule.getRule()).all();
				if(results.size() == 0){
					if(fieldRule.getAllowEmpty() == 1){
						throw new PageProcessException(String.format("{fieldname:@#%s@#,fieldrule:@#%s@#,parentId:@#%d@#,type:@#xpath@#}",fieldRule.getFieldName(),fieldRule.getRule(),fieldRule.getParentId()));
					}
				}
				sb = new StringBuilder();
				for (String result : results) {
					sb.append(result + ",");
				}
				page.putField(fieldRule.getFieldName(), StringUtils.substringBeforeLast(sb.toString().trim(), ","));
				break;
			case 2:
				results = page.getHtml().css(fieldRule.getRule()).all();
				if(results.size() == 0){
					if(fieldRule.getAllowEmpty() == 1){
						throw new PageProcessException(String.format("{fieldname:@#%s@#,fieldrule:@#%s@#,parentId:@#%d@#,type:@#css@#}",fieldRule.getFieldName(),fieldRule.getRule(),fieldRule.getParentId()));
					}
				}
				sb = new StringBuilder();
				for (String result : results) {
					sb.append(result + ",");
				}
				page.putField(fieldRule.getFieldName(), StringUtils.substringBeforeLast(sb.toString().trim(), ","));
				break;
			default:
				if(page.getRequest().getExtra(fieldRule.getFieldName()) == null){
					throw new PageProcessException(String.format("{fieldname:@#%s@#,fieldrule:@#%s@#,type:@#orig@#}",fieldRule.getFieldName(),fieldRule.getRule()));
				}
				page.putField(fieldRule.getFieldName(), page.getRequest().getExtra(fieldRule.getFieldName()));
				break;
			}
		}
	}

	@Override
	public Site getSite() {
		return site;
	}
	
	/**
	 * 公共的添加额外下载需求方法
	 */
	public void addLinkedRequest(Request request){}
	
	/**
	 * 公共的添加额外下载任务方法
	 */
	public void addAdditionRequest(){}
}
