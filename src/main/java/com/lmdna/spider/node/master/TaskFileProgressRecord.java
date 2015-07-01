package com.lmdna.spider.node.master;

public class TaskFileProgressRecord {
	private String taskId;//任务id
	private String bizCode;//业务id
	private Integer bizId;
	private int finishedRows;//完成的行数
	private int nextStartRow;//下次从哪一行开始执行
	private int totalRows;//任务总行数
	private int distributedRows;
	private String task_file_path;//任务文件路径
	private String task_file_name;
	private boolean completed;
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
	public Integer getBizId() {
		return bizId;
	}
	public void setBizId(Integer bizId) {
		this.bizId = bizId;
	}
	public int getFinishedRows() {
		return finishedRows;
	}
	public void setFinishedRows(int finishedRows) {
		this.finishedRows = finishedRows;
	}
	public int getNextStartRow() {
		return nextStartRow;
	}
	public void setNextStartRow(int nextStartRow) {
		this.nextStartRow = nextStartRow;
	}
	public int getTotalRows() {
		return totalRows;
	}
	public void setTotalRows(int totalRows) {
		this.totalRows = totalRows;
	}
	public int getDistributedRows() {
		return distributedRows;
	}
	public void setDistributedRows(int distributedRows) {
		this.distributedRows = distributedRows;
	}
	public String getTask_file_path() {
		return task_file_path;
	}
	public void setTask_file_path(String task_file_path) {
		this.task_file_path = task_file_path;
	}
	public String getTask_file_name() {
		return task_file_name;
	}
	public void setTask_file_name(String task_file_name) {
		this.task_file_name = task_file_name;
	}
	public boolean isCompleted() {
		return completed;
	}
	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
}
