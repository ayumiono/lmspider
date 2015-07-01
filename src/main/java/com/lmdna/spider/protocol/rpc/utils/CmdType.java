package com.lmdna.spider.protocol.rpc.utils;

public class CmdType {
	//spider
	public final static String REMOTE_CMD_SPIDER_RESTART = "spider_restart";//TODO
	public final static String REMOTE_CMD_SPIDER_REMOVE = "spider_remove";//TODO
	public final static String REMOTE_CMD_SPIDER_TERMINATE = "spider_terminate";//TODO
	
	public final static String REMOTE_CMD_COMMON_SPIDER_LOAD = "common_spider_load";
	public final static String REMOTE_CMD_JAR_SPIDER_LOAD = "jar_spider_load";
	//task
	public final static String REMOTE_CMD_NEWTASK = "task_new";
	public final static String REMOTE_CMD_TASK_FILE_BACKUP = "task_file_backup";
	public final static String REMOTE_CMD_TASK_FILE_DELETE = "task_file_delete";
	public final static String REMOTE_CMD_TASK_FINISHED = "task_finished";
	public final static String REMOTE_CMD_MIXEDTASK = "mixed_task";
	//crawl result
	public final static String REMOTE_CMD_CRAWL_RESULT_UPLOAD="crawl_result_upload";
	//checkpoint
	public final static String REMOTE_CMD_CHECKPOINT = "checkpoint";
}
