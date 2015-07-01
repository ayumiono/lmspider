package com.lmdna.spider.apicrawl.sina;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import weibo4j.Location;
import weibo4j.Place;
import weibo4j.Tags;
import weibo4j.Users;
import weibo4j.model.User;
import weibo4j.model.WeiboException;

public class SinaWeiboApiProxy implements Delayed{
	
	private String account;
	private String password;
	private String access_token;
	
	//反监控策略
	private int reuseTimeInterval = 1500;// ms，会根据使用情况自动调整
	private Long canReuseTime = 0L;
	private Long lastBorrowTime = System.currentTimeMillis();
	private Long responseTime = 0L;
	private Long maxVisitTimesPerHour = 0L;
	
	//使用情况记录
	private AtomicInteger failedNum = new AtomicInteger(0);//连续失败次数，每次成功时，会将该值置为0
	private AtomicInteger successNum = new AtomicInteger(0);//只记录前一周期的成功数
	private AtomicInteger borrowNum = new AtomicInteger(0);//只记录前一周期的使用数
	private AtomicInteger deadNum = new AtomicInteger(0);//失效次数
	
	private Users user_api;
	private Location location_api;
	private Place place_api;
	private Tags tag_api;
	
	public void init(){
		//获取access_token
		
		user_api = new Users(access_token);
		location_api = new Location(access_token);
		place_api = new Place(access_token);
		tag_api = new Tags(access_token);
	}
	
	/*----------------------------用户接口----------------------------------------*/
	public User showUserById(String uid) throws WeiboException{
		return user_api.showUserById(uid);
	}
	
	public User showUserByScreenName(String screen_name) throws WeiboException{
		return user_api.showUserByScreenName(screen_name);
	}
	
	public User showUserByDomain(String domain) throws WeiboException{
		return user_api.showUserByDomain(domain);
	}
	/*----------------------------用户接口----------------------------------------*/
	
	
	
	public void setReuseTimeInterval(int reuseTimeInterval) {
		this.reuseTimeInterval = reuseTimeInterval;
		this.canReuseTime = System.nanoTime() + TimeUnit.NANOSECONDS.convert(reuseTimeInterval, TimeUnit.MILLISECONDS);
	}
	
	@Override
	public int compareTo(Delayed o) {
		SinaWeiboApiProxy that = (SinaWeiboApiProxy) o;
		return canReuseTime > that.canReuseTime ? 1 : (canReuseTime < that.canReuseTime ? -1 : 0);
	}

	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(canReuseTime - System.nanoTime(), TimeUnit.NANOSECONDS);
	}
}
