<%@ page contentType="text/html; charset=UTF-8"
	import="com.lmdna.spider.http.util.ServletUtil"%>
<%
	ServletUtil.NodeManageJsp nodeManageJsp = new ServletUtil.NodeManageJsp();
	nodeManageJsp.generateNodesList(application, out, request);
%>
