<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="/c.tld" prefix="c"%>
<span id="escape" style="display:none"></span>
<div class="box">
	<div class="title">
		<h2>Spider Bizs</h2>
		<img src="/spider/static/dmadmin/gfx/title-hide.gif" class="toggle"
			alt="" />
	</div>
	<div class="content pages">
		<table>
			<thead>
				<tr>
					<td style="padding: 0px 8px 0px 6px; width: 13px;"><input
						class="checkall" type="checkbox"></td>
					<td>业务代号</td>
					<td>业务名称</td>
					<td>url匹配条件</td>
					<td>域名</td>
					<td>操作</td>
				</tr>
			</thead>
			<tbody>
				<c:forEach items='${bizlist}' var='model'>
					<tr>
						<td><input type="checkbox" /></td> 
						<td style="width:20%">${model.bizCode}</td>
						<td style="width:20%"><a class='detail' id='${model.id}' href='#'>${model.bizName}</a></td>
						<td style="width:20%">${model.urlRule}</td>
						<td style="width:20%">${model.websiteConfigBO.websiteBO.domain}</td>
						<td>
							<a href="#" onclick="editBiz(${model.id})"><img src="static/dmadmin/gfx/icon-edit.png" title="Edit this biz" /></a> 
							<a href="#"><img src="static/dmadmin/gfx/button-delete.png" title="Delete this biz" /></a>
							<a href="#"><img src="static/dmadmin/gfx/button-add.png" title="Load this biz" /></a>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
		<div class="pages-bottom">
			<div class="actionbox">
				<button onclick="submitJar()"><span>提交jar包</span></button>
				<button onclick="editcode()"><span>编写spider流程</span></button>
			</div>
			<div class="pagination">
				<a href="#" class="left"></a> <a href="#" class="active">1</a> <a
					href="#">2</a> <a href="#">3</a> <a href="#">4</a> <a href="#">5</a>
				<a href="#">6</a> <a href="#">7</a> <a href="#" class="right"></a>
			</div>
		</div>
	</div>
</div>
<div class="box">
	<div class="title">
		<h2>新增</h2>
		<img class="toggle" alt="" src="static/dmadmin/gfx/title-hide.gif">
	</div>
	<div class="content forms" style="display: none;">
		<div style="display:none" class="message red"><img alt="Close this item" src="static/dmadmin/gfx/icon-close.gif"></div>
		<form method="post" action="/spider/biz/save" enctype="application/x-www-form-urlencoded" name="new_biz_form">
			<div class="subbox" style="padding:5px">
				<div>
					<h2>网站信息</h2>
				</div>
				<input class="radio" type="radio" name="website_flag" value="0" checked onclick="switchWebsiteType()" /><span>新建网站信息</span> 
				<input type="radio" name="website_flag" onclick="switchWebsiteType()" value="1" /><span>使用原有网站信息</span>
				<div id="newwebsite">
					<table class="table-bordered table-striped">
						<tr>
							<td>网站中文名</td>
							<td>
								<input type="text" style="width: 370px;" id="sitechnname" name="sitechnname">
							</td>
						</tr>
						<tr>
							<td>网站英文名</td>
							<td>
								<input type="text" style="width: 370px;" id="siteenname" onFocus="intputcheck(1)" name="siteenname">
							</td>
						</tr>
						<tr>
							<td>域名</td>
							<td>
								<input type="text" style="width: 370px;" id="domain" onFocus="intputcheck(2)" name="domain" />
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
			</div>
			<div class="subbox" style="padding:5px">
				<div>
					<h2>业务明细</h2>
				</div>
				<table class="table-bordered table-striped">
					<tr>
						<td valign="top">业务代号</td>
						<td>
							<input type="text" style="width: 370px;" id="bizCode" onFocus="intputcheck(3)" name="bizCode" />
						</td>
					</tr>
					<tr>
						<td valign="top">业务名称</td>
						<td>
							<input type="text" style="width: 370px;" id="bizName" onFocus="intputcheck(4)" name="bizName" />
						</td>
					</tr>
					<tr>
						<td>url匹配规则</td>
						<td>
							<input type="text" style="width: 370px;" id="urlRule" onFocus="intputcheck(5)"  name="urlRule" />
						</td>
					</tr>
					<tr>
						<td>页面有效性验证字段</td>
						<td>
							<input type="text" style="width: 370px;" id="responseValidCheck" name="pageResponseValidCheck" />
						</td>
					</tr>
					<tr>
						<td>抓取处理类</td>
						<td>
							<input type="text" style="width: 370px;" id="responseValidCheck" name="taskProcessClass" />
						</td>
					</tr>
					<tr>
						<td>结果表</td>
						<td>
							<input type="text" style="width: 370px;" id="persistenceTable" name="persistenceTable" style="width: 370px;" />
						</td>
					</tr>
				</table>
			</div>
			<div class="subbox" style="padding:5px">
				<div>
					<h2>网页解析规则</h2>
				</div>
				<table id="fieldlist" style="width:80%;border:1px solid #d3d3d3">
					<thead>
						<tr>
							<th>fieldname</th>
							<th>parent</th>
							<th>type</th>
							<th>rule</th>
						</tr>
					</thead>
					<tbody>
						<tr id="addfieldbtn">
							<td style="text-align: left" colspan="4">
								<a href="javascript:void(0)" onclick="newField()"><span>新增</span></a> 
								<a href="javascript:void(0)" onclick="test()"><span>测试</span></a>
							</td>
						</tr>
					</tbody>
						
				</table>
			</div>
			<div class="subbox" style="padding:5px">
				<div>
					<h2>反监控规则</h2>
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
			</div>
			<div class="row buttons">
				<button id="complete" onclick="formCheck()"><span>完成</span></button>
			</div>
		</form>
	</div>
