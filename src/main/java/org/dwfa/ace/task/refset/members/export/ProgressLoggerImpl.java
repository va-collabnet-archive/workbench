package org.dwfa.ace.task.refset.members.export;

import org.dwfa.ace.task.util.Logger;

import java.util.HashMap;
import java.util.Map;

//TODO: Test
public final class ProgressLoggerImpl implements ProgressLogger {

    private final Map<String, Integer> progressMap;
    private final Logger logger;

    public ProgressLoggerImpl(final Logger logger) {
        this.logger = logger;
        progressMap = new HashMap<String, Integer>();
    }

    public void logProgress(final String refsetName) {
        Integer progress = progressMap.get(refsetName);
        if (progress == null) {
            progress = 0;
        }

        progressMap.put(refsetName, progress++);

        if (progress % 1000 == 0) {
            logger.logInfo("Exported " + progress + " of refset " + refsetName);
        }
    }
}
