<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="/c.tld" prefix="c"%>
<span id="escape" style="display:none;"></span>
	<!--内容-->
	<div class="box">
		<div class="title">
			<h2>${biz.bizName}编辑</h2>
			<img class="toggle" alt="" src="gfx/title-hide.gif">
		</div>
		<div class="content tabs">
			<ul class="tabnav">
				<li><a href="#tab1">业务明细</a></li>
				<li><a href="#tab2">网页解析规则</a></li>
				<li><a href="#tab3">反监控规则</a></li>
			</ul>
			<div id="tab1" class="tabdiv">
				<form id="biz_form">
				<table class="table">
					<tr>
						<td valign="top">代号</td>
						<td><input type="text" style="width: 370px;" class="input_2"
							id="titleone" name="bizCode" value="${biz.bizCode}" />
							<p class="f12">100个字符以内,不能重复。</p></td>
					</tr>
					<tr>
						<td valign="top">名称</td>
						<td><input type="text" style="width: 370px;" class="input_2"
							id="titletwo" name="bizName" value="${biz.bizName}" />
							<p class="f12">100个字符以内,不能重复。</p></td>
					</tr>
					<tr>
						<td>url匹配规则</td>
						<td><input type="text" style="width: 370px;" class="input_3"
							id="price" name="urlRule" value="${biz.urlRule}" /></td>
					</tr>
					<tr>
						<td>页面编码</td>
						<td><select id="charset" name="charset">
								<option>${website.charset}</option>
								<c:forEach items="${charsetList}" var="charset">
									<option value="${charset.id}">${charset.charset}</option>
								</c:forEach>
						</select></td>

					</tr>
					<tr>
						<td>域名</td>
						<td><input type="text" class="input_1" id="connecturl"
							name="domain" value="${website.domain}" /></td>
					</tr>
					<tr>
						<td>结果表</td>
						<td><input type="text" class="input_1" id="connecturl"
							name="persistenceTable" style="width: 370px;"
							value="${biz.persistenceTable}" /></td>
					</tr>
					<tr>
						<td colspan="2"><a href="#" class="gbtn" onclick="saveBiz()">完成</a></td>
					</tr>
				</table>
				</form>
			</div>
			<div id="tab2" class="tabdiv">
				<form id="field_form">
				<table class="table">
					<thead>
						<tr>
							<th>字段名</th>
							<th>父字段</th>
							<th>类型</th>
							<th>规则</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach items="${fieldList}" var="field" varStatus="stat">
						<tr id="field${stat.index}">
							<td valign="top"><a href="#" onclick="edit(${stat.index})">${field.name}</a>
								<input type="hidden" name="fieldRules[${stat.index}].name"
								value="${field.name}" /> <input
								name="fieldRules[${stat.index}].id" value="${field.id}"
								type="hidden" /> <input type='hidden'
								name='fieldRules[${stat.index}].responseValidCheck'
								value="${field.responseValidCheck}" /> <input type='hidden'
								name='fieldRules[${stat.index}].additionalReq'
								value="${field.additionalReq}" /> <input type='hidden'
								name='fieldRules[${stat.index}].allowEmpty'
								value="${field.allowEmpty}" /> <input type='hidden'
								name='fieldRules[${stat.index}].needPersistence'
								value="${field.needPersistence}" /></td>
							<td>${field.parent}<input
								name='fieldRules[${stat.index}].parent' type='hidden'
								value="${field.parent}" /></td>
							<td><c:choose>
									<c:when test="${field.type == 0}">正则</c:when>
									<c:when test="${field.type == 1}">xpath</c:when>
									<c:when test="${field.type == 2}">css</c:when>
									<c:when test="${field.type == 3}">元信息</c:when>
									<c:otherwise></c:otherwise>
								</c:choose> <input type='hidden' name='fieldRules[${stat.index}].type'
								value="${field.type}" /></td>
							<td><textarea readonly name='fieldRules[${stat.index}].rule' style="height: 15px; width: 600px;resize:none">${field.rule}</textarea></td>
						</tr>
					</c:forEach>
					<tr id="addfieldbtn">
						<td style="text-align: right" colspan="4">
							<a href="#" onclick="newField()">新增</a> 
							<a href="#" onclick="test()">测试</a>
						</td>
					</tr>
					</tbody>
				</table>
				<a href="#" class="gbtn" onclick="saveField()">完成</a>
			</form>
			</div>
			<div id="tab3" class="tabdiv">
				<form id="anti_form">
				<input type="hidden" name="antiPolicy.id" value="${antiPolicy.id}" />
				<table class="table">
					<tr>
						<td>抓取失败重试次数:</td>
						<td><input type="text" name="antiPolicy.cycleRetryTimes"
							value="2" /></td>
					</tr>
					<tr>
						<td>抓取间隔:</td>
						<td><input type="text" name="antiPolicy.sleepTime"
							value="1000" /></td>
					</tr>
				</table>
				<c:if test="${antiPolicy.needProxy == 1}">
					<input type="radio" name="useProxy_flag" value="0" onclick="switchProxy()" />使用代理IP
					<input type="radio" name="useProxy_flag" checked onclick="switchProxy()" value="1" />不使用代理IP
					<div id="proxy">
						<table class="table">
							<tr>
								<td>使用多少代理IP:</td>
								<td>-</td>
							</tr>
							<tr>
								<td>每次加载多少个代理IP:</td>
								<td>-</td>
							</tr>
							<tr>
								<td>代理IP代用间隔(毫秒)：</td>
								<td>-</td>
							</tr>
							<tr>
								<td>代理IP提交报告周期(分钟):</td>
								<td>-</td>
							</tr>
							<tr>
								<td>失效代理IP复活时间(小时):</td>
								<td>-</td>
							</tr>
							<tr>
								<td>代理IP连续失败次数阀值:</td>
								<td>-</td>
							</tr>
							<tr>
								<td>代理IP失效次数阀值:</td>
								<td>-</td>
							</tr>
						</table>
					</div>
				</c:if>
				<c:if test="${antiPolicy.needProxy == 0}">
					<input type="radio" name="useProxy_flag" checked value="0" onclick="switchProxy()" />使用代理IP
					<input type="radio" name="useProxy_flag" onclick="switchProxy()" value="1" />不使用代理IP
					<div id="proxy">
						<table class="table">
							<tr>
								<td>使用多少代理IP:</td>
								<td><input type="text" name="proxyIpCount"
									value="${antiPolicy.proxyIpCount}" /></td>
							</tr>
							<tr>
								<td>每次加载多少个代理IP:</td>
								<td><input type="text" name="proxyIpLoadCount"
									value="${antiPolicy.proxyIpLoadCount}" /></td>
							</tr>
							<tr>
								<td>代理IP代用间隔(毫秒)：</td>
								<td><input type="text" name="ipReuseInterval"
									value="${antiPolicy.ipReuseInterval}" /></td>
							</tr>
							<tr>
								<td>代理IP提交报告周期(分钟):</td>
								<td><input type="text" name=ipStatReportInterval
									value="${antiPolicy.ipStatReportInterval}" /></td>
							</tr>
							<tr>
								<td>失效代理IP复活时间(小时):</td>
								<td><input type="text" name=ipReviveinTime
									value="${antiPolicy.ipReviveinTime}" /></td>
							</tr>
							<tr>
								<td>代理IP连续失败次数阀值:</td>
								<td><input type="text" name=failedTimes
									value="${antiPolicy.failedTimes}" /></td>
							</tr>
							<tr>
								<td>代理IP失效次数阀值:</td>
								<td><input type="text" name=deadTimes
									value="${antiPolicy.deadTimes}" /></td>
							</tr>
						</table>
					</div>
				</c:if>
				<a href="#" class="gbtn" onclick="saveAnti()">完成</a>
			</form>
			</div>
		</div>
	</div>
	<script type="text/javascript">
	$(function(){
		$('.tabs').tabs({ fx: { opacity: 'toggle' } });
		$(".tabs table tr td:first-child").css('border-left','none').css('padding-left','0');
		field_index=0;
	});
	</script>