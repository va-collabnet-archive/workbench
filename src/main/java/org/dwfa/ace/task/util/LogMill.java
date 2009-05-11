package org.dwfa.ace.task.util;

import java.io.Serializable;

public interface LogMill extends Serializable {

    void logInfo(Logger logger, String message);

    void logWarn(Logger logger, String message);
}
