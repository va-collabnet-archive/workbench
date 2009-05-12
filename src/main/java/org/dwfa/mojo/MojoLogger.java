package org.dwfa.mojo;

import org.dwfa.ace.task.util.Logger;
import org.apache.maven.plugin.logging.Log;

public final class MojoLogger implements Logger {

    private final Log log;

    public MojoLogger(final Log log) {
        this.log = log;
    }

    public void logInfo(final String message) {
        if (log.isInfoEnabled()) {
            log.info(message);
        }
    }

    public void logWarn(final String message) {
        if (log.isWarnEnabled()) {
            log.warn(message);
        }
    }
}
