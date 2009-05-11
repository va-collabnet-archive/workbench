package org.dwfa.ace.task.util;

public final class SimpleLogMill implements LogMill {

    private static final long serialVersionUID = 2220865588987023380L;

    public void logInfo(final Logger logger, final String message) {
        logger.logInfo(message);
    }

    public void logWarn(final Logger logger, final String message) {
        logger.logWarn(message);
    }    

}
