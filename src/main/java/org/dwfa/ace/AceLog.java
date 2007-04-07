package org.dwfa.ace;

import org.dwfa.util.LogWithAlerts;

public class AceLog {

	private static LogWithAlerts log = new LogWithAlerts(AceLog.class.getName());
	
	public static LogWithAlerts getLog() {
		return log;
	}
	
}
