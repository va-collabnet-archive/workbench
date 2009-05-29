package org.dwfa.ace.task.refset.members;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.task.refset.TaskLogger;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.easymock.IMocksControl;
import org.easymock.classextension.EasyMock;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WriteRefsetDescriptionsWithFailuresTest {

    private static final String DIRECTORY_KEY       = "mykey";
    private static final String EXCEPTION_MESSAGE   = "Exception message";

    private IMocksControl mockControl;
    private I_TermFactory mockTermFactory;
    private CleanableProcessExtByRefBuilder mockBuilder;
    private I_EncodeBusinessProcess mockProcess;
    private I_Work mockWork;
    private LocalVersionedTerminologyWrapper mockTerminologyWrapper;

    @Before
    public void setup() {
        mockControl = EasyMock.createControl();
        mockTerminologyWrapper = mockControl.createMock(LocalVersionedTerminologyWrapper.class);
        mockTermFactory = mockControl.createMock(I_TermFactory.class);
        mockBuilder = mockControl.createMock(CleanableProcessExtByRefBuilder.class);

        mockProcess = mockControl.createMock(I_EncodeBusinessProcess.class);
        mockWork = mockControl.createMock(I_Work.class);
        Logger mockLogger = mockControl.createMock(Logger.class);

        EasyMock.expect(mockWork.getLogger()).andReturn(mockLogger).atLeastOnce();
        EasyMock.expect(mockLogger.isLoggable(EasyMock.isA(Level.class))).andReturn(false).atLeastOnce();
    }

    @Test
    public void shouldExtendsAbstractTask() {
        assertTrue("WriteRefsetDescriptions should extend AbstractTask",
                AbstractTask.class.isAssignableFrom(WriteRefsetDescriptions.class));
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    @Test
    public void shouldThrowAnExceptionIfTheTasksFails() throws Exception {
        EasyMock.expect(mockProcess.readProperty(DIRECTORY_KEY)).andThrow(new IllegalArgumentException(EXCEPTION_MESSAGE));
        mockControl.replay();

        try {
            createTask().evaluate(mockProcess, mockWork);
            fail();
        } catch (TaskFailedException e) {
            assertThat(e.getCause(), notNullValue());
            assertTrue("Expected IllegalArgumentException.", e.getCause().getClass() == IllegalArgumentException.class);
            mockControl.verify();
        }
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    @Test
    public void shouldCloseOpenFilesIfATaskFailsAfterWritingFiles() throws Exception {
        EasyMock.expect(mockTerminologyWrapper.get()).andReturn(mockTermFactory);
        CleanableProcessExtByRef mockCleanableProcess = mockControl.createMock(CleanableProcessExtByRef.class);

        File directoryFile = new File("blah");
        EasyMock.expect(mockProcess.readProperty(DIRECTORY_KEY)).andReturn(directoryFile);
        expectThatCleanableProcessisBuilt(mockCleanableProcess, directoryFile);

        mockTermFactory.iterateExtByRefs(mockCleanableProcess);
        EasyMock.expectLastCall().andThrow(new IllegalStateException());
        mockCleanableProcess.clean();
        mockControl.replay();

        try {
            createTask().evaluate(mockProcess, mockWork);
            fail();
        } catch (TaskFailedException e) {
            mockControl.verify();
        }

        mockControl.verify();
    }

    private void expectThatCleanableProcessisBuilt(final CleanableProcessExtByRef mockCleanableProcess,
                                                   final File directoryFile) {
        EasyMock.expect(mockBuilder.withTermFactory(mockTermFactory)).andReturn(mockBuilder);
        EasyMock.expect(mockBuilder.withLogger(EasyMock.isA(TaskLogger.class))).andReturn(mockBuilder);
        EasyMock.expect(mockBuilder.withSelectedDir(directoryFile)).andReturn(mockBuilder);
        EasyMock.expect(mockBuilder.build()).andReturn(mockCleanableProcess);
    }    

    private WriteRefsetDescriptions createTask() {
        return new WriteRefsetDescriptions(DIRECTORY_KEY, mockTerminologyWrapper,
                mockBuilder);
    }    
}
