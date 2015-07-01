package com.lmdna.spider.protocol.rpc;

import java.io.InputStream;

public interface VerifyImgProtocol {
	public String getVerifyCode(String machineid, String source, InputStream data);
}
