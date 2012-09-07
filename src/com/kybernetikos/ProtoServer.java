package com.kybernetikos;

import java.util.Map;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * ProtoServer supports caching and gzipping
 */
public class ProtoServer extends WebServer {

	public ProtoServer(String path, int port, Map<String, String> proxies) {
		super(path, port, proxies);
	}

	protected void setupFileServing(ServletContextHandler handler) {
		DefaultServlet defaultServlet = new DefaultServlet();
		ServletHolder holder = new ServletHolder(defaultServlet);
		holder.setInitParameter("gzip", "true");
		handler.addServlet(holder, "/");
	}

	public static void main(String[] args) throws InterruptedException {
		showUseIfRequested(args);
		String path = getPathParameter(args);
		int port = getPortParameter(args);
		Map<String, String> proxies = getProxiesParameter(args);

		ProtoServer server = new ProtoServer(path, port, proxies);
		server.start();
		server.join();
	}

}
