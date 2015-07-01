package com.lmdna.spider.pipeline.util;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import com.lmdna.spider.utils.SpiderGlobalConfig;

public class MySQLJdbcTemplate {
	public static JdbcTemplate getJdbcTemplate(){
		JdbcTemplate template = new JdbcTemplate();
//		<property name="driverClassName" value="${jdbc.driverClassName}"/>
//        <property name="url" value="${jdbc.url}"/>
//        <property name="username" value="${jdbc.username}"/>
//        <property name="password" value="${jdbc.password}"/>
//        <property name="maxActive" value="100"/>
//        <property name="maxIdle" value="30"/>
//        <property name="maxWait" value="5000"/>
//        <property name="minIdle" value="10" />
//        <property name="testOnBorrow" value="true"/> 
//    	<property name="testWhileIdle" value="true"/> 
//        <property name="validationQuery">
//            <value>select 1 from dual</value>
//        </property>
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName(SpiderGlobalConfig.getValue("jdbc.driverClassName"));
		ds.setUsername(SpiderGlobalConfig.getValue("jdbc.username"));
		ds.setPassword(SpiderGlobalConfig.getValue("jdbc.password"));
		ds.setMaxActive(100);
		ds.setMaxIdle(30);
		ds.setMaxWait(5000);
		ds.setMinIdle(10);
		ds.setTestOnBorrow(true);
		ds.setTestWhileIdle(true);
		ds.setValidationQuery("select 1 from dual");
		template.setDataSource(ds);
		return template;
	}
}
