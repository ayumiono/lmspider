<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="/c.tld" prefix="c"%>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=9" />
<link rel="stylesheet" type="text/css" href="/spider/static/bootstrap-3.0.2/css/bootstrap.min.css" />
<link rel="stylesheet" type="text/css" href="/spider/static/hadoop.css" />
<link rel="stylesheet" type="text/css" href="/spider/static/tinybox.css" />
<title>LMDNA SPIDER PLATFORM - ADD BIZ</title>
</head>
<body>
	<span id="escape" style="display:none;"></span>
	<!--内容-->
	<div class="container">
		<form id="new_biz_form" action="/spider/biz/save" enctype="application/x-www-form-urlencoded" method="post" name="new_biz_form">
			<div class="page-header">
				<h1>
					<small>网站信息</small>
				</h1>
			</div>
			<input type="radio" name="website_flag" value="0" checked onclick="switchWebsiteType()" />新建网站信息 
			<input type="radio" name="website_flag" onclick="switchWebsiteType()" value="1" />使用原有网站信息
			<div id="newwebsite">
				<table class="table-bordered table-striped">
					<tr>
						<td>网站中文名</td>
						<td>
							<input type="text" style="width: 370px;" id="sitechnname" name="sitechnname">
							<span style="color:red"></span>
						</td>
					</tr>
					<tr>
						<td>网站英文名</td>
						<td>
							<input type="text" style="width: 370px;" id="siteenname" onFocus="intputcheck(1)" name="siteenname">
							<span style="color:red"></span>	
						</td>
					</tr>
					<tr>
						<td>域名</td>
						<td>
							<input type="text" style="width: 370px;" id="domain" onFocus="intputcheck(2)" name="domain" />
							<span style="color:red"></span>
						</td>
					</tr>
					<tr>
						<td>页面编码</td>
						<td><select id="charset" name="charset">
								<option value="utf-8">utf-8</option>
								<option value="gbk">gbk</option>
						</select></td>
					</tr>
				</table>
			</div>
			<div id="useoldwebsite" style="display:none">
				<select id="websiteid" name="websiteid">
					<c:forEach items='${websites}' var='model'>
						<option value="${model.id}">${model.siteChnName}</option>
					</c:forEach>
				</select>
				</div>
			<div class="page-header">
				<h1>
					<small>业务明细</small>
				</h1>
			</div>
			<table class="table-bordered table-striped">
				<tr>
					<td valign="top">业务代号</td>
					<td>
						<input type="text" style="width: 370px;" id="bizCode" onFocus="intputcheck(3)" name="bizCode" />
						<span style="color:red" id="codemsgspan"></span>
					</td>
				</tr>
				<tr>
					<td valign="top">业务名称</td>
					<td>
						<input type="text" style="width: 370px;" id="bizName" onFocus="intputcheck(4)" name="bizName" />
						<span style="color:red" id="namemsgspan"></span>
					</td>
				</tr>
				<tr>
					<td>url匹配规则</td>
					<td><input type="text" style="width: 370px;" id="urlRule" onFocus="intputcheck(5)"  name="urlRule" /><span id="urlmsgspan"></span></td>
				</tr>
				<tr>
					<td>页面有效性验证字段</td>
					<td><input type="text" style="width: 370px;" id="responseValidCheck" name="pageResponseValidCheck" /></td>
				</tr>
				<tr>
					<td>抓取处理类</td>
					<td><input type="text" style="width: 370px;" id="responseValidCheck" name="taskProcessClass" /></td>
				</tr>
				<tr>
					<td>结果表</td>
					<td><input type="text" style="width: 370px;" id="persistenceTable" name="persistenceTable" style="width: 370px;" /><span
						id="tablemsgspan"></span></td>
				</tr>
			</table>
			<div class="page-header">
				<h1>
					<small>网页解析规则</small>
				</h1>
			</div>
			<div>
				<table id="fieldlist" class="table-bordered table-striped">
					<tr>
							<th width="15%">fieldname</th>
							<th width="15%">parent</th>
							<th width="10%">type</th>
							<th>rule</th>
						</tr>
						<tr id="addfieldbtn">
							<td style="text-align: right" colspan="4">
								<a href="#" class="gbtn" onclick="newField()">新增</a> 
							</td>
						</tr>
				</table>
				<a href="#" class="gbtn" onclick="test()">测试</a>
			</div>
			<div class="page-header">
				<h1>
					<small>反监控规则</small>
				</h1>
			</div>
			<div>
				<div id="common_anti_policy">
					<table class="table-bordered table-striped">
						<tr>
							<td>抓取失败重试次数(biz):</td>
							<td><input type="text" name="cycleRetryTimes" value="0" /></td>
						</tr>
						<tr>
							<td>抓取失败重试次数(retryhandler):</td>
							<td><input type="text" name="retryTimes" value="2" /></td>
						</tr>
						<tr>
							<td>抓取间隔:</td>
							<td><input type="text" name="sleepTime" value="1000" /></td>
						</tr>
					</table>
				</div>
				是否使用代理IP: <input type="radio" name="useProxy_flag" value="0"  onclick="switchProxy()" />使用代理IP
				 			<input type="radio" name="useProxy_flag" onclick="switchProxy()" checked value="1" />不使用代理IP
				<div id="proxy"></div>
				是否需要账号: <input type="radio" name="useAccount_flag" value="0"  onclick="switchAccount()" />需要账号 
						  <input type="radio" name="useAccount_flag" onclick="switchAccount()" checked value="1" />不需要账号
				<div id="account"></div>
			</div>
			<button id="complete" onclick="formCheck()">完成</button>
		</form>
	</div>
	
	<script src="/spider/static/jquery-1.10.2.min.js" type="text/javascript"></script>
	<script src="/spider/static/tinybox.js" type="text/javascript"></script>
	<script type="text/javascript">
	$(function(){
		field_index=0;
	});
	
	function intputcheck(type){
		var flag = $("input[name='website_flag']:checked").val();
		var obj;
		if(type==1){
			obj = $("input#sitechnname");
		}else if(type==2){
			obj = $("input#siteenname");
		}else if(type==3){
			obj = $("input#domain");
		}else if(type==4){
			obj = $("input#bizCode");
		}else if(type==5){
			obj = $("input#bizName");
		}
		var input = obj.val();
		$.ajax({
			url:'/spider/biz/intputcheck',
	        type:'get',
	        data:{"val":input,"type":type,"flag":flag},
	        dataType:'text',
	        success:function(data){
	        	obj.parent().find("span").html(data);
	        	if(data!==""){
	        		$("#complete").attr("disabled",true); 
	        	}
		    }
		});
	}
	
	function switchSpiderType(){
		var flag = $("input[name='spider_type_flag']:checked").val();
		if (flag === "1") {
			$("div[id='hasproxy']").hide();
			$("div[id='hasnoproxy']").show();
		} else if (flag === "0") {
			$("div[id='hasproxy']").show();
			$("div[id='hasnoproxy']").hide();
		}
	}
	
	function switchWebsiteType(){
		var flag = $("input[name='website_flag']:checked").val();
		if (flag === "1") {
			$("div[id='newwebsite']").hide();
			$("div[id='useoldwebsite']").show();
		} else if (flag === "0") {
			$("div[id='newwebsite']").show();
			$("div[id='useoldwebsite']").hide();
		}
	}
	
	function switchProxy() {
		var flag = $("input[name='useProxy_flag']:checked").val();
		var hasproxy = "<table class=\"table-bordered table-striped\">"+
							"<tr><td>使用多少代理IP:</td><td><input type=\"text\" name=\"proxyIpCount\" value=\"30\"  /></td></tr>"+
							"<tr><td>每次加载多少个代理IP:</td><td><input type=\"text\" name=\"proxyIpLoadCount\" value=\"10\" /></td></tr>"+
							"<tr><td>代理IP代用间隔(毫秒)：</td><td><input type=\"text\" name=\"ipReuseInterval\" value=\"1000\" /></td></tr>"+
							"<tr><td>代理IP提交报告周期(分钟):</td><td><input type=\"text\" name=\"ipStatReportInterval\" value=\"10\" /></td></tr>"+
							"<tr><td>失效代理IP复活时间(小时):</td><td><input type=\"text\" name=\"ipReviveinTime\" value=\"1\" /></td></tr>"+
							"<tr><td>代理IP连续失败次数阀值:</td><td><input type=\"text\" name=\"failedTimes\" value=\"10\" /></td></tr>"+
							"<tr><td>同一IP访问总次数阀值(一小时内):</td><td><input type=\"text\" name=\"maxVisitPerIp\" value=\"10000\" /></td></tr>"+
							"<tr><td>代理IP失效次数阀值:</td><td><input type=\"text\" name=\"deadTimes\" value=\"2\" /></td></tr>"+
							"</table>";
		var hasnoproxy = "<table class=\"table-bordered table-striped\">"+
								"<tr><td>使用多少代理IP:</td><td>-</td></tr>"+
								"<tr><td>每次加载多少个代理IP:</td><td>-</td></tr>"+
								"<tr><td>代理IP使用间隔(毫秒)：</td><td>-</td></tr>"+
								"<tr><td>代理IP提交报告周期(分钟):</td><td>-</td></tr>"+
								"<tr><td>失效代理IP复活时间(小时):</td><td>-</td></tr>"+
								"<tr><td>代理IP连续失败次数阀值:</td><td>-</td></tr>"+
								"<tr><td>代理IP失效次数阀值:</td><td>-</td></tr></table>";
		if (flag === "1") {
			$("div[id='proxy']").html("");
		} else if (flag === "0") {
			$("div[id='proxy']").html(hasproxy);
		}
	}

	function switchAccount(){
		var flag = $("input[name='useAccount_flag']:checked").val();
		var useaccount = "<table class=\"table-bordered table-striped\">"+
							"<tr><td>登录类:</td><td><input type=\"text\" name=\"loginClass\" value=\"\" /></td></tr>"+
							"<tr><td>使用多少账号:</td><td><input type=\"text\" name=\"accountCount\" value=\"20\" /></td></tr>"+
							"<tr><td>每次加载多少账号:</td><td><input type=\"text\" name=\"accountLoadCount\" value=\"10\" /></td></tr>"+
							"<tr><td>同一账号访问总次数阀值(一小时内):</td><td><input type=\"text\" name=\"maxVisitPerAccount\" value=\"10000\" /></td></tr>"+
							"</table>"
		if (flag === "1") {
			$("div[id='account']").html("");
		} else if (flag === "0") {
			$("div[id='account']").html(useaccount);
		}
	}
	
	function bizCodeUniqCheck(){
		var bizCode = $("#bizCode").val();
		$.ajax({
			url:'/spider/biz/bizCodeUniqCheck',
	        type:'get',
	        data:{"bizCode":bizCode},
	        dataType:'text',
	        success:function(data){
	        	var code = data.code;
	        	var msg = data.msg;
	        	$("span#codemsgspan").html(msg);
		    }
		});
	}
	
	function newField(){
		 TINY.box.show("<div><table style='width:100%'>"+
				 "<tr><td>字段名</td><td><input style='height:20px;width:500px' id='new_name' type='text' /></td></tr>"+
				 "<tr><td>字段规则</td><td><input style='height:20px;width:500px' id='new_rule' type='text' /></td></tr>"+
				"<tr><td>有效性验证</td><td><input style='height:20px;width:500px' id='new_validcheck' type='text' /></td></tr>"+
				"<tr><td>父字段</td><td><input style='height:20px;width:500px' id='new_parent' type='text' /></td></tr>"+
				"<tr><td>类型</td>"+
				 	"<td><select id='new_type'>"+
				 		"<option value='0'>正则</option>"+
				 		"<option value='1'>xpath</option>"+
				 		"<option value='2'>css</option>"+
				 		"<option value='3'>元信息</option></select></td></tr>"+
				"<tr><td colspan='2'>"+
				"<input name='new_additionalreq' id='new_additionalreq' value='0' type='radio' checked/>不产生新任务请求"+
				"<input name='new_additionalreq' id='new_additionalreq' value='1' type='radio' />产生新任务请求"+
				"</td></tr>"+
				"<tr><td colspan='2'>"+
				"<input name='new_additionaldownload' id='new_additionaldownload' value='0' type='radio' checked/>不产生新下载请求"+
				"<input name='new_additionaldownload' id='new_additionaldownload' value='1' type='radio' />产生新下载请求"+
				"</td></tr>"+
				"<tr><td colspan='2'>"+
				"<input id='new_allowempty' name='new_allowempty' value='1' type='radio' checked/>不为空"+
				"<input id='new_allowempty' name='new_allowempty' value='0' type='radio' />允许为空"+
				"</td></tr>"+
				"<tr><td colspan='2'>"+
				"<input id='new_needpersistence' name='new_needpersistence' value='0' type='radio' checked/>需要存储"+
				"<input id='new_needpersistence' name='new_needpersistence' value='1' type='radio' />不需要存储"+
				"</td></tr>"+
				"<tr><td colspan='2' style='text-align:center'><a href='#' class='gbtn' onclick='confirm()'>确认</a></td></tr>"+
				 "</table></div>",0,0,0,0);
	}

	 function confirm(){
			var name = $("#new_name").val();
			var rule = $("#new_rule").val();
			rule = $("#escape").text(rule).html();
			var type = $("#new_type").val();
			var validcheck = $("#new_validcheck").val();
			var additionalreq = $("#new_additionalreq:checked").val();
			var additionaldownload = $("#new_additionaldownload:checked").val();
			var allowempty = $("#new_allowempty:checked").val();
			var needpersistence = $("#new_needpersistence:checked").val();
			var parent = $("#new_parent").val();
			var type_str;
			if(type == 0){
				type_str="正则";
			}else if(type == 1){
				type_str="xpath";
			}else if(type == 2){
				type_str="css";
			}else if(type == 3){
				type_str="元信息";
			}
			var appendtr = "<tr id='"+field_index+"'>"+
			"<td>"+"<a href='#' onclick='edit("+field_index+")'>"+name+"</a><input name='name' type='hidden' value='"+name+"'/></td>"+
			"<td>"+parent+"<input name='parent' type='hidden' value='"+parent+"'/></td>"+
			"<td>"+type_str+"<input type='hidden' name='type' value='"+type+"'/></td>"+
			"<td>"+rule+"<input name='rule' type='hidden' value='"+rule+"'/></td>"+
			"<input type='hidden' name='responseValidCheck' value='"+validcheck+"'/>"+
			"<input type='hidden' name='additionalReq' value='"+additionalreq+"'/>"+
			"<input type='hidden' name='additionalDownload' value='"+additionaldownload+"'/>"+
			"<input type='hidden' name='allowEmpty' value='"+allowempty+"'/>"+
			"<input type='hidden' name='needPersistence' value='"+needpersistence+"'/>"+
			"</tr>";
			$("#addfieldbtn").before(appendtr);
			field_index++;
			$("#tinybox").hide();
			$("#tinymask").hide();
		}

	function edit(id){
		var name=$("tr#"+id).find("input[name$='name']").val();
		var parent=$("tr#"+id).find("input[name$='parent']").val();
		var type=$("tr#"+id).find("input[name$='type']").val();
		var rule=$("tr#"+id).find("input[name$='rule']").val();
		var validcheck=$("tr#"+id).find("input[name$='Check']").val();
		var type_element;
		if(type==0){
			type_element = "<tr><td>类型</td>"+
		 	"<td><select id='edited_type'>"+
		 		"<option value='0' selected>正则</option>"+
		 		"<option value='1'>xpath</option>"+
		 		"<option value='2'>css</option>"+
		 		"<option value='3'>元信息</option></select></td></tr>"
		}else if(type==1){
			type_element = "<tr><td>类型</td>"+
		 	"<td><select id='edited_type'>"+
		 		"<option value='0'>正则</option>"+
		 		"<option value='1' selected>xpath</option>"+
		 		"<option value='2'>css</option>"+
		 		"<option value='3'>元信息</option></select></td></tr>"
		}else if(type==2){
			type_element = "<tr><td>类型</td>"+
		 	"<td><select id='edited_type'>"+
		 		"<option value='0'>正则</option>"+
		 		"<option value='1'>xpath</option>"+
		 		"<option value='2' selected>css</option>"+
		 		"<option value='3'>元信息</option></select></td></tr>"
		}else if(type==3){
			type_element = "<tr><td>类型</td>"+
		 	"<td><select id='edited_type'>"+
		 		"<option value='0'>正则</option>"+
		 		"<option value='1'>xpath</option>"+
		 		"<option value='2'>css</option>"+
		 		"<option value='3' selected>元信息</option></select></td></tr>"
		}
		var allowempty=$("tr#"+id).find("input[name$='Empty']").val();
		var allowempty_element;
		if(allowempty==1){
			allowempty_element = "<tr><td colspan='2'>"+
			"<input id='edited_allowempty' name='edited_allowempty' value='1' type='radio' checked/>不为空"+
			"<input id='edited_allowempty' name='edited_allowempty' value='0' type='radio' />允许为空"+
			"</td></tr>";
		}else{
			allowempty_element = "<tr><td colspan='2'>"+
			"<input id='edited_allowempty' name='edited_allowempty' value='1' type='radio'/>不为空"+
			"<input id='edited_allowempty' name='edited_allowempty' value='0' type='radio' checked/>允许为空"+
			"</td></tr>";
		}
		var additionalreq=$("tr#"+id).find("input[name$='Req']").val();
		var additionalreq_element;
		if(additionalreq==0){
			additionalreq_element="<tr><td colspan='2'>"+
			"<input name='edited_additionalreq' id='edited_additionalreq' value='0' type='radio' checked/>不产生新任务请求"+
			"<input name='edited_additionalreq' id='edited_additionalreq' value='1' type='radio' />产生新任务请求"+
			"</td></tr>"
		}else{
			additionalreq_element="<tr><td colspan='2'>"+
			"<input name='edited_additionalreq' id='edited_additionalreq' value='0' type='radio' />不产生新任务请求"+
			"<input name='edited_additionalreq' id='edited_additionalreq' value='1' type='radio' checked/>产生新任务请求"+
			"</td></tr>"
		}
		var additiondownload = $("tr#"+id).find("input[name$='Download']").val();
		var additiondownload_element;
		if(additionalreq==0){
			additiondownload_element="<tr><td colspan='2'>"+
			"<input name='edited_additionalreq' id='edited_additionalreq' value='0' type='radio' checked/>不产生新下载请求"+
			"<input name='edited_additionalreq' id='edited_additionalreq' value='1' type='radio' />产生新下载请求"+
			"</td></tr>"
		}else{
			additiondownload_element="<tr><td colspan='2'>"+
			"<input name='edited_additionalreq' id='edited_additionalreq' value='0' type='radio' />不产生新下载请求"+
			"<input name='edited_additionalreq' id='edited_additionalreq' value='1' type='radio' checked/>产生新下载请求"+
			"</td></tr>"
		}
		var needpersistence=$("tr#"+id).find("input[name$='Persistence']").val();
		var needpersistence_element;
		if(needpersistence==0){
			needpersistence_element = "<tr><td colspan='2'>"+
			"<input id='edited_needpersistence' name='edited_needpersistence' value='0' type='radio' checked/>需要存储"+
			"<input id='edited_needpersistence' name='edited_needpersistence' value='1' type='radio' />不需要存储"+
			"</td></tr>"
		}else{
			needpersistence_element = "<tr><td colspan='2'>"+
			"<input id='edited_needpersistence' name='edited_needpersistence' value='0' type='radio' />需要存储"+
			"<input id='edited_needpersistence' name='edited_needpersistence' value='1' type='radio' checked/>不需要存储"+
			"</td></tr>"
		}
		TINY.box.show("<div><table>"+
				 "<tr><td>字段名</td><td><input style='height:20px;width:500px' id='edited_name' value='"+name+"' type='text' /></td></tr>"+
				 "<tr><td>字段规则</td><td><input style='height:20px;width:500px' id='edited_rule' value='"+rule+"' type='text' /></td></tr>"+
				"<tr><td>有效性验证</td><td><input style='height:20px;width:500px' id='edited_validcheck' value='"+validcheck+"' type='text' /></td></tr>"+
				"<tr><td>父字段</td><td><input style='height:20px;width:500px' id='edited_parent' value='"+parent+"' type='text' /></td></tr>"+
				type_element+
				additionalreq_element+
				additiondownload_element+
				allowempty_element+
				needpersistence_element+
				"<tr><td colspan='2' style='text-align:center'><a href='#' class='gbtn' onclick='confirmedit("+id+")'>修改</a></td></tr>"+
				 "</table></div>",0,0,0,0);
	}

	function confirmedit(id){
		var name = $("#edited_name").val();
		var rule = $("#edited_rule").val();
		var type = $("#edited_type").val();
		var validcheck = $("#edited_validcheck").val();
		var additionalreq = $("#edited_additionalreq:checked").val();
		var allowempty = $("#edited_allowempty:checked").val();
		var needpersistence = $("#edited_needpersistence:checked").val();
		var parent = $("#edited_parent").val();
		var type_str;
		if(type == 0){
			type_str="正则";
		}else if(type == 1){
			type_str="xpath";
		}else if(type == 2){
			type_str="css";
		}else if(type == 3){
			type_str="元信息";
		}
		var appendtr = "<tr id='"+id+"'>"+
		"<td>"+"<a href='#' onclick='edit("+id+")'>"+name+"</a><input type='hidden' name='name' value='"+name+"'/></td>"+
		"<td>"+parent+"<input type='hidden' name='parent' value='"+parent+"'/></td>"+
		"<td>"+type_str+"<input type='hidden' name='type' value='"+type+"'/></td>"+
		"<td>"+rule+"<input type='hidden' name='rule' value='"+rule+"'/></td>"+
		"<input type='hidden' name='responseValidCheck' value='"+validcheck+"'/>"+
		"<input type='hidden' name='additionalReq' value='"+additionalreq+"'/>"+
		"<input type='hidden' name='allowEmpty' value='"+allowempty+"'/>"+
		"<input type='hidden' name='needPersistence' value='"+needpersistence+"'/>"+
		"</tr>";
		$("tr#"+id).replaceWith(appendtr);
		TINY.box.hide();
	}
	
	function formCheck(){
		//bizCode不能为空
		var bizCode = $.trim($("input#bizCode").val());
		if(bizCode == ''){
			$("span#codemsgspan").html(alertMsg("业务代号不能为空"));
			$("input#bizCode").focus();
			return;
		}else{
			$("span#codemsgspan").html("");
		}
		//bizName不能为空
		var bizName = $.trim($("input#bizName").val());
		if(bizName==''){
			$("span#namemsgspan").html(alertMsg("业务名不能为空"));
			$("input#bizName").focus();
			return;
		}else{
			$("span#namemsgspan").html("");
		}
		//urlRule不能为空
		var urlRule = $.trim($("input#urlRule").val());
		if(urlRule == ''){
			$("span#urlmsgspan").html(alertMsg("url规则不能为空"));
			$("input#urlRule").focus();
			return;
		}else{
			$("span#urlmsgspan").html("");
		}
		var flag = $("input[name='website_flag']:checked").val();
		if(flag == 0){
			var domain = $.trim($("input#domain").val());
			if(domain == ''){
				$("span#domainmsgspan").html(alertMsg("域名不能为空"));
				$("input#domain").focus();
				return;
			}else{
				$("span#domainmsgspan").html("");
			}
		}
		
		//persistencetable不能为空
		var table = $.trim($("input#persistenceTable").val());
		if(table == ''){
			$("span#tablemsgspan").html(alertMsg("结果表不能为空"));
			$("input#persistenceTable").focus();
			return;
		}else{
			$("span#tablemsgspan").html("");
		}
		$("#new_biz_form").submit();
	}
	
	function test(){
		TINY.box.show("URL<input style='width:100%' type='text' name='testurl' id='testurl' value='' /><br><a href='#' onclick='submittest()'>提交测试</a>",0,500,200,0);
	}
	
	function submittest(){
		var json;
		var website_flag = $("input[name='website_flag']:checked").val();
		var testurl = $("#testurl").val();
		var bizcode = $("#bizCode").val();
		var bizname = $("#bizName").val();
		if(website_flag == 0){
			var charset = $("#charset").val();
			json="{'bizcode':'"+bizcode+"','bizname':'"+bizname+"','website_flag':"+website_flag+",'url':'"+testurl+"','charset':'"+charset+"','fieldrules':[";
		}else if(website_flag == 1){
			var websiteid = $("#websiteid").val();
			json="{'bizcode':'"+bizcode+"','bizname':'"+bizname+"','website_flag':"+website_flag+",'url':'"+testurl+"','websiteid':"+websiteid+",'fieldrules':[";
		}
		$("#fieldlist").find("tr[id][id!='addfieldbtn']").each(function(){
			var fieldjson = "{'name':'name_str','rule':'rule_str','parent':'parent_str','type':type_int,'validcheck':'validcheck_str','allowempty':allowempty_int,'persistence':persistence_int,'additionalreq':additionalreq_int,'additionaldownload':additionaldownload_int},"
			fieldjson = fieldjson.replace("name_str",$(this).find("input[name$='name']").val())
			.replace("rule_str",$(this).find("input[name$='rule']").val())
			.replace("parent_str",$(this).find("input[name$='parent']").val())
			.replace("type_int",$(this).find("input[name$='type']").val())
			.replace("validcheck_str",$(this).find("input[name$='Check']").val())
			.replace("allowempty_int",$(this).find("input[name$='Empty']").val())
			.replace("persistence_int",$(this).find("input[name$='Persistence']").val())
			.replace("additionalreq_int",$(this).find("input[name$='Req']").val())
			.replace("additionaldownload_int",$(this).find("input[name$='Download']").val());
			json = json + fieldjson;
		});
		json = json.substring(0,json.length-1) + "]}";
		alert(json);
		$.ajax({
			url:'/spider/biz/check',
	        type:'POST',
	        data:{
		       "param":json},
	        dataType:'text',
	        success:function(data){
	        	TINY.box.append(data);
		    }
		});
	}
	</script>
</body>
</html>