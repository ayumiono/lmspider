package com.lmdna.spider.http.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

import com.lmdna.spider.dao.model.SpiderBiz;
import com.lmdna.spider.http.HttpServer;
import com.lmdna.spider.node.master.MasterNode;
import com.lmdna.spider.node.master.SpiderBizInOperation;
import com.lmdna.spider.node.master.Task;
import com.lmdna.spider.protocol.rpc.utils.HeartBeatData;

public class ServletUtil {
	
	private static final String HTML_TAIL;
	
	static
	  {
	    HTML_TAIL = new StringBuilder().append("<hr />\nThis is <a href='http://lmdna.com/'>LMDNA</a> release ").append("\n").append("</body></html>").toString();
	  }
	
	/**
	 * 节点管理页面util类
	 * @author ayumiono
	 */
	public static class NodeManageJsp{
		/**
		 * 获取slaves node节点信息
		 * @param context
		 * @param out
		 * @param request
		 * @throws IOException 
		 */
		public void generateNodesList(ServletContext context, JspWriter out,HttpServletRequest request) throws IOException{
			final MasterNode masterNode = HttpServer.getMasterNodeFromContext(context);
			List<HeartBeatData> slaveList = masterNode.getSlaveNodeHeartBeatInfo();
			out.print("<div class='box'>");
			out.print("<div class='title'><h2>Slaves</h2><img class='toggle' src='static/dmadmin/gfx/title-hide.gif'></div>");
			out.print("<div class='content pages'>");
			if(slaveList == null || slaveList.size()==0){
				out.print("<div class='message blue'>There are no slavenodes in the cluster!</div>");
			}else{
				out.print("<table id=\"slavenodetable\"> ");
				out.print("<thead><tr><th>ip</th><th>spiders</th><th>threads</th><th>status</th><tr><thead>");
				out.print("<tbody>");
				for(HeartBeatData data : slaveList){
					out.print("<tr>");
					out.println("<td>"+data.getMachineId()+"</td>");
					out.println("<td>"+data.getSpiderCounts()+"</td>");
					out.println("<td>"+data.getActiveThreadCounts()+"</td>");
					out.println("<td>"+data.getLevel()+"</td>");
					out.print("</tr>");
				}
				out.print("</tbody>");
				out.print("</table> ");
			}
			out.print("</div>");
			out.print("</div>");
		}
	}
	
	public static class BizManageJspUtil{
		public void showBiz(ServletContext context, JspWriter out,HttpServletRequest request) throws IOException{
			final MasterNode masterNode = HttpServer.getMasterNodeFromContext(context);
			Map<String,Object> parammap = new HashMap<String,Object>();
			parammap.put("status", 0);
			List<SpiderBiz> bizlist = masterNode.getFacade().getBizList(parammap);
			out.print("<small>");
			out.print("<table id=\"biztable\" class=\"table\"> ");
			out.print("<thead><tr><th>业务代号</th><th>业务名称</th><th>url匹配条件</th><th>域名</th><th>创建时间</th><th>更新时间</th><th>操作</th></tr></thead>");
			out.print("<tbody>");
			for(SpiderBiz biz : bizlist){
				out.print("<tr>");
				out.print("<td>"+biz.getBizCode()+"</td>");
				out.print("<td>"+biz.getBizName()+"</td>");
				out.print("<td>"+biz.getUrlRule()+"</td>");
				out.print("<td>test</td>");
				out.print("<td>"+biz.getCreateTime()+"</td>");
				out.print("<td>"+biz.getUpdateTime()+"</td>");
				out.print("</tr>");
			}
			out.print("</tbody>");
			out.print("</table> ");
			out.print("</small>");
		}
	}
	
	/**
	 * 任务管理页面util类
	 * @author ayumiono
	 */
	public static class TaskManageJsp{
		public void generateTaskProgressList(ServletContext context, JspWriter out,HttpServletRequest request) throws IOException{
			final MasterNode masterNode = HttpServer.getMasterNodeFromContext(context);
			List<Task> taskProgressRecord = masterNode.getTaskProgressRecord();
			out.print("<div class='box'>");
			out.print("<div class='title'><h2>Tasks</h2><img class='toggle' src='static/dmadmin/gfx/title-hide.gif'></div>");
			out.print("<div class='content pages'>");
			if(taskProgressRecord!=null && taskProgressRecord.size()>0){
				out.print("<table id='biztable'>");
				out.print("<thead><tr><th>taskid</th><th>bizcode</th><th>total rows</th><th>total blocks</th><th>distributed rows</th><th>finished rows</th><th>left blocks</th><th>status</th></tr></thead>");
				out.print("<tbody>");
				for(Task record : taskProgressRecord){
					out.print("<tr>");
					out.print("<td>"+record.getTaskId()+"</td>");
					out.print("<td>"+record.getBizCode()+"</td>");
					out.print("<td>"+record.getTotalRow()+"</td>");
					out.print("<td>"+record.getTotalBlock()+"</td>");
					out.print("<td>"+record.getDistributedRows()+"</td>");
					out.print("<td>"+record.getFinishedRows()+"</td>");
					out.print("<td>"+record.getLeftBlock()+"</td>");
					if(record.isOver()){
						out.print("<td><span style='coloc:red'>OVER</span></td>");
					}else{
						out.print("<td><span style='color:green;'>RUNNING</span></td>");
					}
					out.print("</tr>");
				}
				out.print("</tbody>");
				out.print("</table> ");
			}else{
				out.print("<div class='message blue'>no task exist currently!</div>");
			}
			out.print("</div>");
			out.print("</div>");
		}
	}
	