</div>
<script type="text/javascript">
	$(function(){
		$('.toggle').click( function() {
			$(this).parent().next('.content').fadeToggle(400);
		});
		field_index=0;
	});
	
	
	
	function intputcheck(type){
		/* var flag = $("input[name='website_flag']:checked").val();
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
	        	$("div.message").html(data+"<img alt='Close this item' src='static/dmadmin/gfx/icon-close.gif'>").show();
	        	if(data!==""){
	        		$("#complete").attr("disabled",true); 
	        	}
		    }
		}); */
	};
	
	function switchSpiderType(){
		var flag = $("input[name='spider_type_flag']:checked").val();
		if (flag === "1") {
			$("div[id='hasproxy']").hide();
			$("div[id='hasnoproxy']").show();
		} else if (flag === "0") {
			$("div[id='hasproxy']").show();
			$("div[id='hasnoproxy']").hide();
		}
	};
	
	
	
	function switchWebsiteType(){
		var flag = $("input[name='website_flag']:checked").val();
		if (flag === "1") {
			$("div[id='newwebsite']").hide();
			$("div[id='useoldwebsite']").show();
		} else if (flag === "0") {
			$("div[id='newwebsite']").show();
			$("div[id='useoldwebsite']").hide();
		}
	};
	
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
	};

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
	};
	
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
	};
	
	function newField(){
		art.dialog({
			content:"<div><table style='width:100%'>"+
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
			 "</table></div>",
			 lock:true,
			 title:"Add Field",
			 okVal:"Confirm",
			 ok:function(){
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
					var appendtr = "<tr id='field"+field_index+"'>"+
					"<td>"+"<a href='javascript:void(0)' onclick='edit("+field_index+")'>"+name+"</a><input name='name' type='hidden' value='"+name+"'/></td>"+
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
		});
	};

	function edit(id){
		var name=$("tr#field"+id).find("input[name$='name']").val();
		var parent=$("tr#field"+id).find("input[name$='parent']").val();
		var type=$("tr#field"+id).find("input[name$='type']").val();
		var rule=$("tr#field"+id).find("input[name$='rule']").val();
		var validcheck=$("tr#field"+id).find("input[name$='Check']").val();
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
		var allowempty=$("tr#field"+id).find("input[name$='Empty']").val();
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
		var additionalreq=$("tr#field"+id).find("input[name$='Req']").val();
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
		var additiondownload = $("tr#field"+id).find("input[name$='Download']").val();
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
		var needpersistence=$("tr#field"+id).find("input[name$='Persistence']").val();
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
		art.dialog({
			content:"<div><table>"+
			 "<tr><td>字段名</td><td><input style='height:20px;width:500px' id='edited_name' value='"+name+"' type='text' /></td></tr>"+
			 "<tr><td>字段规则</td><td><input style='height:20px;width:500px' id='edited_rule' value='"+rule+"' type='text' /></td></tr>"+
			"<tr><td>有效性验证</td><td><input style='height:20px;width:500px' id='edited_validcheck' value='"+validcheck+"' type='text' /></td></tr>"+
			"<tr><td>父字段</td><td><input style='height:20px;width:500px' id='edited_parent' value='"+parent+"' type='text' /></td></tr>"+
			type_element+
			additionalreq_element+
			additiondownload_element+
			allowempty_element+
			needpersistence_element+
			 "</table></div>",
			lock:true,
			title:"Edit Field",
			okVal:"Edit",
			ok:function(){
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
				"<td>"+"<a href='javascript:void(0);' onclick='edit("+id+")'>"+name+"</a><input type='hidden' name='name' value='"+name+"'/></td>"+
				"<td>"+parent+"<input type='hidden' name='parent' value='"+parent+"'/></td>"+
				"<td>"+type_str+"<input type='hidden' name='type' value='"+type+"'/></td>"+
				"<td>"+rule+"<input type='hidden' name='rule' value='"+rule+"'/></td>"+
				"<input type='hidden' name='responseValidCheck' value='"+validcheck+"'/>"+
				"<input type='hidden' name='additionalReq' value='"+additionalreq+"'/>"+
				"<input type='hidden' name='allowEmpty' value='"+allowempty+"'/>"+
				"<input type='hidden' name='needPersistence' value='"+needpersistence+"'/>"+
				"</tr>";
				$("tr#field"+id).replaceWith(appendtr);
			},
		});
	};

	function formCheck(){
		//bizCode不能为空
		var bizCode = $.trim($("input#bizCode").val());
		if(bizCode == ''){
			/*  $(".message").text("业务代号不能为空");*/
			$("div.message").display();
			$("input#bizCode").focus();
			return;
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
	};
	
	function test(){
		art.dialog({
			content:"<div>URL<input type='text' name='testurl' id='testurl' value='' /></div>",
			lock:true,
			okVal:"Test",
			title:"Test Field",
			ok:function(){
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
		});
	};
		
		
	
	function submitJar(){
		artDialog.notice = function (options) {
		    var opt = options || {},
		        api, aConfig, hide, wrap, top,
		        duration = 800;
		        
		    var config = {
		        id: 'Notice',
		        left: '100%',
		        top: '100%',
		        fixed: true,
		        drag: false,
		        resize: false,
		        follow: null,
		        lock: false,
		        init: function(here){
		            api = this;
		            aConfig = api.config;
		            wrap = api.DOM.wrap;
		            top = parseInt(wrap[0].style.top);
		            hide = top + wrap[0].offsetHeight;
		            
		            wrap.css('top', hide + 'px')
		                .animate({top: top + 'px'}, duration, function () {
		                    opt.init && opt.init.call(api, here);
		                });
		        },
		        close: function(here){
		            wrap.animate({top: hide + 'px'}, duration, function () {
		                opt.close && opt.close.call(this, here);
		                aConfig.close = $.noop;
		                api.close();
		            });
		            
		            return false;
		        }
		    };	
		    
		    for (var i in opt) {
		        if (config[i] === undefined) config[i] = opt[i];
		    };
		    
		    return artDialog(config);
		};
    	
		art.dialog({
			content:"<table><tr><td>Please Select JAR File!</td><td><input type='file' name='upJarfile' id='upJarfile' size='50'></td></tr></table>",
			title:"Upload Jar File",
			lock:true,
			ok:function(){
				var loaddingdialog = art.dialog();
				loaddingdialog.lock(true);
				loaddingdialog.title("Processing...");
				$.ajaxFileUpload({
					type: "post",
					dataType: "text",
			        url: "biz/uploadjar",
			        fileElementId: 'upJarfile',
			        success:function(data) {
			        	loaddingdialog.close();
			        	art.dialog.notice({
			        	    title: 'System Message',
			        	    width: 320,
			        	    content: data,
			        	    icon: 'succeed',
			        	    time: 10
			        	});
			        }
				});
			},
			cancelVal:"Cancel",
			okVal:"Submit",
			cancel:true,
		}); 
	};
	
	function editcode(){
		$.ajax({
			url:"/spider/biz/editcode",
			success:function(data){
				$(".holder").html(data);
			}
		});
	}
	
	function editBiz(bizId){
		$.ajax({
			url:"/spider/biz/edit?bizId="+bizId,
			success:function(data){
				$(".holder").html(data);
			}
		});
	}
	</script>