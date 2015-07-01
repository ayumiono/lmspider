package com.lmdna.spider.node.master;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.codecraft.webmagic.utils.LoggerUtil;

import com.alibaba.fastjson.JSONObject;
import com.lmdna.spider.protocol.TaskStatusListener;
import com.lmdna.spider.utils.SpiderCommonTool;
import com.lmdna.spider.utils.SpiderGlobalConfig;

public class FileServer implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(FileServer.class);
	
	private static final String logName = "FileServer";
	
	private int fsp;
	
	private Map<SocketChannel,Handler> _map;
	private MasterNode master;
	
	private Selector selector;
	private ServerSocketChannel serverSocketChannel;
	private Map<SelectionKey,ReadHandler> _readHandlers = new HashMap<SelectionKey,ReadHandler>();

	public FileServer(int fsp,MasterNode master) {
		this.fsp = fsp;
		this._map = new HashMap<SocketChannel,Handler>();
		this.master = master;
	}

	@Override
	public void run() {
		try {
			selector = Selector.open();
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.socket().setReuseAddress(true);
			serverSocketChannel.socket().bind(new InetSocketAddress(fsp));
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			while (true) {
				selector.select();
				Iterator<SelectionKey> it = selector.selectedKeys().iterator();
				while (it.hasNext()) {
					SelectionKey readyKey = it.next();
					it.remove();
					ServerSocketChannel server = null;
					SocketChannel client = null;
					if (readyKey.isAcceptable()) {
						try{
							server = (ServerSocketChannel) readyKey.channel();
							LoggerUtil.info(logName,"client connection accepted");
							client = server.accept();
							client.configureBlocking(false);
							client.register(selector, SelectionKey.OP_READ);
						}catch(ClosedChannelException e){
							LoggerUtil.info(logName,"client socketchannel register read failed");
						}
					} else if (readyKey.isWritable()) {
						try{
							client = (SocketChannel) readyKey.channel();
							if(_readHandlers.get(readyKey)!=null){
								_readHandlers.remove(readyKey);
							}
							try{
								transport(client);
							}catch(Exception e){
								LoggerUtil.info(logName,"downloadFile error");
								readyKey.channel().close();
								selector.selectNow();
								break;
							}
						}catch(Exception e){
							readyKey.channel().close();
							selector.selectNow();
							LoggerUtil.info(logName,"file server write to client socket channel failed "+e.getMessage());
						}
					} else if (readyKey.isReadable()) {
						try{
							if(_readHandlers.get(readyKey)==null){
								ReadHandler handler = new ReadHandler(readyKey);
								_readHandlers.put(readyKey, handler);
								handler.start();
							}
						}catch(Exception e){
							LoggerUtil.info(logName,"handel readykey.isReadable error!"+e.getMessage());
							readyKey.channel().close();
							break;
						}
					}
				}
			}
		}  catch (IOException ex) {
			logger.error("file server terminated. "+ex.getMessage(), ex);
		} catch (Exception ex){
			logger.error("file server terminated. "+ex.getMessage(),ex);
		}finally {
			try {
				serverSocketChannel.close();
				selector.selectNow();
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
			try {
				selector.close();
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		}
	}

	private class ReadHandler extends Thread{
		
		private SelectionKey readyKey;
		
		public ReadHandler(SelectionKey readyKey){
			this.readyKey = readyKey;
		}
		
		@Override
		public void run(){
			try {
				handle(readyKey);
			} catch (Exception e) {
				logger.info("ReadHandler>>>error!");
			}
		}
	}
	
	//TODO
	public void handle(final SelectionKey readyKey)throws Exception {
		final SocketChannel socketChannel = (SocketChannel) readyKey.channel();
		//解析是下载文件还是上传文件
		final int intLength = 4;
		ByteBuffer buf = ByteBuffer.allocate(intLength);
		int length = 0;
		length = socketChannel.read(buf);
		int cmdtype = 0;
		if(length>0){
			buf.flip();
			cmdtype = buf.getInt();
			buf.clear();
			//download
			if(cmdtype == 0){
				readyKey.attach("download");
				LoggerUtil.info(logName,"FileServer接收到下载文件请求.");
				String filepath = "";
				String filename = "";
				length = socketChannel.read(buf);
				if (length > 0) {
					int filepathlength = 0;
					buf.flip();
					filepathlength = buf.getInt();
					buf.clear();
					try{
						buf = ByteBuffer.allocate(filepathlength);
					}catch(Throwable t){
						logger.error(t.getClass().getName() + "filepathlength:"+filepathlength);
					}
					length = socketChannel.read(buf);
					if (length > 0) {
						buf.flip();
						filepath = new String(buf.array(), "utf-8");
						buf.clear();
						buf = ByteBuffer.allocate(intLength);
						length = socketChannel.read(buf);
						if (length > 0) {
							int filenamelength = 0;
							buf.flip();
							filenamelength = buf.getInt();
							buf.clear();
							try{
								buf = ByteBuffer.allocate(filenamelength);
							}catch(Throwable t){
								LoggerUtil.info(logName,t.getClass().getName() + "filenamelength:"+filenamelength);
							}
							length = socketChannel.read(buf);
							if (length > 0) {
								buf.flip();
								filename = new String(buf.array(), "utf-8");
								buf.clear();
							}
						}
					}
				}
				File file = new File(filepath);
				if (file.exists()) {
					Handler handler = new Handler(file);
					this._map.put(socketChannel, handler);
				} else {
					LoggerUtil.info(logName,"slave请求任务文件路径不存在！");
				}
				readyKey.channel().register(selector, SelectionKey.OP_WRITE);//处理完成之后，册注听事件
				selector.wakeup();
			}else if(cmdtype == 1){
				LoggerUtil.info(logName,"FileServer接收上传文件请求.");
				String fileName = "";
				String filePath = "";
				String parameter = "";
				//upload
				//解析文件名
				length = socketChannel.read(buf);
				if(length>0){
					//type
					int type = 0;//newtask:0 | newjar:1
					buf.flip();
					type = buf.getInt();
					buf.clear();
					//parameter
					length = socketChannel.read(buf);
					buf.flip();
					int paramlength = buf.getInt();
					buf.clear();
					try{
						buf = ByteBuffer.allocate(paramlength);
					}catch(Throwable t){
						logger.error(t.getClass().getName() + "paramlength:"+paramlength);
					}
					length = socketChannel.read(buf);
					buf.flip();
					parameter = new String(buf.array(), "utf-8");//parameter
					buf.clear();
					Map<?,?> paramap = (Map<?, ?>) JSONObject.parse(parameter);
					int bizId = (Integer) paramap.get("bizId");
					int rowPerBlock = (Integer) paramap.get("rowPerBlock");
					String bizCode = (String)paramap.get("bizCode");
					
					//filename
					buf = ByteBuffer.allocate(intLength);
					length = socketChannel.read(buf);
					buf.flip();
					int filenamelength = buf.getInt();
					buf.clear();
					try{
						buf = ByteBuffer.allocate(filenamelength);
					}catch(Throwable t){
						logger.error(t.getClass().getName() + "filenamelength:"+filenamelength);
					}
					length = socketChannel.read(buf);
					if (length > 0) {
						buf.flip();
						fileName = new String(buf.array(), "utf-8");
						buf.clear();
					}
					buf = ByteBuffer.allocate(intLength);
					length = socketChannel.read(buf);
					if (length > 0) {
						int filepathlength = 0;
						buf.flip();
						filepathlength = buf.getInt();
						buf.clear();
						try{
							buf = ByteBuffer.allocate(filepathlength);
						}catch(Throwable t){
							logger.error(t.getClass().getName() + "filepathlength:"+filepathlength);
						}
						length = socketChannel.read(buf);
						buf.flip();
						filePath = new String(buf.array(), "utf-8");
						buf.clear();
						buf = ByteBuffer.allocate(intLength);
						length = socketChannel.read(buf);
						buf.flip();
						int sizecheck = buf.getInt();
						buf.clear();
						buf = ByteBuffer.allocate(1024);
						int receivesize = 0;
						if(type == 0){
							LoggerUtil.info(logName,"start receive task file. filename:"+fileName+" filepath:"+filePath+" expectsize:"+sizecheck);
							SimpleDateFormat dateFormat = new SimpleDateFormat();
							dateFormat.applyPattern("yyyyMMdd");
							String filepath = SpiderGlobalConfig.getValue(SpiderGlobalConfig.SPIDER_TASKFILE_UPLOAD_DIR)+dateFormat.format(new Date())+File.separator+fileName;
							File taskFile = new File(filepath);
							if (!taskFile.getParentFile().exists()) {
								taskFile.getParentFile().mkdirs();
							}
							FileOutputStream fout = new FileOutputStream(taskFile);
							FileChannel fc = fout.getChannel();
							while(receivesize != sizecheck){
								Thread.currentThread().sleep(300);
								while((length = socketChannel.read(buf))>0){
									receivesize = receivesize + length;
									buf.flip();
									fc.write(buf);
		                            fout.flush();
		                            buf.clear();
								}
							}
							fout.close();
							if(receivesize == sizecheck){
								LoggerUtil.info(logName,"task file receive success.");
								try {
									Task task = master.submitTask(bizCode, taskFile.getPath(), fileName, rowPerBlock);
									LoggerUtil.info(logName,"submit newtask success.");
									master.addTaskListener(task.getTaskId(),new TaskStatusListener() {
										@Override
										public void finishNotify() {
											try{
												//向客户端反馈抓取完成信号
												ByteBuffer buffer = ByteBuffer.allocate("crawl_finish_msg".getBytes("utf-8").length);
												buffer.put("crawl_finish_msg".getBytes("utf-8"));
												buffer.flip();
												socketChannel.write(buffer);
												buffer.clear();
												readyKey.cancel();
											}catch(Exception e){
												logger.error(e.getMessage(),e);
											}
										}
									});
									LoggerUtil.info(logName,"add listener success");
								} catch (Exception e) {
									if(e.getMessage()!=null && e.getMessage().contains("no url not seen exist")){
										LoggerUtil.info(logName,"no url not seen exist in the taskfile");
										ByteBuffer buffer = ByteBuffer.allocate("crawl_finish_msg".getBytes("utf-8").length);
										buffer.put("crawl_finish_msg".getBytes("utf-8"));
										buffer.flip();
										socketChannel.write(buffer);
										buffer.clear();
										readyKey.cancel();
									}else{
										logger.error("submit newtask failed!",e);
										LoggerUtil.info(logName,"submit newtask failed!");
										ByteBuffer buffer = ByteBuffer.allocate("task_submit_fail".getBytes("utf-8").length);
										buffer.put("task_submit_fail".getBytes("utf-8"));
										buffer.flip();
										socketChannel.write(buffer);
										buffer.clear();
										readyKey.cancel();
									}
								}
							}else{
								LoggerUtil.info(logName,"task file transport failed!");
								ByteBuffer buffer = ByteBuffer.allocate("taskfile_transport_failed".getBytes("utf-8").length);
								buffer.put("taskfile_transport_failed".getBytes("utf-8"));
								buffer.flip();
								socketChannel.write(buffer);
								buffer.clear();
								readyKey.cancel();
							}
						}else if(type == 1){
							LoggerUtil.info(logName,"start receive jar file. filename:"+fileName+" filepath:"+filePath+" expectsize:"+sizecheck);
							File jarFile = new File(SpiderGlobalConfig.getValue(SpiderGlobalConfig.SPIDER_JAR_UPLOAD_DIR)+fileName);
							if (!jarFile.getParentFile().exists()) {
								jarFile.getParentFile().mkdirs();
							}
							FileOutputStream fout = new FileOutputStream(jarFile);
							FileChannel fc = fout.getChannel();
							while((length = socketChannel.read(buf))>0){
								buf.flip();
								fc.write(buf);
	                            fout.flush();
	                            buf.clear();
							}
							fout.close();
							try {
								master.loadSpider(jarFile.getPath(), fileName);
							} catch (Throwable e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}
	
	private void write(File file,SocketChannel socketChannel) throws IOException{
		String fileName = file.getName();
		int fileNameLength = fileName.getBytes().length;
		if(file.isDirectory()){
			try{
				ByteBuffer write_buffer = ByteBuffer.allocate(1024);
				write_buffer.put(new byte[]{(byte)2},0,1);//cmd:2 文件夹名
				write_buffer.put(SpiderCommonTool.intToBytes(fileNameLength));
				write_buffer.put(fileName.getBytes(), 0, fileNameLength);
				write_buffer.flip();
				socketChannel.write(write_buffer);
				write_buffer.clear();
				File[] files = file.listFiles();
				for(File f : files){
					write(f,socketChannel);
				}
			}catch(Exception e){
				
			}
		}else{
			FileInputStream fis = new FileInputStream(file);
			FileChannel fc = fis.getChannel();
			try {
				ByteBuffer write_buffer = ByteBuffer.allocate(1024*1024);
				write_buffer.put(new byte[] { (byte) 1 },0,1);// 1：文件名
				write_buffer.put(SpiderCommonTool.intToBytes(fileNameLength));
				write_buffer.put(fileName.getBytes(), 0, fileNameLength);
				write_buffer.flip();
				socketChannel.write(write_buffer);
				write_buffer.clear();
				write_buffer.put(new byte[] { (byte) 0 }, 0, 1);
				write_buffer.put(SpiderCommonTool.longToBytes(fis.available()));
				write_buffer.flip();
				socketChannel.write(write_buffer);
				write_buffer.clear();
				int size = 0;
				while ((size = fc.read(write_buffer)) != -1) {
					write_buffer.rewind();
					write_buffer.limit(size);
					//等效于write_buffer.flip();
					while(write_buffer.hasRemaining()){
						socketChannel.write(write_buffer);
					}
					write_buffer.clear();
				}
			}catch(Exception e){
				
			} finally {
				try {
					fc.close();
				} catch (Exception ex) {
				}
				try {
					fis.close();
				} catch (Exception ex) {
				}
				try {
					socketChannel.close();
				} catch (IOException e) {
					
				}
			}
		}
	}

	private void transport(SocketChannel socketChannel) throws IOException {
		Handler handler = this._map.get(socketChannel);
		if(handler!=null){
			File file = handler.getFile();
			write(file,socketChannel);
		}
	}
	
	class Handler{
		private File file;
		public Handler(File file){
			this.setFile(file);
		}
		public File getFile() {
			return file;
		}
		public void setFile(File file) {
			this.file = file;
		}
	}
}
