package org.dwfa.ace.task.refset.members;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.task.refset.TaskLogger;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.easymock.IMocksControl;
import org.easymock.classextension.EasyMock;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WriteRefsetDescriptionsTest {

    private static final String DIRECTORY_KEY       = "mykey";
    private static final String EXCEPTION_MESSAGE   = "Exception message";

    private IMocksControl mockControl;
    private I_TermFactory mockTermFactory;
    private CleanableProcessExtByRefBuilder mockBuilder;
    private I_EncodeBusinessProcess mockProcess;
    private I_Work mockWork;

    @Before
    public void setup() {
        mockControl = EasyMock.createControl();
        mockTermFactory = mockControl.createMock(I_TermFactory.class);
        mockBuilder = mockControl.createMock(CleanableProcessExtByRefBuilder.class);

        mockProcess = mockControl.createMock(I_EncodeBusinessProcess.class);
        mockWork = mockControl.createMock(I_Work.class);
        Logger mockLogger = mockControl.createMock(Logger.class);

        EasyMock.expect(mockWork.getLogger()).andReturn(mockLogger).atLeastOnce();
        EasyMock.expect(mockLogger.isLoggable(EasyMock.isA(Level.class))).andReturn(false).atLeastOnce();        
    }

    @Test
    public void shouldRunAValidTask() throws Exception {
        CleanableProcessExtByRef mockCleanableProcess = mockControl.createMock(CleanableProcessExtByRef.class);
        File directoryFile = new File("blah");
        EasyMock.expect(mockProcess.readProperty(DIRECTORY_KEY)).andReturn(directoryFile);
        expectThatCleanableProcessisBuilt(mockCleanableProcess, directoryFile);

        mockTermFactory.iterateExtByRefs(mockCleanableProcess);
        mockCleanableProcess.clean();
        mockControl.replay();

        WriteRefsetDescriptions writer = new WriteRefsetDescriptions(DIRECTORY_KEY, mockTermFactory,
                mockBuilder);
        Condition condition = writer.evaluate(mockProcess, mockWork);
        assertThat(condition, equalTo(Condition.CONTINUE));

        mockControl.verify();
    }
    
    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    @Test
    public void shouldThrowAnExceptionIfTheTasksFails() throws Exception {
        EasyMock.expect(mockProcess.readProperty(DIRECTORY_KEY)).andThrow(new IllegalArgumentException(EXCEPTION_MESSAGE));
        mockControl.replay();

        try {
            new WriteRefsetDescriptions(DIRECTORY_KEY, mockTermFactory, mockBuilder).evaluate(mockProcess, mockWork);
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
        CleanableProcessExtByRef mockCleanableProcess = mockControl.createMock(CleanableProcessExtByRef.class);

        File directoryFile = new File("blah");
        EasyMock.expect(mockProcess.readProperty(DIRECTORY_KEY)).andReturn(directoryFile);
        expectThatCleanableProcessisBuilt(mockCleanableProcess, directoryFile);

        mockTermFactory.iterateExtByRefs(mockCleanableProcess);
        EasyMock.expectLastCall().andThrow(new IllegalStateException());
        mockCleanableProcess.clean();
        mockControl.replay();

        try {
            new WriteRefsetDescriptions(DIRECTORY_KEY, mockTermFactory, mockBuilder).evaluate(mockProcess, mockWork);
            fail();
        } catch (TaskFailedException e) {
            mockControl.verify();            
        }

        mockControl.verify();
    }
    
    @Test
    public void shouldReturnExpectedConditions() {
        Collection<Condition> conditions = new WriteRefsetDescriptions(DIRECTORY_KEY, mockTermFactory,
                mockBuilder).getConditions();

        assertThat(conditions, notNullValue());
        assertThat(conditions.size(), equalTo(1));
        assertThat(conditions.iterator().next(), equalTo(Condition.CONTINUE));
    }

    public void shouldReturnZeroContainerIds() {
        int[] containerIds = new WriteRefsetDescriptions(DIRECTORY_KEY, mockTermFactory,
                mockBuilder).getDataContainerIds();

        assertThat(containerIds.length, equalTo(0));
    }

    private void expectThatCleanableProcessisBuilt(final CleanableProcessExtByRef mockCleanableProcess,
                                                   final File directoryFile) {
        EasyMock.expect(mockBuilder.withTermFactory(mockTermFactory)).andReturn(mockBuilder);
        EasyMock.expect(mockBuilder.withLogger(EasyMock.isA(TaskLogger.class))).andReturn(mockBuilder);
        EasyMock.expect(mockBuilder.withSelectedDir(directoryFile)).andReturn(mockBuilder);
        EasyMock.expect(mockBuilder.build()).andReturn(mockCleanableProcess);
    }
}
