package com.lmdna.spider.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;

import com.alibaba.fastjson.JSON;
import com.lmdna.spider.pageprocessor.JDSortListPageProcessor;
import com.lmdna.spider.pipeline.MySQLImplJDPipeline;

public class JDCategoryJSON {
	
	private static final String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式  
	
	private static final String regEx_dig_dig = "(\\d+-\\d+)";
	
	private static final String regEx_dig_dig_dig = "(\\d+-\\d+-\\d+)";
	
	private static final String regEx_dig_dig_only = "^(\\d+-\\d+)$";
	
	private static final String regEx_dig_dig_dig_only = "^(\\d+-\\d+-\\d+)$";
	
	private static final String regEx_dig_dig_link = "http://channel.jd.com/(\\d+-\\d+)";
	
	private static final String regEx_dig_dig_dig_link = "http://channel.jd.com/(\\d+-\\d+-\\d+)";
	
	private static final String jd_channel_html = "http://channel.jd.com/$.html";
	
	private static int spidertask = 0;

	private List<JDParent> data;

	public List<JDParent> getData() {
		return data;
	}

	public void setData(List<JDParent> data) {
		this.data = data;
	}

	@SuppressWarnings("resource")
	public static void main(String arg[]) throws IOException, ClassNotFoundException, SQLException, InterruptedException {
		
		Spider spider = new Spider(new JDSortListPageProcessor());
		spider.addPipeline(new MySQLImplJDPipeline());
		spider.thread(5);
		File curImportFile = new File("c:\\data");
		File[] directoryFiles = curImportFile.listFiles();
		for (File file : directoryFiles) {
			BufferedReader reader = null;
			BufferedInputStream bis = null;
			bis = new BufferedInputStream(new FileInputStream(file));
			reader = new BufferedReader(new InputStreamReader(bis, "UTF-8"),
					10 * 1024 * 1024);
			String temp = "";
			StringBuilder sb = new StringBuilder();
			while ((temp = reader.readLine()) != null) {
				sb.append(temp);
			}
			//unicode解码
			String finalJsonStr = SpiderCommonTool.decodeUnicode(sb.toString());
			//过滤所有html标签
			Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);  
	        Matcher m_html = p_html.matcher(finalJsonStr);  
	        finalJsonStr = m_html.replaceAll("");
			
			JDCategoryJSON o = new JDCategoryJSON();
			o = (JDCategoryJSON) JSON.parseObject(finalJsonStr,
					JDCategoryJSON.class);
			//解析JDCategoryJSON对象，生成sql insert语句
			Connection conn = null;
			ResultSet rs = null;
			PreparedStatement ps = null;
			try{
				Class.forName("com.mysql.jdbc.Driver");
				conn = DriverManager.getConnection(
								"jdbc:mysql://192.168.1.111:3306/lemonDNA?useUnicode=true&characterEncoding=UTF8&zeroDateTimeBehavior=convertToNull",
								"root", "111111");

				if (conn != null)
					System.out.println("connection successful");
				else
					System.out.println("connection failure");
				String insertSQL = "insert into spider_jd_category(tagname,parentid,lvl1,lvl2,lvl3,code,level,createtime,updatetime) values(?,?,?,?,?,?,?,now(),now())";
				ps = conn.prepareStatement(insertSQL,Statement.RETURN_GENERATED_KEYS);
				Pattern pp = Pattern.compile(regEx_dig_dig_dig_only,Pattern.CASE_INSENSITIVE);
				Pattern p = Pattern.compile(regEx_dig_dig_only,Pattern.CASE_INSENSITIVE);
				Pattern P = Pattern.compile(regEx_dig_dig_link,Pattern.CASE_INSENSITIVE);
				Pattern PP = Pattern.compile(regEx_dig_dig_dig_link,Pattern.CASE_INSENSITIVE);
				Pattern _p = Pattern.compile(regEx_dig_dig,Pattern.CASE_INSENSITIVE);
				Pattern _pp = Pattern.compile(regEx_dig_dig_dig,Pattern.CASE_INSENSITIVE);
				Matcher m;
				Matcher _m;
				Matcher mm;
				Matcher _mm;
				Matcher M;
				Matcher MM;
				for(JDParent parent : o.getData()){
					//文件中的一级类目不一定能成为真正的一级类目，可能需要分割
					for(Subitem sub : parent.getI()){
						//文件中的二级类目也不一定能成为真正的二级类目，需要看最终的childItem中有没有符合regEx_dig_dig的，如果有，真正的二级类目应该为childItem，一级类目为subitem的n
						String subN = sub.getN();
						m = p.matcher(sub.getU());
						M = P.matcher(sub.getU());
						_m = _p.matcher(sub.getU());
						if(StringUtils.isEmpty(sub.getU())){
							//如果u为空，则不处理
							continue;
						}
						if(m.matches() || M.find()){
							if(_m.find()){
								//如果匹配
								/*
								 * {
									"u":"1620-1621",
									"n":"家纺",
									"i":[
											"1620-1621-1626|床品套件",
											"1620-1621-1627|被子",
											"1620-1621-1632|蚊帐",
											"1620-1621-11963|凉席",
											"1620-1621-1629|床单被罩",
											"1620-1621-1628|枕芯",
											"1620-1621-1634|毛巾浴巾",
											"1620-1621-11962|布艺软饰",
											"1620-1621-1630|毯子",
											"1620-1621-1633|抱枕靠垫",
											"1620-1621-1631|床垫/床褥",
											"1620-1621-4952|窗帘/窗纱",
											"1620-1621-2688|电热毯"
										]
									}
								 * */
								int subtagId = 0;
								ps.setString(1, subN);
								ps.setInt(2, 0);
								ps.setString(3, "");//一级类目先不填
								ps.setString(4, subN);
								ps.setString(5, "");
								ps.setString(6, _m.group(1));
								ps.setInt(7, 2);
								ps.executeUpdate();
								rs = ps.getGeneratedKeys();
								if(rs.next()){
									subtagId = rs.getInt(1);
								}
								for(String child : sub.getI()){
									String childtagname = StringUtils.substringAfter(child, "|");
									String childcode = StringUtils.substringBefore(child, "|");
									_mm = _pp.matcher(childcode);
									if(_mm.find()){
										childcode = _mm.group(1);
									}else{
										continue;
									}
									ps.setString(1, childtagname);
									ps.setInt(2, subtagId);
									ps.setString(3, "");
									ps.setString(4, subN);
									ps.setString(5, childtagname);
									ps.setString(6, childcode);
									ps.setInt(7, 3);
									ps.executeUpdate();
									rs = ps.getGeneratedKeys();
								}
							}
							
						}else{
							//如果二级类目不符合dig_dig的样式如
							/*
							 * {
								"u":"http://channel.jd.com/furniture.html",
								"n":"家具",
								"i":[
										"9847-9848|卧室家具",
										"9847-9849|客厅家具",
										"9847-9850|餐厅家具",
										"9847-9851|书房家具",
										"9847-9852|储物家具",
										"9847-9853|阳台/户外",
										"9847-9854|商业办公",
										"9847-9848-9863|床",
										"9847-9848-9864|床垫",
										"9847-9849-9870|沙发",
										"9847-9851-9882|电脑椅",
										"9847-9848-11972|衣柜",
										"9847-9849-9872|茶几",
										"9847-9849-9873|电视柜",
										"9847-9850-9877|餐桌",
										"9847-9851-11973|电脑桌",
										"9847-9852-9885|鞋架/衣帽架"
									]
								}
							 * 
							 * */
							//再次检验一下子标签是否有二级类目样式
							boolean flag = false;
							
							for(String child : sub.getI()){
								child = StringUtils.substringBefore(child, "|");
								m = p.matcher(child);
								M = P.matcher(child);
								if(m.matches() || M.find()){
									flag = true;
									break;
								}
							}
							if(flag){
								String lvl1code = "";
								for(String child : sub.getI()){
									_m = _p.matcher(child);
									if(_m.find()){
										lvl1code = StringUtils.substringBefore(_m.group(1), "-");
										break;
									}
								}
								int lvl1id = 0;
								ps.setString(1, subN);
								ps.setInt(2, 0);
								ps.setString(3, subN);//二级类目变为一级类目
								ps.setString(4, "");
								ps.setString(5, "");
								ps.setString(6, lvl1code);
								ps.setInt(7, 1);//二级类目变为一级类目
								ps.executeUpdate();
								rs = ps.getGeneratedKeys();
								if(rs.next()){
									lvl1id = rs.getInt(1);
								}
								for(String child : sub.getI()){
									String childtagname = StringUtils.substringAfter(child, "|");
									String childcode = StringUtils.substringBefore(child, "|");
									m = p.matcher(childcode);
									mm = pp.matcher(childcode);
									M = P.matcher(childcode);
									if(m.matches() || M.find() && !mm.find()){
										childcode = m.group(1);
										int lvl2id = 0;
										ps.setString(1, childtagname);
										ps.setInt(2, lvl1id);
										ps.setString(3, subN);
										ps.setString(4, childtagname);//三级类目变为二级类目
										ps.setString(5, "");
										ps.setString(6, m.group(1));
										ps.setInt(7, 2);//三级类目变为二级类目
										ps.executeUpdate();
										rs = ps.getGeneratedKeys();
										if(rs.next()){
											lvl2id = rs.getInt(1);
										}
										//组合成http://channel.jd.com/dig-dig.html
										String url = jd_channel_html.replace("$", childcode);
										//获取三级类目列表
										Request request = new Request(url);
										request.putExtra("parentid", lvl2id);
										request.putExtra("lvl1tag", subN);
										request.putExtra("lvl2tag", childtagname);
										spider.addRequest(request);
										spidertask++;
									}
									
								}
							}else{
								String lvl2code = "";
								String lvl3code ="";
								//如
								/*
								 * {
									"u":"http://channel.jd.com/watch.html",
									"n":"钟表",
									"i":[
										"5025-5026-12091|男表",
										"5025-5026-12092|女表",
										"5025-5026-12093|儿童表",
										"5025-5026-12094|座钟挂钟"
										]
									}
									
									{
									"u":"http://shouji.jd.com/",
									"n":"手机通讯",
									"i":[
										"9987-653-655|手机",
										"9987-653-659|对讲机"
										]
									}
									
									{
									"u":"http://caipiao.jd.com/",
									"n":"彩票",
									"i":[
										"http://caipiao.jd.com/lottery_ssq.html|双色球",
										"http://caipiao.jd.com/lottery_fc3d.html|福彩3D",
										]
									}
								 * */
								for(String child : sub.getI()){
									_m = _p.matcher(child);
									_mm = _pp.matcher(child);
									
									if(_mm.find()){
										lvl3code = _mm.group(1);
										lvl2code = StringUtils.substringBeforeLast(lvl3code, "-");
										break;
									}
									
									if(_m.find()){
										lvl3code = _m.group(1);
										lvl2code = StringUtils.substringBeforeLast(lvl3code, "-");
										break;
									}
								}
								int lvl2id = 0;
								ps.setString(1, subN);
								ps.setInt(2, 0);
								ps.setString(3, "");
								ps.setString(4, subN);
								ps.setString(5, "");
								ps.setString(6, lvl2code);
								ps.setInt(7, 2);//暂时统一定为二级类目
								ps.executeUpdate();
								rs = ps.getGeneratedKeys();
								if(rs.next()){
									lvl2id = rs.getInt(1);
								}
								for(String child : sub.getI()){
									String childtagname = StringUtils.substringAfter(child, "|");
									_m = _p.matcher(child);
									_mm = _pp.matcher(child);
									if(_mm.find()){
										lvl3code = _mm.group(1);
									}else if(_m.find()){
										lvl3code = _m.group(1);
									}else{
										lvl3code = "";
									}
									ps.setString(1, childtagname);
									ps.setInt(2, lvl2id);
									ps.setString(3, "");
									ps.setString(4, subN);
									ps.setString(5, childtagname);
									ps.setString(6, lvl3code);
									ps.setInt(7, 3);
									ps.executeUpdate();
								}
							}
						}
					}
				}
				System.out.println("总共有"+spidertask+"个任务。");
				Thread spiderThread = new Thread(spider);
				spiderThread.start();
				spiderThread.join();
			}catch(Exception e){
				System.out.println("插入失败.");
			}finally{
				try {
					if (rs != null) {
						rs.close();
					}
				} finally {
					try {
						if (ps != null) {
							ps.close();
						}
					} finally {
						if (conn != null) {
							conn.close();
							System.out.println("operation over successfully!");
						}
					}
				}
			}
			
		}
	}

	private static class Subitem {
		private String u;
		private String n;
		private List<String> i;

		public String getU() {
			return u;
		}

		public void setU(String u) {
			this.u = u;
		}

		public String getN() {
			return n;
		}

		public void setN(String n) {
			this.n = n;
		}

		public List<String> getI() {
			return i;
		}

		public void setI(List<String> i) {
			this.i = i;
		}
	}

	private static class JDParent {
		private String u;
		private String n;
		private String t;
		private List<Subitem> i;

		public String getU() {
			return u;
		}

		public void setU(String u) {
			this.u = u;
		}

		public String getN() {
			return n;
		}

		public void setN(String n) {
			this.n = n;
		}

		public String getT() {
			return t;
		}

		public void setT(String t) {
			this.t = t;
		}

		public List<Subitem> getI() {
			return i;
		}

		public void setI(List<Subitem> i) {
			this.i = i;
		}
	}
}
