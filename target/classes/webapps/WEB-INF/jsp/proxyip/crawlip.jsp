<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="/c.tld" prefix="c"%>
<body>
	<div>
		<label> 成功添加了${size}个代理IP </label>
		<table>
			<tr>
				<th scope="col">地址</th>
				<th scope="col">端口号</th>
			</tr>
			<c:forEach items='${proxyiplist}' var='model'>
				<tr>
					<td>${model.ip}</td>
					<td>${model.port}</td>
				</tr>
			</c:forEach>
		</table>
	</div>
</body>
</html>