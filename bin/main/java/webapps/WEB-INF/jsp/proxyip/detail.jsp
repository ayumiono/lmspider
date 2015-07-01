<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="/c.tld" prefix="c"%>
<!-- START TABULAR DATA -->
<body>
<div style="padding: 10px; border: 1px dotted #785;">
	<table width="100%" border="0" cellspacing="0" cellpadding="0"
				class="zstable2">
	<tr>
		<th scope="col">业务名</th>
		<th scope="col">连续失败次数</th>
		<th scope="col">成功次数</th>
		<th scope="col">使用次数</th>
		<th scope="col">失效次数</th>
		<th scope="col">更新时间</th>
	</tr>
	<c:forEach items="${infoList}" var="o">
		<tr>
			<td>${o.bizName}</td>
			<td>${o.failedNum}</td>
			<td>${o.successNum}</td>
			<td>${o.borrowedNum}</td>
			<td>${o.deadNum}</td>
			<td><fmt:formatDate value="${o.updateTime}" pattern="yyyy-MM-dd HH:mm:ss"></fmt:formatDate><br/></td>
		</tr>
	</c:forEach>
		
	</table>
	<div class="pagebox">
<p class="r">
<a href="#" onclick="detailAjax(${id},1)" title="">首页</a> 
<a href="#" onclick="detailAjax(${id},${pb.priorNo})" title="">上页</a> <a 
href="#" onclick="detailAjax(${id},${pb.nextNo})" title="">下页</a> 
<a href="#" onclick="detailAjax(${id},${pb.pageCount})" title="">末页</a>&nbsp;第${pb.pageNo}页&nbsp;共 ${pb.pageCount}页&nbsp;跳转到 
<input type="text" class="input3" id="goToPage" /> 页 
<input type="button" value="确定" class="btn4" id="btn4" />
	<script type="text/javascript">
			var $goToPage = $('#goToPage');
				$(document).ready(function() {$("#btn4").click(function() {
					var goToPage = $goToPage.val();
					if ($.trim(goToPage) != "") {
						detailAjax(${id},goToPage);
						return false;
					}
				});
			});
	</script>
</p>
</div>
</div>

	<script type="text/javascript">
	</script>
</body>
</html>