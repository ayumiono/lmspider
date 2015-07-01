package com.lmdna.spider.apicrawl.tencent;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.thread.CountableThreadPool;

import com.alibaba.fastjson.JSONObject;
import com.lmdna.spider.utils.VerifyDialog;

public class TencentHttpLoginDemo {
	
	private static final Logger logger = Logger.getLogger(TencentHttpLoginDemo.class);
	
	private static ExecutorService executorService = Executors.newFixedThreadPool(200);
	
	private static CountableThreadPool pool = new CountableThreadPool(200, executorService);
	
	private static final BlockingQueue<Request> taskQueue = new LinkedBlockingQueue<Request>();
	
	private static final BlockingQueue<String> dbCache = new LinkedBlockingQueue<String>();
	
	private static final BlockingQueue<String> notoautodbCache = new LinkedBlockingQueue<String>();
	
	private static final Set<String> allProxy = Collections.synchronizedSet(new HashSet<String>());
	
	private static final MyProxyPool proxyPool = new MyProxyPool();
	
	private static int validproxycount = 20;
	
	private static final long scantaskfileinterval = 10*60*1000;//10分钟
	
	private static final long scanproxyfileinterval = 30*60*1000;
	
	private static final long scanaccountfileinterval = 60*60*1000;
	
	private static final long persistenceinterval = 10*60*1000;
	
	private static final long reviveaccountinterval = 30*60*1000;
	
