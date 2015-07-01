package com.lmdna.spider.temptask;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ExportCtripHotelUrls{
	public static void main(String[] args) throws ClassNotFoundException, SQLException, FileNotFoundException, UnsupportedEncodingException{
		final int pageSize = 10000;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String ctriphotelinfo = "C:\\data\\ctriphotelinfo";
		PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(ctriphotelinfo),"UTF-8"));
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
		int startNo = 0;
		String countSql = "select count(*) as rowCount from spider_ctrip_all_hotel_url";
		ps = conn.prepareStatement(countSql);
		rs = ps.executeQuery();
		rs.next();
		int totalCount = rs.getInt("rowCount");
		int dealedCount = 0;
		while(dealedCount<totalCount){
			String insertSQL = "select * from spider_ctrip_all_hotel_url limit "+startNo + "," + pageSize;
			ps = conn.prepareStatement(insertSQL);
			rs = ps.executeQuery();
			while(rs.next()){
				String url = rs.getString("url");
				String city = rs.getString("city");
				//{"url":"http://hotels.ctrip.com/hotel/1000223.html","extras":{"hotelid":"1000223"}}
				String line = String.format("{\"url\":\"%s\",\"extras\":{\"url\":\"%s\",\"city\":\"%s\"}}", url,url,city);
				printWriter.println(line);
			}
			startNo = startNo + pageSize;
			dealedCount = dealedCount + pageSize;
		}
		System.out.println("finish successfully");
	}
	
}