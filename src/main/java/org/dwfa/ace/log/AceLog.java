package org.dwfa.ace.log;


public class AceLog {

	private static AceLogWithAlerts appLog = new AceLogWithAlerts(AceLog.class.getName() + ".app");
	private static AceLogWithAlerts editLog = new AceLogWithAlerts(AceLog.class.getName() + ".edit");
	
	public static AceLogWithAlerts getAppLog() {
		return appLog;
	}
	public static AceLogWithAlerts getEditLog() {
		return editLog;
	}

}
