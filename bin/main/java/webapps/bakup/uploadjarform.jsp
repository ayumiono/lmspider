<%@ page contentType="text/html;charset=gb2312"%>
<html>
<title>LMDNA SPIDER TASK SUBMIT</title>
<body>
	上传文件
	<form action="biz/uploadjar" method="post" enctype="multipart/form-data">
		<%-- 类型enctype用multipart/form-data，这样可以把文件中的数据作为流式数据上传，不管是什么文件类型，均可上传。--%>
		请选择要上传的jar包文件<input type="file" name="upTaskfile" size="50">
		<br>
		<input type="submit" value="提交">
	</form>
</body>
</html>