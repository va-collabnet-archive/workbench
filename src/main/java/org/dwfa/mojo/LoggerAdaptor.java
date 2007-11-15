package org.dwfa.mojo;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.maven.plugin.logging.Log;

/**
 * Class used to adapt from the apache maven Log object to a Logger.
 * NOTE: only the core logging methods have been overridden!
 * 
 * @author dionm
 *
 */
public class LoggerAdaptor extends Logger {
	
	private Log logger;

	public LoggerAdaptor(Log logger) {
		super("BinaryChangeSetReadAll", null);
		this.logger = logger;
	}

	@Override
	public void fine(String msg) {
		logger.debug(msg);
	}

	@Override
	public void finer(String msg) {
		logger.debug(msg);
	}

	@Override
	public void finest(String msg) {
		logger.debug(msg);
	}

	@Override
	public void info(String msg) {
		logger.info(msg);
	}

	@Override
	public boolean isLoggable(Level level) {
		if (logger.isDebugEnabled()) {
			return level.equals(Level.FINE) || level.equals(Level.FINER) || level.equals(Level.FINEST);
		}

		if (logger.isInfoEnabled()) {
			return level.equals(Level.INFO); 
		}
		
		if (logger.isWarnEnabled()) {
			return level.equals(Level.WARNING);
		}
		
		if (logger.isErrorEnabled()) {
			return level.equals(Level.SEVERE); 
		}
		
		return false;
	}

	@Override
	public void log(Level level, String msg, Object param1) {
		logMessage(level, msg, param1);
	}

	private void logMessage(Level level, String msg, Object param1) {
		if (level.equals(Level.FINE) || level.equals(Level.FINER) || level.equals(Level.FINEST)) {
			logger.debug(msg + toString(param1));
		}

		if (level.equals(Level.INFO)) {
			logger.info(msg + toString(param1));
		}
		
		if (logger.isWarnEnabled()) {
			logger.warn(msg + toString(param1));
		}
		
		if (logger.isErrorEnabled()) {
			logger.error(msg + toString(param1));
		}
		
	}

	private String toString(Object param1) {
		if (param1 == null) {
			return null;
		}
		
		return param1.toString();
	}
	
	private String toString(Object[] param1) {
		if (param1 == null) {
			return null;
		}
		
		String string = "[";
		for (Object object : param1) {
			string += toString(object) + ",";
		}
		
		return string.substring(0, string.length() - 2) + "]";
	}

	@Override
	public void log(Level level, String msg, Object[] params) {
		if (level.equals(Level.FINE) || level.equals(Level.FINER) || level.equals(Level.FINEST)) {
			logger.debug(msg + toString(params));
		}

		if (level.equals(Level.INFO)) {
			logger.info(msg + toString(params));
		}
		
		if (logger.isWarnEnabled()) {
			logger.warn(msg + toString(params));
		}
		
		if (logger.isErrorEnabled()) {
			logger.error(msg + toString(params));
		}
	}

	@Override
	public void log(Level level, String msg, Throwable thrown) {
		if (level.equals(Level.FINE) || level.equals(Level.FINER) || level.equals(Level.FINEST)) {
			logger.debug(msg, thrown);
		}

		if (level.equals(Level.INFO)) {
			logger.info(msg, thrown);
		}
		
		if (logger.isWarnEnabled()) {
			logger.warn(msg, thrown);
		}
		
		if (logger.isErrorEnabled()) {
			logger.error(msg, thrown);
		}
	}

	@Override
	public void log(Level level, String msg) {
		if (level.equals(Level.FINE) || level.equals(Level.FINER) || level.equals(Level.FINEST)) {
			logger.debug(msg);
		}

		if (level.equals(Level.INFO)) {
			logger.info(msg);
		}
		
		if (logger.isWarnEnabled()) {
			logger.warn(msg);
		}
		
		if (logger.isErrorEnabled()) {
			logger.error(msg);
		}
	}

	@Override
	public void severe(String msg) {
		logger.error(msg);
	}

	@Override
	public void throwing(String sourceClass, String sourceMethod, Throwable thrown) {
		logger.error(sourceClass + "." + sourceMethod, thrown);
	}

	@Override
	public void warning(String msg) {
		logger.warn(msg);
	}
}