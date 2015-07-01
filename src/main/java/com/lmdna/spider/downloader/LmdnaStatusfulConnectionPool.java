package com.lmdna.spider.downloader;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;

import org.slf4j.LoggerFactory;

import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.statusful.StatusfulConnection;
import us.codecraft.webmagic.statusful.StatusfulConnectionPool;
import us.codecraft.webmagic.utils.LoggerUtil;

import com.lmdna.spider.LoginProxy;
import com.lmdna.spider.dao.model.SpiderBiz;
import com.lmdna.spider.dao.model.SpiderWebsiteAccount;
import com.lmdna.spider.dao.model.SpiderWebsiteConfig;
import com.lmdna.spider.exception.AuthorMatchException;
import com.lmdna.spider.exception.ProxyIpException;
import com.lmdna.spider.exception.SpiderLoginException;
import com.lmdna.spider.protocol.rpc.AccountProtocol;
import com.lmdna.spider.protocol.rpc.RPCProtocolFactory;
import com.lmdna.spider.utils.HttpClientHelper;
import com.lmdna.spider.utils.SpiderGlobalConfig;

public class LmdnaStatusfulConnectionPool implements StatusfulConnectionPool{
	
	private Map<String,AccountProxy> accountInUse = new ConcurrentHashMap<String, AccountProxy>();
	private BlockingQueue<AccountProxy> wait4LoginAccount = new DelayQueue<AccountProxy>();
	private SpiderBiz biz;
	//private Site site;//登入时不使用代理IP
	private SpiderWebsiteConfig websiteConfig;
	private AccountProtocol accountServiceProtocol = RPCProtocolFactory.get(AccountProtocol.class);
	private BlockingQueue<LmdnaStatusfulConnection> connPool = new DelayQueue<LmdnaStatusfulConnection>();
	
	private int base_accountReuseInterval = 1000;
	private int base_ipReuseInterval = 1000;
	
	private String logName = "connectionpool";
	
	private Vector<LmdnaStatusfulConnection> invalidConnsThisHour = new Vector<LmdnaStatusfulConnection>();
	private Timer timer1 = new Timer(true);
	private TimerTask reviveConnsInvalidThisHour = new TimerTask() {
        @Override
        public void run() {
        	if(invalidConnsThisHour.size()>0){
        		for(LmdnaStatusfulConnection conn : invalidConnsThisHour){
        			conn.getAccount().resetBorrowNum();
        		}
        	}
        }
    };
    
	private String poolId;
	
	private long reuseInterval = 1000;
	
	public LmdnaStatusfulConnectionPool(SpiderBiz biz,Site site){
		this.biz = biz;
		this.websiteConfig = biz.getWebsiteConfigBO();
		this.poolId = websiteConfig.getWebsiteBO().getSiteEnName();
		load();
		timer1.schedule(reviveConnsInvalidThisHour, 0, 60*60*1000);
	}
	
	public void load(){
		int needLoadCount = 0;
		LoggerUtil.info(biz.getBizCode(), getLogName(), "开始加载账号...");
		needLoadCount = websiteConfig.getAccountCount();
		LoggerUtil.info(biz.getBizCode(), getLogName(), "需要加载"+needLoadCount+"个账号。");
		//开始登录
		while(true){
			AccountProxy wait4Login = null;
			wait4Login = wait4LoginAccount.poll();
			if(wait4Login != null){
				LmdnaStatusfulConnection connection = login(wait4Login);
				if(connection != null){
					connPool.add(connection);
					accountInUse.put(wait4Login.getAccount(),wait4Login);
					needLoadCount--;
				}
			}else{
				break;
			}
		}
		Map<String,Object> parammap = new HashMap<String,Object>();
		parammap.put("bizCode", biz.getBizCode());
		parammap.put("site", biz.getWebsiteConfigBO().getWebsiteBO().getId());
		parammap.put("machineId", SpiderGlobalConfig.getValue(SpiderGlobalConfig.MACHINE_ID));
		while(needLoadCount>0){
			SpiderWebsiteAccount account = accountServiceProtocol.getAccount(parammap);
			if(account == null){
				LoggerUtil.info(biz.getBizCode(), getLogName(), "获取账号失败，账号不足。");
//				MailSender.getInstance().sendException(biz.getBizCode()+"需要更多账号，请及时补充账号。","chenguolong@alphaun.com","柠檬爬虫系统（需要补充抓取账号）");
				break;
			}
			LoggerUtil.info(biz.getBizCode(), getLogName(), "获取到账号:"+account.getAccount());
			String acc = account.getAccount();
			String password = account.getPassword();
			AccountProxy wait4Login = new AccountProxy(acc,password,websiteConfig.getAccountReuseInterval(),websiteConfig.getMaxVisitPerAccount());
			LmdnaStatusfulConnection connection = login(wait4Login);
			if(connection != null){
				connPool.add(connection);
				accountInUse.put(wait4Login.getAccount(),wait4Login);
				needLoadCount--;
				continue;
			}
		}
	}
	
