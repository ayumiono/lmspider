<%@ page contentType="text/html;charset=gb2312"%>
<html>
<title>LMDNA SPIDER TASK SUBMIT</title>
<body>
	�ϴ��ļ�
	<form action="biz/uploadjar" method="post" enctype="multipart/form-data">
		<%-- ����enctype��multipart/form-data���������԰��ļ��е�������Ϊ��ʽ�����ϴ���������ʲô�ļ����ͣ������ϴ���--%>
		��ѡ��Ҫ�ϴ���jar���ļ�<input type="file" name="upTaskfile" size="50">
		<br>
		<input type="submit" value="�ύ">
	</form>
</body>
</html>