<%@ page contentType="text/html;charset=gb2312"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="X-UA-Compatible" content="IE=9" />
<link rel="stylesheet" type="text/css" href="/spider/static/bootstrap-3.0.2/css/bootstrap.min.css" />
<link rel="stylesheet" type="text/css" href="/spider/static/hadoop.css" />
<link rel="stylesheet" type="text/css" href="/spider/static/tinybox.css" />
<script type="text/javascript" src="/spider/static/script/jquery-1.7.2.min.js"></script>
<script type="text/javascript" src="/spider/static/script/jquery-ui-1.10.3.custom.min.js"></script>
<script type="text/javascript" src="/spider/static/artDialog/artDialog.js?skin=black"></script> 
<title>LMDNA SPIDER PLATFORM</title>
</head>
<body>
	<header class="navbar navbar-inverse bs-docs-nav" role="banner">
	<div class="container">
		<div class="navbar-header">
			<div class="navbar-brand">LMDNA SPIDER</div>
		</div>
		<ul class="nav navbar-nav" id="ui-tabs">
			<li><a href="#" onclick="master()">Overview</a></li>
			<li><a href="#" onclick="getslavenodes()">Slave Nodes</a></li>
			<li><a href="#" onclick="spiderinoperation()">Spider In Operation</a></li>
			<li><a href="#" onclick="getTaskProgress()">TaskProgress</a></li>
			<li><a href="#" onclick="getproxyip(1)">ProxyIp</a></li>
			<li><a href="#" onclick="getwebsiteaccount()">Website Account</a></li>
			<li><a href="#" onclick="getverifyimg()">VerifyImages</a></li>
			<li><a href="#" onclick="getbiz(1)">Spider Bizs Manage</a></li>
		</ul>
	</div>
	</header>
	<div id="maincontent" class="container">
	</div>
</body>
<script src="/spider/static/jquery-1.10.2.min.js" type="text/javascript"></script>
<script src="/spider/static/tinybox.js" type="text/javascript"></script>
<script type="text/javascript">
	$(function(){
		field_index=0;
		getbiz(1);
	});
	
	function master(){
		$.ajax({
			type: "get",
			dataType: "text",
	        url: "/spider/master/overview",
	        contentType: 'application/x-www-form-urlencoded; charset=utf-8',
	        success:function(data) {
	        	$("#maincontent").html(data);
	        }
		});
	}
	
	function getverifyimg(){
		$.ajax({
			type: "get",
			dataType: "text",
	        url: "/spider/verify/get",
	        contentType: 'application/x-www-form-urlencoded; charset=utf-8',
	        success:function(data) {
	        	$("#maincontent").html(data);
	        }
		});
	}
	
	function spiderinoperation(){
		$.ajax({
			type: "get",
			dataType: "text",
	        url: "/spider/master/spiderinoperation",
	        contentType: 'application/x-www-form-urlencoded; charset=utf-8',
	        success:function(data) {
	        	$("#maincontent").html(data);
	        }
		});
	}
	
	function getbiz(pageNo){
		$.ajax({
			type: "get",
			dataType: "text",
	        url: "/spider/biz/show?pageNo="+pageNo,
	        contentType: 'application/x-www-form-urlencoded; charset=utf-8',
	        success:function(data) {
	        	$("#maincontent").html(data);
				$(".detail").click(
						function() {
							var $this = $(this);
							var id = $this.attr("id");
							location.href = "/spider/biz/detail?bizId="+id;
				});
	        }
		});
	}
	function getproxyip(pageno){
		$.ajax({
			type: "get",
			dataType: "text",
	        url: "/spider/proxyip/show?pageNo="+pageno,
	        contentType: 'application/x-www-form-urlencoded; charset=utf-8',
	        success:function(data) {
	        	$("#maincontent").html(data);
	        }
		});
	}
	function getslavenodes(){
		$.ajax({
			type: "get",
			dataType: "text",
	        url: "/spider/slavenodes.jsp",
	        contentType: 'application/x-www-form-urlencoded; charset=utf-8',
	        success:function(data) {
	        	$("#maincontent").html(data);
	        }
		});
	}
	function getwebsiteaccount(){
		
	}
	function getTaskProgress(){
		$.ajax({
			type: "get",
			dataType: "text",
	        url: "/spider/taskprogress.jsp",
	        contentType: 'application/x-www-form-urlencoded; charset=utf-8',
	        success:function(data) {
	        	$("#maincontent").html(data);
	        }
		});
	}
	
	function crawlipPopup(){
		TINY.box.show("<div><p>目前只支持抓取西祠、快代理两个网站上的免费代理IP</p><br>起始页:<input type='text' value='' name='startPage' id='startPage'><br>结束页:<input type='text' value='' name='endPage' id='endPage'><br><a onclick='crawlip()' href='#'>开始抓取</a></div>", 0, 500, 200, 0, 0);
	}

	function crawlip(){
		var startPage = $("#startPage").val();
		var endPage = $("#endPage").val();
		TINY.box.show("/spider/proxyip/crawlip?startPage="+startPage + "&endPage="+endPage,1, 0, 0, 1);
	}

	function detailAjax(id,pageNo){
		var action="${sessionScope.apppath}/spider/proxyip/detail.do?id="+id+"&pageNo="+pageNo;
		$("#tinycontent").load(action);
	}
	
	function alertMsg(msg){
		return '<font color="red">'+msg+'</font>';	 
	 }

</script>
</html>