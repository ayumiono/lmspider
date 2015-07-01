package com.lmdna.spider.node.slave;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LMSpdier分布式文件系统-slave node端server
 * @author ayumiono
 *
 */
public class LMSpiderDFSServer extends Thread{
	
	private final static Logger logger = LoggerFactory.getLogger(LMSpiderDFSServer.class);
	
	private ServerSocket ss;

	LMSpiderDFSServer(ServerSocket ss){
		super.setDaemon(true);
		super.setName("LMSpiderDFSServer-Daemon-Thread");
		this.ss = ss;
	}
	
	@Override
	public void run() {
		while(true){
			try {
				Socket s = this.ss.accept();
				s.setTcpNoDelay(true);
				new RequestHandler(s).start();
			} catch (IOException e) {
				logger.error("LMSpiderDFSServer Failed!",e);
			}
		}
	}
	
	public static class RequestHandler extends Thread{
		private Socket s;
		RequestHandler(Socket s){
			this.s = s;
		}
		@Override
		public void run() {
			DataInputStream in = null;
			try {
				in = new DataInputStream(new BufferedInputStream(this.s.getInputStream(), 1024));
				byte op = in.readByte();
				switch(op){
				case 80:
					read(in);
					break;
				case 81:
					delete(in);
					break;
				case 82:
					rename(in);
					break;
				}
			} catch (Throwable e) {
				e.printStackTrace();
			} finally{
				if(in!=null){
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		private void read(DataInputStream in) throws Exception{
			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(this.s.getOutputStream(),1024));
			try{
				
			}catch(Exception e){
				try{
					out.writeInt(9);
				}finally{
					out.close();
				}
			}
		}
		
		private void delete(DataInputStream in) throws Exception{
			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(this.s.getOutputStream(),1024));
			try{
				
			}catch(Exception e){
				try{
					out.writeInt(9);
				}finally{
					out.close();
				}
				
			}
		}
		
		private void rename(DataInputStream in) throws Exception{
			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(this.s.getOutputStream(),1024));
			try{
				
			}catch(Exception e){
				try{
					out.writeInt(9);
				}finally{
					out.close();
				}
				
			}
		}
	}
	
}
