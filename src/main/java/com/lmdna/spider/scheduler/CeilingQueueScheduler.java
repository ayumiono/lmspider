package com.lmdna.spider.scheduler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.scheduler.DuplicateRemovedScheduler;
import us.codecraft.webmagic.scheduler.MonitorableScheduler;

public class CeilingQueueScheduler extends DuplicateRemovedScheduler implements
		MonitorableScheduler {
	
	private BlockingQueue<Request> queue;
	
	public CeilingQueueScheduler(Integer capacity){
		queue = new LinkedBlockingQueue<Request>(capacity);
	}

	@Override
    public void pushWhenNoDuplicate(Request request, Task task) {
		queue.add(request);
    }

    @Override
    public synchronized Request poll(Task task) {
        return queue.poll();
    }

    @Override
    public long getLeftRequestsCount(Task task) {
        return queue.size();
    }

    @Override
    public long getTotalRequestsCount(Task task) {
        return getDuplicateRemover().getTotalRequestsCount(task);
    }
}
