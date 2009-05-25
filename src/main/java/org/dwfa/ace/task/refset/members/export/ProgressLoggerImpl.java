package org.dwfa.ace.task.refset.members.export;

import org.dwfa.ace.task.util.Logger;

import java.util.HashMap;
import java.util.Map;

public final class ProgressLoggerImpl implements ProgressLogger {

    private final Map<String, Integer> progressMap;
    private final Logger logger;

    public ProgressLoggerImpl(final Logger logger) {
        this.logger = logger;
        progressMap = new HashMap<String, Integer>();
    }

    //for testing.
    ProgressLoggerImpl(final Logger logger, final Map<String, Integer> progressMap) {
        this.logger = logger;
        this.progressMap = progressMap;
    }

    public void logProgress(final String refsetName) {
        if (!progressMap.containsKey(refsetName)) {
            progressMap.put(refsetName, 0);
        } else {
            Integer prevProgress = progressMap.get(refsetName);
            progressMap.put(refsetName, ++prevProgress);
        }

        Integer progress = progressMap.get(refsetName);

        if (progress !=0 && progress % 1000 == 0) {
            logger.logInfo("Exported " + progress + " of refset " + refsetName);
        }
    }
}
