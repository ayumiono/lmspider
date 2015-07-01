package com.lmdna.spider.pipeline;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

public class MysqlImplCtripHotel implements Pipeline {

	@Override
	public void process(Page page, Task task) {
		ResultItems resultItems = page.getResultItems();
		List<String[]> hreflist = resultItems.get("hotelhreflist");
		StringBuilder sql = new StringBuilder("insert into spider_ctrip_all_hotel_url");
		sql.append("(url,city,createtime) values(?,?,now())");
		final String finalSql = sql.toString();
		
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(
							"jdbc:mysql://192.168.1.111:3306/lemonDNA?useUnicode=true&characterEncoding=UTF8&zeroDateTimeBehavior=convertToNull",
							"root", "111111");
			ps = conn.prepareStatement(finalSql,Statement.RETURN_GENERATED_KEYS);
			for (int x = 0;x<hreflist.size();x++) {
				List<Object> values = new ArrayList<Object>();
				values.add(hreflist.get(x));
				values.add(resultItems.getRequest().getExtra("city"));
				for (int i = 0; i < values.size(); i++) {
					Object value = values.get(i);
					if (value != null) {
						if (value instanceof java.lang.Integer) {
							ps.setInt(i + 1, (Integer) value);
						} else if (value instanceof java.lang.Long) {
							ps.setLong(i + 1, (Long) value);
						} else if (value instanceof java.util.Date) {
							ps.setDate(i + 1, new java.sql.Date(
									((Date) value).getTime()));
							ps.setTimestamp(i + 1, new java.sql.Timestamp(
									((Date) value).getTime()));
						} else if (value instanceof java.lang.String) {
							ps.setString(i + 1, value.toString());
						} else if (value instanceof java.lang.Double) {
							ps.setDouble(i + 1, (Double) value);
						} else if (value instanceof java.lang.Byte) {
							ps.setByte(i + 1, (Byte) value);
						} else if (value instanceof java.lang.Character) {
							ps.setString(i + 1, value.toString());
						} else if (value instanceof java.lang.Float) {
							ps.setFloat(i + 1, (Float) value);
						} else if (value instanceof java.lang.Boolean) {
							ps.setBoolean(i + 1, (Boolean) value);
						} else if (value instanceof java.lang.Short) {
							ps.setShort(i + 1, (Short) value);
						} else {
							ps.setObject(i + 1, value);
						}
					} else {
						ps.setNull(i + 1, Types.NULL);
					}
				}
				ps.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}
