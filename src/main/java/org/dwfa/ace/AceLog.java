package org.dwfa.ace;

import org.dwfa.util.LogWithAlerts;

public class AceLog {

	private static LogWithAlerts appLog = new LogWithAlerts(AceLog.class.getName() + ".app");
	private static LogWithAlerts editLog = new LogWithAlerts(AceLog.class.getName() + ".edit");
	
	public static LogWithAlerts getAppLog() {
		return appLog;
	}
	public static LogWithAlerts getEditLog() {
		return editLog;
	}
	
}
