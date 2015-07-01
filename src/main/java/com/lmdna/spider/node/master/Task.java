package com.lmdna.spider.node.master;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import us.codecraft.webmagic.Request;

import com.alibaba.fastjson.JSONObject;
import com.lmdna.spider.jar.utils.TaskFileParseProxyFacotry;
import com.lmdna.spider.protocol.TaskFileParseProtocol;
import com.lmdna.spider.protocol.rpc.utils.CrawlTask;
import com.lmdna.spider.utils.SpiderGlobalConfig;

/**
 * 任务文件分发核心类
 * @author ayumiono
 */
public class Task implements Serializable{
	
	private static final long serialVersionUID = 352739496787636828L;
	private String taskId;
	private String bizCode;
	private String originalTaskFilePath;
	private String originalTaskFileName;
	private String serializeReqFilePath;
	private String serializeReqFileName;
	private int distributedRows;
	private int finishedRows;
	private int totalRow;
	private Integer rowPerBlock;//每个块的行数
	private int blockCount;
	private BlockingQueue<CrawlTask> taskQueue;
//	private List<CrawlTask> allTask;//记录块完成情况
	private Map<String, CrawlTask> _distributeLog = new HashMap<String, CrawlTask>();
	private Date createTime = new Date();//创建日期
	private boolean over = false;
	
	public Task(MasterNode master,int modcount,String bizCode,String filePath,String fileName,Integer rowperblock) throws Exception{
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		BufferedReader br = null;
		try{
			this.rowPerBlock = rowperblock;
			this.bizCode = bizCode;
			this.originalTaskFilePath = filePath;
			this.originalTaskFileName = fileName;
			this.taskId = bizCode+"_"+System.currentTimeMillis();
			TaskFileParseProtocol fileParser = master.getTaskFileParserCache().get(bizCode);
			if(fileParser==null){
				fileParser = (TaskFileParseProtocol) TaskFileParseProxyFacotry.create(master.getUriUniqFilter());
			}
			
			File file = new File(filePath);
			if(file.isDirectory() || !file.exists()){
				throw new IOException("task file not exist!");
			}
			fis = new FileInputStream(file);
			bis = new BufferedInputStream(fis);
			br = new BufferedReader(new InputStreamReader(bis, "UTF-8"),1024*1024);
			
			List<Request> reqs = new ArrayList<Request>();
			String lineSeparator = System.getProperty("line.separator", "\n");
			SimpleDateFormat dateFormat = new SimpleDateFormat();
			dateFormat.applyPattern("yyyyMMdd");
			File serializeRequestFile = new File(SpiderGlobalConfig.getValue(SpiderGlobalConfig.SPIDER_PARSED_REQUEST_DIR)+dateFormat.format(new Date())+File.separator+fileName);
			if(!serializeRequestFile.getParentFile().exists()){
				serializeRequestFile.getParentFile().mkdirs();
			}
			//按每100万行切分文件
			int count = 1000000;
			int floor = 0;
			int ceil = count;
			
			File tempfile = new File(file.getParent(),file.getName()+".temp");
			FileWriter fw = new FileWriter(tempfile,false);
			
			String line = "";
			int no = 0;
			while((line = br.readLine())!=null){
				no+=1;
				if(no>floor && no<=ceil){
					fw.write(line+lineSeparator);
				}else{
					fw.flush();
					fw.close();
					try{
						FileInputStream input = new FileInputStream(tempfile);
						reqs = fileParser.parse(input);
						input.close();
						this.totalRow+=reqs.size();
					}catch(Exception e){
						throw e;
					}
					FileWriter fww = new FileWriter(serializeRequestFile,true);
					for(Request req : reqs){
						String jsontext = JSONObject.toJSONString(req);
						fww.write(jsontext+lineSeparator);
					}
					fww.flush();
					fww.close();
					ceil = ceil + count;
					floor = floor + count;
					fw = new FileWriter(tempfile,false);
				}
			}
			fw.flush();
			fw.close();
			try{
				FileInputStream input = new FileInputStream(tempfile);
				reqs = fileParser.parse(input);
				input.close();
				this.totalRow+=reqs.size();
			}catch(Exception e){
				throw e;
			}
			FileWriter fww = new FileWriter(serializeRequestFile,true);
			for(Request req : reqs){
				String jsontext = JSONObject.toJSONString(req);
				fww.write(jsontext+lineSeparator);
			}
			fww.flush();
			fww.close();
			br.close();
			tempfile.delete();
			if(totalRow == 0){
				throw new IllegalArgumentException("no url not seen exist!");
			}
			this.serializeReqFilePath = serializeRequestFile.getPath();
			this.serializeReqFileName = serializeRequestFile.getName();
			if(modcount==0)
				modcount = 1;
			if(rowPerBlock==null){
				rowPerBlock = totalRow/modcount;
				//求出每个任务块行数合理值
				while(rowPerBlock>10000){
					rowPerBlock = rowPerBlock/modcount;
				}
			}
			double d = Math.ceil((double)totalRow/rowPerBlock);
			blockCount = new Double(d).intValue();
			int blockid = 1;
			taskQueue = new LinkedBlockingQueue<CrawlTask>(blockCount);
//			allTask = new ArrayList<CrawlTask>(blockCount);
			for(;blockid<blockCount;blockid++){
				CrawlTask block = new CrawlTask();
				block.setStart((blockid-1)*rowPerBlock+1);
				block.setEnd(blockid*rowPerBlock);
				block.setBizCode(bizCode);
				block.setTaskId(taskId);
				block.setSize(rowPerBlock);
				block.setBlockId(taskId+"_block"+blockid);
				block.setTaskFileName(this.serializeReqFileName);
				block.setTaskFilePath(this.serializeReqFilePath);
				taskQueue.add(block);
//				allTask.add(block);
			}
			CrawlTask block = new CrawlTask();
			block.setBizCode(bizCode);
			block.setTaskId(taskId);
			block.setBlockId(taskId+"_block"+blockid);
			block.setTaskFileName(this.serializeReqFileName);
			block.setTaskFilePath(this.serializeReqFilePath);
			block.setStart((blockid-1)*rowPerBlock+1);
			block.setEnd(totalRow);
			block.setSize(totalRow-(blockid-1)*rowPerBlock);
			taskQueue.add(block);
//			allTask.add(block);
		}catch(Exception e){
			throw e;
		}finally{
			if(br!=null){
				br.close();	
			}
		}
	}
	
