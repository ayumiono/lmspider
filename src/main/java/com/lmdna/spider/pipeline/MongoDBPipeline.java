package com.lmdna.spider.pipeline;

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import com.lmdna.spider.mongodb.MongoDBTemplate;
import com.lmdna.spider.utils.SpiderGlobalConfig;
import com.mongodb.BasicDBObject;

public class MongoDBPipeline implements Pipeline{

	private static final Logger logger = LoggerFactory.getLogger(MysqlImplPipeline.class);
	private String collectionName;
	private String dbName = SpiderGlobalConfig.getValue("mongo.spider.dbname");
	private MongoDBTemplate mongodbTemplate;
	
	public MongoDBPipeline(MongoDBTemplate mongodbTemplate, String collectionName) {
		this.collectionName = collectionName;
		this.mongodbTemplate = mongodbTemplate;
	}
	
	public MongoDBPipeline(MongoDBTemplate mongodbTemplate, String dbName, String collectionName) {
		this.collectionName = collectionName;
		this.dbName = dbName;
		this.mongodbTemplate = mongodbTemplate;
	}
	
	@Override
	public void process(Page page, Task task) {
		ResultItems resultItems = page.getResultItems();
		BasicDBObject dbObj = new BasicDBObject();
		Map<String,Object> fields = resultItems.getAll();
		for(Entry<String,Object> entry : fields.entrySet()){
			dbObj.put(entry.getKey(), entry.getValue());
		}
		mongodbTemplate.insert(dbName,collectionName, dbObj);
	}
}
