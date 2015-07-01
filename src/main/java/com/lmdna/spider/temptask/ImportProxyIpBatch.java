package com.lmdna.spider.temptask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class ImportProxyIpBatch {
	public static void main(String arg[]) throws NumberFormatException, IOException, SQLException, ClassNotFoundException {
		Connection conn = null;
		PreparedStatement ps = null;
		File file = new File("d:\\data\\proxyip");
		BufferedReader br = null;
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(new FileInputStream(file));
		} catch (FileNotFoundException e2) {
			System.out.println("文件不存在");
			return;
		}
		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager
				.getConnection(
						"jdbc:mysql://192.168.1.111:3306/lemonDNA?useUnicode=true&characterEncoding=UTF8&zeroDateTimeBehavior=convertToNull",
						"root", "111111");
		if (conn != null){
			System.out.println("connection successful");
		}else{
			System.out.println("connection failure");
			return;
		}
			
		String insertSQL = "insert into spider_proxyip(ip,port,createtime,updatetime) values(?,?,now(),now())";
		ps = conn.prepareStatement(insertSQL,Statement.RETURN_GENERATED_KEYS);
		br = new BufferedReader(new InputStreamReader(bis, "UTF-8"),
				10 * 1024 * 1024);
		String line = "";
		while((line = br.readLine())!=null){
			String[] host = line.split("\t");
			ps.setString(1, host[0]);
			ps.setInt(2, Integer.valueOf(host[1]));
			try{
				ps.executeUpdate();
				System.out.println("添加成功"+host[0]);
			}catch(Exception e){
				if(e.getMessage().contains("Duplicate entry")){
					//System.out.println(host[0]+":"+host[1]+"主键冲突");
				}else{
					//System.out.println(e.getMessage());
				}
			}
		}
		br.close();
		file.delete();
	}
}
