package com.lmdna.spider.pipeline;

import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

public interface BatchPipeline extends Pipeline {
	/**
	 * force flush records in cache into persistence media
	 * @param task
	 */
	public void flush(Task task);
}
