package com.kybernetikos;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * DevServer lets you change the files on disk and immediately reload.  It turns caching off.
 */
public class DevServer extends WebServer {
	
	public DevServer(String path, int port, Map<String, String> proxies) {
		super(path, port, proxies);
	}

	@Override
	protected void setupFileServing(ServletContextHandler handler) {
		// Sets up default file serving.
		@SuppressWarnings("serial")
		DefaultServlet defaultServlet = new DefaultServlet() {
			@Override
			public void service(ServletRequest arg0, ServletResponse resp) throws ServletException, IOException {
				if (resp instanceof HttpServletResponse) {
					HttpServletResponse r = (HttpServletResponse) resp;
					r.setHeader("Cache-Control", "no-cache");
				}
				super.service(arg0, resp);
			}
		};
		ServletHolder holder = new ServletHolder(defaultServlet);
		holder.setInitParameter("useFileMappedBuffer", "false");
		holder.setInitParameter("org.eclipse.jetty.servlet.Default.cacheControl", "no-store,no-cache,must-revalidate");
		handler.addServlet(holder, "/");
	}

	public static void main(String[] args) throws InterruptedException {
		showUseIfRequested(args);
		String path = getPathParameter(args);
		int port = getPortParameter(args);
		Map<String, String> proxies = getProxiesParameter(args);
		
		DevServer server = new DevServer(path, port, proxies);
		server.start();
		server.join();
	}
	
}
