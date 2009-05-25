package org.dwfa.ace.task.refset.members.export;

import org.dwfa.ace.task.util.Logger;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import java.util.HashMap;
import java.util.Map;

public final class ProgressLoggerImplTest {

    private static final String NEW_REFSET_NAME = "A new refset";
    private static final String REFSET_NAME     = "Refset name";
    
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

        Map<String,Integer> progressMap = new HashMap<String, Integer>();
        ProgressLogger progressLogger = new ProgressLoggerImpl(mockLogger, progressMap);
        progressLogger.logProgress(NEW_REFSET_NAME);
        assertThat(progressMap.get(NEW_REFSET_NAME), equalTo(0));

        mockControl.verify();
    }
    
    @Test
    public void shouldIncrementAnExistingRefsetProgress() {
        mockControl.replay();

        Map<String,Integer> progressMap = new HashMap<String, Integer>();
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

        Map<String,Integer> progressMap = new HashMap<String, Integer>();
        progressMap.put(REFSET_NAME, 999);
        ProgressLogger progressLogger = new ProgressLoggerImpl(mockLogger, progressMap);
        progressLogger.logProgress(REFSET_NAME);
        assertThat(progressMap.get(REFSET_NAME), equalTo(1000));

        mockControl.verify();
    }
}
