package com.lmdna.spider.http.servlet;

import java.io.IOException;

import javax.servlet.GenericServlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lmdna.spider.http.util.ServletUtil;

public class SpiderSlaveNodesServlet extends GenericServlet{

	@Override
	public void service(ServletRequest req, ServletResponse res)
			throws ServletException, IOException {
		HttpServletRequest httpreq = (HttpServletRequest)req;
		HttpServletResponse httpres = (HttpServletResponse)res;
		service(httpreq,httpres);
		
	}
	
	private void service(HttpServletRequest req, HttpServletResponse res){
		ServletContext context = this.getServletContext();
		ServletUtil.NodeManageJsp nodejsp = new ServletUtil.NodeManageJsp();
		//nodejsp.generateNodesList(context, res.getOutputStream(), req);
	}
}
