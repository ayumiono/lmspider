package com.lmdna.spider.temptask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;

public class MedivQQBizDeom {
	public static void main(String[] args) throws IOException {
		ServerAddress server = new ServerAddress("192.168.1.113", 27017);
		List<ServerAddress> seeds = new ArrayList<ServerAddress>();
		seeds.add(server);
		Mongo client = new Mongo(seeds);
		DBCollection collection = client.getDB("spider").getCollection("tqq");
		
		BufferedReader reader = null;
	    BufferedInputStream bis = null;
	    
	    Jedis redisClient = RedisUtil.getInstance().getJedis("127.0.0.1", 6379);
		
		File sourceFileDir = new File(args[0]);
		if(!sourceFileDir.isDirectory()){
			System.out.println("参数指定错误");
			System.exit(-1);
		}else{
			File[] files = sourceFileDir.listFiles();
			for(File file : files){
				StringBuilder result = new StringBuilder();
				bis = new BufferedInputStream(new FileInputStream(file));
			    reader = new BufferedReader(new InputStreamReader(bis, "UTF-8"), 1048576);
			    String line = "";
			    while ((line = reader.readLine()) != null) {
			    	String ta = line.split("\t")[0].trim();
			    	String qq = line.split("\t")[1].trim();
			    	String cookies = redisClient.hget("mvck", ta);
			    	String[] cookiearr;
			    	if (cookies!=null && !cookies.isEmpty()) {
			    		StringBuilder sb = new StringBuilder();
			    		BasicDBObject qqinfobean = new BasicDBObject();
				    	qqinfobean.append("uin", qq);
				    	qqinfobean = (BasicDBObject) collection.findOne(qqinfobean);
				    	String sex = qqinfobean.getString("sex");
				    	int age = qqinfobean.getInt("age");
				    	int sex_tag_id=0;
				    	int age_tag_id =0;
				    	if("男".equals(sex)){
				    		sex_tag_id = 130001;
				    		sb.append(sex_tag_id+",");
				    	}else if("女".equals("sex")){
				    		sex_tag_id = 130002;
				    		sb.append(sex_tag_id+",");
				    	}else{
				    		
				    	}
				    	if(age<18 && age>=10){
				    		age_tag_id = 120101;
				    		sb.append(age_tag_id+",");
				    	}else if(age>=18 && age<=24){
				    		age_tag_id = 120102;
				    		sb.append(age_tag_id+",");
				    	}else if(age>=25 && age<=34){
				    		age_tag_id = 120103;
				    		sb.append(age_tag_id+",");
				    	}else if(age>=35 && age<=44){
				    		age_tag_id = 120104;
				    		sb.append(age_tag_id+",");
				    	}else if(age>=45 && age<=54){
				    		age_tag_id = 120105;
				    		sb.append(age_tag_id+",");
				    	}else if(age>=55 && age<=64){
				    		age_tag_id = 120106;
				    		sb.append(age_tag_id+",");
				    	}else if(age>64 && age<=70){
				    		age_tag_id = 120107;
				    		sb.append(age_tag_id+",");
				    	}
						JSONArray jsonArray = JSON.parseArray(cookies);
						Object[] obj =  jsonArray.toArray();
						cookiearr = new String[obj.length];
						System.arraycopy(obj, 0, cookiearr, 0, obj.length);
						if (cookiearr != null && cookiearr.length > 0) {
							for (String str : cookiearr) {
								if (str!=null && !"".equals(str.trim())) {
									try {
										result.append(str.substring(0, str.length()-4)).append("\t").append(sb.toString()).append("\n");
									} catch (Exception e1) {
										e1.printStackTrace();
										System.exit(-1);
									}
								}
							}
						}
					}
			    }
			    reader.close();
			    System.out.println(file.getName()+"处理完成.");
			    if(args[1].equals("1")){
			    	long timestamp = Calendar.getInstance().getTimeInMillis()/1000/60;
					String sid = encode("lGExQBbgVkIy6OyohhAR0ncTR1Xj2hCo"+timestamp);
					String uploadUrl = "http://upload.dmp.mediav.com/label/upload?tid=595&sid="+sid;
					Map<String,String> paramMap = new HashMap<String,String>();
					paramMap.put("", "application/x-www-form-urlencoded");
					paramMap.put("Content-Type", "text/plain");
					paramMap.put("host","st3dg.prod.mediav.com:8040");
					paramMap.put("Connection","keep-alive");
					paramMap.put("Content-Length","78");
					String resultString = sendPost(uploadUrl, result.toString(), paramMap);
					System.out.println(resultString);
			    }else if(args[1].equals("0")){
			    	System.out.println("开始输出文件！");
			    	FileWriter writer = new FileWriter(args[2],true);
				    writer.write(result.toString());
				    writer.flush();
				    writer.close();
			    }
			}
		}
	}
	
