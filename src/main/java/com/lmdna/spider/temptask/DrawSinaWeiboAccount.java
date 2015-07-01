package com.lmdna.spider.temptask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONObject;

public class DrawSinaWeiboAccount {
	public static void main(String[] args) throws IOException{
		Set<String> accountSet = new HashSet<String>();
		BufferedInputStream bis = null;
		BufferedReader reader = null;
		File file = new File("D:\\data\\sinaweibo\\final");
		bis = new BufferedInputStream(new FileInputStream(file));
		reader = new BufferedReader(new InputStreamReader(bis, "UTF-8"),1024*1024);
		String line="";
		while((line=reader.readLine())!=null){
			Map<?,?> obj = JSONObject.parseObject(line);
			Map<?,?> value = (Map<?, ?>) obj.get("value");
			for(Entry<?,?> entry : value.entrySet()){
				accountSet.add(entry.getKey().toString());
			}
		}
		reader.close();
		File result = new File("D:\\data\\sinaweibo\\accountonly");
		FileWriter fw = new FileWriter(result);
		for(String account : accountSet){
			fw.write(account+"\r\n");
		}
		fw.flush();
		fw.close();
	}
}
