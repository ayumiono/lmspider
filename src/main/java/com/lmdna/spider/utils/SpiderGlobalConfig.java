package com.lmdna.spider.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 文件配置信息
 * @author ayumiono
 *
 */
public class SpiderGlobalConfig {
	
	private static final String config_file_path = "spider_global_config.properties";
	
	public static final String SPIDER_POOL_SIZE = "spider.pool.size";
	public static final String SPIDER_AUTOLOAD_SWITCH = "spider.autoload.switch";
	public static final String SPIDER_SCHEDULER_CEILING = "spider.scheduler.ceiling";
	public static final String SPIDER_SLAVE_REQQUEUE_CEILING = "spider.slave.reqqueue.ceiling";
	public static final String SPIDER_BIGFILE_READCACHE = "spider.bigfile.readcache";
	public static final String SPIDER_BIGFILE_WRITECACHE = "spider.bigfile.writecache";
	
	public static final String SPIDER_TASKFILE_UPLOAD_DIR = "spider.taskfile.upload.dir";
	public static final String SPIDER_TASKFILE_DOWNLOAD_DIR="spider.taskfile.download.dir";
	public static final String SPIDER_PARSED_REQUEST_DIR = "spider.parsed.request.dir";
	public static final String SPIDER_TASKFILE_BAKUP_DIR = "spider.taskfile.bakup.dir";
	public static final String SPIDER_VERIFYIMG_DIR = "spider.verifyimg.dir";
	public static final String SPIDER_JAR_UPLOAD_DIR = "spider.jar.upload.dir";
	public static final String SPIDER_JAR_DOWNLOAD_DIR = "spider.jar.download.dir";
	public static final String SPIDER_UNJAR_DIR= "spider.unjar.dir";
	public static final String SPIDER_FILE_PERSISTENCE_PATH= "spider.file.persistence.path";
	
	public static final String MACHINE_ID = "machine.id";
	
	public static final String SPIDER_BDB_ENV_PATH = "spider.bdb.env.path";
	public static final String SPIDER_BDB_CHECKPOINT_DIR = "spider.bdb.checkpoint.dir";
	public static final String SPIDER_SLAVE_HEARTBEAT_INTERVAL = "spider.slave.heartbeat.interval";
	
	private static Properties pros;
	
	static{
		pros = new Properties();
		InputStream config = SpiderGlobalConfig.class.getClassLoader().getResourceAsStream(config_file_path);
		try {
			pros.load(config);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void reLoad(InputStream config){
		try {
			pros.load(config);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getValue(String key) {
		return pros.getProperty(key);
	}
	
	/**
	 * 添加或覆盖参数
	 * @param key
	 * @param value
	 */
	public static void putValue(String key,String value){
		pros.put(key, value);
	}
}
