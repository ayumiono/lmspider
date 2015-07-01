package com.lmdna.spider.temptask;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

public class ProxyCheckServlet extends HttpServlet {

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String result = proxyCheck(req, resp);
		resp.getWriter().write(result);
	}

	private String proxyCheck(HttpServletRequest req, HttpServletResponse resp) {

		String para_host = req.getParameter("ip");
		
        String ip = req.getHeader("X-Real-IP");
        if (!StringUtils.isBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip.equals(para_host) ? "Anonymous" : "Normal";
        }
        ip = req.getHeader("X-Forwarded-For");
        if (!StringUtils.isBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
            int index = ip.indexOf(',');
            if (index != -1) {
                return ip.substring(0, index).equals(para_host) ? "Anonymous" : "Normal";
            } else {
                return ip.equals(para_host) ? "Anonymous" : "Normal";
            }
        } else {
            return req.getRemoteAddr().equals(para_host) ? "Anonymous" : "Normal";
        }
		
		
	}
}