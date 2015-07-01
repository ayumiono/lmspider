package com.lmdna.spider.temptask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.exception.PageProcessException;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.processor.PageProcessor;

public class AutoHomeCrawl {
	
	public static String PATH_SEPERATOR = "\\";
	public static String ADVICE = "60/0-0-#pageno#-0/";
	public static String NEWS = "1/0-0-#pageno#-0/";
	public static String DRIVE = "3/0-0-#pageno#-0/";
	public static String MARKET = "2/0-0-#pageno#-0/";
	public static String TEC = "102/0-0-#pageno#-0/";
	public static String CULTURE = "97/0-0-#pageno#-0/";
	
	public static final String regEx_html = "<[^>]+>";
	
	public static void main(String[] args) throws IOException{
		File file = new File("D:\\temp\\crawlresult\\car_info");
		File[] files = file.listFiles();
		for(final File childFile : files){
			final File[] fs = childFile.listFiles();
			File f = fs[0];
			BufferedReader breader = null;
			BufferedInputStream bis = null;
			bis = new BufferedInputStream(new FileInputStream(f));
			breader = new BufferedReader(new InputStreamReader(bis, "UTF-8"),1024*1024);
			String url = breader.readLine();
			String newsurl = (url + NEWS).replace("#pageno#", "1");
			String adviceurl = (url + ADVICE).replace("#pageno#", "1");
			String driveurl = (url + DRIVE).replace("#pageno#", "1");
			String marketurl = (url + MARKET).replace("#pageno#", "1");
			String tecurl = (url + TEC).replace("#pageno#", "1");
			String cultureurl = (url + CULTURE).replace("#pageno#", "1");
			Request newsreq = new Request(newsurl);
			newsreq.putExtra("pageno", 1);
			newsreq.putExtra("type", "page");
			newsreq.putExtra("urlpattern", (url + NEWS));
			Request advicereq = new Request(adviceurl);
			advicereq.putExtra("pageno", 1);
			advicereq.putExtra("type", "page");
			advicereq.putExtra("urlpattern", (url + ADVICE));
			Request drivereq = new Request(driveurl);
			drivereq.putExtra("pageno", 1);
			drivereq.putExtra("type", "page");
			drivereq.putExtra("urlpattern", (url + DRIVE));
			Request marketreq = new Request(marketurl);
			marketreq.putExtra("pageno", 1);
			marketreq.putExtra("type", "page");
			marketreq.putExtra("urlpattern", (url + MARKET));
			Request tecreq = new Request(tecurl);
			tecreq.putExtra("pageno", 1);
			tecreq.putExtra("type", "page");
			tecreq.putExtra("urlpattern", (url + TEC));
			Request culturereq = new Request(cultureurl);
			culturereq.putExtra("pageno", 1);
			culturereq.putExtra("type", "page");
			culturereq.putExtra("urlpattern", (url + CULTURE));
			breader.close();
			Spider spider = Spider.create(new PageProcessor(){
				private Site site = Site.me().setCharset("gbk").setDomain("autohome.com").setSleepTime(2000);
				@Override
				public void process(Page page) throws PageProcessException {
					Request req = page.getRequest();
					String type = (String) req.getExtra("type");
					if("page".equals(type)){
						List<String> doctitles = page.getHtml().xpath("//div[@class='cont-info']/ul/li/h3/a/text()").all();
						List<String> dochref = page.getHtml().xpath("//div[@class='cont-info']/ul/li/h3/a/@href").all();
						if(doctitles!=null && doctitles.size()>0){
							for(int i = 0;i<doctitles.size();i++){
								String title = doctitles.get(i);
								String href = dochref.get(i);
								String articleType = "";
								if(href.contains("/news/")){
									articleType = "新闻";
								}else if(href.contains("/drive/")){
									articleType = "评测";
								}else if(href.contains("/culture/")){
									articleType = "文化";
								}else if(href.contains("/advice/")){
									articleType = "导购";
								}else if(href.contains("/market/")){
									articleType = "行情";
								}else if(href.contains("/tech/")){
									articleType = "技术";
								}
								Request newreq = new Request(href);
								newreq.putExtra("articletype", articleType);
								newreq.putExtra("title", title);
								newreq.putExtra("type", "article");
								page.addTargetRequest(newreq);
							}
							int pageno = (Integer) req.getExtra("pageno");
							int nextpage = pageno + 1;
							String urlpattern = req.getExtra("urlpattern").toString();
							String href = urlpattern.replace("#pageno#", String.valueOf(nextpage));
							Request nextreq = new Request(href);
							nextreq.putExtra("type", "page");
							nextreq.putExtra("pageno", nextpage);
							nextreq.putExtra("urlpattern", urlpattern);
							page.addTargetRequest(nextreq);
						}
						page.setSkip(true);
					}else if("article".equals(type)){
						String title = req.getExtra("title").toString();
						String articleType = req.getExtra("articletype").toString();
						List<String> articleP = page.getHtml().xpath("//div[@id='articleContent']/p").all();
						StringBuilder article = new StringBuilder();
						for(String p : articleP){
							article.append(p+"\r\n");
						}
						String articleStr = article.toString();
						Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
				        Matcher m_html = p_html.matcher(articleStr);
				        articleStr = m_html.replaceAll(""); 
				        page.putField("article", articleStr);
				        page.putField("articletype", articleType);
				        page.putField("title", title);
					}
				}
				@Override
				public Site getSite() {
					return site;
				}});
			
			spider.addPipeline(new Pipeline() {
				@Override
				public void process(Page page, Task task) {
					ResultItems resultItems = page.getResultItems();
					try{
						String articleType = resultItems.get("articletype");
						String title = resultItems.get("title");
						String article = resultItems.get("article");
						File file = getFile(childFile.getPath()+PATH_SEPERATOR+articleType+PATH_SEPERATOR+title);
						FileWriter fw = new FileWriter(file);
						fw.write(article);
						fw.close();
					}catch(Exception e) {
					}
				}
			});
			spider.addRequest(new Request[]{newsreq,marketreq,drivereq,tecreq,advicereq,culturereq});
			spider.thread(10);
			spider.run();
		}
		
	}
	
	public static File getFile(String fullName) {
        checkAndMakeParentDirecotry(fullName);
        return new File(fullName);
    }

    public static void checkAndMakeParentDirecotry(String fullName) {
        int index = fullName.lastIndexOf(PATH_SEPERATOR);
        if (index > 0) {
            String path = fullName.substring(0, index);
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
    }
	
}
