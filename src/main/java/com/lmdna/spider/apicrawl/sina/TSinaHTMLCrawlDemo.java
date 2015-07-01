package com.lmdna.spider.apicrawl.sina;


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
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
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
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.thread.CountableThreadPool;

import com.lmdna.spider.exception.AuthorMatchException;
import com.lmdna.spider.exception.SpiderLoginException;
import com.lmdna.spider.utils.SpiderConstant;

public class TSinaHTMLCrawlDemo {
	
	private static final Logger logger = Logger.getLogger(TSinaHTMLCrawlDemo.class);
	
	private static ExecutorService executorService = Executors.newFixedThreadPool(1);
	
	private static CountableThreadPool pool = new CountableThreadPool(1, executorService);
	
	private static final BlockingQueue<Request> taskQueue = new LinkedBlockingQueue<Request>();
	
	private static final BlockingQueue<String> dbCache = new LinkedBlockingQueue<String>();
	
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
	
	private static final BlockingQueue<TSinaHTMLCrawlDemo.TsinaConn> connPool = new DelayQueue<TSinaHTMLCrawlDemo.TsinaConn>();
	private static final BlockingQueue<TSinaHTMLCrawlDemo.TsinaConn> badconnPool = new DelayQueue<TSinaHTMLCrawlDemo.TsinaConn>();
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
	
	private static class TsinaConn implements Delayed{
		private CloseableHttpClient httpclient;
		private HttpClientContext context;
		private TqqAccount account;
		private long canReuseTime = System.nanoTime() + TimeUnit.NANOSECONDS.convert(1500, TimeUnit.MILLISECONDS);
		private Proxy proxy;
		public TsinaConn(CloseableHttpClient httpclient,HttpClientContext context,TqqAccount account){
			this.httpclient = httpclient;
			this.context = context;
			this.account = account;
		}
		public CloseableHttpResponse execute(String url,String method,Map<String,String> headers,NameValuePair[] nameValuePairs) throws ClientProtocolException, IOException{
			RequestBuilder builder = null;
			
			if(method.equalsIgnoreCase("get")){
				builder = RequestBuilder.get();
			}else if(method.equalsIgnoreCase("post")){
				builder = RequestBuilder.post();
			}
			builder.setUri(url);
			RequestConfig.Builder configBuilder = RequestConfig.custom();
			configBuilder.setSocketTimeout(6000).setConnectTimeout(6000).setConnectionRequestTimeout(6000);
			if(proxy!=null){
				configBuilder.setProxy(proxy.getProxy());
			}
			if(headers!=null && headers.size()>0){
				for(Entry<String,String> entry : headers.entrySet()){
					builder.addHeader(entry.getKey(), entry.getValue());
				}
			}
			if(nameValuePairs!=null){
				for(NameValuePair obj : nameValuePairs){
					builder.addParameter(obj);
				}
			}
			builder.setConfig(configBuilder.build());
			return httpclient.execute(builder.build(),context);
		}
		
