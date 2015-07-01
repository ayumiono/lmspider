<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="/c.tld" prefix="c"%>
<div class="box">
	<div class="title">
		<h2>IPs</h2>
		<img src="/spider/static/dmadmin/gfx/title-hide.gif" class="toggle"
			alt="" />
	</div>
	<div class="content pages">
		<table>
			<thead>
				<tr>
					<th scope='col'>IP</th>
					<th scope='col'>Port</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach items='${proxylist}' var='model'>
					<tr>
						<td><a onclick='detail(${model.id})' id='${model.id}'
							href='#'>${model.ip}</a></td>
						<td>${model.port}</td>
					</tr>
				</c:forEach>
			</tbody>

		</table>
		<div class="pages-bottom">
			<div class="actionbox">
				<button type="submit" onclick='addip()'>
					<span>添加代理IP</span>
				</button>
				<button type="submit" onclick='crawlipPopup()'>
					<span>抓取代理IP</span>
				</button>
			</div>
			<div class="pagination">
				<a href="#" class="left"></a> <a href="#" class="active">1</a> <a
					href="#">2</a> <a href="#">3</a> <a href="#">4</a> <a href="#">5</a>
				<a href="#">6</a> <a href="#">7</a> <a href="#" class="right"></a>
			</div>
		</div>
	</div>
</div>
<script type="text/javascript">
$(document).ready(function(){
	$('.toggle').click( function() {
		$(this).parent().next('.content').fadeToggle(400);
	});
});
</script>


