package com.lmdna.spider.node.master;

import java.util.Date;

import com.lmdna.spider.protocol.rpc.utils.RemoteCmd;

public interface TaskInterface<T extends RemoteCmd>{
	public String getTaskId();
	public T pollCmd();
	public long getTotalCount();
	public long getTotalBlockCount();
	public long getLeftCount();
	public long getLeftBlockCount();
	public Date getCreateDate();
}
