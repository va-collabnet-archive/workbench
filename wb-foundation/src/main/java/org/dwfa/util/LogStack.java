package org.dwfa.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

public class LogStack {

	public static void logException(Logger logger, Throwable ex) {
		StringWriter sw = new StringWriter();
		ex.printStackTrace(new PrintWriter(sw));
		String stacktrace = sw.toString();
		logger.severe(stacktrace);
		}
	
}
