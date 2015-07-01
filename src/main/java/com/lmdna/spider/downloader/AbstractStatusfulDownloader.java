package com.lmdna.spider.downloader;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.Downloader;

public abstract class AbstractStatusfulDownloader implements Downloader{

	@Override
	public Page download(Request request, Task task) {
		return null;
	}

	@Override
	public void setThread(int threadNum) {
	}
	
	protected void onSuccess(Request request) {
    }

    protected void onError(Request request) {
    }
}
