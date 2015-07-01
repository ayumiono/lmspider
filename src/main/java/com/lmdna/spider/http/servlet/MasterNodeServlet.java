package com.lmdna.spider.http.servlet;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;

import com.lmdna.spider.http.HttpServer;
import com.lmdna.spider.node.master.MasterNode;
import com.lmdna.spider.node.master.SpiderBizInOperation;
import com.lmdna.spider.node.master.Task;
import com.lmdna.spider.utils.SpiderGlobalConfig;

public class MasterNodeServlet extends HttpServlet{
	
	public void service(HttpServletRequest request, HttpServletResponse response){
		ServletContext application = null;
		try{
			response.setContentType("text/html; charset=UTF-8");
			application = this.getServletContext();
		    String uri = request.getRequestURI();
			String sub_uri = StringUtils.substringAfterLast(uri, "/");
			if("overview".equals(sub_uri)){
				RequestDispatcher red = request.getRequestDispatcher("/overview.jsp");
				red.forward(request, response);
			}else if("spiderinoperation".equals(sub_uri)){
				final MasterNode masterNode = HttpServer.getMasterNodeFromContext(application);
				List<SpiderBizInOperation> list = masterNode.getSpidersInOperation();
				request.setAttribute("spiderlist", list);
				RequestDispatcher red = request.getRequestDispatcher("/WEB-INF/jsp/spiderinoperation/list.jsp");
				red.forward(request, response);
			}else if("newtask".equals(sub_uri)){
				String bizCode = request.getParameter("bizCode");
				String taskFilePath = request.getParameter("taskFilePath");
				String taskFileName = request.getParameter("taskFileName");
				int rowPerBlock = Integer.valueOf(request.getParameter("rowPerBlock"));
				final MasterNode masterNode = HttpServer.getMasterNodeFromContext(application);
				if(!masterNode.spiderExist(bizCode)){
					response.getWriter().println("爬虫系统中找不到对应的spider!请联系管理员");
					return;
				}
				String temp = request.getParameter("temp");
				masterNode.submitTask(bizCode,taskFilePath,taskFileName,rowPerBlock);
			}else if("uploadtask".equals(sub_uri)){
				String bizCode = request.getParameter("bizCode");
				String rowperblock = request.getParameter("rowPerBlock");
				Integer rowPerBlock = null;
				rowPerBlock = Integer.parseInt(rowperblock);
				final MasterNode masterNode = HttpServer.getMasterNodeFromContext(application);
				if(!masterNode.spiderExist(bizCode)){
					response.getWriter().println("爬虫系统中找不到对应的spider!请联系管理员");
					return;
				}
				DiskFileItemFactory factory = new DiskFileItemFactory();
				factory.setSizeThreshold(20 * 1024);
				ServletFileUpload fileUpload = new ServletFileUpload(factory);
				try {
					response.setHeader("Cache-Control", "no-cache");
					request.setCharacterEncoding("UTF-8");
					fileUpload.setFileSizeMax(1024*1024*500);
					try {
						List<FileItem> fileItemList = fileUpload.parseRequest(request);
						Iterator<FileItem> fileItemIterator = fileItemList.iterator();
						FileItem fileItem = null;
						while (fileItemIterator.hasNext()) {
							fileItem = fileItemIterator.next();
							if (!fileItem.isFormField()) {
								String taskFileName = fileItem.getName();
								SimpleDateFormat dateFormat = new SimpleDateFormat();
								dateFormat.applyPattern("yyyyMMdd");
								String taskFilePath = SpiderGlobalConfig.getValue(SpiderGlobalConfig.SPIDER_TASKFILE_UPLOAD_DIR)+dateFormat.format(new Date())+File.separator+taskFileName;
								File file = new File(taskFilePath);
								if (!file.getParentFile().exists()) {
									file.getParentFile().mkdirs();
								}
								fileItem.write(file);
								try{
									Task record = masterNode.submitTask(bizCode,taskFilePath,taskFileName,rowPerBlock);
									response.getWriter().println("文件上传成功!");
									response.getWriter().println("<table>");
									response.getWriter().println("<tr>");
									response.getWriter().println("<td>biz code:</td>");
									response.getWriter().println("<td>"+record.getBizCode()+"</td>");
									response.getWriter().println("</tr>");
									response.getWriter().println("<tr>");
									response.getWriter().println("<td>file name:</td>");
									response.getWriter().println("<td>"+record.getOriginalTaskFileName()+"</td>");
									response.getWriter().println("</tr>");
									response.getWriter().println("<tr>");
									response.getWriter().println("<td>file path:</td>");
									response.getWriter().println("<td>"+record.getOriginalTaskFilePath()+"</td>");
									response.getWriter().println("</tr>");
									response.getWriter().println("<tr>");
									response.getWriter().println("<td>total rows:</td>");
									response.getWriter().println("<td>"+record.getTotalRow()+"</td>");
									response.getWriter().println("</tr>");
									response.getWriter().println("<tr>");
									response.getWriter().println("<td>row per block:</td>");
									response.getWriter().println("<td>"+record.rowPerBlock()+"</td>");
									response.getWriter().println("</tr>");
									response.getWriter().println("<tr>");
									response.getWriter().println("<td>blocks:</td>");
									response.getWriter().println("<td>"+record.getTotalBlock()+"</td>");
									response.getWriter().println("</tr>");
									response.getWriter().println("</table>");
									return;
								}catch(Throwable t){
									response.getWriter().println(t.getMessage());
								}
							}
						}
						return;
					} catch (FileUploadException e) {
						response.getWriter().write("文件太大！为使后台运行流畅，建议拆分后再上传！");
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
			}else if("terminate".equals(sub_uri)){
				final MasterNode masterNode = HttpServer.getMasterNodeFromContext(application);
				String bizCode = request.getParameter("bizCode");
			}else if("remove".equals(sub_uri)){
				final MasterNode masterNode = HttpServer.getMasterNodeFromContext(application);
				String bizCode = request.getParameter("bizCode");
			}else if("pause".equals(sub_uri)){
				final MasterNode masterNode = HttpServer.getMasterNodeFromContext(application);
				String bizCode = request.getParameter("bizCode");
			}
		}catch(Throwable t){
			t.printStackTrace();
		}finally{
			
		}
	}
}
