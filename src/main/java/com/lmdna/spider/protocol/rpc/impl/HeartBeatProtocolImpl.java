package com.lmdna.spider.protocol.rpc.impl;

import java.util.ArrayList;
import java.util.List;

import us.codecraft.webmagic.utils.LoggerUtil;

import com.lmdna.spider.node.master.MasterNode;
import com.lmdna.spider.node.master.SpiderBizInOperation;
import com.lmdna.spider.node.master.Task;
import com.lmdna.spider.protocol.rpc.HeartBeatProtocol;
import com.lmdna.spider.protocol.rpc.utils.CommonSpiderLoadTask;
import com.lmdna.spider.protocol.rpc.utils.CrawlTask;
import com.lmdna.spider.protocol.rpc.utils.HeartBeatData;
import com.lmdna.spider.protocol.rpc.utils.JarSpiderLoadTask;
import com.lmdna.spider.protocol.rpc.utils.RemoteCmd;
import com.lmdna.spider.protocol.rpc.utils.SpiderStatusSerialization;
import com.lmdna.spider.protocol.rpc.utils.TaskFinishCallBackPack;
import com.lmdna.spider.protocol.rpc.utils.TaskFinishedMsg;

/**
 * @author ayumiono
 *
 */
public class HeartBeatProtocolImpl implements HeartBeatProtocol {
	
	private MasterNode master;

	public HeartBeatProtocolImpl(MasterNode master){
		this.master = master;
	}
	
	@Override
	public List<RemoteCmd> handleHeartBeat(HeartBeatData data) {
		String machineId = data.getMachineId();
		LoggerUtil.info("HeartBeat", data.toString());
		master.saveHeartBeatData(data);
		LoggerUtil.info("HeartBeat", master.reportTaskProgress());
		if(master.isPaused()){
			return new ArrayList<RemoteCmd>();
		}
		List<RemoteCmd> pushTasks = new ArrayList<RemoteCmd>();
		
		for(Task record : master.getTaskProgressRecord()){
			if(!record.isOver()){
				String bizCode = record.getBizCode();
				boolean found = false;
				for(SpiderStatusSerialization spiderStatus : data.getSpiderInfos()){
					if(spiderStatus.getName().equals(bizCode)){
						found = true;
						if((double)spiderStatus.getLeftPageCount()/record.rowPerBlock()<=0.1){
							CrawlTask task = record.checkAndPoll(machineId);
							if(record.isOver()){
								master.notifyTaskListener(record.getTaskId());
								TaskFinishedMsg taskfinishedMsg = new TaskFinishedMsg(record.getTaskId(),record.getBizCode());
								master.addMyCmd(taskfinishedMsg);
								master.taskProgressRecord.remove(record);
								master.finishedTask.add(record);
							}
							if(task != null){
								pushTasks.add(task);
							}
						}
						break;
					}
				}
				if(!found){
					SpiderBizInOperation spider_load = master.getSpiderBizInOperation(record.getBizCode());
					if(spider_load.isCommon()){
						CommonSpiderLoadTask common_spider_load = new CommonSpiderLoadTask();
						common_spider_load.setBiz(spider_load.getSpiderConfig());
						pushTasks.add(common_spider_load);
					}else if(spider_load.isJar()){
						JarSpiderLoadTask jar_spider_load = new JarSpiderLoadTask();
						jar_spider_load.setBiz(spider_load.getSpiderConfig());
						jar_spider_load.setJarFileName(spider_load.getJarName());
						jar_spider_load.setJarFilePath(spider_load.getJarPath());
						pushTasks.add(jar_spider_load);
					}
				}
			}
		}
		RemoteCmd mycmd = master.getMyCmd(machineId);
		if(mycmd!=null){
			pushTasks.add(mycmd);
		}
		return pushTasks;
	}

	@Override
	public List<RemoteCmd> handleSpiderError(CrawlTask block) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RemoteCmd> handleCrawlTaskParseError(CrawlTask block) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RemoteCmd> handleJarParseError() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RemoteCmd> handleCrawlTaskFinishedMsg(CrawlTask block) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RemoteCmd> handleTaskParseFinishCallBack(
			TaskFinishCallBackPack callback) {
//		try {
//			master.adddfs(callback.getTaskId(), callback.getFiles());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return null;
	}

}
