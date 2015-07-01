package com.lmdna.spider.protocol.rpc.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmdna.spider.node.master.MasterNode;
import com.lmdna.spider.node.master.VerifyImgBean;
import com.lmdna.spider.protocol.rpc.VerifyImgProtocol;
import com.lmdna.spider.utils.SpiderCommonTool;
import com.lmdna.spider.utils.SpiderGlobalConfig;

public class VerifyImgProtocolImpl implements VerifyImgProtocol{
	
	private static final Logger logger = LoggerFactory.getLogger(VerifyImgProtocolImpl.class);

	private MasterNode masterNode;
	
	public VerifyImgProtocolImpl(MasterNode masterNode){
		this.masterNode = masterNode;
	}

	@Override
	public String getVerifyCode(String machineid, String source, InputStream data) {
		String curVerifyCode = "";
		try{
			ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
			byte[] buff = new byte[100];
			int rc = 0;
			while ((rc = data.read(buff, 0, 100)) > 0) {
				swapStream.write(buff, 0, rc);
			}
			byte[] bytes = swapStream.toByteArray();
			if(bytes != null && bytes.length>0){
				String verify_dir = SpiderGlobalConfig.getValue(SpiderGlobalConfig.SPIDER_VERIFYIMG_DIR);
				if (StringUtils.isEmpty(verify_dir)) {
					logger.error("verify_dir 目录没有指定");
					throw new Exception("verify_dir 目录没有指定");
				}
				String verifyPath = verify_dir;
				File verifyDir = new File(verifyPath);
				if(!verifyDir.exists()){
					verifyDir.mkdirs();
				}
				final String localIp = SpiderCommonTool.getLocalIP();
				String verifyImageName = "verifyImage_"+System.currentTimeMillis()+".gif";
				String newVerifyCodeUrl = "http://"+localIp+":"+masterNode.getHttpServerPort()+"/verifyimg/"+verifyImageName;
				File verifyImg = new File(verifyPath + "/" + verifyImageName);
				try {
					FileUtils.writeByteArrayToFile(verifyImg, bytes);
				} catch (IOException e) {
					e.printStackTrace();
				}
				int expire = 30;//单位s
				Calendar c = Calendar.getInstance();
				VerifyImgBean img = new VerifyImgBean();
				img.setImg_name(verifyImageName);
				img.setStaticFileURL(newVerifyCodeUrl);
				img.setCreateTime(new Date());
				img.setExpire(30000);
				img.setFrom(source);
				img.setImgCreateTime(new Date());
				img.setPriority(1);
				img.setHost(machineid);
				int id = masterNode.submitVerifyImg(img);
				//MailSender.getInstance().sendException(source+"需要输入验证码","chenguolong@alphaun.com","柠檬爬虫系统（需要输入验证码）");
				long start = c.getTimeInMillis();
				long t1 = 0;
				while(t1 <= expire * 1000){
					String vc = masterNode.getVerifyCode(id);
					if(!StringUtils.isEmpty(vc)){
						curVerifyCode = vc;
						break;
					}
					long now = System.currentTimeMillis();
					t1 = now - start;
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}//休息5s
				}
				if (verifyImg.exists()) {
					verifyImg.delete();
				}
			}
		}catch(Exception e){
			return curVerifyCode;
		}
		return curVerifyCode;
	}

}
