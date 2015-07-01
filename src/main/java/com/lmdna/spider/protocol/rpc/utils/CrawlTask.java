package com.lmdna.spider.protocol.rpc.utils;

import java.io.Serializable;


public class CrawlTask extends RemoteCmd{

	private static final long serialVersionUID = 3231440858734683957L;
	private String taskId;//taskId必须与task_file_path一一对应
	private String bizCode;//任务文件对应的业务代号
	private String blockId;
	private String taskFilePath;
	private String taskFileName;
	private int start;
	private int end;
	private int size;
//	private boolean finished = false;
	public CrawlTask(){
		this.setCmdType(CmdType.REMOTE_CMD_NEWTASK);
	}
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public String getBizCode() {
		return bizCode;
	}
	public void setBizCode(String bizCode) {
		this.bizCode = bizCode;
	}
	public String getBlockId() {
		return blockId;
	}
	public void setBlockId(String blockId) {
		this.blockId = blockId;
	}
	
	public String getTaskFilePath() {
		return taskFilePath;
	}
	public void setTaskFilePath(String taskFilePath) {
		this.taskFilePath = taskFilePath;
	}
	public String getTaskFileName() {
		return taskFileName;
	}
	public void setTaskFileName(String taskFileName) {
		this.taskFileName = taskFileName;
	}
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public int getEnd() {
		return end;
	}
	public void setEnd(int end) {
		this.end = end;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
//	public boolean isFinished() {
//		return finished;
//	}
//	public void setFinished(boolean finished) {
//		this.finished = finished;
//	}
	public String toString(){
		return "Bizcode:"+bizCode+" TaskId:"+taskId+" BlockId:"+blockId+" StartRow:"+start+" EndRow:"+end;
//		return "Bizcode:"+bizCode+" TaskId:"+taskId+" BlockId:"+blockId+" StartRow:"+start+" EndRow:"+end+" Finished:"+finished;
	}

	public enum Status implements Serializable{
        init(0), distributed(1), parsed(2), completed(3);

        private Status(int value) {
            this.value = value;
        }

        private int value;

        int getValue() {
            return value;
        }

        public static Status fromValue(int value) {
            for (Status status : Status.values()) {
                if (status.getValue() == value) {
                    return status;
                }
            }
            return init;
        }
    }
}
