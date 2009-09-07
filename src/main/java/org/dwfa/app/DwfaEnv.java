package org.dwfa.app;

public class DwfaEnv {
    public static boolean isHeadless() {
    	if (System.getProperty("java.awt.headless") == null) {
        	System.setProperty("java.awt.headless", "true");
    	}
		return System.getProperty("java.awt.headless").toLowerCase().equals("true");
    }

    public static void setHeadless(Boolean headless) {
    	System.setProperty("java.awt.headless", headless.toString());
    }

}
