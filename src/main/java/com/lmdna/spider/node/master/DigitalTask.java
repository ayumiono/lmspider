package com.lmdna.spider.node.master;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import us.codecraft.webmagic.Request;

import com.lmdna.spider.jar.utils.TaskFileParseProxyFacotry;
import com.lmdna.spider.protocol.TaskFileParseProtocol;
import com.lmdna.spider.protocol.rpc.utils.CrawlTask;
import com.lmdna.spider.protocol.rpc.utils.RemoteCmd;


public class DigitalTask<T extends RemoteCmd> extends AbstractTask<T> {

	private static final long serialVersionUID = 3890605172950471051L;
	private List<String> urls;
	
	public DigitalTask(List<String> urls){
		this.urls = urls;
		this.taskQueue = new LinkedBlockingQueue<T>();
	}

	@Override
	public void initializeQueue() {
//		setTaskId();
//		File file = new File(filePath);
//		if(file.isDirectory() || !file.exists()){
//			throw new IOException("task file not exist!");
//		}
//		TaskFileParseProtocol fileParser = master.getTaskFileParserCache().get(bizCode);
//		if(fileParser==null){
//			fileParser = (TaskFileParseProtocol) TaskFileParseProxyFacotry.create(master.getUriUniqFilter());
//		}
//		List<Request> reqs = fileParser.parse(new FileInputStream(file));
//		this.totalRow = reqs.size();
//		if(totalRow>=100){
//			master.requestCrawlCheckpoint();
//		}
//		if(totalRow == 0){
//			throw new IllegalArgumentException("no url not seen exist!");
//		}
//		if(modcount==0)
//			modcount = 1;
//		if(rowPerBlock==null){
//			rowPerBlock = totalRow/modcount;
//			//求出每个任务块行数合理值
//			while(rowPerBlock>10000){
//				rowPerBlock = rowPerBlock/modcount;
//			}
//		}
//		double d = Math.ceil((double)totalRow/rowPerBlock);
//		blockCount = new Double(d).intValue();
//		int blockid = 1;
//		taskQueue = new LinkedBlockingQueue<CrawlTask>(blockCount);
//		for(;blockid<blockCount;blockid++){
//			CrawlTask block = new CrawlTask();
//			List<Request> reqlist = new ArrayList<Request>();
//			for(int i=(blockid-1)*rowPerBlock;i<blockid*rowPerBlock;i++){
//				reqlist.add(reqs.get(i));
//			}
//			block.setReqs(reqlist);
//			block.setBizCode(bizCode);
//			block.setTaskId(taskId);
//			block.setBlockId(taskId+"_block"+blockid);
//			taskQueue.add(block);
//		}
//		CrawlTask block = new CrawlTask();
//		List<Request> reqlist = new ArrayList<Request>();
//		for(int i=(blockid-1)*rowPerBlock;i<totalRow;i++){
//			reqlist.add(reqs.get(i));
//		}
//		block.setBizCode(bizCode);
//		block.setTaskId(taskId);
//		block.setBlockId(taskId+"_block"+blockid);
//		taskQueue.add(block);
	}

	@Override
	public String getTaskId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T pollCmd() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getTotalCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getTotalBlockCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getLeftCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getLeftBlockCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Date getCreateDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTaskId() {
		this.taskId = "_"+System.currentTimeMillis();
	}
	
}
