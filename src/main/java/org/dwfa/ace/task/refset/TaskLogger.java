package org.dwfa.ace.task.refset;

import org.dwfa.bpa.process.I_Work;
import org.dwfa.ace.task.util.Logger;

import java.util.logging.Level;

public final class TaskLogger implements Logger {

    private final I_Work i_work;

    public TaskLogger(final I_Work i_work) {
        this.i_work = i_work;
    }

    public void logInfo(final String message) {
        if (i_work.getLogger().isLoggable(Level.INFO)) {
            i_work.getLogger().info(message);
        }
    }

    public void logWarn(final String message) {
        if (i_work.getLogger().isLoggable(Level.WARNING)) {
            i_work.getLogger().warning(message);
        }
    }
}
