package com.lmdna.spider.downloader;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;


public interface StatusfulResponseVerify {

	/**
	 * 检验当前登录用户是否已是异常用户，ip是否被禁用掉
	 * 
	 * @return
	 */
	public boolean validProxyIpSafe(Page page,Site site);
	
	/**
	 * 检验当前登录用户是否已失效
	 * 
	 * @return true：没有，false：已失效
	 */
	public boolean validExpire(Page page,Site site) ;
	
	/**
	 * 检验当前登录用户是否已是异常用户，账号被禁用
	 * 
	 * @return
	 */
	public boolean validUser(Page page,Site site) ;
	
	/**
	 * 检验抓取网页内容是否正常，默认值true
	 * @param content
	 * @return
	 */
	public boolean validPageContext(Page page,Site site);
	
	/**
	 * 检验当前登录用户抓取行为是否正常，默认值true
	 * @param content
	 * @return
	 */
	public boolean validUserAction(Page page,Site site);

}
