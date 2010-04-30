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
package org.dwfa.ace.task.refset.members.export;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.dwfa.ace.task.util.Logger;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

public final class ProgressLoggerImplTest {

    private static final String NEW_REFSET_NAME = "A new refset";
    private static final String REFSET_NAME = "Refset name";

    private IMocksControl mockControl;
    private Logger mockLogger;

    @Before
    public void setup() {
        mockControl = EasyMock.createControl();
        mockLogger = mockControl.createMock(Logger.class);
    }

    @Test
    public void shouldInitializeProgressWithZero() {
        mockControl.replay();

        Map<String, Integer> progressMap = new HashMap<String, Integer>();
        ProgressLogger progressLogger = new ProgressLoggerImpl(mockLogger, progressMap);
        progressLogger.logProgress(NEW_REFSET_NAME);
        assertThat(progressMap.get(NEW_REFSET_NAME), equalTo(0));

        mockControl.verify();
    }

    @Test
    public void shouldIncrementAnExistingRefsetProgress() {
        mockControl.replay();

        Map<String, Integer> progressMap = new HashMap<String, Integer>();
        progressMap.put(REFSET_NAME, 0);
        ProgressLogger progressLogger = new ProgressLoggerImpl(mockLogger, progressMap);
        progressLogger.logProgress(REFSET_NAME);
        assertThat(progressMap.get(REFSET_NAME), equalTo(1));

        mockControl.verify();
    }

    @Test
    public void shouldLogProgressEveryThousandthProgression() {
        mockLogger.logInfo("Exported 1000 of refset " + REFSET_NAME);
        mockControl.replay();

        Map<String, Integer> progressMap = new HashMap<String, Integer>();
        progressMap.put(REFSET_NAME, 999);
        ProgressLogger progressLogger = new ProgressLoggerImpl(mockLogger, progressMap);
        progressLogger.logProgress(REFSET_NAME);
        assertThat(progressMap.get(REFSET_NAME), equalTo(1000));

        mockControl.verify();
    }
}
