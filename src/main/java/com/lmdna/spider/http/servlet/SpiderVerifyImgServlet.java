package com.lmdna.spider.http.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.lmdna.spider.node.master.MasterNode;
import com.lmdna.spider.node.master.VerifyImgBean;

public class SpiderVerifyImgServlet extends HttpServlet {
	
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ServletContext application = null;
		try{
			response.setContentType("text/html; charset=UTF-8");
			application = this.getServletContext();
		    String uri = request.getRequestURI();
			String sub_uri = StringUtils.substringAfterLast(uri, "/");
			if("get".equals(sub_uri)){
				final MasterNode masterNode = (MasterNode) application.getAttribute("master.node");
				VerifyImgBean img = masterNode.displayOneVerifyImg();
				if(img == null){
					response.getWriter().write("<div class='box'>");
					response.getWriter().write("<div class='title'><h2>Images</h2><img class='toggle' src='static/dmadmin/gfx/title-hide.gif'></div>");
					response.getWriter().write("<div class='content pages'>");
					response.getWriter().write("<div class='message blue'>no verifyimgs exist currently!</div>");
					response.getWriter().write("</div>");
					response.getWriter().write("</div>");
				}else{
					request.setAttribute("verifyimg", img);
					RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/verifyimage/verify.jsp");
					dispatcher.forward(request, response);
				}
			}else if("submit".equals(sub_uri)){
				final MasterNode masterNode = (MasterNode) application.getAttribute("master.node");
				String verifyCode = request.getParameter("verifycode");
				String id = request.getParameter("id");
				Map<String,Object> paramMap = new HashMap<String,Object>();
				paramMap.put("id", id);
				paramMap.put("verifycode", verifyCode);
				masterNode.submitVerifyCode(Integer.valueOf(id), verifyCode);
				VerifyImgBean img = masterNode.displayOneVerifyImg();
				if(img==null){
					response.getWriter().write("<tr>no verifyimgs exist currently!</tr>");
				}else{
					request.setAttribute("verifyimg", img);
					RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/verifyimage/ajax.jsp");
					dispatcher.forward(request, response);
				}
			}
		}catch(Throwable t){
			t.printStackTrace();
		}finally{
			
		}
	}
}
