package com.lmdna.spider.jar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import us.codecraft.webmagic.Request;

import com.lmdna.spider.protocol.TaskFileParseProtocol;

public class TencentIndividualInfoTaskPaser implements TaskFileParseProtocol{

	@Override
	public List<Request> parse(InputStream in)
			throws IOException {
		List<Request> reqList = new ArrayList<Request>();
		FileInputStream fs = (FileInputStream)in;
		FileChannel fc = fs.getChannel();
		MappedByteBuffer byteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
		Charset charset = Charset.forName("utf-8");
        CharsetDecoder decoder = charset.newDecoder();
        CharBuffer charBuffer = decoder.decode(byteBuffer);
        Scanner sc = new Scanner(charBuffer);
        String s = "";
        while((s = sc.nextLine())!=null){
        	Request request = new Request("http://user.qzone.qq.com/"+s+"/1");
			request.setBizcode("tqq_individual_info_crawl");
			reqList.add(request);
        }
        sc.close();
		return reqList;
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException{
		TencentIndividualInfoTaskPaser test = new TencentIndividualInfoTaskPaser();
		test.parse(new FileInputStream(new File("E:\\chenxuelong_tmp\\data\\qqwait4crawl\\qqwait4crawlsegmentbl")));
	}

}
