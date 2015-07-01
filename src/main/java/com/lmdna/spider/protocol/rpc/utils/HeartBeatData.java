package com.lmdna.spider.protocol.rpc.utils;

import java.io.Serializable;
import java.util.List;

import com.lmdna.spider.node.SpiderNode.Level;

public class HeartBeatData implements Serializable{
	private static final long serialVersionUID = 6009690582240110212L;
	private String machineId;//机器IP
	private int spiderCounts;//spider容器数量
	private int activeThreadCounts;//spider相关线程总数
	private Level level;
	private List<SpiderStatusSerialization> spiderInfos;
	public String getMachineId() {
		return machineId;
	}
	public void setMachineId(String machineId) {
		this.machineId = machineId;
	}
	public int getSpiderCounts() {
		return spiderCounts;
	}
	public void setSpiderCounts(int spiderCounts) {
		this.spiderCounts = spiderCounts;
	}
	public int getActiveThreadCounts() {
		return activeThreadCounts;
	}
	public void setActiveThreadCounts(int activeThreadCounts) {
		this.activeThreadCounts = activeThreadCounts;
	}
	public List<SpiderStatusSerialization> getSpiderInfos() {
		return spiderInfos;
	}
	public void setSpiderInfos(List<SpiderStatusSerialization> spiderInfos) {
		this.spiderInfos = spiderInfos;
	}
	public Level getLevel() {
		return level;
	}
	public void setLevel(Level level) {
		this.level = level;
	}
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("machineid:%20s >> spidercount:%3d >> spidertotalthread:%4d >> nodelevel:%8s", this.getMachineId(),this.getSpiderCounts(),this.getActiveThreadCounts(),this.getLevel().toString()));
		sb.append("\n");
		for(SpiderStatusSerialization item : this.getSpiderInfos()){
			sb.append(item.toString()+"\n");
		}
		return sb.toString();
	}
}
