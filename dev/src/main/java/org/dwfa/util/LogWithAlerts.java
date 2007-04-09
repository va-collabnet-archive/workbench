package org.dwfa.util;

import java.awt.Component;
import java.util.ResourceBundle;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

public class LogWithAlerts {
	Logger logger;
	
	public LogWithAlerts(String logName) {
		super();
		logger = Logger.getLogger(logName);
	}

	protected Logger getLogger() {
		return logger;
	}

	public void alertAndLogException(Component parent, Throwable ex) {
		alertAndLog(parent, Level.SEVERE, ex.getLocalizedMessage(), ex);
	}
	public void alertAndLogException(Throwable ex) {
		alertAndLog(null, Level.SEVERE, ex.getLocalizedMessage(), ex);
	}
	public void alertAndLog(Level level, String message, Throwable ex) {
		alertAndLog(null, level, message, ex);
	}
	public void alertAndLog(Component parent, Level level, String message, Throwable ex) {
		getLogger().log(level, message, ex);
		message = "<html>" + message;
		if (level.intValue() <= Level.INFO.intValue()) {
			JOptionPane.showMessageDialog(parent,
					message,
				    "Information has been logged",
				    JOptionPane.INFORMATION_MESSAGE);
		} else if (level.intValue() <= Level.WARNING.intValue()) {
			JOptionPane.showMessageDialog(parent,
					message,
				    "A warning has been logged",
				    JOptionPane.WARNING_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(parent,
					message,
				    "An error has been logged",
				    JOptionPane.ERROR_MESSAGE);
		} 
	}
	public void addHandler(Handler arg0) throws SecurityException {
		getLogger().addHandler(arg0);
	}

	public void config(String arg0) {
		getLogger().config(arg0);
	}

	public void entering(String arg0, String arg1, Object arg2) {
		getLogger().entering(arg0, arg1, arg2);
	}

	public void entering(String arg0, String arg1, Object[] arg2) {
		getLogger().entering(arg0, arg1, arg2);
	}

	public void entering(String arg0, String arg1) {
		getLogger().entering(arg0, arg1);
	}


	public void exiting(String arg0, String arg1, Object arg2) {
		getLogger().exiting(arg0, arg1, arg2);
	}

	public void exiting(String arg0, String arg1) {
		getLogger().exiting(arg0, arg1);
	}

	public void fine(String arg0) {
		getLogger().fine(arg0);
	}

	public void finer(String arg0) {
		getLogger().finer(arg0);
	}

	public void finest(String arg0) {
		getLogger().finest(arg0);
	}

	public Filter getFilter() {
		return getLogger().getFilter();
	}

	public Handler[] getHandlers() {
		return getLogger().getHandlers();
	}

	public Level getLevel() {
		return getLogger().getLevel();
	}

	public String getName() {
		return getLogger().getName();
	}

	public Logger getParent() {
		return getLogger().getParent();
	}

	public ResourceBundle getResourceBundle() {
		return getLogger().getResourceBundle();
	}

	public String getResourceBundleName() {
		return getLogger().getResourceBundleName();
	}

	public boolean getUseParentHandlers() {
		return getLogger().getUseParentHandlers();
	}

	public void info(String arg0) {
		getLogger().info(arg0);
	}

	public boolean isLoggable(Level arg0) {
		return getLogger().isLoggable(arg0);
	}

	public void log(Level arg0, String arg1, Object arg2) {
		getLogger().log(arg0, arg1, arg2);
	}

	public void log(Level arg0, String arg1, Object[] arg2) {
		getLogger().log(arg0, arg1, arg2);
	}

	public void log(Level arg0, String arg1, Throwable arg2) {
		getLogger().log(arg0, arg1, arg2);
	}

	public void log(Level arg0, String arg1) {
		getLogger().log(arg0, arg1);
	}

	public void log(LogRecord arg0) {
		getLogger().log(arg0);
	}

	public void logp(Level arg0, String arg1, String arg2, String arg3, Object arg4) {
		getLogger().logp(arg0, arg1, arg2, arg3, arg4);
	}

	public void logp(Level arg0, String arg1, String arg2, String arg3, Object[] arg4) {
		getLogger().logp(arg0, arg1, arg2, arg3, arg4);
	}

	public void logp(Level arg0, String arg1, String arg2, String arg3, Throwable arg4) {
		getLogger().logp(arg0, arg1, arg2, arg3, arg4);
	}

	public void logp(Level arg0, String arg1, String arg2, String arg3) {
		getLogger().logp(arg0, arg1, arg2, arg3);
	}

	public void logrb(Level arg0, String arg1, String arg2, String arg3, String arg4, Object arg5) {
		getLogger().logrb(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	public void logrb(Level arg0, String arg1, String arg2, String arg3, String arg4, Object[] arg5) {
		getLogger().logrb(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	public void logrb(Level arg0, String arg1, String arg2, String arg3, String arg4, Throwable arg5) {
		getLogger().logrb(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	public void logrb(Level arg0, String arg1, String arg2, String arg3, String arg4) {
		getLogger().logrb(arg0, arg1, arg2, arg3, arg4);
	}

	public void removeHandler(Handler arg0) throws SecurityException {
		getLogger().removeHandler(arg0);
	}

	public void setFilter(Filter arg0) throws SecurityException {
		getLogger().setFilter(arg0);
	}

	public void setLevel(Level arg0) throws SecurityException {
		getLogger().setLevel(arg0);
	}

	public void setParent(Logger arg0) {
		getLogger().setParent(arg0);
	}

	public void setUseParentHandlers(boolean arg0) {
		getLogger().setUseParentHandlers(arg0);
	}

	public void severe(String arg0) {
		getLogger().severe(arg0);
	}

	public void throwing(String arg0, String arg1, Throwable arg2) {
		getLogger().throwing(arg0, arg1, arg2);
	}

	public void warning(String arg0) {
		getLogger().warning(arg0);
	}

}
