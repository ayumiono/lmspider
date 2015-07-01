package com.lmdna.spider.apicrawl.tencent;

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
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

public class TempImportTqqInfo {
	public static void main(String arg[]) throws NumberFormatException, IOException, SQLException, ClassNotFoundException {
		Connection conn = null;
		PreparedStatement ps = null;
		File file = new File("D:\\data\\data_spider\\tqqinfo");
		File[] files = file.listFiles();
		for(File filen : files){
			BufferedReader br = null;
			BufferedInputStream bis = null;
			try {
				bis = new BufferedInputStream(new FileInputStream(filen));
			} catch (FileNotFoundException e2) {
				System.out.println("文件不存在");
				return;
			}
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager
					.getConnection(
							"jdbc:mysql://192.168.1.113:3306/lemonDNA?useUnicode=true&characterEncoding=UTF8&zeroDateTimeBehavior=convertToNull",
							"root", "111111");
			if (conn != null){
				System.out.println("connection successful");
			}else{
				System.out.println("connection failure");
				return;
			}
				
			String insertSQL = "insert into spider_tencent_info(uin,sex,age,birthday,location,home,marriage,lmid) values"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?),"
					+ "(?,?,?,?,?,?,?,?)";
			ps = conn.prepareStatement(insertSQL,Statement.RETURN_GENERATED_KEYS);
			br = new BufferedReader(new InputStreamReader(bis, "UTF-8"),
					10 * 1024 * 1024);
			String line = "";
			int successInsertCount = 0;
			int duperrcount = 0;
			List<JSONObject> objList = new ArrayList<JSONObject>(35);
			while((line = br.readLine())!=null){
				try{
					JSONObject obj0 = JSONObject.parseObject(line);
					objList.add(obj0);
					if(objList.size()==35){
						for(int i=0;i<=34;i++){
							JSONObject obj = objList.get(i);
							String uin = obj.getString("uin");
							String sex = obj.getString("sex");
							String birthday = obj.getString("birthday");
							int age = obj.getIntValue("age");
							String location = obj.getString("location");
							String home = obj.getString("home");
							String marriage = obj.getString("marriage");
							String lmid = obj.getString("lmid");
							ps.setString(8*i+1, uin);
							ps.setString(8*i+2, sex);
							ps.setInt(8*i+3, age);
							ps.setString(8*i+4, birthday);
							ps.setString(8*i+5, location);
							ps.setString(8*i+6, home);
							ps.setString(8*i+7, marriage);
							ps.setString(8*i+8, lmid);
						}
						ps.executeUpdate();
						successInsertCount = successInsertCount + 1;
						objList.clear();
					}
				}catch(Exception e){
					if(e.getMessage().contains("Duplicate entry")){
						duperrcount = duperrcount + 1;
					}else{
						continue;
					}
				}
			}
			br.close();
			System.out.println("成功数量："+successInsertCount);
			System.out.println("主键冲突数量："+duperrcount);
		}
		
	}
}
