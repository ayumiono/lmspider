package com.lmdna.spider.http.servlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.GenericServlet;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lmdna.spider.dao.model.SpiderBiz;
import com.lmdna.spider.dao.model.SpiderFieldRule;
import com.lmdna.spider.dao.model.SpiderSiteCharset;
import com.lmdna.spider.dao.model.SpiderWebsite;
import com.lmdna.spider.dao.model.SpiderWebsiteConfig;
import com.lmdna.spider.http.HttpServer;
import com.lmdna.spider.node.SpiderNode;
import com.lmdna.spider.node.master.MasterNode;
import com.lmdna.spider.pipeline.MyServletPipeline;
import com.lmdna.spider.utils.SpiderGlobalConfig;

public class SpiderBizServlet extends GenericServlet {

	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ServletContext application = null;
		try{
			response.setContentType("text/html; charset=UTF-8");
			application = this.getServletContext();
		    String uri = request.getRequestURI();
			String sub_uri = StringUtils.substringAfterLast(uri, "/");
			if("show".equals(sub_uri)){
				String pageNoStr = request.getParameter("pageNo");
				int pageNo = StringUtils.isEmpty(pageNoStr) ? 1 : Integer.parseInt(pageNoStr);
				final MasterNode masterNode = HttpServer.getMasterNodeFromContext(application);
				Map<String,Object> parammap = new HashMap<String,Object>();
				parammap.put("startRow", (pageNo-1)*10);
				parammap.put("pageSize", 10);
				List<SpiderBiz> bizlist = masterNode.getFacade().getBizList(parammap);
				parammap.clear();
				int totalcount = masterNode.getFacade().getBizCount(parammap);
				double pageCount = Math.ceil((double)totalcount/10);
				request.setAttribute("bizlist",bizlist);
				request.setAttribute("priorNo", pageNo > 1 ? pageNo - 1 : 1);
				request.setAttribute("nextNo", pageNo+1 > pageCount ? (int)pageCount : (int)pageNo+1);
				request.setAttribute("pageCount", (int)pageCount);
				List<SpiderWebsite>  t = masterNode.getFacade().getAllWebsite();
				request.setAttribute("websites", t);
				RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/biz/list.jsp");
				dispatcher.forward(request, response);
			}else if("edit".equals(sub_uri)){
				String bizId = request.getParameter("bizId");
				final MasterNode masterNode = HttpServer.getMasterNodeFromContext(application);
				SpiderBiz biz = masterNode.getFacade().getBiz(Integer.valueOf(bizId));
				List<SpiderFieldRule> fieldList = masterNode.getFacade().getFieldRuleByBizId(Integer.valueOf(bizId));
				List<FieldRule> fieldFormBeanList = new ArrayList<FieldRule>();
				for(SpiderFieldRule field : fieldList){
					FieldRule rule = new FieldRule();
					rule.setId(field.getId());
					rule.setAdditionalReq(field.getAdditionDownload());
					rule.setAllowEmpty(field.getAllowEmpty());
					rule.setName(field.getFieldName());
					rule.setNeedPersistence(field.getNeedPersistence());
					for(SpiderFieldRule field2 : fieldList){
						if(field2.getId().equals(String.valueOf(field.getParentId()))){
							rule.setParent(field2.getFieldName());
						}
					}
					rule.setResponseValidCheck(field.getResponseValidCheck());
					rule.setType(field.getType());
					rule.setRule(field.getRule());
					fieldFormBeanList.add(rule);
				}
				SpiderWebsiteConfig websiteConfig = new SpiderWebsiteConfig();
				Map<String,Object> parammap = new HashMap<String,Object>();
				List<SpiderSiteCharset> charsetList = masterNode.getFacade().getSiteCharset(parammap);
				if(biz != null){
					request.setAttribute("biz", biz);
					request.setAttribute("fieldList", fieldFormBeanList);
					request.setAttribute("antiPolicy", biz.getWebsiteConfigBO());
					request.setAttribute("charsetList", charsetList);
					RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/biz/edit.jsp");
					dispatcher.forward(request, response);
				}
			}else if("intputcheck".equals(sub_uri)){
				final MasterNode masterNode = HttpServer.getMasterNodeFromContext(application);
				String type = request.getParameter("type");
				String val = request.getParameter("val");
				String flag = request.getParameter("flag");
				switch(Integer.parseInt(type)){
					case 1:
						if("1".equals(flag)){
							break;
						}
						if(StringUtils.isEmpty(val)){
							response.getWriter().print("网站中文名不能为空");
							break;
						}
						SpiderWebsite t2 = new SpiderWebsite();
						t2.setSiteChnName(val);
						t2 = masterNode.getFacade().getWebsite(t2);
						if(t2!=null){
							response.getWriter().print("重复");
							break;
						}
						break;
					case 2:
						if("1".equals(flag)){
							break;
						}
						if(StringUtils.isEmpty(val)){
							response.getWriter().print("网站英文名不能为空");
							break;
						}
						SpiderWebsite t = new SpiderWebsite();
						t.setSiteEnName(val);
						t = masterNode.getFacade().getWebsite(t);
						if(t!=null){
							response.getWriter().print("重复");
						}
						break;
					case 3:
						if("1".equals(flag)){
							break;
						}
						if(StringUtils.isEmpty(val)){
							response.getWriter().print("domain不能为空");
							break;
						}
						SpiderWebsite t3 = new SpiderWebsite();
						t3.setDomain(val);
						t3 = masterNode.getFacade().getWebsite(t3);
						if(t3!=null){
							response.getWriter().print("重复");
						}
						break;
					case 4:
						if(StringUtils.isEmpty(val)){
							response.getWriter().print("业务代号不能为空");
							break;
						}
						Map<String,Object> parammap = new HashMap<String,Object>();
						parammap.put("bizCode", val);
						List<SpiderBiz> result = masterNode.getFacade().getBizList(parammap);
						if(result.size()!=0){
							response.getWriter().print("重复");
						}
						break;
					case 5:
						if(StringUtils.isEmpty(val)){
							response.getWriter().print("业务名不能为空");
							break;
						}
						Map<String,Object> parammap2 = new HashMap<String,Object>();
						parammap2.put("bizName", val);
						List<SpiderBiz> result2 = masterNode.getFacade().getBizList(parammap2);
						if(result2.size()!=0){
							response.getWriter().print("重复");
						}
						break;
				}
			}else if("save".equals(sub_uri)){
				final MasterNode masterNode = HttpServer.getMasterNodeFromContext(application);
				String website_flag = request.getParameter("website_flag");
				String useProxy_flag = request.getParameter("useProxy_flag");
				String useAccount_flag = request.getParameter("useAccount_flag");
				int website_id = 0;
				if(Integer.valueOf(website_flag) == 0){
					String sitechnname = request.getParameter("sitechnname");
					String siteenname = request.getParameter("siteenname");
					String charset = request.getParameter("charset");
					String domain = request.getParameter("domain");
					SpiderWebsite website = new SpiderWebsite();
					website.setSiteChnName(sitechnname);
					website.setSiteEnName(siteenname);
					website.setDomain(domain);
					website.setCharset(charset);
					website_id = masterNode.getFacade().addWebsite(website);
				}else{
					String websiteid = request.getParameter("websiteid");
					website_id = Integer.valueOf(websiteid);
				}
				String cycleRetryTimes = request.getParameter("cycleRetryTimes");
				String sleepTime = request.getParameter("sleepTime");
				String retryTimes = request.getParameter("retryTimes");
				Integer proxyIpCount = null;
				Integer proxyIpLoadCount = null;
				Integer ipReuseInterval = null;
				Integer ipStatReportInterval = null;
				Integer ipReviveinTime = null;
				Integer failedTimes = null;
				Integer deadTimes = null;
				Integer maxVisitPerIp = null;
				if(Integer.valueOf(useProxy_flag) == 0){
					proxyIpCount = Integer.valueOf(request.getParameter("proxyIpCount"));
					proxyIpLoadCount = Integer.valueOf(request.getParameter("proxyIpLoadCount"));
					ipReuseInterval = Integer.valueOf(request.getParameter("ipReuseInterval"));
					ipStatReportInterval = Integer.valueOf(request.getParameter("ipStatReportInterval"));
					ipReviveinTime = Integer.valueOf(request.getParameter("ipReviveinTime"));
					failedTimes = Integer.valueOf(request.getParameter("failedTimes"));
					deadTimes = Integer.valueOf(request.getParameter("deadTimes"));
					maxVisitPerIp = Integer.valueOf(request.getParameter("maxVisitPerIp"));
				}
				Integer accountCount = null;
				Integer accountLoadCount = null;
				String loginClass = null;
				Integer maxVisitPerAccount = null;
				if(Integer.valueOf(useAccount_flag) == 0){
					accountCount = Integer.valueOf(request.getParameter("accountCount"));
					accountLoadCount = Integer.valueOf(request.getParameter("accountLoadCount"));
					loginClass = request.getParameter("loginClass");
					maxVisitPerAccount = Integer.valueOf(request.getParameter("maxVisitPerAccount"));
				}
				SpiderWebsiteConfig config = new SpiderWebsiteConfig();
				config.setCycleRetryTimes(Integer.valueOf(cycleRetryTimes));
				config.setSleepTime(Integer.valueOf(sleepTime));
				config.setRetryTimes(Integer.valueOf(retryTimes));
				config.setNeedProxy(Integer.valueOf(useProxy_flag));
				config.setDeadTimes(deadTimes);
				config.setProxyIpCount(proxyIpCount);
				config.setProxyIpLoadCount(proxyIpLoadCount);
				config.setIpReuseInterval(ipReuseInterval);
				config.setIpStatReportInterval(ipStatReportInterval);
				config.setIpReviveinTime(ipReviveinTime);
				config.setFailedTimes(failedTimes);
				config.setMaxVisitPerIp(maxVisitPerIp);
				config.setNeedLogin(Integer.valueOf(useAccount_flag));
				config.setLoginClass(loginClass);
				config.setMaxVisitPerAccount(maxVisitPerAccount);
				config.setAccountCount(accountCount);
				config.setAccountLoadCount(accountLoadCount);
				config.setWebsite(website_id);
				int config_id = masterNode.getFacade().addWebsiteConfig(config);
				
				String bizCode = request.getParameter("bizCode");
				String bizName = request.getParameter("bizName");
				String urlRule = request.getParameter("urlRule");
				String responseValidCheck = request.getParameter("pageResponseValidCheck");
				String persistenceTable = request.getParameter("persistenceTable");
				SpiderBiz biz = new SpiderBiz();
				biz.setBizCode(bizCode);
				biz.setBizName(bizName);
				biz.setUrlRule(urlRule);
				biz.setWebsiteConfig(config_id);
				biz.setPersistenceTable(persistenceTable);
				biz.setResponseValidCheck(responseValidCheck);
				int biz_id = masterNode.getFacade().addBiz(biz);
				
				String[] namearr = request.getParameterValues("name");
				String[] parentarr = request.getParameterValues("parent");
				String[] typearr = request.getParameterValues("type");
				String[] rulearr = request.getParameterValues("rule");
				String[] responseValidCheckarr = request.getParameterValues("responseValidCheck");
				String[] additionalReqarr = request.getParameterValues("additionalReq");
				String[] additionalDownloadarr = request.getParameterValues("additionalDownload");
				String[] allowEmptyarr = request.getParameterValues("allowEmpty");
				String[] needPersistencearr = request.getParameterValues("needPersistence");
				List<FieldRule> fieldRuleList = new ArrayList<FieldRule>();
				if(namearr!=null){
					for(int i = 0;i<namearr.length;i++){
						FieldRule rule = new FieldRule();
						rule.setName(namearr[i]);
						rule.setAdditionalReq(Integer.valueOf(additionalReqarr[i]));
						rule.setAdditionalDownload(Integer.valueOf(additionalDownloadarr[i]));
						rule.setAllowEmpty(Integer.valueOf(allowEmptyarr[i]));
						rule.setBizId(biz_id);
						rule.setNeedPersistence(Integer.valueOf(needPersistencearr[i]));
						rule.setParent(parentarr[i]);
						rule.setResponseValidCheck(responseValidCheckarr[i]);
						rule.setRule(rulearr[i]);
						rule.setType(Integer.valueOf(typearr[i]));
						fieldRuleList.add(rule);
					}
					Map<String,Integer> parentIds = new HashMap<String,Integer>();
					Iterator<FieldRule> iterator = fieldRuleList.iterator();
					while(iterator.hasNext()){
						FieldRule rule = iterator.next();
						String parent = rule.getParent();
						if(StringUtils.isEmpty(parent)){
							SpiderFieldRule spiderFieldRule = new SpiderFieldRule();
							spiderFieldRule.setBizId(rule.getBizId());
							spiderFieldRule.setFieldName(rule.getName());
							spiderFieldRule.setRule(rule.getRule());
							spiderFieldRule.setType(rule.getType());
							spiderFieldRule.setResponseValidCheck(rule.getResponseValidCheck());
							spiderFieldRule.setAdditionRequest(rule.getAdditionalReq());
							spiderFieldRule.setAdditionDownload(rule.getAdditionalDownload());
							spiderFieldRule.setAllowEmpty(rule.getAllowEmpty());
							spiderFieldRule.setNeedPersistence(rule.getNeedPersistence());
							spiderFieldRule.setParentId(0);
							int id = masterNode.getFacade().addFieldRule(spiderFieldRule);
							parentIds.put(spiderFieldRule.getFieldName(), id);
							iterator.remove();
							iterator = fieldRuleList.iterator();
						}else{
							Integer parentid = parentIds.get(parent);
							if(parentid == null){
								continue;
							}else{
								SpiderFieldRule spiderFieldRule = new SpiderFieldRule();
								spiderFieldRule.setBizId(rule.getBizId());
								spiderFieldRule.setFieldName(rule.getName());
								spiderFieldRule.setRule(rule.getRule());
								spiderFieldRule.setType(rule.getType());
								spiderFieldRule.setResponseValidCheck(rule.getResponseValidCheck());
								spiderFieldRule.setAdditionRequest(rule.getAdditionalReq());
								spiderFieldRule.setAdditionDownload(rule.getAdditionalDownload());
								spiderFieldRule.setAllowEmpty(rule.getAllowEmpty());
								spiderFieldRule.setNeedPersistence(rule.getNeedPersistence());
								spiderFieldRule.setParentId(parentid);
								int id = masterNode.getFacade().addFieldRule(spiderFieldRule);
								parentIds.put(spiderFieldRule.getFieldName(), id);
								iterator.remove();
								iterator = fieldRuleList.iterator();
							}
						}
					}
					response.sendRedirect("/spider");
				}else{
					response.getWriter().print("没有添加网页抓取字段规则!");
				}
				
			}else if("detail".equals(sub_uri)){
				String bizId = request.getParameter("bizId");
				final MasterNode masterNode = HttpServer.getMasterNodeFromContext(application);
				SpiderBiz biz = masterNode.getFacade().getBiz(Integer.valueOf(bizId));
				List<SpiderFieldRule> fieldList = masterNode.getFacade().getFieldRuleByBizId(Integer.valueOf(bizId));
				request.setAttribute("biz", biz);
				request.setAttribute("fieldList", fieldList);
				request.setAttribute("website", biz.getWebsiteConfigBO().getWebsiteBO());
				request.setAttribute("antiPolicy", biz.getWebsiteConfigBO());
				RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/biz/detail.jsp");
				dispatcher.forward(request, response);
			}else if("check".equals(sub_uri)){
				final MasterNode masterNode = HttpServer.getMasterNodeFromContext(application);
				String param = request.getParameter("param");
				JSONObject paramap = JSONObject.parseObject(param);
				String url = paramap.getString("url");
				String bizcode = paramap.getString("bizcode");
				String bizname = paramap.getString("bizname");
				int website_flag = paramap.getIntValue("website_flag");
				SpiderWebsite website = null;
				String charset = "";
				if(website_flag == 0){
					charset = paramap.get("charset").toString();
					website = new SpiderWebsite();
					website.setSiteEnName("temp");
					website.setSiteChnName("临时测试网站");
					website.setCharset(charset);
					website.setDomain("temp");
				}else if(website_flag == 1){
					int websiteid = paramap.getIntValue("websiteid");
					website = masterNode.getFacade().getWebsite(websiteid);
				}
				JSONArray fieldrules = (JSONArray)paramap.get("fieldrules");
				Object[] arr = fieldrules.toArray();
				
				List<FieldRule> fieldRuleList = new ArrayList<FieldRule>();
				for(Object o : arr){
					FieldRule rule = new FieldRule();
					rule.setName(((JSONObject)o).get("name").toString());
					rule.setAdditionalReq((Integer)(((JSONObject)o).get("additionalreq")));
					rule.setAdditionalDownload((Integer)(((JSONObject)o).get("additionaldownload")));
					rule.setAllowEmpty((Integer)(((JSONObject)o).get("allowempty")));
					rule.setBizId(1);
					rule.setNeedPersistence((Integer)(((JSONObject)o).get("persistence")));
					rule.setParent(((JSONObject)o).get("parent").toString());
					rule.setResponseValidCheck(((JSONObject)o).get("validcheck").toString());
					rule.setRule(((JSONObject)o).get("rule").toString());
					rule.setType((Integer)(((JSONObject)o).get("type")));
					fieldRuleList.add(rule);
				}
				final List<SpiderFieldRule> rulesList = new ArrayList<SpiderFieldRule>();
				Iterator<FieldRule> iterator = fieldRuleList.iterator();
				Map<String,Integer> parentIds = new HashMap<String,Integer>();
				while(iterator.hasNext()){
					int id = 1;
					FieldRule rule = iterator.next();
					String parent = rule.getParent();
					if(StringUtils.isEmpty(parent)){
						SpiderFieldRule spiderFieldRule = new SpiderFieldRule();
						spiderFieldRule.setBizId(rule.getBizId());
						spiderFieldRule.setFieldName(rule.getName());
						spiderFieldRule.setRule(rule.getRule());
						spiderFieldRule.setType(rule.getType());
						spiderFieldRule.setResponseValidCheck(rule.getResponseValidCheck());
						spiderFieldRule.setAdditionRequest(rule.getAdditionalReq());
						spiderFieldRule.setAdditionDownload(rule.getAdditionalDownload());
						spiderFieldRule.setAllowEmpty(rule.getAllowEmpty());
						spiderFieldRule.setNeedPersistence(rule.getNeedPersistence());
						spiderFieldRule.setParentId(0);
						spiderFieldRule.setId(id);
						rulesList.add(spiderFieldRule);
						id = id + 1;
						parentIds.put(spiderFieldRule.getFieldName(), id);
						iterator.remove();
						iterator = fieldRuleList.iterator();
					}else{
						Integer parentid = parentIds.get(parent);
						if(parentid == null){
							continue;
						}else{
							SpiderFieldRule spiderFieldRule = new SpiderFieldRule();
							spiderFieldRule.setBizId(rule.getBizId());
							spiderFieldRule.setFieldName(rule.getName());
							spiderFieldRule.setRule(rule.getRule());
							spiderFieldRule.setType(rule.getType());
							spiderFieldRule.setResponseValidCheck(rule.getResponseValidCheck());
							spiderFieldRule.setAdditionRequest(Integer.valueOf(rule.getAdditionalReq()));
							spiderFieldRule.setAllowEmpty(Integer.valueOf(rule.getAllowEmpty()));
							spiderFieldRule.setNeedPersistence(rule.getNeedPersistence());
							spiderFieldRule.setParentId(parentid);
							spiderFieldRule.setId(id);
							rulesList.add(spiderFieldRule);
							parentIds.put(spiderFieldRule.getFieldName(), id);
							iterator.remove();
							iterator = fieldRuleList.iterator();
						}
					}
				}
				SpiderWebsiteConfig websiteConfig = new SpiderWebsiteConfig();
				SpiderBiz biz = new SpiderBiz();
				biz.setBizCode(bizcode);
				biz.setBizName(bizname);
				websiteConfig.setNeedProxy(1);
				websiteConfig.setRetryTimes(2);
				websiteConfig.setSleepTime(3000);
				websiteConfig.setWebsiteBO(website);
				websiteConfig.setNeedLogin(0);
				biz.setWebsiteConfigBO(websiteConfig);
				biz.setFieldRules(rulesList);
				Spider spider = SpiderNode.fixSpider(biz);
				spider.addRequest(new Request(url));
				MyServletPipeline pipeline = new MyServletPipeline(response.getOutputStream());
				spider.addPipeline(pipeline);
				spider.thread(1);
				spider.setExitWhenComplete(true);
				spider.setUUID(biz.getBizCode());
				spider.run();
			}else if("load".equals(sub_uri)){
				String bizId = request.getParameter("bizId");
				final MasterNode masterNode = HttpServer.getMasterNodeFromContext(application);
				try{
					masterNode.loadSpider(Integer.valueOf(bizId));
					response.getWriter().write("{\"result\":\"success\"}");
				}catch(Exception e){
					if(e.getMessage().contains("no biz exist")){
						response.getWriter().write("{\"result\":\"failed\",\"msg\":\"no biz config exist\"}");
					}
				}
			}else if("uploadjar".equals(sub_uri)){
				final MasterNode masterNode = HttpServer.getMasterNodeFromContext(application);
				DiskFileItemFactory factory = new DiskFileItemFactory();
				factory.setSizeThreshold(20 * 1024);
				ServletFileUpload fileUpload = new ServletFileUpload(factory);
				try {
					response.setHeader("Cache-Control", "no-cache");
					request.setCharacterEncoding("UTF-8");
					fileUpload.setFileSizeMax(102400 * 102400);
					try {
						List<FileItem> fileItemList = fileUpload.parseRequest(request);
						Iterator<FileItem> fileItemIterator = fileItemList.iterator();
						FileItem fileItem = null;
						while (fileItemIterator.hasNext()) {
							fileItem = fileItemIterator.next();
							if (!fileItem.isFormField()) {
								String fileName = fileItem.getName();
								String fileType = getFileExt(fileName); // 获取文件后缀
								if (fileType == null || fileType.equals("")) {
									response.getWriter().write("仅能上传.jar文件!");
									return;
								}
								String jarFilePath = SpiderGlobalConfig.getValue(SpiderGlobalConfig.SPIDER_JAR_UPLOAD_DIR)+fileName;
								File file = new File(jarFilePath);
								if (!file.getParentFile().exists()) {
									file.getParentFile().mkdirs();
								}
								fileItem.write(file);
								try{
									masterNode.loadSpider(jarFilePath,fileName);
								}catch(Throwable t){
									response.getWriter().println(t.getMessage());
								}
								response.getWriter().println("jar包上传成功!");
								return;
							}
						}
						response.getWriter().write("文件不存在!");
						return;
					} catch (FileUploadException e) {
						response.getWriter().write("文件太大！");
						return;
					} catch (Exception e) {
						response.getWriter().write("数据处理错误:"+e.getMessage());
						return;
					}
				} catch (IOException e) {
					response.getWriter().write("数据处理错误:上传文件失败");
					return;
				} finally {
					if (response != null) {
						response = null;
					}
					if (fileUpload != null) {
						fileUpload = null;
					}
					if (factory != null) {
						factory = null;
					}
				}
			}else if("editcode".equals(sub_uri)){
				RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/biz/code-editor.jsp");
				dispatcher.forward(request, response);
			}
		}catch (Throwable t) {
			t.printStackTrace();
		}finally {
			
		}
	}
	
	private String getFileExt(String fileName) {
		String fileType = "jar";
		if (fileName == null)
			return null;
		else {
			String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1);
			if (fileType.indexOf(fileExt) >= 0) {
				return fileExt;
			} else {
				return null;
			}
		}
	}

	@Override
	public void service(ServletRequest req, ServletResponse res)
			throws ServletException, IOException {
		HttpServletRequest httpreq = (HttpServletRequest)req;
		HttpServletResponse httpres = (HttpServletResponse)res;
		service(httpreq,httpres);
	}
}