	private LmdnaStatusfulConnection login(AccountProxy wait4Login){
		String account=wait4Login.getAccount();
		String password=wait4Login.getPassword();
		LoggerUtil.info(biz.getBizCode(),getLogName(),"开始处理账号："+account+"...");
		String clazz = websiteConfig.getLoginClass();
		LoginProxy loginProxy = null;
		HttpClientHelper httpclienthelper = null;
		int loginRetryTimes = 5;
		
		LmdnaStatusfulConnection connection = null;
		while(loginRetryTimes>0){
			httpclienthelper = new HttpClientHelper(true);
			Constructor<?> c;
			Object[] params = {account,password,httpclienthelper};
			try {
				c = Class.forName(clazz).getConstructor(String.class, String.class, HttpClientHelper.class);
				loginProxy = (LoginProxy) c.newInstance(params);
			} catch (Exception e) {
				LoggerFactory.getLogger(LmdnaStatusfulConnectionPool.class).error("连接池初始化失败！找不到登录类", e);
				break;
			}
			try {
				loginProxy.login();
				connection = new LmdnaStatusfulConnection(wait4Login);
				connection.setHttphelper(httpclienthelper);
				LoggerUtil.info(biz.getBizCode(),getLogName(),"账号："+account+"登录成功。");
				break;
			} catch (Exception e) {
				if(e instanceof ProxyIpException){
					LoggerUtil.info(biz.getBizCode(),getLogName(),"账号："+account+"登录时发生代理IP错误！");
					loginRetryTimes--;
				}else if(e instanceof SpiderLoginException){
					LoggerUtil.info(biz.getBizCode(),getLogName(),"账号："+account+"登录时发生错误！"+e.getMessage());
					loginRetryTimes--;
				}else if(e instanceof AuthorMatchException){
					//记录账号异常信息
					LoggerUtil.info(biz.getBizCode(),getLogName(),"账号："+account+"异常！"+e.getMessage());
					break;
				}else{
					loginRetryTimes--;
				}
			}
		}
		if(connection==null){}
		return connection;
	}
	
	public StatusfulConnection getConn(){
		LmdnaStatusfulConnection conn = null;
        try {
            Long time = System.currentTimeMillis();
            while(true){
            	conn = connPool.take();
            	if(conn.isValid()){
            		break;
            	}else{
            		invalidConnsThisHour.add(conn);
            	}
            }
            conn.getAccount().borrow();
            double costTime = (System.currentTimeMillis() - time) / 1000.0;
            if (costTime > reuseInterval) {
                LoggerUtil.info(getLogName(),poolId,"get conn time >>>> {}",new Object[]{costTime});
            }
        } catch (InterruptedException e) {
        	LoggerFactory.getLogger(LmdnaStatusfulConnectionPool.class).error("get proxy error", e);
        }
        if (conn == null) {
            throw new NoSuchElementException();
        }
        return conn;
	}
	
	@Override
	public void returnConn(StatusfulConnection conn, int statCode) {
		LmdnaStatusfulConnection lmdnaconn = (LmdnaStatusfulConnection)conn;
		AccountProxy accountProxy = lmdnaconn.getAccount();
		String machineId=SpiderGlobalConfig.getValue(SpiderGlobalConfig.MACHINE_ID);
		switch(statCode){
			case LmdnaStatusfulConnection.SUCCESS:
				accountProxy.setReuseTimeInterval(getBase_accountReuseInterval());
				accountProxy.resetFailedNum();
				connPool.add(lmdnaconn);
				break;
			case LmdnaStatusfulConnection.AUTH_EXPIRE:
				//重新登录
				accountProxy.setReuseTimeInterval(websiteConfig.getAccountReuseInterval());
				addAsWaitLoginAccount(accountProxy);
				break;
			case LmdnaStatusfulConnection.ACCOUNT_TOO_OFFTEN:
				//调整时间间隔
				accountProxy.setReuseTimeInterval(getBase_accountReuseInterval()*accountProxy.getFailedNum());
				connPool.add(lmdnaconn);
				break;
			case LmdnaStatusfulConnection.INVALID_ACCOUNT:
				accountServiceProtocol.removeInvalidAccount(biz.getWebsiteConfigBO().getWebsiteBO().getId(), accountProxy.getAccount(), accountProxy.getPassword(), machineId, "账号失效");
				break;
			case LmdnaStatusfulConnection.PROXYIP_TOO_OFFTEN:
				//调整时间间隔
				connPool.add(lmdnaconn);
				break;
			case Proxy.ERROR_Proxy://绑定的代理IP错误
				break;
			case Proxy.ERROR_BANNED:
				break;
			default:break;
		}
	}
	
	private void addAsWaitLoginAccount(AccountProxy waitLogin){
		wait4LoginAccount.add(waitLogin);
	}
	
	
	public String getLogName(){
		return this.logName;
	}

	public int getBase_accountReuseInterval() {
		return base_accountReuseInterval;
	}

	public void setBase_accountReuseInterval(int base_accountReuseInterval) {
		this.base_accountReuseInterval = base_accountReuseInterval;
	}

	public int getBase_ipReuseInterval() {
		return base_ipReuseInterval;
	}

	public void setBase_ipReuseInterval(int base_ipReuseInterval) {
		this.base_ipReuseInterval = base_ipReuseInterval;
	}

	
}
