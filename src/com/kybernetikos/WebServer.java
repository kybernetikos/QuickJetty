package com.kybernetikos;

import java.io.IOException;
import java.net.InetSocketAddress;
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
	private final Map<String, String> proxies;
	private Server server;
	private InetSocketAddress address;

	public WebServer(String path, InetSocketAddress address, Map<String, String> proxies) {
		this.path = path;
		this.address = address;
		this.proxies = proxies;
	}

	public void start() {
		server = new Server(this.address);
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
		
		// took this out because it was annoying, but this would
		// automatically open a browser to the root of the jetty server.
		
		// openRoot();
	}

	protected void openRoot() {
		try {
			java.awt.Desktop.getDesktop().browse( java.net.URI.create("http://"+address.getAddress()));
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
			ServletHolder proxyHolder = new ServletHolder(new ProxyServlet.Transparent());
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
			if (args[0].equalsIgnoreCase("/?") || args[0].equalsIgnoreCase("-h") || args[0].equalsIgnoreCase("--help")) {
				showUseSyntax();
				System.exit(0);
			}
		}
	}

	protected static InetSocketAddress getAddressParameter(String[] args) {
		InetSocketAddress address = new InetSocketAddress(8181);
		int port = 8181;
		String bindAddress = "";
		if (args.length > 1) {
			String addressString = args[1];
			if (addressString.contains(".") || addressString.contains(":")) {
				String[] parts = addressString.split(":");
				if (parts.length > 1) {
					port = Integer.parseInt(parts[1]); 
				}
				bindAddress = parts[0];
			} else {
				port = Integer.parseInt(addressString);
			}
		}
		address = new InetSocketAddress(bindAddress, port);
		return address;
	}

	private static void showUseSyntax() {
		System.out.println("Syntax:");
		System.out.println("\tjava -jar httpd.jar [<path to serve> [<port|interface to bind to> [\"prefix->proxyTo\" ...]]]");
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
