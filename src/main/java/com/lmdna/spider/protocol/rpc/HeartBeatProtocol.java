package com.lmdna.spider.protocol.rpc;

import java.util.List;

import com.lmdna.spider.protocol.rpc.utils.CrawlTask;
import com.lmdna.spider.protocol.rpc.utils.HeartBeatData;
import com.lmdna.spider.protocol.rpc.utils.RemoteCmd;
import com.lmdna.spider.protocol.rpc.utils.TaskFinishCallBackPack;
import com.lmdna.spider.protocol.rpc.utils.TaskParseFinishedMsg;


public interface HeartBeatProtocol {
	public List<RemoteCmd> handleHeartBeat(HeartBeatData data);
	public List<RemoteCmd> handleSpiderError(CrawlTask block);
	public List<RemoteCmd> handleCrawlTaskParseError(CrawlTask block);
	public List<RemoteCmd> handleJarParseError();
	public List<RemoteCmd> handleCrawlTaskFinishedMsg(CrawlTask block);
	public List<RemoteCmd> handleTaskParseFinishCallBack(TaskFinishCallBackPack callback);
}
