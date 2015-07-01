package com.lmdna.spider.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.codecraft.webmagic.selector.Html;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;


public class SpiderCommonTool{
	
	private static String LOCAL_IP = null;
	
	private static final Logger logger = LoggerFactory.getLogger(SpiderCommonTool.class);
	
	static HttpClientHelper httpClient = HttpClientHelper.instance();
	
	static final String ipcheckurl = "http://web.chacuo.net/netproxycheck";
	static final String freeproxyipurl = "http://www.xici.net.co/nn/%d";
	static final String freeproxyipurl2 = "http://www.xici.net.co/nt/%d";
	static final String kuaidailiipurl = "http://www.kuaidaili.com/proxylist/%d/";
	
	static final String http_valid = "【能提供Http代理功能】";
	static final String anonymous_valid="它属于【高级匿名代理】";
	
	public static boolean anonymousProxy(HttpHost host){
		
		String ip = host.getHostName();
		
		int port = host.getPort();
		
		int check_times = 0;
		
		int try_times = 0;
		
		while(check_times<2 && try_times<2){
			String getUrl = ipcheckurl.replace("{ip}", ip).replace("{port}", String.valueOf(port));
			try {
				String result = httpClient.doGet(getUrl,"UTF-8",6000,6000,6000);
				if(StringUtils.isEmpty(result)){
					++try_times;
					continue;
				}
				result = SpiderCommonTool.decodeUnicode(result);
				if(result.contains(anonymous_valid)){
					logger.info("代理IP{}通过匿名性检测！",ip+":"+port);
					return true;
				}else{
					++check_times;
				}
			} catch (IOException e) {
				++try_times;
			}
		}
		return false;
	}
	
