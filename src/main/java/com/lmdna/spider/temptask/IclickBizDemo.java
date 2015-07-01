package com.lmdna.spider.temptask;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

public class IclickBizDemo {
	
	private static Map<String,TqqInfo> tqq = new HashMap<String,TqqInfo>();
	
	private static Map<String,Integer> domainCountMap = new HashMap<String,Integer>();
	private static Map<String,Integer> successCountMap = new HashMap<String,Integer>();
	
	private static Map<String,String> lbsAssertTag = new HashMap<String,String>();
	
	static{
		lbsAssertTag.put("620001", "100万以下");
		lbsAssertTag.put("620002", "100-150万");
		lbsAssertTag.put("620003", " 150-200万");
		lbsAssertTag.put("620004", " 200-300万");
		lbsAssertTag.put("620005", "300-500万");
		lbsAssertTag.put("620006", "500-1000万");
		lbsAssertTag.put("620007", "1000万以上");
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException{
		BufferedReader reader = null;
		BufferedInputStream bis = null;
		BufferedWriter writer = null;
		BufferedOutputStream bos = null;
		
		Connection conn = null;
		ResultSet rs = null;
		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager
				.getConnection(
						"jdbc:mysql://192.168.1.111:3306/lemonDNA?useUnicode=true&characterEncoding=UTF8&zeroDateTimeBehavior=convertToNull",
						"root", "111111");
		final String queryAllSql = "select * from spider_tencent_info";
		
		final PreparedStatement ps0 = conn.prepareStatement(queryAllSql);
		
		rs = ps0.executeQuery();
		while(rs.next()){
			TqqInfo qq = new TqqInfo();
			qq.setAge(rs.getInt("age"));
			qq.setSex(rs.getString("sex"));
			qq.setUin(rs.getString("uin"));
			qq.setLmid(rs.getString("lmid"));
			tqq.put(rs.getString("lmid"), qq);
		}
		
		System.out.println("当前lmid对应qq账号有"+tqq.size()+"个");
		
		int noiseDateCount = 0;
		Map<String,TqqInfo> tempmap = new HashMap<String,TqqInfo>();
		for(Entry<String,TqqInfo> entry: tqq.entrySet()){
			Integer age = entry.getValue().age;
			if(age == null){
				noiseDateCount ++;
			}else if(age <10 || age>70){
				noiseDateCount ++;
			}else{
				tempmap.put(entry.getKey(), entry.getValue());
			}
		}
		tqq = tempmap;
		
		System.out.println(String.format("非真实QQ信息有%d个，占总量的 %-3.2f%%",noiseDateCount,noiseDateCount * 100.0 / tqq.size()));
		
		File file = new File("C:\\data\\iclickaddomain");
		File resultFile = new File("c:\\data\\iclick_result");
		bis = new BufferedInputStream(new FileInputStream(file));
		reader = new BufferedReader(new InputStreamReader(bis, "UTF-8"),1024*1024);
		bos = new BufferedOutputStream(new FileOutputStream(resultFile));
		writer = new BufferedWriter(new OutputStreamWriter(bos, "UTF-8"),1024*1024);
		String originDataLine = "";
		
		while((originDataLine=reader.readLine())!=null){
			String[] dataArr = originDataLine.split("\t");
			String lmid = dataArr[0].trim();
			String domain = dataArr[1].trim();
			if(StringUtils.isEmpty(lmid) || StringUtils.isEmpty(domain)){
				continue;
			}
			Integer count = domainCountMap.get(domain);
			if(count==null){
				count = 1;
				domainCountMap.put(domain, count);
			}else{
				domainCountMap.put(domain, count+1);
			}
			TqqInfo qq = tqq.get(lmid);
			if(qq!=null){
				Integer successCount = successCountMap.get(domain);
				if(successCount == null){
					successCount = 1;
					successCountMap.put(domain, successCount);
				}else{
					successCountMap.put(domain, successCount+1);
				}
				StringBuilder sb = new StringBuilder(domain+","+lmid+",");
				int age = qq.getAge();
				String sex = qq.getSex();
				sb.append(age+","+sex+"\r\n");
				writer.write(sb.toString());
			}
		}
		writer.flush();
		writer.close();
		reader.close();
		System.out.println("抽样结果统计:>>>>");
		for(String domain: domainCountMap.keySet()){
			System.out.println(String.format("%40s	UV:%6d	成功抽样：%5d", domain,domainCountMap.get(domain),successCountMap.get(domain)));
		}
		System.out.println("<<<<抽样结果统计");
		
	}
	
	private static class TqqInfo{
		private String sex;
		private Integer age;
		private String uin;
		private String lmid;
		public String getSex() {
			return sex;
		}
		public void setSex(String sex) {
			this.sex = sex;
		}
		public Integer getAge() {
			return age;
		}
		public void setAge(Integer age) {
			this.age = age;
		}
		public String getUin() {
			return uin;
		}
		public void setUin(String uin) {
			this.uin = uin;
		}
		public String getLmid() {
			return lmid;
		}
		public void setLmid(String lmid) {
			this.lmid = lmid;
		}
	}
}
