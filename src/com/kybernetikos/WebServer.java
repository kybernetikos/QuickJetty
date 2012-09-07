package com.kybernetikos;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.jasper.servlet.JspServlet;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.ProxyServlet;

/**
 * Does the meat of the jetty configuration.
 */
public abstract class WebServer {

	private final String path;
	private final int port;
	private final Map<String, String> proxies;
	private Server server;

	public WebServer(String path, int port, Map<String, String> proxies) {
		this.path = path;
		this.port = port;
		this.proxies = proxies;
	}

	public void start() {
		server = new Server(port);
		HandlerCollection collection = new HandlerCollection();
		ServletContextHandler handler = new ServletContextHandler();
		handler.setResourceBase(path);

		setupMimeMappings(handler);

		SessionManager sm = new HashSessionManager();
		SessionHandler sh = new SessionHandler(sm);
		handler.setSessionHandler(sh);

		// Set up JSP support
		setupJSP(handler);

		setupProxies(proxies, handler);

		// Sets up default file serving.
		setupFileServing(handler);

		collection.addHandler(handler);

		addRequestLog(collection);
		server.setHandler(collection);
		try {
			server.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		// openRoot();
	}

	protected void openRoot() {
		try {
			java.awt.Desktop.getDesktop().browse(
					java.net.URI.create("http://localhost:" + port));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected abstract void setupFileServing(ServletContextHandler handler);

	public void join() throws InterruptedException {
		server.join();
	}

	protected void setupMimeMappings(ServletContextHandler handler) {
		handler.getMimeTypes().addMimeMapping("xml", "text/xml");
		handler.getMimeTypes().addMimeMapping("appcache", "text/cache-manifest");
		handler.getMimeTypes().addMimeMapping("ico", "image/vnd.microsoft.icon");
	}

	protected void setupJSP(ServletContextHandler handler) {
		handler.addServlet(new ServletHolder(new JspServlet()), "*.jsp");
	}

	protected void setupProxies(Map<String, String> proxies, ServletContextHandler handler) {
		// Sets up proxies
		for (Map.Entry<String, String> details : proxies.entrySet()) {
			ServletHolder proxyHolder = new ServletHolder(
					new ProxyServlet.Transparent());
			String prefix = details.getKey();
			if (prefix.startsWith("/") == false) {
				prefix = "/" + prefix;
			}
			proxyHolder.setInitParameter("Prefix", prefix);
			proxyHolder.setInitParameter("ProxyTo", details.getValue());
			handler.addServlet(proxyHolder, prefix + "/*");
		}
	}

	protected void addRequestLog(HandlerCollection collection) {
		NCSARequestLog requestLog = new NCSARequestLog();
		requestLog.setLogLatency(true);
		RequestLogHandler requestLogHandler = new RequestLogHandler();
		requestLogHandler.setRequestLog(requestLog);
		requestLog.setLogDateFormat("yyyy-mm-dd HH:mm:ss");
		collection.addHandler(requestLogHandler);
	}

	protected static String getPathParameter(String[] args) {
		String path = ".";
		if (args.length > 0) {
			path = args[0];
		}
		return path;
	}

	protected static void showUseIfRequested(String[] args) {
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("/?")
					|| args[0].equalsIgnoreCase("-h")
					|| args[0].equalsIgnoreCase("--help")) {
				showUseSyntax();
				System.exit(0);
			}
		}
	}

	protected static int getPortParameter(String[] args) {
		int port = 8181;
		if (args.length > 1) {
			port = Integer.parseInt(args[1], 10);
		}
		return port;
	}

	private static void showUseSyntax() {
		System.out.println("Syntax:");
		System.out.println("\tjava -jar httpd.jar [<path to serve> [<port> [\"prefix->proxyTo\" ...]]]");
	}

	protected static Map<String, String> getProxiesParameter(String[] args) {
		Map<String, String> proxy = new HashMap<String, String>();
		for (int i = 2; i < args.length; ++i) {
			String[] parts = args[i].split("->");
			proxy.put(parts[0], parts[1]);
		}
		return proxy;
	}
}
