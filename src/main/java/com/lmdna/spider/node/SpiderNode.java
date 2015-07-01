package com.lmdna.spider.node;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.lang3.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import com.lmdna.spider.boot.SpiderProcess;
import com.lmdna.spider.dao.model.SpiderBiz;
import com.lmdna.spider.dao.model.SpiderFieldRule;
import com.lmdna.spider.dao.model.SpiderWebsite;
import com.lmdna.spider.dao.model.SpiderWebsiteConfig;
import com.lmdna.spider.downloader.LmdnaStatusfulConnectionPool;
import com.lmdna.spider.downloader.LmdnaStatusfulConnectionPoolManager;
import com.lmdna.spider.downloader.StatusFulDownloader;
import com.lmdna.spider.pageprocessor.LmdnaCommonPageProcessor;
import com.lmdna.spider.proxy.LmdnaProxyPool;
import com.lmdna.spider.utils.RunJar;
import com.lmdna.spider.utils.SpiderGlobalConfig;
import com.sun.xml.internal.ws.Closeable;

public abstract class SpiderNode implements Closeable{
	
	private static AtomicInteger decreaseId = new AtomicInteger(1000000);//id从999999开始往前取,避免与数据库自动生成的id重复
	
	protected SpiderContext _context;
	private static final Logger logger = LoggerFactory.getLogger(SpiderProcess.class);
	
	public SpiderNode(){
		
	}
	
	public abstract void run();
	/**
	 * worker不参加master竞选，follower参于master竞选
	 */
	public enum Level implements Serializable{
        master(0), follower(1),worker(2);

        private Level(int value) {
            this.value = value;
        }

        private int value;

        int getValue() {
            return value;
        }

        public static Level fromValue(int value) {
            for (Level status : Level.values()) {
                if (status.getValue() == value) {
                    return status;
                }
            }
            return follower;
        }
    }
	
	/**
	 * 给spider装配pageProcessor
	 * @param spider
	 * @param biz
	 * @param antiMonitorPolicy
	 * @param fieldRules
	 * @throws Exception
	 */
	public static Spider fixSpider(SpiderBiz biz) throws Exception{ 
		Spider spider = null;
		Site siteTemp = fixSite(biz);
		final Site siteFinal = siteTemp;
		PageProcessor pp = new LmdnaCommonPageProcessor(biz.getFieldRules(), siteFinal);
		//判断SpiderBiz中是否有指定的任务处理类,如果没有，则用通用的的Spider
		if(StringUtils.isEmpty(biz.getTaskProcessClass())){
			spider = Spider.create(pp);
		}else{
			Constructor<?> c = Class.forName(biz.getTaskProcessClass()).getConstructor();
			spider = (Spider) c.newInstance();
			spider.setSite(siteFinal);
		}
		//websiteConfigBO.needLogin:0需要登入，1不需要登入
		if(biz.getWebsiteConfigBO().getNeedLogin() == 0){
			spider.setDownloader(new StatusFulDownloader());
		}
		spider.setUUID(biz.getBizCode());
		return spider;
	}
	
