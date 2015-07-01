package com.lmdna.spider.pageprocessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;



import com.lmdna.spider.dao.model.SpiderFieldRule;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.exception.PageProcessException;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * 柠檬抓取业务通用页面解析类
 * @author ayumi
 *
 */
public class LmdnaCommonPageProcessorBak implements PageProcessor{
	
	public List<SpiderFieldRule> fieldRules;
	
	public Site site;
	
	public LmdnaCommonPageProcessorBak(List<SpiderFieldRule> fieldRules,Site site){
		this.fieldRules = fieldRules;
		this.site = site;
	}

	/* 
	 * 原则：
	 * 	当前page下要求解析出来的字段都要保证在当前page生命周期内解析完成。
	 * 	哪些规则是当前page下的，由page.getRequest().getFiledRuleId()决定
	 */
	@Override
	public void process(Page page) throws PageProcessException {
		
		Request originalReq = page.getRequest();//源rquest,非null
		Integer fieldRuleId = originalReq.getFieldRuleId();
		if(fieldRuleId !=null){
			if(fieldRuleId == 20)
			System.out.println("test");
		}
		
		Request nextRequest = originalReq.getNextRequest();//抽出下一步request,允许null
		Request templast = nextRequest;//创建请求链时需要的临时节点
		
		//找到当前page下要求解析的字段
		final List<SpiderFieldRule> dependenceFieldRules = new ArrayList<SpiderFieldRule>();
		
		for (SpiderFieldRule fieldRule : fieldRules) {
			if(fieldRule.getParentId() == (fieldRuleId == null ? 0 :fieldRuleId)){
				dependenceFieldRules.add(fieldRule);
			}
		}
		//开始解析当前page下要求解析的字段
		for (SpiderFieldRule fieldRule : dependenceFieldRules) {
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
				//判断是否会产生新的下载请求,如果产生新的下载请求，则当前规则只解析顶级层，如果有子规则，要=到新的下载完成之后才能解析
				if(fieldRule.getAdditionDownload() == 1){
					//将fieldRule.getId()保存下来，新请求的页面将筛选parentid为该id的规则进行解析
					for(String result : results){
						if(!result.startsWith("http")){
							result = "http://"+site.getDomain()+result.trim().replace("|", "%7C");
						}
						Request additionReq = new Request(result);
						additionReq.setFieldRuleId(fieldRule.getId());
						if(templast == null){
							nextRequest = additionReq;
							templast = nextRequest;
						}else{
							templast.setNextRequest(additionReq);
							templast = additionReq;
						}
						if(fieldRule.getNeedPersistence() == 0){
							sb = new StringBuilder();
							sb.append(result + ",");
							page.putField(fieldRule.getFieldName(), StringUtils.substringBeforeLast(sb.toString().trim(), ","));
						}
					}
				}else if(fieldRule.getAdditionRequest() == 1){
					for(String result : results){
						if(!result.startsWith("http")){
							result = "http://"+site.getDomain()+result.trim().replace("|", "%7C");
						}
						Request additionReq = new Request(result);
						additionReq.setFieldRuleId(fieldRule.getId());
						transmitResultItem(page, additionReq);
						page.addTargetRequest(additionReq);
						if(fieldRule.getNeedPersistence() == 0){
							sb = new StringBuilder();
							sb.append(result + ",");
							page.putField(fieldRule.getFieldName(), StringUtils.substringBeforeLast(sb.toString().trim(), ","));
						}
					}
				}else{
					if(fieldRule.getNeedPersistence() == 0){
						sb = new StringBuilder();
						for (String result : results) {
							sb.append(result + ",");
						}
						page.putField(fieldRule.getFieldName(), StringUtils.substringBeforeLast(sb.toString().trim(), ","));
					}
					ruleComplierLoop(fieldRule.getId(),page,nextRequest,templast);
				}
				break;
			case 1:
				results = page.getHtml().xpath(fieldRule.getRule()).all();
				if(results.size() == 0){
					if(fieldRule.getAllowEmpty() == 1){
						throw new PageProcessException(String.format("{fieldname:@#%s@#,fieldrule:@#%s@#,parentId:@#%d@#,type:@#xpath@#}",fieldRule.getFieldName(),fieldRule.getRule(),fieldRule.getParentId()));
					}
				}
				//判断是否会产生新的下载请求
				if(fieldRule.getAdditionDownload() == 1){
					//将fieldRule.getId()保存下来，新请求的页面将筛选parentid为该id的规则进行解析
					for(String result : results){
						if(!result.startsWith("http")){
							result = "http://"+site.getDomain()+result.trim().replace("|", "%7C");
						}
						Request additionReq = new Request(result);
						additionReq.setFieldRuleId(fieldRule.getId());
						if(templast == null){
							nextRequest = additionReq;
							templast = nextRequest;
						}else{
							templast.setNextRequest(additionReq);
							templast = additionReq;
						}
						if(fieldRule.getNeedPersistence() == 0){
							sb = new StringBuilder();
							sb.append(result + ",");
							page.putField(fieldRule.getFieldName(), StringUtils.substringBeforeLast(sb.toString().trim(), ","));
						}
					}
				}else if(fieldRule.getAdditionRequest() == 1){
					for(String result : results){
						if(!result.startsWith("http")){
							result = "http://"+site.getDomain()+result.trim().replace("|", "%7C");
						}
						Request additionReq = new Request(result);
						additionReq.setFieldRuleId(fieldRule.getId());
						transmitResultItem(page, additionReq);
						page.addTargetRequest(additionReq);
						if(fieldRule.getNeedPersistence() == 0){
							sb = new StringBuilder();
							sb.append(result + ",");
							page.putField(fieldRule.getFieldName(), StringUtils.substringBeforeLast(sb.toString().trim(), ","));
						}
					}
				}else{
					if(fieldRule.getNeedPersistence() == 0){
						sb = new StringBuilder();
						for (String result : results) {
							sb.append(result + ",");
						}
						page.putField(fieldRule.getFieldName(), StringUtils.substringBeforeLast(sb.toString().trim(), ","));
					}
					ruleComplierLoop(fieldRule.getId(),page,nextRequest,templast);
				}
				break;
			case 2:
				results = page.getHtml().css(fieldRule.getRule()).all();
				if(results.size() == 0){
					if(fieldRule.getAllowEmpty() == 1){
						throw new PageProcessException(String.format("{fieldname:@#%s@#,fieldrule:@#%s@#,parentId:@#%d@#,type:@#css@#}",fieldRule.getFieldName(),fieldRule.getRule(),fieldRule.getParentId()));
					}
				}
				//判断是否会产生新的下载请求
				if(fieldRule.getAdditionDownload() == 1){
					//将fieldRule.getId()保存下来，新请求的页面将筛选parentid为该id的规则进行解析
					for(String result : results){
						if(!result.startsWith("http")){
							result = "http://"+site.getDomain()+result.trim().replace("|", "%7C");
						}
						Request additionReq = new Request(result);
						additionReq.setFieldRuleId(fieldRule.getId());
						if(templast == null){
							nextRequest = additionReq;
							templast = nextRequest;
						}else{
							templast.setNextRequest(additionReq);
							templast = additionReq;
						}
						if(fieldRule.getNeedPersistence() == 0){
							sb = new StringBuilder();
							sb.append(result + ",");
							page.putField(fieldRule.getFieldName(), StringUtils.substringBeforeLast(sb.toString().trim(), ","));
						}
					}
				}else if(fieldRule.getAdditionRequest() == 1){
					for(String result : results){
						if(!result.startsWith("http")){
							result = "http://"+site.getDomain()+result.trim().replace("|", "%7C");
						}
						Request additionReq = new Request(result);
						additionReq.setFieldRuleId(fieldRule.getId());
						transmitResultItem(page, additionReq);
						page.addTargetRequest(additionReq);
						if(fieldRule.getNeedPersistence() == 0){
							sb = new StringBuilder();
							sb.append(result + ",");
							page.putField(fieldRule.getFieldName(), StringUtils.substringBeforeLast(sb.toString().trim(), ","));
						}
					}
				}else{
					if(fieldRule.getNeedPersistence() == 0){
						sb = new StringBuilder();
						for (String result : results) {
							sb.append(result + ",");
						}
						page.putField(fieldRule.getFieldName(), StringUtils.substringBeforeLast(sb.toString().trim(), ","));
					}
					ruleComplierLoop(fieldRule.getId(),page,nextRequest,templast);
				}
				break;
			default:
				if(page.getRequest().getExtra(fieldRule.getFieldName()) == null){
					throw new PageProcessException(String.format("{fieldname:@#%s@#,fieldrule:@#%s@#,type:@#orig@#}",fieldRule.getFieldName(),fieldRule.getRule()));
				}
				page.putField(fieldRule.getFieldName(), page.getRequest().getExtra(fieldRule.getFieldName()));
				break;
			}
		}
		//最后判断一下当前page有没有产生新的下载请求或任务请求，如果有，将page.getResultItems()中的解析结果通过request传递到下一个page中去
		if(nextRequest!=null){
			transmitResultItem(page,nextRequest);
			page.setSkip(true);
			originalReq.setNextRequest(nextRequest);
		}
		
	}

	@Override
	public Site getSite() {
		return site;
	}
	
	/**当某个解析规则不会产生新的下载请求时(这种情况下当前page生命周期已经结束)，当前page必须解析完该规则下的所有字段，存在子规则层层嵌套的情况
	 * @param parentRuleId
	 * @param page
	 * @param nextRequest
	 * @param templast
	 * @throws PageProcessException
	 */
	private void ruleComplierLoop(int parentRuleId,Page page,Request nextRequest,Request templast) throws PageProcessException{
		
		List<SpiderFieldRule> childs = new ArrayList<SpiderFieldRule>();
		for(SpiderFieldRule fieldRule : fieldRules){
			if(fieldRule.getParentId() == parentRuleId){
				childs.add(fieldRule);
			}
		}
		if(childs.size()==0){
			return;
		}else{
			for(SpiderFieldRule child : childs){
				List<String> results;
				StringBuilder sb;
				switch (child.getType()) {
				case 0:
					results = page.getHtml().regex(child.getRule()).all();
					if(results.size() == 0){
						if(child.getAllowEmpty() == 1){
							throw new PageProcessException(String.format("{fieldname:@#%s@#,fieldrule:@#%s@#,parentId:@#%d@#,type:@#regex@#}",child.getFieldName(),child.getRule(),child.getParentId()));
						}
					}
					//判断是否会产生新的下载请求
					if(child.getAdditionDownload() == 1){
						//page.setIncludeAddition(true);
						//将fieldRule.getId()保存下来，新请求的页面将筛选parentid为该id的规则进行解析
						for(String result : results){
							if(!result.startsWith("http")){
								result = "http://"+site.getDomain()+result.trim().replace("|", "%7C");
							}
							Request additionReq = new Request(result);
							additionReq.setFieldRuleId(child.getId());
							if(templast == null){
								nextRequest = additionReq;
								templast = nextRequest;
							}else{
								templast.setNextRequest(additionReq);
								templast = additionReq;
							}
							if(child.getNeedPersistence() == 0){
								sb = new StringBuilder();
								sb.append(result + ",");
								page.putField(child.getFieldName(), StringUtils.substringBeforeLast(sb.toString().trim(), ","));
							}
						}
					}else if(child.getAdditionRequest() == 1){
						for(String result : results){
							if(!result.startsWith("http")){
								result = "http://"+site.getDomain()+result.trim().replace("|", "%7C");
							}
							Request additionReq = new Request(result);
							additionReq.setFieldRuleId(child.getId());
							page.addTargetRequest(additionReq);
							if(child.getNeedPersistence() == 0){
								sb = new StringBuilder();
								sb.append(result + ",");
								page.putField(child.getFieldName(), StringUtils.substringBeforeLast(sb.toString().trim(), ","));
							}
						}
					}else{
						if(child.getNeedPersistence() == 0){
							sb = new StringBuilder();
							for (String result : results) {
								sb.append(result + ",");
							}
							page.putField(child.getFieldName(), StringUtils.substringBeforeLast(sb.toString().trim(), ","));
						}
						ruleComplierLoop(child.getId(), page,nextRequest,templast);
					}
					break;
				case 1:
					results = page.getHtml().xpath(child.getRule()).all();
					if(results.size() == 0){
						if(child.getAllowEmpty() == 1){
							throw new PageProcessException(String.format("{fieldname:@#%s@#,fieldrule:@#%s@#,parentId:@#%d@#,type:@#xpath@#}",child.getFieldName(),child.getRule(),child.getParentId()));
						}
					}
					//判断是否会产生新的下载请求
					if(child.getAdditionDownload() == 1){
						//page.setIncludeAddition(true);
						//将fieldRule.getId()保存下来，新请求的页面将筛选parentid为该id的规则进行解析
						for(String result : results){
							if(!result.startsWith("http")){
								result = "http://"+site.getDomain()+result.trim().replace("|", "%7C");
							}
							Request additionReq = new Request(result);
							additionReq.setFieldRuleId(child.getId());
							if(templast == null){
								nextRequest = additionReq;
								templast = nextRequest;
							}else{
								templast.setNextRequest(additionReq);
								templast = additionReq;
							}
							if(child.getNeedPersistence() == 0){
								sb = new StringBuilder();
								sb.append(result + ",");
								page.putField(child.getFieldName(), StringUtils.substringBeforeLast(sb.toString().trim(), ","));
							}
						}
					}else if(child.getAdditionRequest() == 1){
						for(String result : results){
							if(!result.startsWith("http")){
								result = "http://"+site.getDomain()+result.trim().replace("|", "%7C");
							}
							Request additionReq = new Request(result);
							additionReq.setFieldRuleId(child.getId());
							page.addTargetRequest(additionReq);
							if(child.getNeedPersistence() == 0){
								sb = new StringBuilder();
								sb.append(result + ",");
								page.putField(child.getFieldName(), StringUtils.substringBeforeLast(sb.toString().trim(), ","));
							}
						}
					}else{
						if(child.getNeedPersistence() == 0){
							sb = new StringBuilder();
							for (String result : results) {
								sb.append(result + ",");
							}
							page.putField(child.getFieldName(), StringUtils.substringBeforeLast(sb.toString().trim(), ","));
						}
						ruleComplierLoop(child.getId(), page,nextRequest,templast);
					}
					break;
				case 2:
					results = page.getHtml().css(child.getRule()).all();
					if(results.size() == 0){
						if(child.getAllowEmpty() == 1){
							throw new PageProcessException(String.format("{fieldname:@#%s@#,fieldrule:@#%s@#,parentId:@#%d@#,type:@#css@#}",child.getFieldName(),child.getRule(),child.getParentId()));
						}
					}
					//判断是否会产生新的下载请求
					if(child.getAdditionDownload() == 1){
						//page.setIncludeAddition(true);
						//将fieldRule.getId()保存下来，新请求的页面将筛选parentid为该id的规则进行解析
						for(String result : results){
							if(!result.startsWith("http")){
								result = "http://"+site.getDomain()+result.trim().replace("|", "%7C");
							}
							Request additionReq = new Request(result);
							additionReq.setFieldRuleId(child.getId());
							if(templast == null){
								nextRequest = additionReq;
								templast = nextRequest;
							}else{
								templast.setNextRequest(additionReq);
								templast = additionReq;
							}
							if(child.getNeedPersistence() == 0){
								sb = new StringBuilder();
								sb.append(result + ",");
								page.putField(child.getFieldName(), StringUtils.substringBeforeLast(sb.toString().trim(), ","));
							}
						}
					}else if(child.getAdditionRequest() == 1){
						for(String result : results){
							if(!result.startsWith("http")){
								result = "http://"+site.getDomain()+result.trim().replace("|", "%7C");
							}
							Request additionReq = new Request(result);
							additionReq.setFieldRuleId(child.getId());
							page.addTargetRequest(additionReq);
							if(child.getNeedPersistence() == 0){
								sb = new StringBuilder();
								sb.append(result + ",");
								page.putField(child.getFieldName(), StringUtils.substringBeforeLast(sb.toString().trim(), ","));
							}
						}
					}else{
						if(child.getNeedPersistence() == 0){
							sb = new StringBuilder();
							for (String result : results) {
								sb.append(result + ",");
							}
							page.putField(child.getFieldName(), StringUtils.substringBeforeLast(sb.toString().trim(), ","));
						}
						ruleComplierLoop(child.getId(), page,nextRequest,templast);
					}
					break;
				default:
					if(page.getRequest().getExtra(child.getFieldName()) == null){
						throw new PageProcessException(String.format("{fieldname:@#%s@#,fieldrule:@#%s@#,type:@#orig@#}",child.getFieldName(),child.getRule()));
					}
					page.putField(child.getFieldName(), page.getRequest().getExtra(child.getFieldName()));
					break;
				}
			}
		}
	}
	
	/**
	 * 将当前页面解析到的字段传递给新的request
	 * @param page
	 * @param nextRequest
	 */
	private void transmitResultItem(Page page,Request nextRequest){
		//将当前page解析下来的字段转交到nextRequest中
		Map<String, Object> fields = page.getResultItems().getAll();
		for(Entry<String,Object> entry : fields.entrySet()){
			nextRequest.addInheritField(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * 公共的添加额外下载需求方法
	 */
	public void addLinkedRequest(Request request){
		
	}
	
	/**
	 * 公共的添加额外下载任务方法
	 */
	public void addAdditionRequest(){}
}
