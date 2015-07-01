package com.lmdna.spider.apicrawl.tencent.api;

import com.lmdna.spider.apicrawl.tencent.constants.APIConstants;
import com.lmdna.spider.apicrawl.tencent.oauth.OAuthV2Request;
import com.lmdna.spider.apicrawl.tencent.utils.QHttpClient;

/**
 * API类的通用部分
 */
public abstract class BasicAPI {
    
    protected RequestAPI requestAPI;
    protected String apiBaseUrl=null;

    public BasicAPI(){
    	requestAPI = new OAuthV2Request();
        apiBaseUrl=APIConstants.API_V2_BASE_URL;
    }
    
    public BasicAPI(QHttpClient qHttpClient){
    	requestAPI = new OAuthV2Request(qHttpClient);
        apiBaseUrl=APIConstants.API_V2_BASE_URL;
    }
    
    public void shutdownConnection(){
        requestAPI.shutdownConnection();
    }

    public String getAPIBaseUrl() {
        return apiBaseUrl;
    }

    public abstract  void setAPIBaseUrl(String apiBaseUrl);
    
}
