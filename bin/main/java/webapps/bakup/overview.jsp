<%@ page
  contentType="text/html; charset=UTF-8"
  import="com.lmdna.spider.http.util.ServletUtil"
%>
<%!
  //for java.io.Serializable
  private static final long serialVersionUID = 1L;
%>
<% ServletUtil.MasterNodeInfoUtil master = new ServletUtil.MasterNodeInfoUtil();
   master.summaryOverview(application, out, request); 
   master.taskProgressOverview(application, out, request);
   master.spidersInfoOverview(application, out, request);
   out.println(ServletUtil.printFooter());%>
