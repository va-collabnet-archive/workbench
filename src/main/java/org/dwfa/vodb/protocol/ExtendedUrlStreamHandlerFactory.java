package org.dwfa.vodb.protocol;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class ExtendedUrlStreamHandlerFactory implements URLStreamHandlerFactory {
	public static String PROTOCOL="tiuid";
	public static String ACE_PROTOCOL="ace";
	public URLStreamHandler createURLStreamHandler(String protocol) {
		
		if (protocol.equalsIgnoreCase(PROTOCOL)) {
			return new Handler();
		}
		if (protocol.equalsIgnoreCase(ACE_PROTOCOL)) {
			return new Handler();
		}
		return null;
	}

}
