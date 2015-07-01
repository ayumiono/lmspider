package com.lmdna.spider.berkeleydb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class BdbUriUniqFilterTest {
	public static void main(String[] args) throws IOException{
		File envHomePath = new File("C:\\Users\\ayumiono\\Desktop\\alreadyseen");
		File checkFile = new File("E:\\cache_jd.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(checkFile), "utf-8"));
		if(!envHomePath.isDirectory() && !envHomePath.mkdirs()){
			throw new IOException();
		}
		EnvironmentConfig config = new EnvironmentConfig();
		config.setAllowCreate(true);//如果设置了true则表示当数据库环境不存在时候重新创建一个数据库环境，默认为false.
		config.setCacheSize(1024*1024*20);//设置数据库缓存大小
//		config.setTransactional(true);//事务支持,如果为true，则表示当前环境支持事务处理，默认为false，不支持事务处理。
		Environment environment = new Environment(envHomePath,config);
		DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setDeferredWrite(true);
        dbConfig.setAllowCreate(true);
		Database db = environment.openDatabase(null, "alreadySeenUrl", dbConfig);
		String line = "";
		br.close();
	}
}
