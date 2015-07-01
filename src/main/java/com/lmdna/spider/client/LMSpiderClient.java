package com.lmdna.spider.client;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClients;


/**
 * LM-Spider客户端程序
 * @author ayumiono
 *
 */
public class LMSpiderClient {
	private String ip;
	private int port;
	private static final String submit_task = "/spider/master/submittask";
	private HttpClient httpclient = HttpClients.createDefault();
	
	public LMSpiderClient(String ip,int port){
		this.ip = ip;
		this.port = port;
	}
	public void submitTask(String fileName,String filePath){
		HttpPost httppost = new HttpPost(ip+":"+port+submit_task);
		MultipartEntityBuilder reqEntity = MultipartEntityBuilder.create();
		FileBody b = new FileBody(new File(filePath));
		StringBody taskFileName = new StringBody(fileName,ContentType.TEXT_PLAIN);  
		StringBody bizId = new StringBody("",ContentType.TEXT_PLAIN);
		StringBody taskFilePath = new StringBody("",ContentType.TEXT_PLAIN);
		StringBody temp = new StringBody("1",ContentType.TEXT_PLAIN);
		reqEntity.addPart("taskFile", b);
		reqEntity.addPart("taskFileName", taskFileName);
		reqEntity.addPart("taskFilePath", taskFilePath);
		reqEntity.addPart("bizId", bizId);
		reqEntity.addPart("temp", temp);
		httppost.setEntity(reqEntity.build());
		try {
			HttpResponse response = httpclient.execute(httppost);
			int statusCode = response.getStatusLine().getStatusCode();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}  
	}
}
