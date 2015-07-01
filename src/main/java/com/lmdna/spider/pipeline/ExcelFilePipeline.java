package com.lmdna.spider.pipeline;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmdna.spider.temptask.DataCacheContainer;
import com.lmdna.spider.temptask.DomainBean;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.utils.FilePersistentBase;

public class ExcelFilePipeline extends FilePersistentBase implements Pipeline {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private String excelName = "";
	
	private String sheetName = "";

    /**
     * new JsonFilePageModelPipeline with default path "/data/webmagic/"
     */
    public ExcelFilePipeline() {
        setPath("/data/webmagic");
        setExcelName("lmdna");
        setSheetName("lmdna-data");
    }
    
    public ExcelFilePipeline(String path) {
    	setPath(path);
        setExcelName("lmdna");
        setSheetName("lmdna-data");
    }
    
    public ExcelFilePipeline(String path,String excelName) {
    	setPath(path);
    	setExcelName(excelName);
        setSheetName("lmdna-data");
    }

    public ExcelFilePipeline(String path,String excelName,String sheetName) {
        setPath(path);
        setExcelName(excelName);
        setSheetName(sheetName);
    }

    @Override
    public void process(Page page, Task task) {
    	DataCacheContainer cache = DataCacheContainer.getInstance();
    	DomainBean bean = new DomainBean();
    	List<String> titleList = new ArrayList<String>();
    	List<Object> dataList = new ArrayList<Object>();
    	for(String key : page.getResultItems().getAll().keySet()){
    		titleList.add(key);
    		dataList.add(page.getResultItems().getAll().get(key));
    	}
    	bean.setDataList(dataList);
    	bean.setTitleList(titleList);
    	cache.addData(bean);
    }

	public void setExcelName(String excelName) {
		this.excelName = excelName;
	}

	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}

}
