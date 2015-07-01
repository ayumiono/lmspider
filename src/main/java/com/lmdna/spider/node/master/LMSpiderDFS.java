package com.lmdna.spider.node.master;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * LMSpdier分布式文件系统
 * @author ayumiono
 *
 */
public class LMSpiderDFS {
	
	public static final byte OP_READ = 80;
	public static final byte OP_DELETE = 81;
	public static final byte OP_RENAME = 82;
	/**
	 * lmspiderfs://taskid/slaveip:port:slaveid/physical_path
	 * @param path
	 * @return
	 */
	private FileSystem fs = Jimfs.newFileSystem("lmspiderfs");
	public static LMSpiderDFS instance = new LMSpiderDFS();
	
	private LMSpiderDFS(){}
	
	public void createFile(String path) throws IOException{
		Path file = fs.getPath(path);
		Path parent = file.getParent();
		Files.createDirectories(parent);
		file = parent.resolve(file.getFileName());
	}
	
	public void createFile(Path parent,String filename) throws IOException{
		if(!Files.exists(parent)){
			Files.createDirectories(parent);
		}
		parent.resolve(filename);
	}
	
	public List<Path> list(Path parent) throws IOException{
		List<Path> files = new ArrayList<Path>();
		Files.list(parent).forEach(file->{
			if(!Files.isDirectory(file)){
				files.add(file);
			}else{
				try {
					files.addAll(list(file));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return files;
	}
	
	/**
	 * /jd_12121324341/192.168.1.125:43210:spider_slave/opt/data/lmdna/spider/crawlresult/jd_12121324341/00000000.src
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public InputStream open(String path) throws Exception{
		String slaveip = "";
		int port = 0;
		Socket s = new Socket();
		try {
			s.connect(new InetSocketAddress(slaveip,port));
			s.getOutputStream().write(OP_READ);
			String physical_path = "";
			byte[] bs = physical_path.getBytes();
			s.getOutputStream().write(bs);
			return s.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}finally{
			s.close();
		}
	}
	
}