	public synchronized CrawlTask checkAndPoll(String machineid){
		//check
		CrawlTask oldtask = _distributeLog.get(machineid);
		if(oldtask!=null){
			this.finishedRows +=oldtask.getSize();
			if(finishedRows == totalRow){
				this.over = true;
			}
		}
		//poll
		CrawlTask newtask = taskQueue.poll();
		this._distributeLog.put(machineid, newtask);
		if(newtask!=null){
			this.distributedRows+=newtask.getSize();
		}
		return newtask;
	}
	
//	public boolean finishCheck(String machineId) {
//		CrawlTask task = _distributeLog.get(machineId);
//		if(task!=null)
//			task.setFinished(true);
//		for(CrawlTask t : allTask){
//			if(!t.isFinished()){
//				return false;
//			}
//		}
//		this.setOver(true);
//		return true;
//	}
	
	public synchronized void returnCrawlTask(String machineid){
		CrawlTask task = this._distributeLog.get(machineid);
		if(task != null){
			this.taskQueue.add(task);
			this.distributedRows-=task.getSize();
			_distributeLog.remove(machineid);//2015-05-26------》修正slave挂掉重连后finishedrow>distributedrow的bug
		}
	} 
	
	public String getTaskId() {
		return taskId;
	}
	
	public String getBizCode() {
		return bizCode;
	}
	
	public String getOriginalTaskFilePath() {
		return originalTaskFilePath;
	}
	
	public String getOriginalTaskFileName() {
		return originalTaskFileName;
	}
	
	public String getSerializeReqFilePath() {
		return serializeReqFilePath;
	}
	
	public String getSerializeReqFileName() {
		return serializeReqFileName;
	}
	
	public int rowPerBlock(){
		return this.rowPerBlock;
	}
	
	public Date getSubmitDate(){
		return this.createTime;
	}
	
	public int getTotalBlock() {
		return blockCount;
	}
	
	public synchronized int getDistributedRows() {
		return distributedRows;
	}
	
	public synchronized int getDistributedBlocks(){
		return this.blockCount - this.taskQueue.size();
	}
	
	public synchronized int getLeftBlock(){
		return this.taskQueue.size();
	}
	
	public synchronized int getFinishedRows() {
		return finishedRows;
	}

	public synchronized int getTotalRow() {
		return totalRow;
	}
	
	public synchronized boolean isOver() {
		return over;
	}

	public synchronized boolean isAllDistributed() {
		if(taskQueue.size()==0){
			return true;
		}
		return false;
	}
	
	public synchronized String toString(){
		return String.format("bizCode:%s >> taskId:%s >> leftBlocks:%d >> totalRows:%d >> distributedRows:%d >> finishedRows:%d", this.bizCode,this.taskId,this.taskQueue.size(),this.totalRow,this.distributedRows,this.finishedRows);
	}
}
