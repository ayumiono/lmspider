<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html;charset=utf-8"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
<title>LMSpider Platform</title>
<style type="text/css">
	@import url("static/dmadmin/css/style.css");
	@import url('static/dmadmin/css/style_text.css');
	@import url('static/dmadmin/css/c-grey.css');
		/* COLOR FILE CAN CHANGE TO c-blue.ccs, c-grey.ccs, c-orange.ccs, c-purple.ccs or c-red.ccs */
	@import url('static/dmadmin/css/datepicker.css');
	@import url('static/dmadmin/css/form.css');
	@import url('static/dmadmin/css/menu.css');
	@import url('static/dmadmin/css/messages.css');
	@import url('static/dmadmin/css/statics.css');
	@import url('static/dmadmin/css/tabs.css');
	@import url('static/dmadmin/css/wysiwyg.css');
	@import url('static/dmadmin/css/wysiwyg.modal.css');
	@import url('static/dmadmin/css/wysiwyg-editor.css');
</style>

<script type="text/javascript" src="static/dmadmin/js/jquery-1.6.1.min.js"></script>

</head>

<body>

	<div class="wrapper">
		<div class="container">
			<div class="top">
				<div class="split">
					<h1>LMSpider Platform</h1>
				</div>
				<div class="split">
					<div class="logout">
						<img src="static/dmadmin/gfx/icon-logout.gif" align="left" alt="Logout" /> <a
							href="#">logout</a>
					</div>
					<div>
						<img src="static/dmadmin/gfx/icon-welcome.gif" align="left" alt="Welcome" />Welcome,Admin
					</div>
				</div>
			</div>
			<div class="menu">
				<ul>
					<li class="current"><a onclick="master()" href="#">Overview</a></li>
					<li class="break"></li>
					<li><a onclick="getslavenodes()" href="#">Slave Nodes</a></li>
					<li class="break"></li>
					<li><a onclick="spiderinoperation()" href="#">Spider In Operation</a></li>
					<li class="break"></li>
					<li><a onclick="getTaskProgress()" href="#">Task Progress</a></li>
					<li class="break"></li>
					<li><a href="#">Spider Source</a>
						<ul>
							<li class="current"><a onclick="getproxyip(1)" href="#">Proxy IP</a></li>
							<li class="current"><a onclick="getwebsiteaccount()" href="#">Website Account</a></li>
						</ul></li>
					<li class="break"></li>
					<li><a onclick="getverifyimg()" href="#">Verify Images</a></li>
					<li class="break"></li>
					<li><a onclick="getbiz(1)" href="#">Spider Bizs Manage</a></li>
				</ul>
			</div>
			<div class="holder">
			</div>
			<div class="footer">
				<div class="split">
					&#169; Copyright <a href="http://lmdna.com/">柠檬数据</a>
				</div>
				<div class="split right">
					Powered by <a href="http://lmdna.com/">LMDNA</a>
				</div>
			</div>
		</div>
	</div>

	<script type="text/javascript" src="static/dmadmin/js/jquery-ui.js"></script>
	<script type="text/javascript" src="static/dmadmin/js/jquery.pngFix.js"></script>
	<script type="text/javascript" src="static/dmadmin/js/hoverIntent.js"></script>
	<script type="text/javascript" src="static/dmadmin/js/superfish.js"></script>
	<script type="text/javascript" src="static/dmadmin/js/supersubs.js"></script>
	<script type="text/javascript" src="static/dmadmin/js/date.js"></script>
	<script type="text/javascript" src="static/dmadmin/js/jquery.sparkbox-select.js"></script>
	<script type="text/javascript" src="static/dmadmin/js/jquery.datepicker.js"></script>
	<script type="text/javascript" src="static/dmadmin/js/jquery.filestyle.mini.js"></script>
	<script type="text/javascript" src="static/dmadmin/js/jquery.flot.js"></script>
	<script type="text/javascript" src="static/dmadmin/js/jquery.graphtable-0.2.js"></script>
	<script type="text/javascript" src="static/dmadmin/js/jquery.wysiwyg.js"></script>
	<script type="text/javascript" src="static/dmadmin/js/plugins/wysiwyg.rmFormat.js"></script>
	<script type="text/javascript" src="static/dmadmin/js/controls/wysiwyg.link.js"></script>
	<script type="text/javascript" src="static/dmadmin/js/controls/wysiwyg.table.js"></script>
	<script type="text/javascript" src="static/dmadmin/js/controls/wysiwyg.image.js"></script>
	<script type="text/javascript" src="static/dmadmin/js/inline.js"></script>
	<script type="text/javascript" src="static/artDialog/artDialog.js?skin=default"></script>
	<script type="text/javascript" src="static/script/ajaxfileupload.js"></script>
	<script src="http://rawgithub.com/ajaxorg/ace-builds/master/src-noconflict/ace.js" type="text/javascript" charset="utf-8"></script>
	<script type="text/javascript">
	$(document).ready(function(){
		field_index=0;
		getbiz(1);
		$(".menu").find("ul li").last().attr("class","sfHover");
	});
	
	function master(){
		$.ajax({
			type: "get",
			dataType: "text",
	        url: "/spider/master/overview",
	        contentType: 'application/x-www-form-urlencoded; charset=utf-8',
	        success:function(data) {
	        	$(".holder").html(data);
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
	        	$(".holder").html(data);
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
	        	$(".holder").html(data);
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
	        	$(".holder").html(data);
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
	        	$(".holder").html(data);
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
	        	$(".holder").html(data);
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
	        	$(".holder").html(data);
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
</body>

</html>
