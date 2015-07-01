package com.lmdna.spider.node;

import com.lmdna.spider.node.SpiderNode.Level;

public class ZKNodeData {
	private String machineId;
	private String file_server_address;
	private String jetty_server_address;
	private Level level;
	public String getMachineId() {
		return machineId;
	}
	public void setMachineId(String machineId) {
		this.machineId = machineId;
	}
	public String getFile_server_address() {
		return file_server_address;
	}
	public void setFile_server_address(String file_server_address) {
		this.file_server_address = file_server_address;
	}
	public String getJetty_server_address() {
		return jetty_server_address;
	}
	public void setJetty_server_address(String jetty_server_address) {
		this.jetty_server_address = jetty_server_address;
	}
	public Level getLevel() {
		return level;
	}
	public void setLevel(Level level) {
		this.level = level;
	}
}
