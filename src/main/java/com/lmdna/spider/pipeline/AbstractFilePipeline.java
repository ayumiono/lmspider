package com.lmdna.spider.pipeline;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.utils.LoggerUtil;

import com.lmdna.spider.utils.SpiderGlobalConfig;


/**
 * @author ayumiono
 *
 */
public abstract class AbstractFilePipeline implements BatchPipeline{

	protected File dir;
	protected File file;
	private static DecimalFormat df = new DecimalFormat("00000000");
	private static String lineSeparator = System.getProperty("line.separator", "\n");
	private volatile AtomicBoolean isFlushing = new AtomicBoolean(false);
	private volatile AtomicInteger _count = new AtomicInteger(0);
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	private static final String OTHER = "other".intern();
	private static final String FILE_SUFIX = ".src".intern();
	
	protected final Map<String,List<String>> fileCache = new HashMap<String,List<String>>();
	protected Logger logger = LoggerFactory.getLogger(AbstractFilePipeline.class);
	
	public static void ensureDirectory(File dir) throws IOException {
		if (!dir.mkdirs() && !dir.isDirectory()) {
			throw new IOException("Mkdirs failed to create " + dir.toString());
		}
	}
	
	private void initLogDir() throws IOException{
		if(dir!=null){
			String dirname = dir.getName();
			Date curDate = new Date();
			String curDateStr = dateFormat.format(curDate);
			if(!curDateStr.equals(dirname)){
				dir = new File(SpiderGlobalConfig.getValue(SpiderGlobalConfig.SPIDER_FILE_PERSISTENCE_PATH)+File.separator+curDateStr);
			}
		}else{
			Date curDate = new Date();
			String dirname = dateFormat.format(curDate);
			dir = new File(SpiderGlobalConfig.getValue(SpiderGlobalConfig.SPIDER_FILE_PERSISTENCE_PATH)+File.separator+dirname);
		}
	}
	
	private void switchFile(String host) throws IOException{
		initLogDir();
		File subDir = new File(dir,host);
		subDir.mkdirs();
		File[] files = subDir.listFiles();
		String smallFileName = "00000000"+FILE_SUFIX;
		if(files.length==0){
			file = new File(subDir,smallFileName);
			return;
		}
		for(File f : files){
			String currentFileName = f.getName();
			if(currentFileName.compareTo(smallFileName)>=0){
				file = f;
				smallFileName = currentFileName;
			}
		}
		if(file.exists()){
			FileInputStream fis = new FileInputStream(file);
			if(fis.available()/(1024*1024)>=100){
				int currentFileIndex = Integer.parseInt(StringUtils.substringBefore(file.getName(), FILE_SUFIX));
				String newname = df.format(currentFileIndex+1)+FILE_SUFIX;
				this.file = new File(subDir,newname);
			}
			fis.close();
		}
	}
	
	public abstract String processSinglePage(Page page) throws Exception;
	
	@Override
	public void process(Page page, Task task) {
		String content;
		try {
			content = processSinglePage(page);
		} catch (Exception e1) {
			return;
		}
		String host = page.getHost();
		if(host==null){
			host = OTHER;
		}
		if(fileCache.get(host)==null){
			fileCache.put(host, new ArrayList<String>());
		}
		if(_count.incrementAndGet()>=10000){
			synchronized (fileCache) {
				if(fileCache.size()<10000){
					fileCache.get(host).add(content);
					return;
				}
				isFlushing.set(true);
				LoggerUtil.info("AbstractFilePipeline",task.getUUID()+">>>开始持久化文件缓存...");
				try {
					for(Entry<String,List<String>> entry : fileCache.entrySet()){
						switchFile(entry.getKey());
						OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file, true));
						for(String item : entry.getValue()){
							osw.write(item+lineSeparator);
						}
						entry.getValue().clear();
						osw.flush();
						osw.close();
					}
				} catch (IOException e) {
					LoggerUtil.info("AbstractFilePipeline",task.getUUID()+">>>持久化文件缓存失败.");
				}
				isFlushing.set(false);
				LoggerUtil.info("AbstractFilePipeline",task.getUUID()+">>>持久化文件缓存结束.");
			}
		}else{
			fileCache.get(host).add(content);
		}
	}
	
	@Override
	public void flush(Task task){
		if(isFlushing.get()){
			LoggerUtil.info("AbstractFilePipeline",task.getUUID()+">>>正在持久化文件缓存.");
			return;
		}
		synchronized (fileCache) {
			LoggerUtil.info("AbstractFilePipeline",task.getUUID()+">>>开始持久化文件缓存...");
			try {
				for(Entry<String,List<String>> entry : fileCache.entrySet()){
					switchFile(entry.getKey());
					OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file, true));
					for(String item : entry.getValue()){
						osw.write(item+lineSeparator);
					}
					entry.getValue().clear();
					osw.flush();
					osw.close();
				}
			} catch (IOException e) {
				LoggerUtil.info("AbstractFilePipeline",task.getUUID()+">>>持久化文件缓存失败.");
			}
			LoggerUtil.info("AbstractFilePipeline",task.getUUID()+">>>持久化文件缓存结束.");
		}
	}
	
	public static void main(String[] args) throws IOException, NoSuchMethodException, SecurityException{
		Pipeline p = new AbstractFilePipeline() {
			
			@Override
			public String processSinglePage(Page page) {
				return null;
			}
		};
		Method m = p.getClass().getMethod("flush", Task.class);
		System.out.print(m.getName());
	}
}
