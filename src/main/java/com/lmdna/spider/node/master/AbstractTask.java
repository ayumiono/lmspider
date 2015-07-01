package com.lmdna.spider.node.master;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import com.lmdna.spider.protocol.rpc.utils.RemoteCmd;

import us.codecraft.webmagic.Request;

public abstract class AbstractTask<T extends RemoteCmd> implements Serializable,TaskInterface<T>{

	private static final long serialVersionUID = 3127743550703148302L;
	protected String taskId;
	protected List<Request> reqs;
	protected int distributedRows;
	protected int totalRow;
	protected Integer rowPerBlock;//每个块的行数
	protected int blockCount;
	protected BlockingQueue<T> taskQueue;
	protected Date createTime = new Date();//创建日期
	
	public AbstractTask(){
		
	}
	
	public abstract void initializeQueue();
	public abstract void setTaskId();
}