	public static class MasterNodeInfoUtil{
		public void summaryOverview(ServletContext context, JspWriter out,HttpServletRequest request) throws IOException{
			final MasterNode masterNode = HttpServer.getMasterNodeFromContext(context);
			out.print("<div class='box'>");
			out.print("<div class='title'><h2>Summary</h2></div>");
			out.print("<div class='content pages'>");
			out.print("<table>");
			out.print("<tbody>");
			out.print("<tr><td>Slave Node Count</td><td>"+masterNode.getSlaveNodeHeartBeatInfo().size()+"</td></tr>");
			out.print("<tr><td>Task Count</td><td>"+masterNode.getTaskProgressRecord().size()+"</td></tr>");
			out.print("<tr><td>Spider In Operation Count</td><td>"+masterNode.getSpidersInOperation().size()+"</td></tr>");
			out.print("<tr><td>ProxyIp In Use Count</td><td>"+"</td></tr>");
			out.print("<tr><td>Account In Use Count</td><td>"+"</td></tr>");
			out.print("</tbody>");
			out.print("</table>");
			out.print("</div>");
			out.print("</div>");
		}
		public void taskProgressOverview(ServletContext context, JspWriter out,HttpServletRequest request) throws IOException{
			final MasterNode masterNode = HttpServer.getMasterNodeFromContext(context);
			out.print("<div class='box'>");
			out.print("<div class='title'><h2>Finished Task</h2></div>");
			out.print("<div class='content pages'>");
			out.print("<table>");
			out.print("<thead>");
			out.print("<tr><td><b>Task Id</b></td><td><b>Biz Code</b></td><td><b>Total Rows</b></td><td><b>Distributed Rows</b></td></tr>");
			out.print("</thead>");
			out.print("<tbody>");
			for(Task task : masterNode.finishedTask){
				out.print("<tr>");
				out.print("<td>"+task.getTaskId()+"</td>");
				out.print("<td>"+task.getBizCode()+"</td>");
				out.print("<td>"+task.getTotalRow()+"</td>");
				out.print("<td>"+task.getDistributedRows()+"</td>");
				out.print("</tr>");
			}
			out.print("</tbody>");
			out.print("</table>");
			out.print("</div>");
			out.print("</div>");
		}
		
		public void spidersInfoDetail(ServletContext context, JspWriter out,HttpServletRequest request) throws IOException{
			final MasterNode masterNode = HttpServer.getMasterNodeFromContext(context);
			List<SpiderBizInOperation> spiderlist = masterNode.getSpidersInOperation();
			out.print("<div class='box'>");
			out.print("<div class='title'><h2>Spiders</h2><img class='toggle' src='static/dmadmin/gfx/title-hide.gif'></div>");
			out.print("<div class='content pages'>");
			if(spiderlist.size()>0){
				out.print("<table id=\"slavenodetable\"> ");
				out.print("<thead><tr><td><b>Biz Code</b></td><td><b>Type</b></td><td><b>Site</b></td><td><b>Need Proxy</b></td><td><b>Need Account</b></td><td><b>Operation</b></td></tr></thead>");
				out.print("<tbody>");
				for(SpiderBizInOperation spider : spiderlist){
					out.print("<tr>");
					out.print("<td>"+spider.getBizCode()+"</td>");
					if(spider.isCommon()){
						out.print("<td>common</td>");
					}else if(spider.isJar()){
						out.print("<td>jar("+spider.getJarPath()+")</td>");
					}
					if(spider.getSpiderConfig().getWebsiteConfigBO()==null){
						out.print("<td></td>");
					}else if(spider.getSpiderConfig().getWebsiteConfigBO().getWebsiteBO()==null){
						out.print("<td></td>");
					}else{
						out.print("<td>"+spider.getSpiderConfig().getWebsiteConfigBO().getWebsiteBO().getSiteEnName()+"</td>");
					}
					if(spider.getSpiderConfig().getWebsiteConfigBO()==null){
						out.print("<td></td>");
					}else{
						try{
							if(spider.getSpiderConfig().getWebsiteConfigBO().getNeedProxy() == 0){
								out.print("<td>yes</td>");
							}else{
								out.print("<td>no</td>");
							}
						}catch(Exception e){
							System.out.println(e);
						}
						
					}
					
					if(spider.getSpiderConfig().getWebsiteConfigBO()==null){
						out.print("<td></td>");
					}else{
						if(spider.getSpiderConfig().getWebsiteConfigBO().getNeedLogin() == 0){
							out.print("<td>yes("+spider.getSpiderConfig().getWebsiteConfigBO().getLoginClass()+")</td>");
						}else{
							out.print("<td>no</td>");
						}
					}
					out.print("<td><a href=\"#\" onclick=\"submitTask('"+spider.getSpiderConfig().getBizCode()+"')\"><img src='static/dmadmin/gfx/file-export.png' title='submit task file' /></a><a href=\"#\" onclick=\"terminatespider('"+spider.getSpiderConfig().getBizCode()+"')\"><img src='static/dmadmin/gfx/stop-2.png' title='terminate the spider' /></a><a href=\"#\" onclick=\"removespider('"+spider.getSpiderConfig().getBizCode()+"')\"><img src='static/dmadmin/gfx/button-delete.png' title='remove the spider' /></a></td>");
					out.print("</tr>");
				}
				out.print("</tbody>");
				out.print("</table> ");
			}else{
				out.print("<div class='message blue'>no spider is in operation currently!</div>");
			}
			out.print("</div>");
			out.print("</div>");
		}
		
