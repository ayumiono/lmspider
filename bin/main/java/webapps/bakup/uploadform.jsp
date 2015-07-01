<%@ page contentType="text/html;charset=gb2312"%>
<html>
<title>LMDNA SPIDER TASK SUBMIT</title>
<body>
	上传文件
	<form action="master/uploadtask?bizCode=${bizCode}" method="post" enctype="multipart/form-data">
		<table>
			<tr><td>请选择要上传的任务文件</td><td><input type="file" name="upTaskfile" size="50"></td></tr>
			<tr><td>请输入任务块大小</td><td><input type="text" name="rowperblock" id="rowperblock"></td></tr>
			<tr><td><input type="submit" value="提交"></td></tr>
		</table>
	</form>
</body>
</html>