package org.ihtsdo.rf2.util;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class LoggerUtil {

	/*
	 If need to move logging to AceLog use this class and wrap Acelog 
	 */
	
	/*
	AceLog.getAppLog().alertAndLogException(e);
	AceLog.getAppLog().info("exportIncrementalRefinabilityRefset failed");
	AceLog.getAppLog().alertAndLog(contentPanel, Level.SEVERE,"Database Exception: " + e.getLocalizedMessage(), e);
	AceLog.getAppLog().log(Level.SEVERE, "Exception reading: " + conceptid, e);
	*/
	
	
	private static Logger logger = Logger.getLogger(LoggerUtil.class.getName());

	public static void init() {
		BasicConfigurator.configure();
	}

	public static Logger getLogger() {
		return logger;
	}
}
