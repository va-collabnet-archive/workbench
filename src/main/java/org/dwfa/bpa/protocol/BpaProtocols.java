package org.dwfa.bpa.protocol;


public class BpaProtocols {
	public static void setupExtraProtocols() {
		if (System.getProperty("java.protocol.handler.pkgs") != null) {
		     System.setProperty("java.protocol.handler.pkgs", 
		    		 System.getProperty("java.protocol.handler.pkgs") + "|org.dwfa.bpa.protocol");
		 } else {
		     System.setProperty("java.protocol.handler.pkgs", "org.dwfa.bpa.protocol");
		 }
	}
}
