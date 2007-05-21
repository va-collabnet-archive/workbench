package org.dwfa.ace.url.tiuid;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;


public class Handler extends URLStreamHandler {

	@Override
	protected URLConnection openConnection(URL u) throws IOException {
		return new BdbImageConnection(u);
	}

	@Override
	protected void parseURL(URL u, String spec, int start, int limit) {
		
		this.setURL(u, ExtendedUrlStreamHandlerFactory.PROTOCOL,
				"", -1, "", "", "", spec.substring(start), "");
	}

	@Override
	protected String toExternalForm(URL u) {
		return ExtendedUrlStreamHandlerFactory.PROTOCOL + ":" + u.getQuery();
	}

}
