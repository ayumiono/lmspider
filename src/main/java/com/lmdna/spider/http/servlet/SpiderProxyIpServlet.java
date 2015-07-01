package com.lmdna.spider.http.servlet;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.lmdna.spider.dao.model.SpiderBiz;
import com.lmdna.spider.dao.model.SpiderProxyIp;
import com.lmdna.spider.http.HttpServer;
import com.lmdna.spider.node.master.MasterNode;
import com.lmdna.spider.utils.SpiderCommonTool;

public class SpiderProxyIpServlet extends HttpServlet {
	public void service(HttpServletRequest request, HttpServletResponse response){
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
				List<SpiderProxyIp> proxylist = masterNode.getFacade().getAllProxyIp(parammap);
				parammap.clear();
				int totalcount = masterNode.getFacade().getProxyipCount(parammap);
				double pageCount = Math.ceil((double)totalcount/10);
				request.setAttribute("proxylist",proxylist);
				request.setAttribute("priorNo", pageNo > 1 ? pageNo - 1 : 1);
				request.setAttribute("nextNo", pageNo+1 > pageCount ? (int)pageCount : (int)pageNo+1);
				request.setAttribute("pageCount", (int)pageCount);
				RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/proxyip/list.jsp");
				dispatcher.forward(request, response);
			}else if("crawlip".equals(sub_uri)){
				final MasterNode masterNode = HttpServer.getMasterNodeFromContext(application);
				int startPage = Integer.valueOf(request.getParameter("startPage"));
				int endPage = Integer.valueOf(request.getParameter("endPage"));
				List<String> freeproxylist = null;
				for(int i=startPage;i<=endPage;i++){
					freeproxylist = SpiderCommonTool.crawlFreeProxyIp(1);
				}
				List<String> rslist = SpiderCommonTool.batchCheckIpAnonymous(freeproxylist);
				List<SpiderProxyIp> finalrslist = new ArrayList<SpiderProxyIp>();
				for(String proxyip : rslist){
					Map<?,?> ipmap = JSONObject.parseObject(proxyip);
					SpiderProxyIp t = new SpiderProxyIp();
					t.setIp(ipmap.get("ip").toString());
					t.setPort(Integer.valueOf(ipmap.get("port").toString()));
					t.setCreateTime(new Date());
					t.setUpdateTime(new Date());
					try{
						masterNode.getFacade().addProxyIp(t);//发生主键冲突的记录
						finalrslist.add(t);
					}catch(Exception e){
						if(e.getMessage().indexOf("Duplicate entry")>-1){
							continue;
						}
					}
				}
				request.setAttribute("size", finalrslist.size());
				request.setAttribute("proxyiplist", finalrslist);
				RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/jsp/proxyip/crawlip.jsp");
				rd.forward(request, response);
			}
		}catch (Throwable t) {
			t.printStackTrace();
		}finally {
		}
	}
}
