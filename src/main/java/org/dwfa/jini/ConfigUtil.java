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
	
	public static String getJiniPort() {
		if (System.getProperties().get("org.dwfa.jiniport") != null) {
			return (String) System.getProperties().get("org.dwfa.jiniport");
		}
		return "8081";
	}
	
	public static String getJiniPortUrlPart() {
		return ":" + getJiniPort() + "/";
	}
	
	public static void main(String[] args) {
		try {
			System.out.println("getHostIPAddress(): " + InetAddress.getLocalHost().getHostAddress());
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
}
