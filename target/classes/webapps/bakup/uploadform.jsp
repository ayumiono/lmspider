<%@ page contentType="text/html;charset=gb2312"%>
<html>
<title>LMDNA SPIDER TASK SUBMIT</title>
<body>
	�ϴ��ļ�
	<form action="master/uploadtask?bizCode=${bizCode}" method="post" enctype="multipart/form-data">
		<table>
			<tr><td>��ѡ��Ҫ�ϴ��������ļ�</td><td><input type="file" name="upTaskfile" size="50"></td></tr>
			<tr><td>������������С</td><td><input type="text" name="rowperblock" id="rowperblock"></td></tr>
			<tr><td><input type="submit" value="�ύ"></td></tr>
		</table>
	</form>
</body>
</html>