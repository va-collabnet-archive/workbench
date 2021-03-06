package org.ihtsdo.rf2.module.util;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class LoggerUtil.
 */
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
	
	
	/** The logger. */
	private static Logger logger = Logger.getLogger(LoggerUtil.class.getName());

	/**
	 * Inits the.
	 */
	public static void init() {
		BasicConfigurator.configure();
	}

	/**
	 * Gets the logger.
	 *
	 * @return the logger
	 */
	public static Logger getLogger() {
		return logger;
	}
}
