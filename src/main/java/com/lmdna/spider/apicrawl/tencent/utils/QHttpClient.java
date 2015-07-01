package com.lmdna.spider.apicrawl.tencent.utils;

import java.io.File;
import java.net.URI;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;


/**
 * 自定义参数的Httpclient。<br>
 * 提供httpGet，httpPost两种传送消息的方式<br>
 * 提供httpPost上传文件的方式
 */
public class QHttpClient {

    // SDK默认参数设置
    public static final int CONNECTION_TIMEOUT = 5000;
    public static final int CON_TIME_OUT_MS = 5000;
    public static final int SO_TIME_OUT_MS = 5000;
    public static final int MAX_CONNECTIONS_PER_HOST = 20;
    public static final int MAX_TOTAL_CONNECTIONS = 200;

    // 日志输出
    private static Log log = LogFactory.getLog(QHttpClient.class);

    private CloseableHttpClient httpClient;
    
    public QHttpClient() {
        this(MAX_CONNECTIONS_PER_HOST, MAX_TOTAL_CONNECTIONS, CON_TIME_OUT_MS, SO_TIME_OUT_MS,null,null);
    }

    /**
     * 个性化配置连接管理器
     * @param maxConnectionsPerHost 设置默认的连接到每个主机的最大连接数
     * @param maxTotalConnections 设置整个管理连接器的最大连接数
     * @param conTimeOutMs  连接超时
     * @param soTimeOutMs socket超时
     * @param routeCfgList 特殊路由配置列表，若无请填null
     * @param proxy 代理设置，若无请填null
     */
    public QHttpClient(int maxConnectionsPerHost, int maxTotalConnections, int conTimeOutMs, int soTimeOutMs, List<RouteCfg> routeCfgList, HttpHost proxy) {

    	// 使用默认的 socket factories 注册 "http" & "https" protocol scheme
    	Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", SSLConnectionSocketFactory.getSocketFactory())
                .build();
    	PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(reg);
    	
        SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(soTimeOutMs).build();
        connectionManager.setDefaultMaxPerRoute(maxConnectionsPerHost);
        connectionManager.setMaxTotal(maxTotalConnections);
        connectionManager.setDefaultSocketConfig(socketConfig);
        // 对特定路由修改最大连接数 
        if(null!=routeCfgList){
            for(RouteCfg routeCfg:routeCfgList){
                HttpHost localhost = new HttpHost(routeCfg.getHost(), routeCfg.getPort());
                connectionManager.setMaxPerRoute(new HttpRoute(localhost), routeCfg.getMaxConnetions());
            }
        }  
        
        //设置代理
        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
        httpClient = HttpClients.custom().setConnectionManager(connectionManager).setRoutePlanner(routePlanner).build();
    }

    /**
     * Get方法传送消息
     * 
     * @param url  连接的URL
     * @param queryString  请求参数串
     * @return 服务器返回的信息
     * @throws Exception
     */
    public String httpGet(String url, String queryString) throws Exception {

        String responseData = null;
        if (queryString != null && !queryString.equals("")) {
            url += "?" + queryString;
        }
        RequestBuilder requestBuilder = RequestBuilder.get().setUri(url);
        log.info("QHttpClient httpGet [1] url = " + url);
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(new Integer(CONNECTION_TIMEOUT)).setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();
        requestBuilder.setConfig(requestConfig);
        HttpResponse response;
        HttpGet httpGet = (HttpGet) requestBuilder.build();
        response = httpClient.execute(httpGet);
        try {
            log.info("QHttpClient httpGet [2] StatusLine : " + response.getStatusLine());
            responseData = EntityUtils.toString(response.getEntity());
            log.info("QHttpClient httpGet [3] Response = " + responseData);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            httpGet.abort();
        }
        return responseData;
    }

    /**
     * Post方法传送消息
     * 
     * @param url  连接的URL
     * @param queryString 请求参数串
     * @return 服务器返回的信息
     * @throws Exception
     */
    public String httpPost(String url, String queryString) throws Exception {
        String responseData = null;
        URI tmpUri = new URI(url);
        URI uri = new URI(tmpUri.getScheme(), null,tmpUri.getHost(), tmpUri.getPort(), tmpUri.getPath(),queryString, null);
        log.info("QHttpClient httpPost [1] url = " + uri.toURL());
        RequestBuilder requestBuilder = RequestBuilder.post().setUri(uri);
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(new Integer(CONNECTION_TIMEOUT)).setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();
        requestBuilder.setConfig(requestConfig);
        HttpPost httpPost = (HttpPost) requestBuilder.build();
        if (queryString != null && !queryString.equals("")) {
            StringEntity reqEntity = new StringEntity(queryString);
            // 设置类型
            reqEntity.setContentType("application/x-www-form-urlencoded");
            // 设置请求的数据
            httpPost.setEntity(reqEntity);
        }
        try {
            HttpResponse response = httpClient.execute(httpPost);
            log.info("QHttpClient httpPost [2] StatusLine = " + response.getStatusLine());
            responseData = EntityUtils.toString(response.getEntity());
            log.info("QHttpClient httpPost [3] responseData = " + responseData);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            httpPost.abort();
        }
        return responseData;
    }

    /**
     * Post方法传送消息
     * 
     * @param url  连接的URL
     * @param queryString 请求参数串
     * @return 服务器返回的信息
     * @throws Exception
     */
    public String httpPostWithFile(String url, String queryString, List<NameValuePair> files) throws Exception {
        String responseData = null;
        URI tmpUri = new URI(url);
        URI uri = new URI(tmpUri.getScheme(), null, tmpUri.getHost(), tmpUri.getPort(), tmpUri.getPath(),
                queryString, null);
        log.info("QHttpClient httpPostWithFile [1]  uri = " + uri.toURL());
        MultipartEntityBuilder mpEntityBuilder = MultipartEntityBuilder.create();
        HttpPost httpPost = new HttpPost(uri);
        StringBody stringBody;
        FileBody fileBody;
        File targetFile;
        String filePath;
        ContentType contentType = ContentType.create("text/plain","UTF-8");

        List<NameValuePair> queryParamList = QStrOperate.getQueryParamsList(queryString);
        for (NameValuePair queryParam : queryParamList) {
            stringBody = new StringBody(queryParam.getValue(), contentType);
            mpEntityBuilder.addPart(queryParam.getName(), stringBody);
            // log.info("------- "+queryParam.getName()+" = "+queryParam.getValue());
        }

        for (NameValuePair param : files) {
            filePath = param.getValue();
            targetFile = new File(filePath);
            log.info("---------- File Path = " + filePath + "\n---------------- MIME Types = "
                    + QHttpUtil.getContentType(targetFile));
            fileBody = new FileBody(targetFile, ContentType.create(QHttpUtil.getContentType(targetFile), "UTF-8"));
            mpEntityBuilder.addPart(param.getName(), fileBody);

        }
        // log.info("---------- Entity Content Type = "+mpEntity.getContentType());
        httpPost.setEntity(mpEntityBuilder.build());
        try {
            HttpResponse response = httpClient.execute(httpPost);
            log.info("QHttpClient httpPostWithFile [2] StatusLine = " + response.getStatusLine());
            responseData = EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            httpPost.abort();
        }
        log.info("QHttpClient httpPostWithFile [3] responseData = " + responseData);
        return responseData;
    }

    /**
     * 断开QHttpClient的连接
     */
    public void shutdownConnection() {
        try {
            httpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
