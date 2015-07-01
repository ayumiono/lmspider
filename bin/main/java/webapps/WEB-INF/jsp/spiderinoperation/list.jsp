<%@page import="com.lmdna.spider.http.util.ServletUtil"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="/c.tld" prefix="c"%>
<%@ page import="com.lmdna.spider.http.util.ServletUtil"%>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=9" />
<% ServletUtil.MasterNodeInfoUtil master = new ServletUtil.MasterNodeInfoUtil();
master.spidersInfoDetail(application, out, request);
%>
<script type='text/javascript'>
	function submitTask(bizCode){
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
			content:"<table><tr><td>请选择要上传的任务文件</td><td><input type='file' name='upTaskfile' id='upTaskfile' size='50'></td></tr><tr><td>请输入任务块大小</td><td><input type='text' name='rowperblock' id='rowperblock'></td></tr></table>",
			title:"Upload Task File",
			lock:true,
			ok:function(){
				var loading = art.dialog();
				loading.title("Processing...");
				loading.lock(true);
				$.ajaxFileUpload({
					type: "post",
					dataType: "text",
			        url: "master/uploadtask?bizCode="+bizCode+"&rowPerBlock=1000",
			        fileElementId: 'upTaskfile',
			        success:function(data) {
			        	loading.close();
			        	art.dialog.notice({
			        	    title: '系统消息',
			        	    width: 320,
			        	    content: data,
			        	    icon: 'succeed',
			        	    time: 10
			        	});
			        }
				});
			},
			cancelVal:"取消",
			okVal:"提交",
			cancel:true,
		}); 
	}
	      
	function removespider(bizCode){
	}
			
	function terminatespider(bizCode){
		
	}
	
</script>
