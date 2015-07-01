<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="/c.tld" prefix="c"%>
{"code":${code},
"msg":"${msg}",
"data":{"content":"
<a href='${sessionScope.apppath}biz/edit.do?bizId=${model.id}'>编辑</a>|
<c:if test='${code==0}'>
<c:choose>
<c:when test='${type==0}'>
<span style='color:grey'>载入|重新载入|重新开始|</span>
<a href='#' id='${bizId}' name='shutdown' class='operation'>暂停</a>|
<a href='#' id='${bizId}' name='remove' class='operation'>移除</a>|
<a href='${sessionScope.apppath}biz/preview.do?bizId=${model.id}'>结果预览</a>
</c:when>
<c:when test='${type==1}'>
<span style='color:grey'>载入|重新载入|</span>
<a href='#' id='${bizId}' name='start' class='operation'>重新开始</a>|
<span style='color:grey'>暂停|</span>
<a href='#' id='${bizId}' name='remove' class='operation'>移除</a>|
<span style='color:grey'>结果预览</span>
</c:when>
<c:when test='${type==2}'>
<span style='color:grey'>载入|</span>
<a href='#' id='${bizId}' name='reload' class='operation'>重新载入</a>|
<span style='color:grey'>重新开始|暂停|移除|结果预览</span>
</c:when>
</c:choose>
</c:if>
"}}