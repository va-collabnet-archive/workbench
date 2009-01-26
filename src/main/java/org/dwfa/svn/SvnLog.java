package org.dwfa.svn;

import java.util.ResourceBundle;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.dwfa.vodb.VodbEnv;
import org.tigris.subversion.javahl.ClientException;

public class SvnLog {

		private static Logger logger = Logger.getLogger(SvnLog.class
				.getName());

		public static void addHandler(Handler arg0) throws SecurityException {
			logger.addHandler(arg0);
		}

		public static void config(String arg0) {
			logger.config(arg0);
		}

		public static void entering(String arg0, String arg1, Object arg2) {
			logger.entering(arg0, arg1, arg2);
		}

		public static void entering(String arg0, String arg1, Object[] arg2) {
			logger.entering(arg0, arg1, arg2);
		}

		public static void entering(String arg0, String arg1) {
			logger.entering(arg0, arg1);
		}

		public static void exiting(String arg0, String arg1, Object arg2) {
			logger.exiting(arg0, arg1, arg2);
		}

		public static void exiting(String arg0, String arg1) {
			logger.exiting(arg0, arg1);
		}

		public static void fine(String arg0) {
			logger.fine(arg0);
		}

		public static void finer(String arg0) {
			logger.finer(arg0);
		}

		public static void finest(String arg0) {
			logger.finest(arg0);
		}

		public static Filter getFilter() {
			return logger.getFilter();
		}

		public static Handler[] getHandlers() {
			return logger.getHandlers();
		}

		public static Level getLevel() {
			return logger.getLevel();
		}

		public static String getName() {
			return logger.getName();
		}

		public static Logger getParent() {
			return logger.getParent();
		}

		public static ResourceBundle getResourceBundle() {
			return logger.getResourceBundle();
		}

		public static String getResourceBundleName() {
			return logger.getResourceBundleName();
		}

		public static boolean getUseParentHandlers() {
			return logger.getUseParentHandlers();
		}

		public static void info(String arg0) {
			logger.info(arg0);
		}

		public static boolean isLoggable(Level arg0) {
			return logger.isLoggable(arg0);
		}

		public static void log(Level arg0, String arg1, Object arg2) {
			logger.log(arg0, arg1, arg2);
		}

		public static void log(Level arg0, String arg1, Object[] arg2) {
			logger.log(arg0, arg1, arg2);
		}

		public static void log(Level arg0, String arg1, Throwable arg2) {
			logger.log(arg0, arg1, arg2);
		}

		public static void log(Level arg0, String arg1) {
			logger.log(arg0, arg1);
		}

		public static void log(LogRecord arg0) {
			logger.log(arg0);
		}

		public static void logp(Level arg0, String arg1, String arg2, String arg3, Object arg4) {
			logger.logp(arg0, arg1, arg2, arg3, arg4);
		}

		public static void logp(Level arg0, String arg1, String arg2, String arg3, Object[] arg4) {
			logger.logp(arg0, arg1, arg2, arg3, arg4);
		}

		public static void logp(Level arg0, String arg1, String arg2, String arg3, Throwable arg4) {
			logger.logp(arg0, arg1, arg2, arg3, arg4);
		}

		public static void logp(Level arg0, String arg1, String arg2, String arg3) {
			logger.logp(arg0, arg1, arg2, arg3);
		}

		public static void logrb(Level arg0, String arg1, String arg2, String arg3, String arg4, Object arg5) {
			logger.logrb(arg0, arg1, arg2, arg3, arg4, arg5);
		}

		public static void logrb(Level arg0, String arg1, String arg2, String arg3, String arg4, Object[] arg5) {
			logger.logrb(arg0, arg1, arg2, arg3, arg4, arg5);
		}

		public static void logrb(Level arg0, String arg1, String arg2, String arg3, String arg4, Throwable arg5) {
			logger.logrb(arg0, arg1, arg2, arg3, arg4, arg5);
		}

		public static void logrb(Level arg0, String arg1, String arg2, String arg3, String arg4) {
			logger.logrb(arg0, arg1, arg2, arg3, arg4);
		}

		public static void removeHandler(Handler arg0) throws SecurityException {
			logger.removeHandler(arg0);
		}

		public static void setFilter(Filter arg0) throws SecurityException {
			logger.setFilter(arg0);
		}

		public static void setLevel(Level arg0) throws SecurityException {
			logger.setLevel(arg0);
		}

		public static void setParent(Logger arg0) {
			logger.setParent(arg0);
		}

		public static void setUseParentHandlers(boolean arg0) {
			logger.setUseParentHandlers(arg0);
		}

		public static void severe(String arg0) {
			logger.severe(arg0);
		}

		public static void throwing(String arg0, String arg1, Throwable arg2) {
			logger.throwing(arg0, arg1, arg2);
		}

		public static void warning(String arg0) {
			logger.warning(arg0);
		}

		public static void alertAndLog(ClientException e) {
			if (VodbEnv.headless == false) {
				JOptionPane.showMessageDialog(null,
					    "<html>" + e.getMessage() + 
					    "<br>See log for more details",
					    "Subversion Exception",
					    JOptionPane.ERROR_MESSAGE);
			}
			logger.log(Level.SEVERE, e.getMessage(), e);
			
		}

}
