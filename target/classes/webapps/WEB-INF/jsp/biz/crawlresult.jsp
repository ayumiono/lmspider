<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="/c.tld" prefix="c"%>
<!-- START TABULAR DATA -->
<style type="text/css">
#tab2 {
	font-size: 13px;
	color: #555;
}

#tab2 p {
	line-height: 27px;
	color: #555;
}

#tab2 label {
	font-weight: bold;
	color: #555;
	display: inline-block;
	padding-right: 5px;
}

#tab_domain_list  th, #tab_domain_list td {
	border: 1px solid #dbdbdb;
	text-align: center;
	line-height: 18px;
	font-size: 13px;
	color: #333;
}

#tab_domain_list  th {
	background-color: #f2f2f2;
	font-weight: bold;
}

.zstable2 tr td label {
	font-weight: bold;
}

.zstable2 th {
	text-align: left;
}
</style>
<body>
	<div>
		<label>抓取结果:</label>
		<div style="padding: 10px; border: 1px dotted #785;">
			<table style='width: 100%; border: 0; cellspacing: 0; cellpadding: 0'
				class="zstable2">
				<c:forEach items="${crawlResult}" var="entry">
					<tr>
						<td>${entry.key}</td>
						<td>${entry.value}</td>
					</tr>
				</c:forEach>
			</table>
		</div>
	</div>
</body>
</html>