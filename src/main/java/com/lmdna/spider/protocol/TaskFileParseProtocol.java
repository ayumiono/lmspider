package com.lmdna.spider.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import us.codecraft.webmagic.Request;

public interface TaskFileParseProtocol {
	public List<Request> parse(InputStream in)throws IOException;
}
