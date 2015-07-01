<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="/c.tld" prefix="c"%>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=9" />
<link rel="stylesheet" type="text/css" href="/spider/static/bootstrap-3.0.2/css/bootstrap.min.css" />
<link rel="stylesheet" type="text/css" href="/spider/static/hadoop.css" />
<title>LMDNA SPIDER PLATFORM - BIZ DETAIL INFO</title>
</head>
<body>
	<div class="container">
		<div class="page-header">
			<h1>Biz Info</h1>
		</div>
		<table class="table table-bordered table-striped">
				<tr>
					<td><label>业务ID：</label></td>
					<td>${biz.id}</td>
				</tr>
				<tr>
					<td><label>业务代号:</label></td>
					<td>${biz.bizCode}</td>
				</tr>
				<tr>
					<td><label>业务名:</label></td>
					<td>${biz.bizName}</td>
				</tr>
				<tr>
					<td><label>域名:</label></td>
					<td>${website.domain}</td>
				</tr>
				<tr>
					<td><label>URL规则:</label></td>
					<td>${biz.urlRule}</td>
				</tr>
				<tr>
					<td><label>网站编码规则:</label></td>
					<td>${website.charset}</td>
				</tr>
				<tr>
					<td><label>数据表:</label></td>
					<td>${biz.persistenceTable}</td>
				</tr>
				<tr>
					<td><label>状态：</label></td>
					<td><c:choose>
							<c:when test="${biz.status == 0}">
							还未被加载
							</c:when>
							<c:when test="${biz.status == 1}">
							<span style="color:green">正在运行</span>
							</c:when>
							<c:when test="${biz.status == 2}">
							已经被停止
							</c:when>
							<c:when test="${biz.status == 3}">
							<span style="color:red">已经被移除</span>
							</c:when>
						</c:choose></td>
				</tr>
			</table>
			<div class="page-header">
				<h1>Page Parse Rule Info</h1>
			</div>
		<table class="table table-bordered table-striped">
				<tr>
					<th><label>字段名</label></th>
					<th><label>类型</label></th>
					<th><label>父字段</label></th>
					<th><label>匹配规则</label></th>
				</tr>
				<c:forEach items="${fieldList}" var="field">
					<tr>
						<td><label>${field.fieldName}</label></td>
						<td>
						<c:choose>
							<c:when test="${field.type == 0}">
							正则
							</c:when>
							<c:when test="${field.type == 1}">
							xpath
							</c:when>
							<c:when test="${field.type == 2}">
							css
							</c:when>
							<c:when test="${field.type == 3}">
							元信息
							</c:when>
						</c:choose></td>
						<td><label>${field.parentId}</label></td>
						<td><textarea readonly rows="1" style="height:30px;width:600px;resize:none;" class="input_2" >${field.rule}</textarea></td>
					</tr>
				</c:forEach>
			</table>
		<div class="page-header">
				<h1>Anti Policy Info</h1>
			</div>
		<table class="table table-bordered table-striped">
				<tr>
					<td><label>是否使用代理IP:</label></td>
					<td><c:if test="${antiPolicy.needProxy == 1}">
					<span style="color:red">不使用代理IP</span>
					</c:if> <c:if test="${antiPolicy.needProxy == 0}">
					使用代理IP
					</c:if></td>
				</tr>
				<c:if test="${antiPolicy.needProxy== 1 }">
					<tr>
						<td><label>使用多少代理IP:</label></td>
						<td>-</td>
					</tr>
					<tr>
						<td><label>每次加载多少个代理IP:</label></td>
						<td>-</td>
					</tr>
					<tr>
						<td><label>代理IP代用间隔(毫秒)：</label></td>
						<td>-</td>
					</tr>
					<tr>
						<td><label>代理IP提交报告周期(分钟):</label></td>
						<td>-</td>
					</tr>
					<tr>
						<td><label>失效代理IP复活时间(小时):</label></td>
						<td>-</td>
					</tr>
					<tr>
						<td><label>代理IP连续失败次数阀值:</label></td>
						<td>-</td>
					</tr>
					<tr>
						<td><label>代理IP失效次数阀值:</label></td>
						<td>-</td>
					</tr>
				</c:if>
				<c:if test="${antiPolicy.needProxy==0}">
					<tr>
						<td><label>使用多少代理IP:</label></td>
						<td>${antiPolicy.proxyIpCount}</td>
					</tr>
					<tr>
						<td><label>每次加载多少个代理IP:</label></td>
						<td>${antiPolicy.proxyIpLoadCount}</td>
					</tr>
					<tr>
						<td><label>代理IP代用间隔(毫秒)：</label></td>
						<td>${antiPolicy.ipReuseInterval}</td>
					</tr>
					<tr>
						<td><label>代理IP提交报告周期(分钟):</label></td>
						<td>${antiPolicy.ipStatReportInterval}</td>
					</tr>
					<tr>
						<td><label>失效代理IP复活时间(小时):</label></td>
						<td>${antiPolicy.ipReviveinTime}</td>
					</tr>
					<tr>
						<td><label>代理IP连续失败次数阀值:</label></td>
						<td>${antiPolicy.failedTimes}</td>
					</tr>
					<tr>
						<td><label>代理IP失效次数阀值:</label></td>
						<td>${antiPolicy.deadTimes}</td>
					</tr>
				</c:if>
				<tr>
					<td><label>抓取失败重试次数:</label></td>
					<td>${antiPolicy.retryTimes}</td>
				</tr>
			</table>
	</div>
	<!-- DATA EDN -->
	<script type="text/javascript">
		
	</script>
</body>
</html>