	public static String checkProxyValid(String ip,int port){
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("Referer","http://web.chacuo.net/netproxycheck");
		int trytimes=3;
		String arg = "p%3D"+port+"_t%3D1_o%3D3";
		while(trytimes>0){
			try {
				NameValuePair[] nameValuePair = new NameValuePair[]{new BasicNameValuePair("data", ip),new BasicNameValuePair("type", "proxycheck"),new BasicNameValuePair("arg", arg)};
				String result = httpClient.doPost(ipcheckurl, "UTF-8", nameValuePair, headers);
				if(StringUtils.isEmpty(result)){
					--trytimes;
					Random r = new Random();
					long sleeptime = r.nextInt(1000);
					try {
						Thread.sleep(sleeptime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					continue;
				}
				result = SpiderCommonTool.decodeUnicode(result);
				if(result.contains(anonymous_valid)){
					int speed_level=0;
					if(result.contains("它的访问速度【非常快】!")){
						speed_level=1;
					}else{
						speed_level=2;
					}
					String connect_time_str = StringUtils.substringBetween(result, "测试connect共花费：[", "]毫秒！");
					String response_time_str = StringUtils.substringBetween(result, "读取代理返回数据花费：[", "]毫秒！");
					int connect_time = Integer.valueOf(connect_time_str);
					int response_time = Integer.valueOf(response_time_str);
					logger.info("代理IP{}:{}通过高级匿名性检测！",ip,port);
					return String.format("{'status':1,'info':{'connect_time':%d,'response_time':%d,'speed_level':%d}}", connect_time,response_time,speed_level);
				}else if(result.contains("503 Service Temporarily Unavailable")){
					logger.info("代理IP{}:{} 503 Service Temporarily Unavailable, Sleep 3 Seconds...",ip,port);
					try {
						Thread.sleep(8000);
					} catch (InterruptedException e) {
					}
				}else{
					--trytimes;
					try {
						Thread.sleep(8000);
					} catch (InterruptedException e) {
					}
				}
			} catch (IOException e) {
				logger.info("代理IP{}:{}高级匿名性检测异常，错误信息{}", ip,port,e.getMessage());
				--trytimes;
				Random r = new Random();
				long sleeptime = r.nextInt(1000);
				try {
					Thread.sleep(sleeptime);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		logger.info("{}:{}没有通过高级匿名性检测", ip,port);
		return "{'status':0,'info':{'connect_time':0,'response_time':0,'speed_level':0}}";
	}
	
	/**
	 * 每天抓xici代理IP的第一页数据页数据
	 * @return
	 */
	public static List<String> crawlFreeProxyIp(int pageno){
		List<String> proxyiplist = new ArrayList<String>();
		try {
			String url = String.format(freeproxyipurl, pageno);
			String result = httpClient.doGet(url,"UTF-8");
			if(!StringUtils.isEmpty(result)){
				try{
					Html html = new Html(result);
					List<String> iplist = html.xpath("//table[@id='ip_list']//tr/td[3]/text()").all();
					List<String> portlist = html.xpath("//table[@id='ip_list']//tr/td[4]/text()").all();
					List<String> protocollist = html.xpath("//table[@id='ip_list']//tr/td[7]/text()").all();
					List<String> speedlist = html.xpath("//table[@id='ip_list']//tr/td[8]/div[@class='bar']/@title").all();
					for(int x=0;x<speedlist.size();x++){
						if(Double.valueOf(StringUtils.substringBeforeLast(speedlist.get(x), "秒"))<=3){
							if("HTTP".equalsIgnoreCase(protocollist.get(x))){
								String proxyipjson = String.format("{'ip':'%s','port':'%s','speed':'%s','protocol':'%s'}", iplist.get(x),portlist.get(x),speedlist.get(x),protocollist.get(x));
								proxyiplist.add(proxyipjson);
							}
						}
					}
				}catch(Exception e){
					logger.info("爬取免费代理IP异常！"+e.getMessage());
				}
			}
			String url2 = String.format(freeproxyipurl2, pageno);
			String result2 = httpClient.doGet(url2,"UTF-8");
			if(!StringUtils.isEmpty(result2)){
				try{
					Html html2 = new Html(result2);
					List<String> iplist2 = html2.xpath("//table[@id='ip_list']//tr/td[3]/text()").all();
					List<String> portlist2 = html2.xpath("//table[@id='ip_list']//tr/td[4]/text()").all();
					List<String> protocollist2 = html2.xpath("//table[@id='ip_list']//tr/td[7]/text()").all();
					List<String> speedlist2 = html2.xpath("//table[@id='ip_list']//tr/td[8]/div[@class='bar']/@title").all();
					for(int x=0;x<speedlist2.size();x++){
						if(Double.valueOf(StringUtils.substringBeforeLast(speedlist2.get(x), "秒"))<=3){
							if("HTTP".equalsIgnoreCase(protocollist2.get(x))){
								String proxyipjson2 = String.format("{'ip':'%s','port':'%s','speed':'%s','protocol':'%s'}", iplist2.get(x),portlist2.get(x),speedlist2.get(x),protocollist2.get(x));
								proxyiplist.add(proxyipjson2);
							}
						}
					}
				}catch(Exception e){
					logger.info("爬取免费代理IP异常！"+e.getMessage());
				}
			}
			
			String kuaidaili = String.format(kuaidailiipurl, pageno);
			String kuaidailiresp = httpClient.doGet(kuaidaili,"UTF-8");
			if(!StringUtils.isEmpty(kuaidailiresp)){
				try{
					Html kuaidailihtml = new Html(kuaidailiresp);
					List<String> ipl = kuaidailihtml.xpath("//div[@id='list']/table/tbody/tr/td[1]/text()").all();
					List<String> portl = kuaidailihtml.xpath("//div[@id='list']/table/tbody/tr/td[2]/text()").all();
					List<String> protocol = kuaidailihtml.xpath("//div[@id='list']/table/tbody/tr/td[4]/text()").all();
					List<String> speedl = kuaidailihtml.xpath("//div[@id='list']/table/tbody/tr/td[7]/text()").all();
					for(int x=0;x<speedl.size();x++){
						if(Double.valueOf(StringUtils.substringBeforeLast(speedl.get(x), "秒"))<=3){
							if("HTTP".equalsIgnoreCase(protocol.get(x))){
								String proxyipjson2 = String.format("{'ip':'%s','port':'%s','speed':'%s','protocol':'%s'}", ipl.get(x),portl.get(x),speedl.get(x),protocol.get(x));
								proxyiplist.add(proxyipjson2);
							}
						}
					}
				}catch(Exception e){
					logger.info("爬取免费代理IP异常！"+e.getMessage());
				}
			}
		} catch (IOException e) {
			logger.info("爬取免费代理IP时发生网络错误！");
		} catch (Exception e){
			logger.info("爬取免费代理IP时发生异常！");
		}
		return proxyiplist;
	}
	
	public static List<String> batchCheckIpAnonymous(List<String> iplist){
		ExecutorService threadPool = Executors.newFixedThreadPool(20);
		final Vector<String> rslist = new Vector<String>();
		final CountDownLatch latch = new CountDownLatch(iplist.size());
		for(String proxyiptemp : iplist){
			final String proxyip = proxyiptemp;
			threadPool.execute(new Runnable(){
				@Override
				public void run() {
					Map<?,?> ipmap = JSONObject.parseObject(proxyip);
					try {
						Random r = new Random();
						int seed = r.nextInt(20);
						Thread.sleep(seed*500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					String checkrs = SpiderCommonTool.checkProxyValid(ipmap.get("ip").toString(), Integer.valueOf(ipmap.get("port").toString()));
					Map<?,?> checkmap = JSONObject.parseObject(checkrs);
					if((Integer)checkmap.get("status") == 1){
						rslist.add(proxyip);
					}
					latch.countDown();
				}});
			
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			logger.error("批量检测代理IP匿名性异常！");
		}
		threadPool.shutdownNow();
		return rslist;
	}
	
	public static String decodeUnicode(String theString) {

		char aChar;
		int len = theString.length();
		StringBuffer outBuffer = new StringBuffer(len);
		for (int x = 0; x < len;) {
			aChar = theString.charAt(x++);
			if (aChar == '\\') {
				aChar = theString.charAt(x++);
				if (aChar == 'u') {
					int value = 0;
					for (int i = 0; i < 4; i++) {
						aChar = theString.charAt(x++);
						switch (aChar) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							value = (value << 4) + aChar - '0';
							break;
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;
						default:
							throw new IllegalArgumentException(
									"Malformed   \\uxxxx   encoding.");
						}
					}
					outBuffer.append((char) value);
				} else {
					if (aChar == 't')
						aChar = '\t';
					else if (aChar == 'r')
						aChar = '\r';
					else if (aChar == 'n')
						aChar = '\n';
					else if (aChar == 'f')
						aChar = '\f';
					outBuffer.append(aChar);
				}
			} else
				outBuffer.append(aChar);
		}
		return outBuffer.toString();
	}
	
	public static String getAttachName(String filePath) {
		if (Strings.isNullOrEmpty(filePath)) {
			return "";
		}
		filePath = filePath.trim();
		int pos = 0;
		pos = filePath.lastIndexOf("\\");
		if (pos > -1) {
			filePath = filePath.substring(pos + 1);
		}
		pos = filePath.lastIndexOf("/");
		if (pos > -1) {
			filePath = filePath.substring(pos + 1);
		}
		pos = filePath.lastIndexOf(File.separatorChar);
		if (pos > -1) {
			filePath = filePath.substring(pos + 1);
		}
		return filePath;
	}
	
	public static void downloadImg(String savePath,String url) throws IOException{
		HttpResponse response = httpClient.doReq(url, "get", null, null);
		InputStream is = response.getEntity().getContent();
		File file = new File(savePath);
		FileOutputStream fos = new FileOutputStream(file);
		byte[] buffer = new byte[1024];
		while(is.read(buffer)!=-1){
			fos.write(buffer);
		}
		fos.flush();
		fos.close();
		EntityUtils.consume(response.getEntity());
	}
	
	public static void downloadVerifyImg(String savePath,String url,HttpClientHelper httpClient) throws IOException{
		HttpResponse response = httpClient.doReq(url, "get", null, null);
		InputStream is = response.getEntity().getContent();
		File file = new File(savePath);
		FileOutputStream fos = new FileOutputStream(file);
		byte[] buffer = new byte[1024];
		while(is.read(buffer)!=-1){
			fos.write(buffer);
		}
		fos.flush();
		fos.close();
		EntityUtils.consume(response.getEntity());
	}
	
	public static boolean isWindowsOS() {
		boolean isWindowsOS = false;
		String osName = System.getProperty("os.name");
		if (osName.toLowerCase().indexOf("windows") > -1) {
			isWindowsOS = true;
		}
		return isWindowsOS;
	}
	
	public static String getLocalIP(){

		String sIP = "";
		// 为空才进行获取本地IP
		if (StringUtils.isEmpty(LOCAL_IP)) {
			InetAddress ip = null;
			try {
				if (isWindowsOS()) {// 如果是Windows操作系统
					ip = InetAddress.getLocalHost();
				} else {// 如果是Linux操作系统
					boolean bFindIP = false;
					Enumeration netInterfaces = (Enumeration) NetworkInterface
							.getNetworkInterfaces();
					while (netInterfaces.hasMoreElements()) {
						if (bFindIP) {
							break;
						}
						NetworkInterface ni = (NetworkInterface) netInterfaces
								.nextElement();
						Enumeration ips = ni.getInetAddresses();
						while (ips.hasMoreElements()) {
							ip = (InetAddress) ips.nextElement();
							if (!ip.isLoopbackAddress()
									&& ip.getHostAddress().indexOf(":") == -1) {// 127.开头的都是lookback地址
																				
								if (!ip.getHostAddress().equals("127.0.0.1")
										&& (ip.getHostAddress().startsWith(
												"192.168")
												|| ip.getHostAddress()
														.startsWith("10.")
												|| ip.getHostAddress()
														.startsWith("180.") || ip
												.getHostAddress().startsWith(
														"172."))) {
									// 判断虚拟ip，虚拟ip除去，默认是200及200以上的是虚拟IP
									if (ip.getHostAddress().startsWith(
											"192.168.1.")) {
										String ipAddr = ip.getHostAddress();
										ipAddr = ipAddr.substring("192.168.1."
												.length());
										int ipNumber = Integer.valueOf(ipAddr);
										if (ipNumber >= 200) {
											continue;
										}
									}

									bFindIP = true;
									break;
								}

							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (null != ip) {
				sIP = ip.getHostAddress();
			}
			if (!StringUtils.isEmpty(sIP)) {
				LOCAL_IP = sIP;
			}
		}
		return LOCAL_IP;
	}
	
	public static int bytesToInt(byte[] buf, int off) {
		int i = 0;
		i = i | ((buf[off] & 255) << 24);
		i = i | ((buf[off + 1] & 255) << 16);
		i = i | ((buf[off + 2] & 255) << 8);
		i = i | (buf[off + 3] & 255);
		return i;
	}

	public static long bytesToLong(byte[] buf, int off) {
		long i = 0;
		i = i | (((long) buf[off] & 255) << 56)
				| (((long) buf[off + 1] & 255) << 48)
				| (((long) buf[off + 2] & 255) << 40)
				| (((long) buf[off + 3] & 255) << 32)
				| (((long) buf[off + 4] & 255) << 24)
				| (((long) buf[off + 5] & 255) << 16)
				| (((long) buf[off + 6] & 255) << 8)
				| ((long) buf[off + 7] & 255);
		return i;
	}
	
	public static byte[] intToBytes(int i) {
		byte[] b = new byte[4];
		b[0] = (byte) ((i >>> 24) & 255);
		b[1] = (byte) ((i >>> 16) & 255);
		b[2] = (byte) ((i >>> 8) & 255);
		b[3] = (byte) (i & 255);
		return b;
	}

	public static byte[] longToBytes(long i) {
		byte[] b = new byte[8];
		b[0] = (byte) ((i >>> 56) & 255);
		b[1] = (byte) ((i >>> 48) & 255);
		b[2] = (byte) ((i >>> 40) & 255);
		b[3] = (byte) ((i >>> 32) & 255);
		b[4] = (byte) ((i >>> 24) & 255);
		b[5] = (byte) ((i >>> 16) & 255);
		b[6] = (byte) ((i >>> 8) & 255);
		b[7] = (byte) (i & 255);
		return b;
	}
	
	public static void main(String arg[]) throws IOException, InterruptedException{
		File ip = new File("d:\\data\\proxyip");
		for(int i=1;i<=4;i++){
			List<String> iprs = crawlFreeProxyIp(i);
			List<String> checkrs = batchCheckIpAnonymous(iprs);
			for(String c : checkrs){
				Map<?,?> ipmap = JSONObject.parseObject(c);
				String s = ipmap.get("ip").toString() + "\t" + ipmap.get("port").toString()+"\r\n";
				FileWriter fw = new FileWriter(ip, true);
				fw.write(s);
				fw.flush();
				fw.close();
			}
		}
	}
}