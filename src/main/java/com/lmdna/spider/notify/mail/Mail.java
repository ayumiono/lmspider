package com.lmdna.spider.notify.mail;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.commons.lang.StringUtils;
public class Mail {  
    //定义发件人、收件人、SMTP服务器、用户名、密码、主题、内容等  
    private String displayName;  
    private List<String> toList;  
    private String from;  
    private String smtpServer;  
    private String username;  
    private String password;  
    private String subject;  
    private String content;  
    private boolean ifAuth; //服务器是否要身份认证  
    private List<String> file =new ArrayList<String>(); //用于保存发送附件的文件名的集合  
    public static Mail mail=null;
     
    public  void init(){
    	mail =new Mail(displayName, from, smtpServer, username, password, subject);
    }
    
    /**
     * 通过邮件列表发送内容
     * @param content
     * @param toList
     * @return
     */
    public static  boolean sendMailContantAndMailList(String content,String mailAcct){
    	System.out.println("content="+content+"_mailAcct="+mailAcct+"_mail="+mail);
    	if(mail!=null && StringUtils.isNotBlank(mailAcct) && StringUtils.isNotBlank(content)){
    		mail.setContent(content);
    		List<String> mailList=new ArrayList<String>();
    		mailList.add(mailAcct);
    		mail.toList=mailList;
    		System.out.println(mail);
    		Map<String, String> map=mail.send();
    		if(!"failed".equals(map.get("state"))){
    			System.out.println("发送邮件成功");
    			return true;
    		}else{
    			System.out.println("发送邮件失败");
    			return false;
    		}
    	}else return false;
    }
    public Mail(String displayName, String from, String smtpServer, String username, String password, String subject) {
		this.displayName = displayName;
		this.from = from;
		this.smtpServer = smtpServer;
		this.username = username;
		this.password = password;
		this.subject = subject;
		this.ifAuth = true;
		this.file = null;
	}

    public static void main(String[] args) {
		mail=new Mail("mmhub客服","services@mmhub.com","smtp.exmail.qq.com","services@mmhub.com","mmhub213","gaga");
//		System.out.println(sendMailContantAndMailList(pinjieContent("123213", "187289003@qq.com", "123213"), "187289003@qq.com"));

	}
	/** 
     * 设置SMTP服务器地址 
     */  
    public void setSmtpServer(String smtpServer){  
        this.smtpServer=smtpServer;  
    }  
     
    /** 
     * 设置发件人的地址 
     */  
    public void setFrom(String from){  
        this.from=from;  
    }  
    /** 
     * 设置显示的名称 
     */  
    public void setDisplayName(String displayName){  
        this.displayName=displayName;  
    }  
     
    /** 
     * 设置服务器是否需要身份认证 
     */  
    public void setIfAuth(boolean ifAuth){  
        this.ifAuth=ifAuth;  
    }  
     
    /** 
     * 设置E-mail用户名 
     */  
    public void setUserName(String username){  
        this.username=username;  
    }  
     
    /** 
     * 设置E-mail密码 
     */  
    public void setPassword(String password){  
        this.password=password;  
    }  
     
    /** 
     * 设置接收者 
     */  
    public void setTo(List<String> toList){  
        this.toList=toList;  
    }  
     
    /** 
     * 设置主题 
     */  
    public void setSubject(String subject){  
        this.subject=subject;  
    }  
     
    public void setUsername(String username) {
		this.username = username;
	}

	/** 
     * 设置主体内容 
     */  
    public void setContent(String content){  
        this.content=content;  
    }  
     
    /** 
     * 该方法用于收集附件名 
     */  
    public void addAttachfile(String fname){  
        file.add(fname);  
    }  
     
    public Mail(){  
         
    }  
     
    /** 
     * 初始化SMTP服务器地址、发送者E-mail地址、用户名、密码、接收者、主题、内容 
     */  
    public Mail(String smtpServer,String from,String displayName,String username,String password,List<String> toList,String subject,String content){  
        this.smtpServer=smtpServer;  
        this.from=from;  
        this.displayName=displayName;  
        this.ifAuth=true;  
        this.username=username;  
        this.password=password;  
        this.toList=toList;  
        this.subject=subject;  
        this.content=content;  
    }  
     
    /** 
     * 初始化SMTP服务器地址、发送者E-mail地址、接收者、主题、内容 
     */  
    public Mail(String smtpServer,String from,String displayName,List<String> toList,String subject,String content){  
        this.smtpServer=smtpServer;  
        this.from=from;  
        this.displayName=displayName;  
        this.ifAuth=false;  
        this.toList=toList;  
        this.subject=subject;  
        this.content=content;  
    }  
  
    /** 
     * 发送邮件 
     */  
    public Map<String, String> send(){  
        Map<String,String> map=new HashMap<String,String>();  
        map.put("state", "success");  
        String message="邮件发送成功！";  
        Session session=null;  
        Properties props = System.getProperties();  
        props.put("mail.smtp.host", smtpServer);  
        if(ifAuth){ //服务器需要身份认证  
            props.put("mail.smtp.auth","true");     
            SmtpAuth smtpAuth=new SmtpAuth(username,password);  
            session=Session.getDefaultInstance(props, smtpAuth);   
        }else{  
            props.put("mail.smtp.auth","false");  
            session=Session.getDefaultInstance(props, null);  
        }  
        session.setDebug(false);  
        Transport trans = null;    
        try {
            Message msg = new MimeMessage(session);   
            try{  
                Address from_address = new InternetAddress(from, displayName);  
                msg.setFrom(from_address);  
            }catch(java.io.UnsupportedEncodingException e){  
                e.printStackTrace();  
            }  
            InternetAddress[] address=new InternetAddress[toList.size()];  
            for(int i=0;i<toList.size();i++){
            	address[i]=new InternetAddress(toList.get(i));
            }
            msg.setRecipients(Message.RecipientType.TO,address);  
            msg.setSubject(subject);  
            Multipart mp = new MimeMultipart();  
            MimeBodyPart mbp = new MimeBodyPart();  
            mbp.setContent(content.toString(), "text/html;charset=utf-8");  
            mp.addBodyPart(mbp);    
            if(file!=null && !file.isEmpty()){//有附件  
                for(String filename:file){
                	  mbp=new MimeBodyPart();  
                      FileDataSource fds=new FileDataSource(filename); //得到数据源  
                      mbp.setDisposition( "attachment "); 
                      mbp.setFileName(MimeUtility.encodeText(new File(filename).getName())); 
                      mbp.setDataHandler(new DataHandler(fds)); //得到附件本身并至入BodyPart  
                      mp.addBodyPart(mbp);  
                }
            }   
            msg.setContent(mp); //Multipart加入到信件  
            msg.setSentDate(new Date());     //设置信件头的发送日期  
            //发送信件  
            msg.saveChanges();   
            trans = session.getTransport("smtp");  
            trans.connect(smtpServer, username, password);  
            trans.sendMessage(msg, msg.getAllRecipients());  
            trans.close();  
             
        }catch(AuthenticationFailedException e){     
             map.put("state", "failed");  
             message="邮件发送失败！错误原因：\n"+"身份验证错误!";  
             e.printStackTrace();   
        }catch (MessagingException e) {  
             message="邮件发送失败！错误原因：\n"+e.getMessage();  
             map.put("state", "failed");  
             e.printStackTrace();  
             Exception ex = null;  
             if ((ex = e.getNextException()) != null) {  
                 System.out.println(ex.toString());  
                 ex.printStackTrace();  
             }   
        } catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        //System.out.println("\n提示信息:"+message);  
        map.put("message", message);  
        return map;  
    }  
     
    
}  

 
