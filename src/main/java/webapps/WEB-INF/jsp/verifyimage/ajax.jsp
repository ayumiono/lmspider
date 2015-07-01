<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!--内容-->
<tr>
			<td>${verifyimg.from}</td>
			<td>${verifyimg.host}</td>
			<td><img src="${verifyimg.staticFileURL}" /></td>
			<td>
				<form id="myform">
					<input name="verifycode" type="text" />
					<input name="id" type="hidden" value="${verifyimg.id}"/>
				</form>
			</td>
			<td>
				<a href="#" onclick="ajaxSubmit()">提交</a>|
				<a href="#" onclick="ajaxSubmitAndNext()">提交并获取下一个</a>
			</td>
		</tr>
</html>