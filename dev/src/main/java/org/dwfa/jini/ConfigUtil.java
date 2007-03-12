package org.dwfa.jini;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

public class ConfigUtil {
	public static String getHostIPAddress() throws UnknownHostException {
		return InetAddress.getLocalHost().getHostAddress();
	}
	
	private static String uniqueJvmGroup;
	
	public static String getUniqueJvmGroup() {
		if (uniqueJvmGroup == null) {
			uniqueJvmGroup = UUID.randomUUID().toString();
		}
		return uniqueJvmGroup;
	}
	
	public static void setUniqueJvmGroup(String uniqueJvmGroup) {
		ConfigUtil.uniqueJvmGroup = uniqueJvmGroup;
	}
}
