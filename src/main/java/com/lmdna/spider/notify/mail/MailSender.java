package com.lmdna.spider.notify.mail;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;


public class MailSender {
	private final static Logger logger=Logger.getLogger(MailSender.class);
	private MailSender(){
	}
	private static class InstanceHolder{
		private static MailSender instance=new MailSender();
	}
	public static  MailSender  getInstance(){
		return InstanceHolder.instance;
	}
	
	
	/**
	 * 发送异常邮件
	 * @param content	邮件内容
	 */
	public void sendMail(String content) {
		this.sendMail(content, null);
	}
	
	/**
	 * 发送异常邮件
	 * @param content	邮件内容
	 * @param toAddress	收件人	不填会使用系统默认邮箱接收（多个用|分割）
	 */
	public void sendMail(String content, String toAddress) {
		this.sendMail(content, toAddress, null);
	}
	
	/**
	 * 发送异常邮件
	 * @param content	邮件内容
	 * @param toAddress	收件人	不填会使用系统默认邮箱接收（多个用|分割）
	 * @param subject	邮件主题	不填会使用系统默认邮件主题
	 */
	public void sendMail(String content, String toAddress, String subject) {
		InputStream is = MailSender.class.getResourceAsStream("/spider_global_config.properties");
		Properties prop = new Properties();
		try {
			prop.load(is);
			String serverHost = prop.getProperty("serverHost");
			String userName = prop.getProperty("userName");
			String userPassword = prop.getProperty("userPassword");
			String displayName = prop.getProperty("displayName");
			if (toAddress == null || "".equals(toAddress.trim()))
				toAddress = prop.getProperty("toAddress");
			if (subject == null || "".equals(subject.trim()))
				subject = prop.getProperty("subject");
			String[] toAddresses = toAddress.split("\\|");
			if (toAddress.length() <= 0) {
				logger.info("邮件接收地址异常");
				return ;
			}
			List<String> toList = new ArrayList<String>();
			for (String to : toAddresses) {
				toList.add(to);
			}
			Mail mail = new Mail(serverHost, userName, displayName, userName, userPassword, toList, subject, content);
			Map<String, String> result = mail.send();
			logger.info("发送邮件：" + result);
		} catch (Exception e) {
			logger.info("发送异常邮件发生异常：content=" + content + ";Exception=" + e.getMessage());
			e.printStackTrace();
		}
	}
}
