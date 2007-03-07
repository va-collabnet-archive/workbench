package org.dwfa.jini;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ConfigUtil {
	public static String getHostIPAddress() throws UnknownHostException {
		return InetAddress.getLocalHost().getHostAddress();
	}
}
