package org.dwfa.ace.config;

import java.net.URL;

import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.url.tiuid.ExtendedUrlStreamHandlerFactory;
import org.dwfa.bpa.protocol.BpaProtocols;


public class AceProtocols {
	public static void setupExtraProtocols() {
		 BpaProtocols.setupExtraProtocols();
		 AceLog.getAppLog().info("java.protocol.handler.pkgs: " + System.getProperty("java.protocol.handler.pkgs"));
		 URL.setURLStreamHandlerFactory(new ExtendedUrlStreamHandlerFactory());
	}

}