	public static Site fixSite(SpiderBiz biz){
		SpiderWebsiteConfig websiteConfig = biz.getWebsiteConfigBO();
		if (websiteConfig == null) {
			logger.info(biz.getBizName() + ":the websiteConfig info not exist！");
			return null;
		} 
		SpiderWebsite website = websiteConfig.getWebsiteBO();
		if(website == null){
			logger.info(biz.getBizName() + ":the website info not exist！");
			return null;
		}
		Site siteTemp = null;
		siteTemp = Site.me()
				.setDomain(website.getDomain())
				.setCharset(website.getCharset())
				.setRetryTimes(websiteConfig.getRetryTimes())
				.setCycleRetryTimes(websiteConfig.getCycleRetryTimes())
				.setSleepTime(websiteConfig.getSleepTime());
		if(StringUtils.isNotEmpty(biz.getResponseValidCheck())){
			siteTemp.putValidCheck(0, biz.getResponseValidCheck());
		}
		//websiteConfigBO.needProxy:0需要代理IP，1不需要代理IP
		if(websiteConfig.getNeedProxy() == 0){
			LmdnaProxyPool proxyPool = new LmdnaProxyPool(biz);
			siteTemp.setHttpProxyPool(proxyPool);
		}else{
			logger.info(biz.getBizName() + " do not use proxy ip！");
		}
		if(websiteConfig.getNeedLogin() == 0){
			logger.info(biz.getBizName() + " need login!");
			LmdnaStatusfulConnectionPool connPool = LmdnaStatusfulConnectionPoolManager.instance().getPool(biz);
			if(connPool == null){
				logger.info(website.getSiteEnName()+":the connection pool not exist,START to load the connection pool...");
				connPool = LmdnaStatusfulConnectionPoolManager.instance().createPool(biz, siteTemp);
				logger.info(website.getSiteEnName()+":connection pool load completed.");
			}
			siteTemp.setConnectionPool(connPool);
		}
		final List<SpiderFieldRule> fieldRules = biz.getFieldRules();
		if(fieldRules == null)
			return siteTemp;
		for(SpiderFieldRule fieldRule : fieldRules){
			if(fieldRule.getAdditionRequest() == 1 || fieldRule.getAdditionDownload() == 1){
				if(StringUtils.isNotEmpty(fieldRule.getResponseValidCheck()))
				siteTemp.putValidCheck(fieldRule.getId(), fieldRule.getResponseValidCheck());
			}
		}
		return siteTemp;
	}
	