		public void spidersInfoOverview(ServletContext context, JspWriter out,HttpServletRequest request) throws IOException{
			final MasterNode masterNode = HttpServer.getMasterNodeFromContext(context);
			List<SpiderBizInOperation> spiderlist = masterNode.getSpidersInOperation();
			out.print("<div class='box'>");
			out.print("<div class='title'><h2>Spider In Operation</h2></div>");
			out.print("<div class='content pages'>");
			out.print("<table>");
			out.print("<thead>");
			out.print("<tr><td><b>Biz Code</b></td><td><b>Type</b></td><td><b>Site</b></td><td><b>Need Proxy</b></td><td><b>Need Account</b></td></tr>");
			out.print("</thead>");
			out.print("<tbody>");
			for(SpiderBizInOperation spider : spiderlist){
				out.print("<tr>");
				out.print("<td>"+spider.getBizCode()+"</td>");
				if(spider.isCommon()){
					out.print("<td>common</td>");
				}else if(spider.isJar()){
					out.print("<td>jar("+spider.getJarPath()+")</td>");
				}
				if(spider.getSpiderConfig().getWebsiteConfigBO()==null){
					out.print("<td></td>");
				}else if(spider.getSpiderConfig().getWebsiteConfigBO().getWebsiteBO()==null){
					out.print("<td></td>");
				}else{
					out.print("<td>"+spider.getSpiderConfig().getWebsiteConfigBO().getWebsiteBO().getSiteEnName()+"</td>");
				}
				if(spider.getSpiderConfig().getWebsiteConfigBO()==null){
					out.print("<td></td>");
				}else{
					try{
						if(spider.getSpiderConfig().getWebsiteConfigBO().getNeedProxy() == 0){
							out.print("<td>yes</td>");
						}else{
							out.print("<td>no</td>");
						}
					}catch(Exception e){
						e.printStackTrace();
					}
					
				}
				
				if(spider.getSpiderConfig().getWebsiteConfigBO()==null){
					out.print("<td></td>");
				}else{
					if(spider.getSpiderConfig().getWebsiteConfigBO().getNeedLogin() == 0){
						out.print("<td>yes("+spider.getSpiderConfig().getWebsiteConfigBO().getLoginClass()+")</td>");
					}else{
						out.print("<td>no</td>");
					}
				}
				out.print("</tr>");
			}
			out.print("</tbody>");
			out.print("</table>");
			out.print("</div>");
			out.print("</div>");
		}
		public void proxyipInfoOverview(ServletContext context, JspWriter out,HttpServletRequest request){
			final MasterNode masterNode = HttpServer.getMasterNodeFromContext(context);
		}
		public void accountInfoOverview(ServletContext context, JspWriter out,HttpServletRequest request){
			final MasterNode masterNode = HttpServer.getMasterNodeFromContext(context);
		}
		public void slaveNodesOverview(ServletContext context, JspWriter out,HttpServletRequest request){
			final MasterNode masterNode = HttpServer.getMasterNodeFromContext(context);
		}
		public void showVerifyImgs(ServletContext context, JspWriter out,HttpServletRequest request){
			final MasterNode masterNode = HttpServer.getMasterNodeFromContext(context);
		}
	}
	
	public static String printFooter(){
		return HTML_TAIL;
	}
}