	private static final long reviveproxyinterval = 60*60*1000;
			
	
	static{
		PatternLayout patternLayout = new PatternLayout("%d{yy-MM-dd HH:mm:ss,SSS} %-5p %c(%F:%L) ## %m%n");
		try {
			RollingFileAppender fileAppender = new RollingFileAppender(patternLayout,"d:\\log\\tqqinfo.log");
			fileAppender.setEncoding("gbk");
			fileAppender.setMaxFileSize("10MB");
			fileAppender.setMaxBackupIndex(50);
			fileAppender.setName("tqqinfo");
			logger.addAppender(fileAppender);
			logger.setLevel(Level.INFO);
			logger.setAdditivity(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static final BlockingQueue<TencentHttpLoginDemo.TqqConn> connPool = new DelayQueue<TencentHttpLoginDemo.TqqConn>();
	private static final BlockingQueue<TencentHttpLoginDemo.TqqConn> badconnPool = new DelayQueue<TencentHttpLoginDemo.TqqConn>();
	private static final Map<String,String> allAccount = new HashMap<String,String>();
	
	private static class MyProxyPool{
		private Vector<Proxy> pool = new Vector<Proxy>();
		private Vector<Proxy> validpool = new Vector<Proxy>();//保留被用过后能正常抓取的代理IP,get方法优先从这个pool中取
		private Vector<Proxy> restpool = new Vector<Proxy>();
		private ReentrantLock lock = new ReentrantLock();
		private Condition newProxySignal = lock.newCondition();
		private Timer timer = new Timer();
		private TimerTask revivetask = new TimerTask() {
	        @Override
	        public void run() {
	        	if(restpool.size()>0){
	        		logger.info("开始恢复代理IP,restpool中在休息的代理IP的个数为:"+restpool.size());
	        		for(Proxy proxy : restpool){
	        			pool.add(proxy);
	        		}
	        	}
	        }
	    };
	    
	    public MyProxyPool(){
	    	timer.schedule(revivetask, 0, reviveproxyinterval);
	    }
	    
		public Proxy get() throws InterruptedException{
			Proxy x;
			lock.lockInterruptibly();
			try{
				//保证validpool中有至少10个代理IP可以交替使用
				if(validpool.size()>=validproxycount){
					Random r = new Random();
					int index = r.nextInt(validpool.size());
					x = validpool.get(index);
					return x;
				}
				while(pool.size()<=0){
					logger.info("等待新的代理IP...");
					newProxySignal.await();
				}
				Random r = new Random();
				int index = r.nextInt(pool.size());
				x = pool.get(index);
				if(pool.size()>0){
					newProxySignal.signal();
				}
			}finally{
				lock.unlock();
			}
			return x;
		}
		public void add(Proxy proxy){
			lock.lock();
			try{
				pool.add(proxy);
				newProxySignal.signal();
			}finally{
				lock.unlock();
			}
		}
		public void addValid(Proxy proxy){
			if(!validpool.contains(proxy)){
				proxy.setValid();
				validpool.add(proxy);
			}
		}
		public void remove(Proxy proxy){
			//被移除之前的状态是有效的，则放在休息池内
			if(proxy.isValid()){
				if(!restpool.contains(proxy)){
					proxy.setInValid();
					proxy.resetstatus();
					restpool.add(proxy);
				}
			}
			validpool.remove(proxy);
			pool.remove(proxy);
		}
	}
	
	private static class TqqConn implements Delayed{
		private CloseableHttpClient httpclient;
		private HttpClientContext context;
		private TqqAccount account;
		private long canReuseTime = System.nanoTime() + TimeUnit.NANOSECONDS.convert(1500, TimeUnit.MILLISECONDS);
		private Proxy proxy;
		public TqqConn(CloseableHttpClient httpclient,HttpClientContext context,TqqAccount account){
			this.httpclient = httpclient;
			this.context = context;
			this.account = account;
		}
		public CloseableHttpResponse execute(String url) throws ClientProtocolException, IOException{
			RequestBuilder builder = RequestBuilder.get().setUri(url);
			RequestConfig.Builder configBuilder = RequestConfig.custom();
			configBuilder.setSocketTimeout(6000).setConnectTimeout(6000).setConnectionRequestTimeout(6000);
			if(proxy!=null){
				configBuilder.setProxy(proxy.getProxy());
			}
			builder.setConfig(configBuilder.build());
			return httpclient.execute(builder.build(),context);
		}
		
		@Override
		public int compareTo(Delayed o) {
			TqqConn that = (TqqConn) o;
			return canReuseTime > that.canReuseTime ? 1 : (canReuseTime < that.canReuseTime ? -1 : 0);
		}
		@Override
		public long getDelay(TimeUnit unit) {
			return unit.convert(canReuseTime - System.nanoTime(), TimeUnit.NANOSECONDS);
		}
		
		public void setReuseTimeInterval(long interval){
			this.canReuseTime = System.nanoTime() + TimeUnit.NANOSECONDS.convert(interval, TimeUnit.MILLISECONDS);
		}
		public Proxy getProxy() {
			return proxy;
		}
		public void setProxy(Proxy proxy) {
			this.proxy = proxy;
		}
	}
	
	
	private static class TqqAccount{
		private String account;
		private String password;
		public TqqAccount(String account,String password){
			this.account = account;
			this.password = password;
		}
	}
	
	private static class Proxy{
		private HttpHost proxy;
		private AtomicInteger errCount = new AtomicInteger(0);
		private AtomicInteger deadCount = new AtomicInteger(0);
		private AtomicLong successCount = new AtomicLong(0);
		private AtomicLong borrowCount = new AtomicLong(0);
		private AtomicBoolean isValid = new AtomicBoolean(false);
		public Proxy(HttpHost proxy){
			this.proxy = proxy;
		}
		public HttpHost getProxy() {
			return proxy;
		}
		public boolean dead(){
			if(errCount.incrementAndGet() >=50){
				reset();//重置连续错误次数
				if(deadCount.incrementAndGet()>=10){
					deadCount = new AtomicInteger(0);//重置连续死亡次数
					return true;
				}
				return false;
			}
			return false;
		}
		private void reset(){
			errCount = new AtomicInteger(0);
		}
		public void success(){
			reset();
			successCount.incrementAndGet();
		}
		public void resetstatus(){
			successCount = new AtomicLong(0);
			borrowCount = new AtomicLong(0);
		}
		public void borrow(){
			borrowCount.incrementAndGet();
		}
		public String toString(){
			return String.format("host:%s,borrowcount:%d,successcount:%d,errcount:%d,deadcount:%d", proxy.toHostString(),borrowCount.get(),successCount.get(),errCount.get(),deadCount.get());
		}
		public void setValid(){
			isValid.set(true);
		}
		public void setInValid(){
			isValid.set(false);
		}
		public boolean isValid(){
			return isValid.get();
		}
	}
	
	private static class Mythread extends Thread{
		public void run() {
			for(;;){
				logger.info("后台检测线程启动。");
				if(badconnPool.size()>0){
					while(true){
						TqqConn conn = badconnPool.poll();
						if(conn!=null){
							TqqConn refreshedConn = login(conn.account.account, conn.account.password);
							connPool.add(refreshedConn);
							logger.info(conn.account.account+"重新刷新成功，当前连接池大小为"+connPool.size());
						}else{
							break;
						}
					}
				}
				logger.info("后台检测线程结束。半小时后再次启动。");
				try {
					Thread.sleep(reviveaccountinterval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static class Mythread2 extends Thread{
		public void run() {
			for(;;){
				logger.info("后台账号登录线程启动。");
				BufferedReader breader0 = null;
				BufferedInputStream bis0 = null;
				try{
					File file0 = new File("d:\\data\\qqaccount");
					bis0 = new BufferedInputStream(new FileInputStream(file0));
					breader0 = new BufferedReader(new InputStreamReader(bis0, "UTF-8"),1024*1024);
					String accountLine = "";
					while((accountLine=breader0.readLine())!=null){
						try{
							String[] accountinfo = accountLine.split("\t");
							String account = accountinfo[0].trim();
							String password = accountinfo[1].trim();
							if(allAccount.containsKey(account)){
								continue;
							}
							TqqConn conn = login(account,password);
							if(conn != null){
								connPool.add(conn);
								logger.info("连接池当前有"+connPool.size()+"个连接。");
								allAccount.put(account, password);
							}
						}catch(Exception e){
							continue;
						}
					}
				}catch(Exception e){
				}
				logger.info("后台账号登录线程结束。稍后再次启动。");
				try {
					Thread.sleep(scanaccountfileinterval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static class Mythread3 extends Thread{
		public void run() {
			for(;;){
				logger.info("后台扫描任务文档线程启动。");
				logger.info("当前任务队列中还有"+taskQueue.size()+"个任务，活跃线程数为"+pool.getThreadAlive()+",数据缓存中有"+dbCache.size()+"条数据。");
				if(taskQueue.size()<=0){
					try{
						File file = new File("d:\\data\\qqwait4crawl");
						if(file.isDirectory() && file.exists()){
							File[] filelist = file.listFiles();
							for(File f : filelist){
								if(f.getName().contains(".bak")){
									continue;
								}else{
									String line = "";
									BufferedReader breader = null;
									BufferedInputStream bis = null;
									bis = new BufferedInputStream(new FileInputStream(f));
									breader = new BufferedReader(new InputStreamReader(bis, "UTF-8"),1024*1024);
									while((line=breader.readLine())!=null){
										String qq = line.trim();
										String url = "http://user.qzone.qq.com/"+qq+"/1";
										Request request = new Request(url);
										taskQueue.add(request);
										continue;
									}
									breader.close();
								}
								logger.info("任务文件"+f.getName()+"读取完成。当前任务队列中有"+taskQueue.size()+"个任务。");
								f.renameTo(new File("d:\\data\\qqwait4crawl\\"+f.getName()+".bak"));
								break;
							}
						}else{
							logger.info("任务文件目录d:\\data\\qqwait4crawl不存在。");
							break;
						}
						logger.info("后台扫描任务文档线程结束。稍后再次启动。");
					}catch(Exception e){
					}
				}else{
					logger.info("任务队列中还留有上个文件中的数据。");
					logger.info("后台扫描任务文档线程结束。稍后再次启动。");
				}
				try {
					Thread.sleep(scantaskfileinterval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static class Mythread4 extends Thread{
		public void run(){
			for(;;){
				logger.info("开始同步缓存数据到文件中...");
				synchronized (dbCache) {
					logger.info("授权qq缓存当前有"+dbCache.size()+"条数据。");
					try{
						File file = new File("d:\\data\\qqlog");
						BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file,true));
						BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, "UTF-8"), 1024*1024);
						while(true){
							String o = dbCache.poll();
							if(o==null){
								break;
							}
							writer.write(o+"\r\n");
						}
						writer.flush();
						writer.close();
					}catch(Exception e){
						logger.info("同步数据缓存出错!");
					}
				}
				synchronized (notoautodbCache) {
					logger.info("未授权qq缓存当前有"+notoautodbCache.size()+"条数据。");
					try{
						File file = new File("d:\\data\\notoauthqqlog");
						BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file,true));
						BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, "UTF-8"), 1024*1024);
						while(true){
							String o = notoautodbCache.poll();
							if(o==null){
								break;
							}
							writer.write(o+"\r\n");
						}
						writer.flush();
						writer.close();
					}catch(Exception e){
						logger.info("同步数据缓存出错!");
					}
				}
				logger.info("同步线程结束，稍后再次重启。");
				try {
					Thread.sleep(persistenceinterval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static class Mythread5 extends Thread{
		public void run(){
			for(;;){
				logger.info("扫描代理IP线程启动。");
				BufferedReader breader0 = null;
				BufferedInputStream bis0 = null;
				try{
					for(Proxy proxy : proxyPool.validpool){
						logger.info(proxy.toString());
						proxy.resetstatus();
					}
					File file0 = new File("d:\\data\\proxyip");
					bis0 = new BufferedInputStream(new FileInputStream(file0));
					breader0 = new BufferedReader(new InputStreamReader(bis0, "UTF-8"),1024*1024);
					String proxyinfoLine = "";
					while((proxyinfoLine=breader0.readLine())!=null){
						try{
							String[] proxyinfo = proxyinfoLine.split("\t");
							String host = proxyinfo[0].trim();
							String port = proxyinfo[1].trim();
							String key = host.trim()+":"+port.trim();
							if(allProxy.contains(key)){
								continue;
							}else{
								HttpHost httphost = new HttpHost(host,Integer.valueOf(port));
								Proxy proxy = new Proxy(httphost);
								allProxy.add(httphost.toHostString());
								proxyPool.add(proxy);
							}
						}catch(Exception e){
							continue;
						}
					}
				}catch(Exception e){
				}
				logger.info("扫描代理IP线程结束。稍后再次启动。");
				try {
					Thread.sleep(scanproxyfileinterval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static int getACSRFToken(String skey){
		int hash = 5381;  
	    for (int i = 0, len = skey.length(); i < len; ++i)  
	        hash += (hash << 5) + TencentHttpLoginDemo.toUnicode(skey.charAt(i));  
	    return hash & 2147483647;
	}
	
	public static int toUnicode(char str){
        int iValue=0;
        iValue=(int)str;          
        return iValue;
    }
	
	private static TqqConn login(String account , String password){
		for(;;){
			HttpClientBuilder httpClientBuilder = HttpClients.custom().setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:32.0) Gecko/20100101 Firefox/32.0");
			CloseableHttpClient httpClient = httpClientBuilder.build();
			HttpClientContext context = HttpClientContext.create();
			CookieStore cookieStore = new BasicCookieStore();
			context.setCookieStore(cookieStore);
			TqqConn conn = new TqqConn(httpClient,context,new TqqAccount(account,password));
			try {
				String u1 = "";
				u1 = URLEncoder.encode("http://t.qq.com/", "UTF-8");
				String loginSigUIURL = "http://ui.ptlogin2.qq.com/cgi-bin/login?appid=716027609&style=13&lang=&low_login=1&hide_title_bar=1&hide_close_icon=1&self_regurl=http%3A//reg.t.qq.com/index.php&s_url="
						+ u1 + "&daid=6";
				CloseableHttpResponse response = conn.execute(loginSigUIURL);
				String login_sig = EntityUtils.toString(response.getEntity());
				login_sig = StringUtils.substringBetween(login_sig, "login_sig:\"", "\"");
				String verifyURL = "http://check.ptlogin2.qq.com/check?regmaster=&uin=" + account + "&appid=716027609&js_ver=10060&js_type=1&login_sig=" + login_sig + "&u1=" + u1 + "&r=" + Math.random();
				String loginURL = "http://ptlogin2.qq.com/login?";
				String verifyImageUrl = "http://captcha.qq.com/getimage?aid=716027609";
				String encodeUin = "";
				String curVerifyCode = "";
				int verifyCodeRetry = 2;
				int retryCount = 0;
				String br = "";
				String cap_cd="";
				while (retryCount <= verifyCodeRetry) {
					response = conn.execute(verifyURL);
					br = EntityUtils.toString(response.getEntity());
					String retCode = br.substring(14, 15);
					if (!retCode.equals("1")) {
						curVerifyCode = br.substring(18, 22);
						encodeUin = StringUtils.substringBetween(br, curVerifyCode+"','", "'");
						break;
					}else{
						cap_cd = StringUtils.substringBetween(br, "ptui_checkVC('1','", "'");
						encodeUin = StringUtils.substringBetween(br, cap_cd+"','", "'");
					}
					logger.info(account + ": 请输入验证码...");
					String curVerifyImageUrl = verifyImageUrl + "&uin=" + account + "&cap_cd=" + cap_cd;
					response = conn.execute(curVerifyImageUrl);
		            byte[] image = EntityUtils.toByteArray(response.getEntity());
		            VerifyDialog dialog = new VerifyDialog(image);
					dialog.setVisible(true);
					curVerifyCode = dialog.waitOK();
					if (!StringUtils.isEmpty(curVerifyCode)) {
						break;
					}
					retryCount++;
					if (retryCount >= verifyCodeRetry) {
						break;
					}
				}
				
				TQQLoginJS tqqLoginJS = new TQQLoginJS();
				String passwd = tqqLoginJS.getPassword(encodeUin, password, curVerifyCode);
				String urlEncodeUin = URLEncoder.encode(account, "UTF-8");
				String curLoginURL = loginURL
						+ "u="
						+ urlEncodeUin
						+ "&p="
						+ passwd
						+ "&verifycode="
						+ curVerifyCode
						+ "&aid=716027609&u1="
						+ u1
						+ "&h=1&ptredirect=1&ptlang=2052&daid=6&from_ui=1&dumy=&low_login_enable=1&low_login_hour=720&regmaster=&fp=loginerroralert&action=25-31-1386760113562&mibao_css=&t=1&g=1&js_ver=10060&js_type=1&login_sig="
						+ login_sig + "&pt_rsa=0";
				response = conn.execute(curLoginURL);
				String responseStr = EntityUtils.toString(response.getEntity());
				String retCode2 = StringUtils.substringBetween(responseStr, "ptuiCB('", "'");
				int loginSuccessRetCode = 0;// 登录成功
				int loginFailedRetCode = 3;// 帐号或密码错误
				int userNameErrorRetCode = 10;// 帐号不正确
				int netConnErrorRetCode = 7;// 提交参数错误，请检查。(1008730546)
				int userLimitRetCode = 19;// 您的号码已被冻结
				int verifyCodeWrong = 4;//验证码错误
				int iRetCode = -1;
				try {
					iRetCode = Integer.valueOf(retCode2);
				} catch (NumberFormatException e) {
					iRetCode = -1;
				}
				if (iRetCode == loginSuccessRetCode) {
					logger.info(account + "登录成功！");
					return conn;
				}else if(iRetCode == loginFailedRetCode){
					logger.info(account + "账号密码错误");
					return null;
				}else if(iRetCode == userNameErrorRetCode){
					logger.info(account + "账号错误");
					return null;
				}else if(iRetCode == userLimitRetCode){
					logger.info(account + "您的号码已被冻结");
					return null;
				}else if(iRetCode == netConnErrorRetCode){
					logger.info("提交参数错误，请检查。(1008730546)");
					continue;
				}else if(iRetCode == verifyCodeWrong ){
					continue;
				}else{
					return null;
				}
			} catch (UnsupportedEncodingException e1) {
				continue;
			} catch (ClientProtocolException e) {
				continue;
			} catch (IOException e) {
				continue;
			} catch (Exception e){
				continue;
			}
		}
	}

	public static void  main(String[] args) throws ParseException, IOException, InterruptedException, ClassNotFoundException, SQLException{
		
		Thread mythread = new Mythread();
		Thread mythread2 = new Mythread2();
		Thread mythread3 = new Mythread3();
		Thread mythread4 = new Mythread4();
		Thread mythread5 = new Mythread5();
		mythread.start();
		mythread2.start();
		mythread3.start();
		mythread4.start();
		mythread5.start();
		try {
			for(;;){
				final Request request = taskQueue.take();
				pool.execute(new Runnable(){
					@Override
					public void run() {
						for(;;){
							TqqConn tqqConn = null;
							try {
								tqqConn = connPool.take();
								Proxy proxy = proxyPool.get();
								proxy.borrow();
								tqqConn.setProxy(proxy);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
							String url = request.getUrl();
							try{
								String uinStr = StringUtils.substringBetween(url, "http://user.qzone.qq.com/", "/1");
								CloseableHttpResponse response = tqqConn.execute(url);
								if(response.getStatusLine().getStatusCode()!=200){
									if(tqqConn.getProxy().dead()){
										proxyPool.remove(tqqConn.getProxy());
										logger.info("移除无效代理IP>>>"+tqqConn.getProxy().toString());
									}
									tqqConn.setReuseTimeInterval(1500);
									connPool.add(tqqConn);
									continue;
								}
								String qqzonehtml = EntityUtils.toString(response.getEntity());
								response.close();
								List<Cookie> cookies = tqqConn.context.getCookieStore().getCookies();
								String skey="";
								for(Cookie cookie : cookies){
									if(cookie.getName().equals("skey")){
										skey = cookie.getValue();
									}
								}
								int g_tk = TencentHttpLoginDemo.getACSRFToken(skey);
								String userProfile_url = "http://base.s11.qzone.qq.com/cgi-bin/user/cgi_userinfo_get_all?uin="+uinStr+"&vuin="+tqqConn.account.account+"&fupdate=1&rd="+Math.random()+"&g_tk="+g_tk;
								response = tqqConn.execute(userProfile_url);
								if(response.getStatusLine().getStatusCode()!=200){
									if(tqqConn.getProxy().dead()){
										proxyPool.remove(tqqConn.getProxy());
										logger.info("移除无效代理IP>>>"+tqqConn.getProxy().toString());
									}
									tqqConn.setReuseTimeInterval(1500);
									connPool.add(tqqConn);
									continue;
								}
								String g_userProfile = EntityUtils.toString(response.getEntity());
								String uin = "";
								String home = "";
								int marriage = 0;
								String marriageStr = "";
								int sex = 0;
								String sexStr = "";
								int age = 0;
								String birthday = "";
								int birthyear = 0;
								String location = "";
								if(!StringUtils.isEmpty(g_userProfile)){
									String data = StringUtils.substringBetween(g_userProfile,"_Callback(",");");
									if(StringUtils.isEmpty(data)){
										tqqConn.setReuseTimeInterval(1500);
										connPool.add(tqqConn);
										continue;
									}
									Map<String, Object> json = JSONObject.parseObject(data);
									int code = (Integer) json.get("code");
									String message =  (String) json.get("message");
									if(code == 0 && "获取成功".equals(message)){
										Map<?, ?> dataMap = (Map<?, ?>) json.get("data");
										uin = dataMap.get("uin").toString();
										sex = (Integer) dataMap.get("sex");
										if(sex == 1){
											sexStr = "男";
										}else if(sex == 2){
											sexStr = "女";
										}else{
											sexStr = "";
										}
										age = (Integer) dataMap.get("age");
										birthyear = (Integer) dataMap.get("birthyear");
										birthday = (String) dataMap.get("birthday");
										birthday = birthyear + "-" + birthday;
										String country = (String) dataMap.get("country");
										String province = (String) dataMap.get("province");
										String city = (String) dataMap.get("city");
										String hco = (String) dataMap.get("hco");
										String hp = (String) dataMap.get("hp");
										String hc = (String) dataMap.get("hc");
										home = hco+"@"+hp+"@"+hc;
										location = country+"@"+province+"@"+city;
										marriage = (Integer) dataMap.get("marriage");
										if(marriage == 1){
											marriageStr = "单身";
										}else if(marriage == 2){
											marriageStr = "已婚";
										}else{
											marriageStr = "";
										}
										String qqInfo = String.format(""
												+ "{'uin':'%s',"
												+ "'sex':'%s',"
												+ "'age':%d,"
												+ "'birthday':'%s',"
												+ "'location':'%s',"
												+ "'home':'%s',"
												+ "'marriage':'%s'}", uin,sexStr,age,birthday,location,home,marriageStr);
										dbCache.add(qqInfo);
										tqqConn.setReuseTimeInterval(1500);
										connPool.add(tqqConn);
										tqqConn.getProxy().success();
										proxyPool.addValid(tqqConn.getProxy());
										break;
									}else if(message.contains("请先登录")){
										logger.info(tqqConn.account.account+ "	"+message);
										tqqConn.getProxy().success();
										proxyPool.addValid(tqqConn.getProxy());
										badconnPool.add(tqqConn);
									}else if(message.contains("服务器繁忙，请稍候再试。")){
										//代理IP错误
										//logger.info(tqqConn.account.account+ "	"+message);
										if(tqqConn.getProxy().dead()){
											proxyPool.remove(tqqConn.getProxy());
											logger.info("移除无效代理IP>>>"+tqqConn.getProxy().toString());
										}
										tqqConn.setReuseTimeInterval(1500);
										connPool.add(tqqConn);
									}else{
										//“您无权访问”的情况
										Html html = new Html(qqzonehtml);
										String user_infor = html.xpath("//div[@class='user_infor']/p[2]/text()").toString();
										if(StringUtils.isNotEmpty(user_infor)){
											if(user_infor.contains("男")){
												sexStr = "男";
												String qqInfo = String.format(""
														+ "{'uin':'%s',"
														+ "'sex':'%s',"
														+ "'age':%d,"
														+ "'birthday':'%s',"
														+ "'location':'%s',"
														+ "'home':'%s',"
														+ "'marriage':'%s'}", uinStr,sexStr,0,"","","","");
												notoautodbCache.add(qqInfo);
											}else if(user_infor.contains("女")){
												sexStr = "女";
												String qqInfo = String.format(""
														+ "{'uin':'%s',"
														+ "'sex':'%s',"
														+ "'age':%d,"
														+ "'birthday':'%s',"
														+ "'location':'%s',"
														+ "'home':'%s',"
														+ "'marriage':'%s'}", uinStr,sexStr,0,"","","","");
												notoautodbCache.add(qqInfo);
											}
										}
										tqqConn.setReuseTimeInterval(1500);
										tqqConn.getProxy().success();
										proxyPool.addValid(tqqConn.getProxy());
										connPool.add(tqqConn);
										break;
									}
								}else{
									logger.info("g_userProfile为空");
									tqqConn.setReuseTimeInterval(1500);
									connPool.add(tqqConn);
									tqqConn.getProxy().success();
									proxyPool.addValid(tqqConn.getProxy());
									break;
								}
							}catch(IOException e){
								//代理IP错误
								if(tqqConn.getProxy().dead()){
									proxyPool.remove(tqqConn.getProxy());
									logger.info("移除无效代理IP>>>"+tqqConn.getProxy().toString());
								}
								tqqConn.setReuseTimeInterval(1500);
								connPool.add(tqqConn);
							}catch(Exception e){
								e.printStackTrace();
								tqqConn.setReuseTimeInterval(1500);
								connPool.add(tqqConn);
								break;
							}
						}
					}
				});
			}
		}catch (Exception e) {
		} 
	}
}
