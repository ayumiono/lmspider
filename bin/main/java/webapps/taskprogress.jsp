<%@page import="com.lmdna.spider.http.util.ServletUtil"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="/c.tld" prefix="c"%>
<%@ page import="com.lmdna.spider.http.util.ServletUtil"%>
<%
	ServletUtil.TaskManageJsp taskjsp = new ServletUtil.TaskManageJsp();
	taskjsp.generateTaskProgressList(application, out, request);
%>
