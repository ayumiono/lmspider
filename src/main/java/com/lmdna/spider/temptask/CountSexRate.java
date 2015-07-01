package com.lmdna.spider.temptask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.alibaba.fastjson.JSONObject;

public class CountSexRate {
	public static void main(String[] args) throws IOException{
		int male = 0;
		int fmale = 0;
		int unknown = 0;
		BufferedInputStream bis = null;
		BufferedReader reader = null;
		File file = new File("E:\\BaiduYunDownload\\重要数据\\qqlog");
		bis = new BufferedInputStream(new FileInputStream(file));
		reader = new BufferedReader(new InputStreamReader(bis, "UTF-8"),1024*1024);
		String l = "";
		while((l=reader.readLine())!=null){
			JSONObject obj = JSONObject.parseObject(l);
			String sex = obj.getString("sex");
			if("男".equals(sex)){
				male = male + 1;
			}else if("女".equals(sex)){
				fmale = fmale + 1;
			}else{
				unknown = unknown + 1;
			}
		}
		reader.close();
		System.out.println("male:" + male);
		System.out.println("fmale:" + fmale);
		System.out.println("unknown:" + unknown);
	}
}
