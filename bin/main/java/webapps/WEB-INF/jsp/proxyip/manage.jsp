<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="/c.tld" prefix="c"%>
<body>
	<div id="search_condition">
		<form id="search_form">
		</form>
	</div>
 	<!--内容-->
 	<div id="content"></div>
<script type="text/javascript">
	$(function(){
		searchAjax(1);
	});
		
	function searchAjax(pageNo){
		$.ajax({
			type: "post",
			dataType: "text",
	        url: "${sessionScope.apppath}/spider/proxyip/list.do?pageNo="+pageNo,
	        data:$('#searchForm').serializeArray(),
	        contentType: 'application/x-www-form-urlencoded; charset=utf-8',
	        success:function(data) {
	        	data = data.replace(/\n|\r|\t/g, "");
				var jsonData = $.parseJSON(data);
				var code = jsonData.code;
				var msg = jsonData.msg;
				var content = jsonData.data.content;
				if(code===0){
					$("#content").html(content);
					$(".operation").click(function() {
						operation($(this), "post");
					});
					$(".addip").click(function(){
						TINY.box.show("${sessionScope.apppath}/proxyip/add.do",1,0,0,1)
						});
					//详情弹出层
					$(".detail").click(
							function() {
								var $this = $(this);
								var id = $this.attr("id");
								TINY.box.show("${sessionScope.apppath}/spider/proxyip/detail.do?id="+ id, 1, 0, 0, 1)
							});
				}else{
					TINY.box.show(msg,0,0,0,0,2);
					}
	        }
		});
	}

	function crawlipPopup(){
		TINY.box.show("<div><p>目前只支持抓取西祠、快代理两个网站上的免费代理IP</p><br>起始页:<input type='text' value='' name='startPage' id='startPage'><br>结束页:<input type='text' value='' name='endPage' id='endPage'><br><a onclick='crawlip()' href='#'>开始抓取</a></div>", 0, 300, 100, 0, 0);
		//TINY.box.show("${sessionScope.apppath}/spider/proxyip/crawlip.do",1, 0, 0, 1);
	}

	function crawlip(){
		var startPage = $("#startPage").val();
		var endPage = $("#endPage").val();
		TINY.box.show("${sessionScope.apppath}/spider/proxyip/crawlip.do?startPage="+startPage + "&endPage="+endPage,1, 0, 0, 1);
	}

	function detailAjax(id,pageNo){
		var action="${sessionScope.apppath}/spider/proxyip/detail.do?id="+id+"&pageNo="+pageNo;
		$("#tinycontent").load(action);
	}

</script>
</body>
</html>