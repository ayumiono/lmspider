package com.lmdna.spider.apicrawl.tencent;

import org.apache.http.message.BasicNameValuePair;

import com.lmdna.spider.apicrawl.tencent.api.BasicAPI;
import com.lmdna.spider.apicrawl.tencent.oauth.OAuth;
import com.lmdna.spider.apicrawl.tencent.utils.QArrayList;
import com.lmdna.spider.apicrawl.tencent.utils.QHttpClient;

public class TQQApi extends BasicAPI{
	
	 private String userInfoUrl=apiBaseUrl+"/user/info";
	 private String userOtherInfoUrl=apiBaseUrl+"/user/other_info";
	 private String userInfosUrl=apiBaseUrl+"/user/infos";
	
	/**
     * 使用完毕后，请调用 shutdownConnection() 关闭自动生成的连接管理器
     * @param OAuthVersion 根据OAuthVersion，配置通用请求参数
     */
    public TQQApi() {
        super();
    }

    /**
     * @param OAuthVersion 根据OAuthVersion，配置通用请求参数
     * @param qHttpClient 使用已有的连接管理器
     */
    public TQQApi(QHttpClient qHttpClient) {
        super(qHttpClient);
    }

	@Override
	public void setAPIBaseUrl(String apiBaseUrl) {
		
	}
	
	/**
	 * 获取自己的资料
	 * 
	 * @param oAuth 
	 * @param format 返回数据的格式（json或xml）
	 * @return
	 * @throws Exception
     * @see <a href="http://wiki.open.t.qq.com/index.php/%E5%B8%90%E6%88%B7%E7%9B%B8%E5%85%B3/%E8%8E%B7%E5%8F%96%E8%87%AA%E5%B7%B1%E7%9A%84%E8%AF%A6%E7%BB%86%E8%B5%84%E6%96%99">腾讯微博开放平台上关于此条API的文档</a>
	 */
	public String info(OAuth oAuth, String format) throws Exception {
		QArrayList paramsList = new QArrayList();
		paramsList.add(new BasicNameValuePair("format", format));
		return requestAPI.getResource(userInfoUrl, paramsList,
				oAuth);
	}

	/**
	 * 获取其他用户个人资料
	 * 
	 * @param oAuth
	 * @param format 返回数据的格式（json或xml）
	 * @param name 他人的帐户名（可选）
	 * @param fopenid  他人的openid（可选） name和fopenid至少选一个，若同时存在则以name值为主 
	 * @return
	 * @throws Exception
     * @see <a href="http://wiki.open.t.qq.com/index.php/%E5%B8%90%E6%88%B7%E7%9B%B8%E5%85%B3/%E8%8E%B7%E5%8F%96%E5%85%B6%E4%BB%96%E4%BA%BA%E8%B5%84%E6%96%99">腾讯微博开放平台上关于此条API的文档</a>
	 */
	public String otherInfo(OAuth oAuth, String format, String name, String fopenid)
			throws Exception {
		QArrayList paramsList = new QArrayList();
		paramsList.add(new BasicNameValuePair("format", format));
		paramsList.add(new BasicNameValuePair("name", name));
        paramsList.add(new BasicNameValuePair("fopenid", fopenid));
		
		return requestAPI.getResource(userOtherInfoUrl,
				paramsList, oAuth);
	}



	/**
	 * 获取一批人的简单资料
	 * @param oAuth
	 * @param format 返回数据的格式 是（json或xml）
	 * @param names 用户ID列表 比如 abc,edf,xxxx（最多30，可选）
	 * @param fopenids  你需要读取的用户openid列表，用下划线“_”隔开，<br>
	 *                             例如：B624064BA065E01CB73F835017FE96FA_B624064BA065E01CB73F835017FE96FB_B624064BA065E01CB73F835017FE96FC<br>
	 *                             （个数与names保持一致，最多30，可选）
	 * @return
	 * @throws Exception
     * @see <a href="http://wiki.open.t.qq.com/index.php/%E5%B8%90%E6%88%B7%E7%9B%B8%E5%85%B3/%E8%8E%B7%E5%8F%96%E4%B8%80%E6%89%B9%E4%BA%BA%E7%9A%84%E7%AE%80%E5%8D%95%E8%B5%84%E6%96%99">腾讯微博开放平台上关于此条API的文档</a>
	 */
	public String infos(OAuth oAuth, String format, String names ,String fopenids
			)
			throws Exception {
		QArrayList paramsList = new QArrayList();
		paramsList.add(new BasicNameValuePair("format", format));
		paramsList.add(new BasicNameValuePair("names", names));
        paramsList.add(new BasicNameValuePair("fopenids", fopenids));
		
		return requestAPI.getResource(userInfosUrl,paramsList, oAuth);
	}
	
}
