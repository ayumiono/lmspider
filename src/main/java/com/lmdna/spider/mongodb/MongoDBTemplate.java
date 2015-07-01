package com.lmdna.spider.mongodb;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.lmdna.spider.utils.SpiderGlobalConfig;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;

/**
 * mongodb
 * @author ayumiono
 */
public class MongoDBTemplate{
	
	private Mongo client;
	private static MongoDBTemplate instance = new MongoDBTemplate();
	
	public MongoDBTemplate(List<ServerAddress> seeds){
		client = new Mongo(seeds);
	}
	
	public static MongoDBTemplate Default(){
		return instance;
	}
	
	public MongoDBTemplate(){
		String mongosServers = SpiderGlobalConfig.getValue("mongos.cluster.server");
		String[] mongosServerArr = mongosServers.split(";");
		List<ServerAddress> seeds = new ArrayList<ServerAddress>();
		for(String server : mongosServerArr){
			try{
				seeds.add(new ServerAddress(server.split(":")[0],Integer.valueOf(server.split(":")[1])));
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		client = new Mongo(seeds);
	}
	
	private DB getDB(String dbName){
		return client.getDB(dbName);
	}
	
	private DBCollection getCollection(String dbName,String collectionName){
		DB db = getDB(dbName);
		return db.getCollection(collectionName);
	}
	
	public void insert(String dbName,String collectionName,BasicDBObject insertDoc){
		DBCollection collection = getCollection(dbName,collectionName);
		collection.insert(insertDoc);
	}
	
	
	public void findList(String dbName,String collectionName, BasicDBObject queryDoc){
		DBCollection collection = getCollection(dbName,collectionName);
		collection.find(queryDoc);
	}
	
	
	public BasicDBObject findOne(String dbName, String collectionName, BasicDBObject queryDoc){
		DBCollection collection = getCollection(dbName,collectionName);
		return (BasicDBObject) collection.findOne(queryDoc);
	}
	
	
	public void remove(String dbName, String collectionName, BasicDBObject deleteDoc){
		DBCollection collection = getCollection(dbName,collectionName);
		collection.remove(deleteDoc);
	}
	
	
	public void update(String dbName, String collectionName, BasicDBObject qDoc, BasicDBObject oDoc){
		DBCollection collection = getCollection(dbName,collectionName);
		collection.update(qDoc, oDoc);
	}

	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException{
		//导数据
//		int unreasonablecount = 0;
		ServerAddress server = new ServerAddress("192.168.1.113", 27017);
		List<ServerAddress> seeds = new ArrayList<ServerAddress>();
		seeds.add(server);
		MongoDBTemplate template = new MongoDBTemplate(seeds);
//		File qqinfodir = new File("D:\\data\\qqinfo");
//		if(qqinfodir.isDirectory() && qqinfodir.exists()){
//			File[] files = qqinfodir.listFiles();
//			for(File file : files){
//				BufferedReader breader = null;
//				BufferedInputStream bis = null;
//				bis = new BufferedInputStream(new FileInputStream(file));
//				breader = new BufferedReader(new InputStreamReader(bis, "UTF-8"),1024*1024);
//				String line = "";
//				while((line = breader.readLine())!=null){
//					if(StringUtils.isNotEmpty(line)){
//						JSONObject obj = JSONObject.parseObject(line);
//						String uin = obj.getString("uin");
//						String sex = obj.getString("sex");
//						int age = obj.getIntValue("age");
//						String birthday = obj.getString("birthday");
//						String location = obj.getString("location");
//						String home = obj.getString("home");
//						String marriage = obj.getString("marriage");
//						if(age<10 || age>70){
//							unreasonablecount = unreasonablecount + 1;
//						}
//						BasicDBObject dbobj = new BasicDBObject();
//						dbobj.append("uin", uin);
//						dbobj.append("sex", sex);
//						dbobj.append("age", age);
//						dbobj.append("birthday", birthday);
//						dbobj.append("location", location);
//						dbobj.append("home", home);
//						dbobj.append("marriage", marriage);
//						template.insert("spider", "tqq", dbobj);
//					}
//				}
//				breader.close();
//				System.out.println(file.getName()+"导入完成。");
//			}
//			System.out.println("unreasonablecount : "+unreasonablecount);
//		}else{
//			System.out.println("d:\\data\\qqinfo not exists");
//		}
		BasicDBObject dbobj = new BasicDBObject();
		dbobj.append("uin", "1379069879");
		dbobj = template.findOne("spider", "tqq", dbobj);
		String sex = dbobj.getString("sex");
		int age = dbobj.getInt("age");
		System.out.println(sex);
		System.out.println(age);
	}
}
