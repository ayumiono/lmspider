<%@ page
  contentType="text/html; charset=UTF-8"
  import="com.lmdna.spider.http.util.ServletUtil"
%>
<%!
  //for java.io.Serializable
  private static final long serialVersionUID = 1L;
%>

<!DOCTYPE html>
<html>
<link rel="stylesheet" type="text/css" href="/static/hadoop.css">
<title>lmdna-spider&nbsp;</title>
<% ServletUtil.NodeManageJsp nodeManageJsp = new ServletUtil.NodeManageJsp();%>
<body>
<h1></h1>
<br />
<hr>
<% nodeManageJsp.generateNodesList(application, out, request); %>
<%out.println(ServletUtil.printFooter());%>
<script type="text/javascript">
	
</script>
