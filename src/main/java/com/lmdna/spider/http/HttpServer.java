package com.lmdna.spider.http;

import java.io.IOException;
import java.net.URL;

import javax.servlet.GenericServlet;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.webapp.WebAppContext;

import com.lmdna.spider.node.SpiderNode;
import com.lmdna.spider.node.master.MasterNode;
import com.lmdna.spider.utils.SpiderGlobalConfig;

public class HttpServer {
	protected final Server webServer;
	protected final Connector connector;
	protected final WebAppContext webAppContext;
	private static final String MASTERNODE_ATTRIBUTE_KEY = "master.node";

	public HttpServer(int port, SpiderNode node) throws IOException {
		this.webServer = new Server();

		this.connector = createDefaultChannelConnector();
		this.connector.setPort(port);
		this.webServer.addConnector(this.connector);

		ContextHandlerCollection contexts = new ContextHandlerCollection();
		this.webServer.setHandler(contexts);

		this.webAppContext = new WebAppContext();
		this.webAppContext.setDisplayName("spider");
		this.webAppContext.setContextPath("/spider");

		this.webAppContext.setDescriptor(getWebAppsPath() + "/WEB-INF/web.xml");// 指定war包或者指定webapp项目的路径
		this.webAppContext.setResourceBase(getWebAppsPath());
		this.webAppContext.getServletContext().setAttribute(MASTERNODE_ATTRIBUTE_KEY, node);
		this.webServer.addHandler(this.webAppContext);//addHandler会将handler加到webserver的handlercollection中
		
		WebAppContext verifyimgApp = new WebAppContext();
		verifyimgApp.setDisplayName("verifyimg");
		verifyimgApp.setContextPath("/verifyimg");
		verifyimgApp.setResourceBase(SpiderGlobalConfig.getValue(SpiderGlobalConfig.SPIDER_VERIFYIMG_DIR));
		verifyimgApp.addServlet(new ServletHolder(new DefaultServlet()), "/*");
		this.webServer.addHandler(verifyimgApp);
	}

	public static Connector createDefaultChannelConnector() {
		SelectChannelConnector ret = new SelectChannelConnector();
		ret.setLowResourceMaxIdleTime(10000);
		ret.setMaxIdleTime(30000);
		ret.setAcceptQueueSize(128);
		ret.setResolveNames(false);
		ret.setUseDirectBuffers(false);
		return ret;
	}

	public void addInternalServlet(String name, String pathSpec,
			GenericServlet servlet) {
		ServletHolder holder = new ServletHolder(servlet);
		if (name != null) {
			holder.setName(name);
		}
		this.webAppContext.addServlet(holder, pathSpec);
	}

	public void addInternalServlet(String name, String pathSpec,
			Class<? extends HttpServlet> clazz) {
		ServletHolder holder = new ServletHolder(clazz);
		if (name != null) {
			holder.setName(name);
		}
		this.webAppContext.addServlet(holder, pathSpec);
	}

	protected String getWebAppsPath() throws IOException {
		URL url = getClass().getClassLoader().getResource("webapps");
		if (url == null)
			throw new IOException("webapps not found in CLASSPATH");
		return url.toString();
	}

	public static MasterNode getMasterNodeFromContext(ServletContext context) {
		return (MasterNode) context.getAttribute(MASTERNODE_ATTRIBUTE_KEY);
	}

	public void start() throws Exception {
		this.webServer.start();
	}

	public void stop() throws Exception {
		this.webServer.stop();
	}

	public static void main(String[] args) throws Exception {
		Server server = new Server();
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(8080);
        server.addConnector(connector);
 
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setWelcomeFiles(new String[]{ "index.html" });
        resource_handler.setResourceBase("D:\\verifyimages");
 
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resource_handler, new DefaultHandler() });
        server.setHandler(handlers);
 
        server.start();
        server.join();
	}
}