	private static String sendPost(String urlStr, String param,Map<String,String> propertyMap) {
        String result = "";
        OutputStreamWriter writer = null;
        try {
            // Send the request
            URL url = new URL(urlStr);
            URLConnection conn = url.openConnection();
            
            if(propertyMap!=null){
            	for (Entry<String,String> entry : propertyMap.entrySet()) {
            		conn.setRequestProperty(entry.getKey(), entry.getValue());
				}
            }
            conn.setConnectTimeout(3000);
            conn.setDoOutput(true);
            
            
            writer = new OutputStreamWriter(conn.getOutputStream());

            writer.write(param);
            writer.flush();

            // Get the response
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
            	result+=line;
            }
            writer.close();
            reader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally{
        	if(writer!=null){
        		try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }
        return result;
    }
	
	private static String encode(String source) {
		//logger.info("MD5 source: " + source);
		String result = null;
		char hexDigits[] = { // Used to convert 16-byte hexadecimal characters.  
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
				'e', 'f' };
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			md.update(source.getBytes("UTF-8"));
			byte tmp[] = md.digest(); // MD5 calculation is a 128-bit long integer, that is with 16-byte byte.  
			char str[] = new char[16 * 2]; // Each byte expressed in hexadecimal using 2 characters, so that 32 bytes as hexadecimal. 
			int k = 0; // The index of character in convert result.  
			for (int i = 0; i < 16; i++) { // Convert each byte to hexadecimal of MD5.  
				byte byte0 = tmp[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			result = new String(str); // Convert the result from byte to string.  

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;  
	}
	
	static class RedisUtil  {
	    protected Logger log = LoggerFactory.getLogger(getClass());
		private RedisUtil() {
		    
		}
		private static JedisPool pool  = null;
	    private static JedisPool getPool(String ip,int port) {
	        if(pool==null) {
	        	JedisPoolConfig config = new JedisPoolConfig();
	            config.setTestOnBorrow(false);
	            config.setTestOnReturn(true);
	            try{  
	                pool = new JedisPool(config, ip, port,60000);
	            } catch(Exception e) {
	                e.printStackTrace();
	            }
	        }
	        return pool;
	    }
	    private static class RedisUtilHolder{
	        private static RedisUtil instance = new RedisUtil();
	    }
		public static RedisUtil getInstance() {
			return RedisUtilHolder.instance;
		}
		public Jedis getJedis(String ip,int port) {
			Jedis jedis  = null;
			int count =0;
			do{
	    		try{ 
	    			jedis = getPool(ip,port).getResource();
	    		} catch (Exception e) {
	    			log.error("get redis master1 failed!", e);
	    			getPool(ip,port).returnBrokenResource(jedis);  
	    		}
	    		count++;
			}while(jedis==null&&count<30);
			return jedis;
		}
		public void closeJedis(Jedis jedis,String ip,int port) {
			if(jedis != null) {
			    getPool(ip,port).returnResource(jedis);
			}
		}
	}
}