<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="/c.tld" prefix="c"%>
<style>
.field_rule_txtArea{
width:600px;
height:20px;
background:#fffffc;
border:1px #c2c1bd solid;
margin:4px 0;
overflow:auto;
max-width:600px;
min-width:600px;
min-height:20px;
max-height:20px; 
}	
</style>
<body>
	<span id="escape" style="display:none;"></span>
	<div id="search_condition">
		<form id="search_form">
		</form>
	</div>
	<!--内容-->
	<div id="content"></div>
	<script type="text/javascript">
	$(function(){
		field_index=0;
		searchAjax(1);
	});
	
	function permissionCheck(){
		if(parent.permissionList!=undefined&&parent.permissionList.length>0){
			for (i=0;i<parent.permissionList.length;i++){
				var obj= eval('(' + parent.permissionList[i] + ')');
		    	if(obj!=undefined&&obj.path!=undefined&&obj.method!=undefined&&path==obj.path&&method==obj.method)
		    		return "";
		    }
		}
	}

	function add(){
		$("#content").load("${sessionScope.apppath}/spider/biz/add.do");
	}

	function editBiz(bizId){
		$("#content").load("${sessionScope.apppath}/spider/biz/edit.do?bizId="+bizId);
	}

	function load(bizId){
		listserver(bizId,0);
	}

	function terminate(bizId){
		listserver(bizId,1);
	}

	function remove(bizId){
		listserver(bizId,2);
	}

	function listserver(bizId,type){
		$.ajax({
			type: "post",
			dataType: "text",
	        url: "${sessionScope.apppath}/spider/server/list.do?status=1",
	        contentType: 'application/x-www-form-urlencoded; charset=utf-8',
	        async :false,
	        success:function(data) {
	        	data = data.replace(/\n|\r|\t/g, "");
				var jsonData = $.parseJSON(data);
				var code = jsonData.code;
				var msg = jsonData.msg;
				var content = jsonData.data.content;
				if(type==0){
					content+="<a onclick='operation("+bizId+","+type+")' href='#'>加载</a>"
				}else if(type==1){
					content+="<a onclick='operation("+bizId+","+type+")' href='#'>暂停</a>"
				}else if(type==2){
					content+="<a onclick='operation("+bizId+","+type+")' href='#'>移除</a>"
				}
				
				if(code===0){
					TINY.box.show(content,0,500,0,0,0);
				}else{
					TINY.box.show(msg,0,0,0,0,2);
				}
	        }
		});
	}

	function operation(bizId, type) {
		var servers = "";
		$("input[name='servers']:checked").each(function(){
			servers+=$(this).parent().next().text()+",";
		});
		alert(servers);
		var url = "${sessionScope.apppath}/spider/biz/operation.do";
		alert(url);
		$.ajax({
			url : url,
			type : "get",
			data : {
				"bizId" : bizId,
				"type" : type,
				"servers" : servers,
			},
			dataType : 'text',
			success : function(data) {
				TINY.box.show(data,0,0,0,0,2);
			}
		});
	}

	function searchAjax(pageNo){
		$.ajax({
			type: "post",
			dataType: "text",
	        url: "${sessionScope.apppath}/spider/biz/list.do?pageNo="+pageNo,
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
					//详情弹出层
					$(".detail").click(
							function() {
								var $this = $(this);
								var id = $this.attr("id");
								TINY.box.show("${sessionScope.apppath}/spider/biz/detail.do?bizId="+ id, 1, 0, 0, 1)
					});
				}else{
					TINY.box.show(msg,0,0,0,0,2);
					}
	        }
		});
	}

	function saveBiz(){
		$.ajax({
			type: "post",
			dataType: "text",
	        url: "${sessionScope.apppath}/spider/biz/editbiz.do",
	        data:$('#biz_form').serializeArray(),
	        contentType: 'application/x-www-form-urlencoded; charset=utf-8',
	        success:function(data) {
	        	TINY.box.show(data.msg,0,0,0,0,2);
	        }
		});
	}

	function saveAnti(){
		$.ajax({
			type: "post",
			dataType: "text",
	        url: "${sessionScope.apppath}/spider/biz/editanti.do",
	        data:$('#anti_form').serializeArray(),
	        contentType: 'application/x-www-form-urlencoded; charset=utf-8',
	        success:function(data) {
	        	TINY.box.show(data.msg,0,0,0,0,2);
	        }
		});
	}

	function saveField(){
		$.ajax({
			type: "post",
			dataType: "text",
	        url: "${sessionScope.apppath}/spider/biz/editfield.do",
	        data:$('#field_form').serializeArray(),
	        contentType: 'application/x-www-form-urlencoded; charset=utf-8',
	        success:function(data) {
	        	TINY.box.show(data.msg,0,0,0,0,2);
	        }
		});
	}

	function switchProxy() {
		var flag = $("input[name='useProxy_flag']:checked").val();
		if (flag === "1") {
			$("div[id='hasproxy']").hide();
			$("div[id='hasnoproxy']").show();
		} else if (flag === "0") {
			$("div[id='hasproxy']").show();
			$("div[id='hasnoproxy']").hide();
		}
	}

	function switchAccount(){
		var flag = $("input[name='useAccount_flag']:checked").val();
		if (flag === "1") {
			$("div[id='hasaccount']").hide();
			$("div[id='hasnoaccount']").show();
		} else if (flag === "0") {
			$("div[id='hasaccount']").show();
			$("div[id='hasnoaccount']").hide();
		}
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
		var domain = $.trim($("input#domain").val());
		if(domain == ''){
			$("span#domainmsgspan").html(alertMsg("域名不能为空"));
			$("input#domain").focus();
			return;
		}else{
			$("span#domainmsgspan").html("");
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

	function alertMsg(msg){
		return '<font color="red">'+msg+'</font>';	 
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
				"<input name='new_additionalreq' id='new_additionalreq' value='0' type='radio' checked/>不产生新下载请求"+
				"<input name='new_additionalreq' id='new_additionalreq' value='1' type='radio' />产生新下载请求"+
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
			"<td>"+"<a href='#' onclick='edit("+field_index+")'>"+name+"</a><input name='fieldRules["+field_index+"].name' type='hidden' value='"+name+"'/></td>"+
			"<td>"+parent+"<input name='fieldRules["+field_index+"].parent' type='hidden' value='"+parent+"'/></td>"+
			"<td>"+type_str+"<input type='hidden' name='fieldRules["+field_index+"].type' value='"+type+"'/></td>"+
			"<td>"+rule+"<input name='fieldRules["+field_index+"].rule' type='hidden' value='"+rule+"'/></td>"+
			"<input type='hidden' name='fieldRules["+field_index+"].responseValidCheck' value='"+validcheck+"'/>"+
			"<input type='hidden' name='fieldRules["+field_index+"].additionalReq' value='"+additionalreq+"'/>"+
			"<input type='hidden' name='fieldRules["+field_index+"].allowEmpty' value='"+allowempty+"'/>"+
			"<input type='hidden' name='fieldRules["+field_index+"].needPersistence' value='"+needpersistence+"'/>"+
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
			"<input name='edited_additionalreq' id='edited_additionalreq' value='0' type='radio' checked/>不产生新下载请求"+
			"<input name='edited_additionalreq' id='edited_additionalreq' value='1' type='radio' />产生新下载请求"+
			"</td></tr>"
		}else{
			additionalreq_element="<tr><td colspan='2'>"+
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
		"<td>"+"<a href='#' onclick='edit("+id+")'>"+name+"</a><input type='hidden' name='fieldRules["+id+"].name' value='"+name+"'/></td>"+
		"<td>"+parent+"<input type='hidden' name='fieldRules["+id+"].parent' value='"+parent+"'/></td>"+
		"<td>"+type_str+"<input type='hidden' name='fieldRules["+id+"].type' value='"+type+"'/></td>"+
		"<td>"+rule+"<input type='hidden' name='fieldRules["+id+"].rule' value='"+rule+"'/></td>"+
		"<input type='hidden' name='fieldRules["+id+"].responseValidCheck' value='"+validcheck+"'/>"+
		"<input type='hidden' name='fieldRules["+id+"].additionalReq' value='"+additionalreq+"'/>"+
		"<input type='hidden' name='fieldRules["+id+"].allowEmpty' value='"+allowempty+"'/>"+
		"<input type='hidden' name='fieldRules["+id+"].needPersistence' value='"+needpersistence+"'/>"+
		"</tr>";
		$("tr#"+id).replaceWith(appendtr);
		TINY.box.hide();
	}

	function test(){
		TINY.box.show("URL<input style='width:100%' type='text' name='testurl' id='testurl' value='' /><br><a href='#' onclick='submittest()'>提交测试</a>",0,500,200,0);
	}

	function submittest(){
		var testurl = $("#testurl").val();
		var charset = $("#charset").val();
		var json="{'url':'"+testurl+"','charset':'"+charset+"','fieldrules':[";
		$("#fieldlist").find("tr[id][id!='addfieldbtn']").each(function(){
			var fieldjson = "{'name':'name_str','rule':'rule_str','parent':'parent_str','type':type_int,'validcheck':'validcheck_str','allowempty':allowempty_int,'persistence':persistence_int,'additionalreq':additionalreq_int},"
			fieldjson = fieldjson.replace("name_str",$(this).find("input[name$='name']").val())
			.replace("rule_str",$(this).find("input[name$='rule']").val())
			.replace("parent_str",$(this).find("input[name$='parent']").val())
			.replace("type_int",$(this).find("input[name$='type']").val())
			.replace("validcheck_str",$(this).find("input[name$='Check']").val())
			.replace("allowempty_int",$(this).find("input[name$='Empty']").val())
			.replace("persistence_int",$(this).find("input[name$='Persistence']").val())
			.replace("additionalreq_int",$(this).find("input[name$='Req']").val());
			json = json + fieldjson;
		});
		json = json.substring(0,json.length-1) + "]}";
		alert(json);
		$.ajax({
			url:'${sessionScope.apppath}/spider/biz/check.do',
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