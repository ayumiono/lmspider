<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="/c.tld" prefix="c"%>
<!--内容-->
<div class="box">
	<div class="title">
		<h2>Spider Bizs</h2>
		<img src="/spider/static/dmadmin/gfx/title-hide.gif" class="toggle"
			alt="" />
	</div>
	<div class="content pages">
		<table class="table">
			<thead>
				<tr>
					<th>来源</th>
					<th>机器</th>
					<th>验证码</th>
					<th>验证码</th>
					<th></th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td>${verifyimg.from}</td>
					<td>${verifyimg.host}</td>
					<td><img src="${verifyimg.staticFileURL}" /></td>
					<td>
						<form id="myform">
							<input name="verifycode" type="text" /> <input name="id"
								type="hidden" value="${verifyimg.id}" />
						</form>
					</td>
					<td><a href="#" onclick="ajaxSubmit()"><img alt="submit the verify code" src="gfx/icon-edit.png"></a></td>
				</tr>
			</tbody>
		</table>
	</div>
</div>

<script type="text/javascript">
	function ajaxSubmit() {
		$.ajax({
			type : "get",
			dataType : "text",
			url : "/spider/verify/submit",
			data : $('#myform').serializeArray(),
			contentType : 'application/x-www-form-urlencoded; charset=utf-8',
			success : function(data) {
				$(".table").find("tr").remove();
				$(".table").append(data);
			}
		});
	}
</script>
</html>