/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.mojo.refset.members.export;

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

    // for testing.
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

        if (progress != 0 && progress % 1000 == 0) {
            logger.logInfo("Exported " + progress + " of refset " + refsetName);
        }
    }
}