		@Override
		public int compareTo(Delayed o) {
			TsinaConn that = (TsinaConn) o;
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
						TsinaConn conn = badconnPool.poll();
						if(conn!=null){
							TsinaConn refreshedConn = login(conn.account.account, conn.account.password);
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
					File file0 = new File("d:\\data\\sinaaccount");
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
							TsinaConn conn = login(account,password);
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
						File file = new File("D:\\data\\sinaweibo\\accountonly");
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
										String account = line.trim();
										String url = "";
										if(account.startsWith("u/")){
											url = "http://weibo.com/"+account;
										}else if(account.matches("[0-9]+")){
											url = "http://weibo.com/u/"+account;
										}else{
											url = " http://weibo.com/"+account;
										}
										Request request = new Request(url);
										taskQueue.add(request);
										continue;
									}
									breader.close();
								}
								logger.info("任务文件"+f.getName()+"读取完成。当前任务队列中有"+taskQueue.size()+"个任务。");
								f.renameTo(new File("d:\\data\\sinaweibo\\accountonly\\"+f.getName()+".bak"));
								break;
							}
						}else{
							logger.info("任务文件目录d:\\data\\sinaweibo\\accountonly不存在。");
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
						File file = new File("d:\\data\\sinalog");
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
	        hash += (hash << 5) + TSinaHTMLCrawlDemo.toUnicode(skey.charAt(i));  
	    return hash & 2147483647;
	}
	
	public static int toUnicode(char str){
        int iValue=0;
        iValue=(int)str;          
        return iValue;
    }
	
	private static TsinaConn login(String account , String password){
		for(;;){
			String preLoginUrl = "http://login.sina.com.cn/sso/prelogin.php?entry=weibo&callback=sinaSSOController.preloginCallBack&rsakt=mod&checkpin=1&client=ssologin.js(v1.4.18)";
			String loginUrl = "http://login.sina.com.cn/sso/login.php?client=ssologin.js(v1.4.18)";
			HttpClientBuilder httpClientBuilder = HttpClients.custom().setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:32.0) Gecko/20100101 Firefox/32.0");
			CloseableHttpClient httpClient = httpClientBuilder.build();
			HttpClientContext context = HttpClientContext.create();
			CookieStore cookieStore = new BasicCookieStore();
			context.setCookieStore(cookieStore);
			TsinaConn conn = new TsinaConn(httpClient,context,new TqqAccount(account,password));
			try {
				TSinaLoginJS tsinaLoginJS = new TSinaLoginJS();
				String su = tsinaLoginJS.getSU(account);
				String su2 = su.replaceAll("=", "%3D");
				String curPrefixLoginUrl = preLoginUrl + "&su=" + su2 + "&_=" + System.currentTimeMillis();
//				String curPrefixLoginUrl = preLoginUrl + "&_=" + System.currentTimeMillis();
				Map<String,String> headers = new HashMap<String,String>();
				headers.put("Referer", "http://weibo.com/");
				CloseableHttpResponse response = conn.execute(curPrefixLoginUrl,"get",headers,null);
				String preloginstr = EntityUtils.toString(response.getEntity());
				boolean needVerifyCode = false;
				
				String showpin = StringUtils.substringBetween(preloginstr, "showpin\":", ",");
				needVerifyCode = null != showpin && showpin.equals("1");
				int verifyCodeRetry = 2;
				int retryCount = 0;
				while(retryCount<=verifyCodeRetry){
					if(needVerifyCode){
						System.out.println("需要输入验证码...");
						retryCount++;
					}
					break;
				}
				
				String nonce = StringUtils.substringBetween(preloginstr, "nonce\":\"", "\"");
				String pubkey = StringUtils.substringBetween(preloginstr, "pubkey\":\"", "\"");
				String rsakv = StringUtils.substringBetween(preloginstr, "rsakv\":\"", "\"");
				
				String strServerTime = StringUtils.substringBetween(preloginstr, "servertime\":", ",");
				String sp = tsinaLoginJS.getSP(pubkey, strServerTime, nonce, password);
				
				NameValuePair loginparams[] = { 
						new BasicNameValuePair("encoding", "UTF-8"),
						new BasicNameValuePair("returntype", "META"),
						new BasicNameValuePair("entry", "weibo"),
						new BasicNameValuePair("nonce", nonce),
						new BasicNameValuePair("sp", sp),
						new BasicNameValuePair("rsakv", rsakv),
						new BasicNameValuePair("su", su2),
						new BasicNameValuePair("useticket", "1"),
						new BasicNameValuePair("vsnf", "1"),
						new BasicNameValuePair("servertime", strServerTime),
						new BasicNameValuePair("pwencode", "rsa2"),
						new BasicNameValuePair("url", "http://weibo.com/ajaxlogin.php?framelogin=1&callback=parent.sinaSSOController.feedBackUrlCallBack"),
						new BasicNameValuePair("service","miniblog"),
						new BasicNameValuePair("pagerefer","http://login.sina.com.cn/sso/logout.php?entry=miniblog&r=http%3A%2F%2Fweibo.com%2Flogout.php%3Fbackurl%3D%252F")
				};
				
				response = conn.execute(loginUrl,"post",headers,loginparams);
				String loginstr = EntityUtils.toString(response.getEntity());
				String retCode_0 = "retcode=0";// 登录成功
	            String retCode_5 = "retcode=5";// 登录名或密码错误
	            String retCode_20 = "retcode=20";// 用户名为null
	            String retCode_80 = "retcode=80";// 密码为null
	            String retCode_101 = "retcode=101";// 用户名正确，密码错误
	            String retCode_4057 = "retcode=4057";// 您的账号有异常，请验证身份
	            String retCode_4049 = "retcode=4049";// 为了您的帐号安全，请输入验证码
	            String retCode_4403 = "retcode=4403";// 抱歉！登录失败，请稍候再试
	            String retcode_4069 = "retcode=4069";// http://weibo.com/ajaxlogin.php?framelogin=1&callback=parent.sinaSSOController.feedBackUrlCallBack&sudaref=weibo.com&retcode=4069&reason=帐号太久未登录，请<a href="http://login.sina.com.cn/member/testify/testify.php?entry=weibo&at=AT-MTkxMTgzOTg0MQ==-1363326474-xd-E0AFD6B9F0EDD4F0EF3A7031AFB52157&r=http%3A%2F%2Fweibo.com%2F" target="_blank">验证身份</a>|http://login.sina.com.cn/member/testify/testify.php?entry=weibo&at=AT-MTkxMTgzOTg0MQ==-1363326474-xd-E0AFD6B9F0EDD4F0EF3A7031AFB52157&r=http%3A%2F%2Fweibo.com%2F
	            String retcode_4040 = "retcode=4040";// retcode=4040&reason=%B5%C7%C2%BC%B4%CE%CA%FD%B9%FD%B6%E0%A1%A3
	            String retcode_2070 = "retcode=2070";// retcode=2070&reason=%CA%E4%C8%EB%B5%C4%D1%E9%D6%A4%C2%EB%B2%BB%D5%FD%C8%B7
				
	            if (loginstr.indexOf(retCode_0) != -1)
	            {
	                String curAjaxLoginUrl = StringUtils.substringBetween(loginstr, "location.replace('", "'");
					if (StringUtils.isEmpty(curAjaxLoginUrl)) {
						curAjaxLoginUrl = StringUtils.substringBetween(loginstr, "location.replace(\"", "\"");
					}
					headers.put("Referer", "http://weibo.com/");
	                response = conn.execute(curAjaxLoginUrl,"get",headers,null);
	                String content = EntityUtils.toString(response.getEntity());
	                
	                String result = StringUtils.substringBetween(content, "result\":", ",");
	                if(!"true".equals(result)){
	                	throw new SpiderLoginException("登录失败");
	                }
	                response = conn.execute("http://weibo.com/","get",headers,null);
	                content = EntityUtils.toString(response.getEntity());
	                System.out.print(content);
	                if(content.indexOf("抱歉，您的帐号存在异常")!=-1){
	                	throw new AuthorMatchException("抱歉，您的帐号存在异常，目前无法进行登录。");
	                }
					if(content.indexOf("手机验证")!=-1 || content.indexOf("微博帐号解冻")!=-1){
						throw new AuthorMatchException("你当前使用的帐号异常，请完成手机验证，提升帐号安全。。");
					}
	                if(content.indexOf("帐号存在高危风险")!=-1){
	                	throw new AuthorMatchException("帐号安全系统检测到您的帐号存在高危风险，请先验证安全信息并修改密码，以保障您帐号安全！");
	                }
	                if(content.indexOf("您的帐号存在安全风险")!=-1){
	                	throw new AuthorMatchException("抱歉，您的帐号存在异常，目前无法进行登录。");
	                }
	                return conn;
	            }else if(loginstr.indexOf(retcode_2070) != -1){
	            	throw new SpiderLoginException("输入的验证码不正确");
	            }else if (loginstr.indexOf(retCode_5) != -1){
//	                throw new SpiderLoginException("登录名或密码错误");
	            	return null;
	            }else if (loginstr.indexOf(retCode_20) != -1){
//	                throw new SpiderLoginException("请输入正确的用户名");
	            	return null;
	            }else if (loginstr.indexOf(retCode_80) != -1){
//	                throw new SpiderLoginException("请输入正确的密码");
	                return null;
	            }else if (loginstr.indexOf(retCode_101) != -1){
//	                throw new SpiderLoginException("请输入正确的密码");
	            	return null;
	            }else if(loginstr.indexOf(retcode_4069) != -1){
//					throw new AuthorMatchException("帐号太久未登录，需验证身份");
	            	return null;
				}else if(loginstr.indexOf(retCode_4057) != -1){
//	            	throw new AuthorMatchException("您的账号有异常，请验证身份");
					return null;
	            }else if(loginstr.indexOf(retCode_4049) != -1){
//	            	if(needVerifyCode){
//	            		throw new AuthorMatchException(SpiderConstant.AUTHORMATCH_VERIFYCODE,"为了您的帐号安全，请输入验证码");
//	            	}else{
//	            		throw new AuthorMatchException(SpiderConstant.AUTHORMATCH_VERIFYCODE2,"为了您的帐号安全，请输入验证码2");
//	            	}
	            	return null;
	            }else if(loginstr.indexOf(retCode_4403) != -1){
//	            	 throw new SpiderLoginException("抱歉！登录失败，请稍候再试");
	            	return null;
	            }else if(loginstr.indexOf(retcode_4040) != -1){
//	            	throw new AuthorMatchException(SpiderConstant.AUTHORMATCH_LOGIN_OFTEN,"账号登录次数过多");
	            	return null;
	            }else{
//	                throw new SpiderLoginException("访问新浪微博出现异常，请稍后再试" + loginstr);
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
							TsinaConn tqqConn = null;
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
								CloseableHttpResponse response = tqqConn.execute(url,"get",null,null);
								if(response.getStatusLine().getStatusCode()!=200){
									if(tqqConn.getProxy().dead()){
										proxyPool.remove(tqqConn.getProxy());
										logger.info("移除无效代理IP>>>"+tqqConn.getProxy().toString());
									}
									tqqConn.setReuseTimeInterval(1500);
									connPool.add(tqqConn);
									continue;
								}
								String sinahtml = EntityUtils.toString(response.getEntity());
								response.close();
								String page_id = "";
								page_id = StringUtils.substringBetween(sinahtml, "$CONFIG['page_id']='", "';");
								String personal_info_url = "http://weibo.com/p/"+page_id+"/info?mod=pedit_more";
								response = tqqConn.execute(personal_info_url,"get",null,null);
								if(response.getStatusLine().getStatusCode()!=200){
									if(tqqConn.getProxy().dead()){
										proxyPool.remove(tqqConn.getProxy());
										logger.info("移除无效代理IP>>>"+tqqConn.getProxy().toString());
									}
									tqqConn.setReuseTimeInterval(1500);
									connPool.add(tqqConn);
									continue;
								}
								String personal_info = EntityUtils.toString(response.getEntity());
								if(!StringUtils.isEmpty(personal_info)){
									//“您无权访问”的情况
									Html html = new Html(personal_info);
									String headfollowcount = html.xpath("//table[@class='tb_counter']//tr/td[1]//strong/text()").toString();
									String fanscount = html.xpath("//table[@class='tb_counter']//tr/td[2]//strong/text()").toString();
									String weibocount = html.xpath("//table[@class='tb_counter']//tr/td[3]//strong/text()").toString();
									System.out.println(headfollowcount);
									System.out.println(fanscount);
									System.out.println(weibocount);
									tqqConn.setReuseTimeInterval(1500);
									tqqConn.getProxy().success();
									proxyPool.addValid(tqqConn.getProxy());
									connPool.add(tqqConn);
									break;
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