	/**
	 * 获取内存使用率（仅限于linux机器）
	 * @return
	 */
	protected float getMemoUsage() {
		float memUsage = 0.0f;
		Process pro = null;
		Runtime r = Runtime.getRuntime();
		try {
			String command = "cat /proc/meminfo";
			pro = r.exec(command);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					pro.getInputStream()));
			String line = null;
			int count = 0;
			long totalMem = 0, freeMem = 0;
			while ((line = in.readLine()) != null) {
				String[] memInfo = line.split("\\s+");
				if (memInfo[0].startsWith("MemTotal")) {
					totalMem = Long.parseLong(memInfo[1]);
				}
				if (memInfo[0].startsWith("MemFree")) {
					freeMem = Long.parseLong(memInfo[1]);
				}
				memUsage = 1 - (float) freeMem / (float) totalMem;
				if (++count == 2) {
					break;
				}
			}
			in.close();
			pro.destroy();
		} catch (IOException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
		}
		return memUsage;
	}
	
	private static void ensureDirectory(File dir) throws IOException {
	    if (!dir.mkdirs() && !dir.isDirectory()) {
	      throw new IOException("Mkdirs failed to create " +
	                            dir.toString());
	    }
	  }
	
	public String getPipelineClassFromJar(String jarPath)throws Throwable{
		String spiderClass = null;
		JarFile jarFile;
	    try {
	      jarFile = new JarFile(jarPath);
	    } catch(IOException io) {
	    	throw new IOException("Error opening job jar: " + jarPath).initCause(io);
	    }
	    Manifest manifest = jarFile.getManifest();
	    if (manifest != null) {
	    	spiderClass = manifest.getMainAttributes().getValue("Spider-Pipeline-Class");
	    }
	    jarFile.close();
		return spiderClass;
	}
	
	public String getSpiderClassFromJar(String jarPath) throws Throwable{
		String spiderClass = null;
		JarFile jarFile;
	    try {
	      jarFile = new JarFile(jarPath);
	    } catch(IOException io) {
	    	throw new IOException("Error opening job jar: " + jarPath).initCause(io);
	    }
	    Manifest manifest = jarFile.getManifest();
	    if (manifest != null) {
	    	spiderClass = manifest.getMainAttributes().getValue("Spider-Class");
	    }
	    jarFile.close();
		return spiderClass;
	}
	
	public String getTaskFileParseClassFromJarName(String jarPath) throws Throwable{
		String spiderTaskParseClass= null;
		JarFile jarFile;
	    try {
	      jarFile = new JarFile(jarPath);
	    } catch(IOException io) {
	    	throw new IOException("Error opening job jar: " + jarPath).initCause(io);
	    }
	    Manifest manifest = jarFile.getManifest();
	    if (manifest != null) {
		    spiderTaskParseClass = manifest.getMainAttributes().getValue("Spider-TaskFile-Parser");
	    }
	    jarFile.close();
		return spiderTaskParseClass;
	}
	
	/**
	 * 从下载过来的jar包中解析出spider类，加载到spider_pool中
	 * @throws IOException 
	 */
	public ClassLoader newClassLoader(String jarPath) throws Throwable{
		File file = new File(jarPath);
		if (!file.exists() || !file.isFile()){
			System.err.println("Not a valid JAR: " + file.getCanonicalPath());
		    System.exit(-1);
		}
		final File workDir;
		File tmpDir = new File(SpiderGlobalConfig.getValue(SpiderGlobalConfig.SPIDER_UNJAR_DIR));
		ensureDirectory(tmpDir);
		
		workDir = File.createTempFile("spider-unjar", "", tmpDir);
		workDir.delete();
		ensureDirectory(workDir);
		
		RunJar.unJar(file, workDir);
		ArrayList<URL> classPath = new ArrayList<URL>();
	    classPath.add(new File(workDir+"/").toURI().toURL());
	    classPath.add(new File(workDir, "classes/").toURI().toURL());
	    File[] libs = new File(workDir, "lib").listFiles();
	    if (libs != null) {
	    	for(int i = 0; i < libs.length; i++){
	    		classPath.add(libs[i].toURI().toURL());
	    	}
	    }
	    ClassLoader loader = new URLClassLoader(classPath.toArray(new URL[0]));
	    return loader;
	}
	
	public static void transformBizBeanToXml(SpiderBiz biz, String xml_file_path){
		
	}
	
	public static SpiderBiz parseBizBeanFromJar(String jarFilePath) throws Throwable{
		JarFile jarFile = new JarFile(new File(jarFilePath));
		Enumeration<JarEntry> es = jarFile.entries();
		while (es.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) es.nextElement();
            String name = jarEntry.getName();
            if(name.endsWith("spider_biz_config.xml")){
                InputStream inputStream = jarFile.getInputStream(jarEntry);  
                try {
					return parseBizBeanFromXml(inputStream);
				} catch (Throwable e) {
					throw e;
				}
            }
        }
		throw new Exception("没有找到spider_biz_config.xml文件!");
	}
	
	public static SpiderBiz parseBizBeanFromXml(InputStream xml_file_data) throws Throwable{
		int id = decreaseId.decrementAndGet();
		SpiderBiz bizBean = new SpiderBiz();
		bizBean.setId(id);
		SpiderWebsiteConfig websiteconfigBean = new SpiderWebsiteConfig();
		SpiderWebsite websiteBean = new SpiderWebsite();
        SAXBuilder builder = new SAXBuilder();
        Document doc = null;
        try {
            doc = builder.build(xml_file_data);
            if(doc != null){
                Element e = doc.getRootElement();
                List<Element> biz_properties = e.getChildren("property");
                for(Element element : biz_properties){
                	String property_name = element.getAttributeValue("name");
                	String property_value = element.getChildText("value");
                	Field field = SpiderBiz.class.getDeclaredField(property_name);
                	String fieldname = field.getName();
                	Class<?> fieldtype = field.getType();
                	if(fieldtype.isAssignableFrom(Integer.class)){
                		SpiderBiz.class.getMethod("set"+fieldname.substring(0,1).toUpperCase()+fieldname.substring(1), fieldtype).invoke(bizBean, Integer.valueOf(property_value));
                	}else if(fieldtype.isAssignableFrom(String.class)){
                		SpiderBiz.class.getMethod("set"+fieldname.substring(0,1).toUpperCase()+fieldname.substring(1), fieldtype).invoke(bizBean, property_value);
                	}else if(fieldtype.isAssignableFrom(Long.class)){
                		SpiderBiz.class.getMethod("set"+fieldname.substring(0,1).toUpperCase()+fieldname.substring(1), fieldtype).invoke(bizBean, Long.valueOf(property_value));
                	}else if(fieldtype.isAssignableFrom(Boolean.class)){
                		if("0".equals(property_value)){
                			SpiderBiz.class.getMethod("set"+fieldname.substring(0,1).toUpperCase()+fieldname.substring(1), fieldtype).invoke(bizBean, false);
                		}else if("1".equals(property_value)){
                			SpiderBiz.class.getMethod("set"+fieldname.substring(0,1).toUpperCase()+fieldname.substring(1), fieldtype).invoke(bizBean, true);
                		}else{
                			
                		}
                	}
                }
                Element websiteconfig = e.getChild("websiteconfig");
                List<Element> websiteconfig_properties = websiteconfig.getChildren("property");  
                for (Element element : websiteconfig_properties) {  
                	String property_name = element.getAttributeValue("name");
                	String property_value = element.getChildText("value");
                	Field field = SpiderWebsiteConfig.class.getDeclaredField(property_name);
                	String fieldname = field.getName();
                	Class<?> fieldtype = field.getType();
                	if(fieldtype.isAssignableFrom(Integer.class)){
                		SpiderWebsiteConfig.class.getMethod("set"+fieldname.substring(0,1).toUpperCase()+fieldname.substring(1), fieldtype).invoke(websiteconfigBean, Integer.valueOf(property_value));
                	}else if(fieldtype.isAssignableFrom(String.class)){
                		SpiderWebsiteConfig.class.getMethod("set"+fieldname.substring(0,1).toUpperCase()+fieldname.substring(1), fieldtype).invoke(websiteconfigBean, property_value);
                	}else if(fieldtype.isAssignableFrom(Long.class)){
                		SpiderWebsiteConfig.class.getMethod("set"+fieldname.substring(0,1).toUpperCase()+fieldname.substring(1), fieldtype).invoke(websiteconfigBean, Long.valueOf(property_value));
                	}else if(fieldtype.isAssignableFrom(Boolean.class)){
                		if("0".equals(property_value)){
                			SpiderWebsiteConfig.class.getMethod("set"+fieldname.substring(0,1).toUpperCase()+fieldname.substring(1), fieldtype).invoke(websiteconfigBean, false);
                		}else if("1".equals(property_value)){
                			SpiderWebsiteConfig.class.getMethod("set"+fieldname.substring(0,1).toUpperCase()+fieldname.substring(1), fieldtype).invoke(websiteconfigBean, true);
                		}else{
                			
                		}
                	}
                }  
                Element website = websiteconfig.getChild("website");
                List<Element> website_properties = website.getChildren("property");
                for (Element element : website_properties) {  
                	String property_name = element.getAttributeValue("name");
                	String property_value = element.getChildText("value");
                	Field field = SpiderWebsite.class.getDeclaredField(property_name);
                	String fieldname = field.getName();
                	Class<?> fieldtype = field.getType();
                	if(fieldtype.isAssignableFrom(Integer.class)){
                		SpiderWebsite.class.getMethod("set"+fieldname.substring(0,1).toUpperCase()+fieldname.substring(1), fieldtype).invoke(websiteBean, Integer.valueOf(property_value));
                	}else if(fieldtype.isAssignableFrom(String.class)){
                		SpiderWebsite.class.getMethod("set"+fieldname.substring(0,1).toUpperCase()+fieldname.substring(1), fieldtype).invoke(websiteBean, property_value);
                	}else if(fieldtype.isAssignableFrom(Long.class)){
                		SpiderWebsite.class.getMethod("set"+fieldname.substring(0,1).toUpperCase()+fieldname.substring(1), fieldtype).invoke(websiteBean, Long.valueOf(property_value));
                	}else if(fieldtype.isAssignableFrom(Boolean.class)){
                		if("0".equals(property_value)){
                			SpiderWebsite.class.getMethod("set"+fieldname.substring(0,1).toUpperCase()+fieldname.substring(1), fieldtype).invoke(websiteBean, false);
                		}else if("1".equals(property_value)){
                			SpiderWebsite.class.getMethod("set"+fieldname.substring(0,1).toUpperCase()+fieldname.substring(1), fieldtype).invoke(websiteBean, true);
                		}else{
                			
                		}
                	}
                }  
            }  
        } catch (JDOMException e) {  
            throw e;  
        }finally{  
            if(xml_file_data != null){  
            	xml_file_data.close();  
            }  
        }    
		websiteconfigBean.setWebsiteBO(websiteBean);
		bizBean.setWebsiteConfigBO(websiteconfigBean);
		return bizBean;
	}
	
	public SpiderContext get_config() {
		return _context;
	}
	public void setContext(SpiderContext _context) {
		this._context = _context;
	}
	public static void main(String[] args){
		Level s = Level.follower;
		System.out.println(s.toString());
	}
}
