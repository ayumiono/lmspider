package com.lmdna.spider.protocol;

import java.util.Date;

import com.lmdna.spider.node.master.Task;
import com.lmdna.spider.notify.mail.MailSender;

public class MailTaskStatusListener implements TaskStatusListener {
	
	private static final long serialVersionUID = -219944810007911711L;
	
	private Task task;
	
	public MailTaskStatusListener(Task task){
		this.task = task;
	}

	@Override
	public void finishNotify() {
		MailSender.getInstance().sendMail(String.format("%s执行完成<br>起始时间:%s<br>结束时间:%s<br>总任务数:%d", task.getTaskId(),task.getSubmitDate(),new Date(),task.getTotalRow()),"chenxuelong@alphaun.com","柠檬爬虫抓取任务完成");
	}

}
