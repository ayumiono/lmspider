package com.lmdna.spider.pipeline;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

public class MyServletPipeline implements Pipeline {
	
	private OutputStream os;
	
	public MyServletPipeline(OutputStream os){
		this.os = os;
	}

	@Override
	public void process(Page page, Task task) {
		ResultItems resultItems = page.getResultItems();
		StringBuilder sb = new StringBuilder("<table class='table'>");
		sb.append("<tbody>");
		for (Map.Entry<String, Object> entry : resultItems.getAll().entrySet()) {
			sb.append("<tr>");
            sb.append("<td>"+entry.getKey()+"</td>");
            sb.append("<td>"+entry.getValue()+"</td>");
            sb.append("</tr>");
        }
		sb.append("</tbody>");
		sb.append("</table>");
		try {
			os.write(sb.toString().getBytes());
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(os != null){
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
