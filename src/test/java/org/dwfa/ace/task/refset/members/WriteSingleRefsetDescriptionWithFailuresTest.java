package org.dwfa.ace.task.refset.members;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.task.util.Logger;
import org.dwfa.bpa.process.I_DefineTask;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public final class WriteSingleRefsetDescriptionWithFailuresTest {

    private static final String WORKING_REFSET_KEY  = "REFSET_KEY";
    private static final String DIRECTORY_KEY       = "DIR_KEY";

    private IMocksControl mockControl;
    private I_TermFactory mockTermFactory;
    private PropertyValidator mockPropertyValidator;
    private LocalVersionedTerminologyWrapper mockTerminologyWrapper;

    @Before
    public void setup() {
        mockControl = EasyMock.createControl();
        mockTermFactory = mockControl.createMock(I_TermFactory.class);
        mockPropertyValidator = mockControl.createMock(PropertyValidator.class);

        mockTerminologyWrapper = mockControl.createMock(LocalVersionedTerminologyWrapper.class);        
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    @Test
    public void shouldRethrowAnExceptionIfTheTaskFails() throws Exception {
        I_EncodeBusinessProcess mockBusinessProcess = mockControl.createMock(I_EncodeBusinessProcess.class);
        I_Work mockWork = mockControl.createMock(I_Work.class);
        CleanableProcessExtByRefBuilder mockCleanableProcessBuilder =
                mockControl.createMock(CleanableProcessExtByRefBuilder.class);
        EasyMock.expect(mockBusinessProcess.readProperty(DIRECTORY_KEY)).andThrow(new IllegalArgumentException());
        mockControl.replay();

        I_DefineTask task = createTask(mockCleanableProcessBuilder);
        try {
            task.evaluate(mockBusinessProcess, mockWork);
            fail();
        } catch (TaskFailedException e) {

            assertThat(e.getCause(), notNullValue());
            assertTrue("Expected IllegalArgumentException. Got " + e.getCause(),
                    e.getCause().getClass() == IllegalArgumentException.class);
            mockControl.verify();
        }
    }

    @Test
    public void shouldCleanupResourcesIfAProcessorFails() throws Exception {
        I_EncodeBusinessProcess mockBusinessProcess = mockControl.createMock(I_EncodeBusinessProcess.class);
        I_Work mockWork = mockControl.createMock(I_Work.class);
        CleanableProcessExtByRef mockCleanableProcess = mockControl.createMock(CleanableProcessExtByRef.class);
        CleanableProcessExtByRefBuilder mockCleanableProcessBuilder =
                mockControl.createMock(CleanableProcessExtByRefBuilder.class);

        expectEvaluateCalled(mockBusinessProcess, mockCleanableProcess, mockCleanableProcessBuilder);
        mockControl.replay();

        I_DefineTask task = createTask(mockCleanableProcessBuilder);
        try {
            task.evaluate(mockBusinessProcess, mockWork);
            fail();
        } catch (TaskFailedException e) {
            assertThat(e.getCause(), notNullValue());
            assertTrue("Expected IllegalStateException. Got " + e.getCause(),
                    e.getCause().getClass() == IllegalStateException.class);
            mockControl.verify();
        }
    }

    private WriteSingleRefsetDescription createTask(final CleanableProcessExtByRefBuilder mockCleanableProcessBuilder) {
        return new WriteSingleRefsetDescription(WORKING_REFSET_KEY, mockCleanableProcessBuilder,
                DIRECTORY_KEY, mockPropertyValidator, mockTerminologyWrapper);
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    private void expectEvaluateCalled(final I_EncodeBusinessProcess mockBusinessProcess,
                                      final CleanableProcessExtByRef mockCleanableProcess,
                                      final CleanableProcessExtByRefBuilder mockCleanableProcessBuilder)
            throws Exception {

        File mockDirectoryFile = mockControl.createMock(File.class);
        I_ThinExtByRefVersioned mockRefset = mockControl.createMock(I_ThinExtByRefVersioned.class);

        EasyMock.expect(mockTerminologyWrapper.get()).andReturn(mockTermFactory);
        EasyMock.expect(mockBusinessProcess.readProperty(DIRECTORY_KEY)).andReturn(mockDirectoryFile);
        EasyMock.expect(mockBusinessProcess.readProperty(WORKING_REFSET_KEY)).andReturn(mockRefset);
        mockPropertyValidator.validate(mockDirectoryFile, "output directory");
        mockPropertyValidator.validate(mockRefset, "selected refset");        

        EasyMock.expect(mockCleanableProcessBuilder.withTermFactory(mockTermFactory)).
                andReturn(mockCleanableProcessBuilder);
        EasyMock.expect(mockCleanableProcessBuilder.withSelectedDir(mockDirectoryFile)).
                andReturn(mockCleanableProcessBuilder);
        EasyMock.expect(mockCleanableProcessBuilder.withLogger(EasyMock.isA(Logger.class))).
                andReturn(mockCleanableProcessBuilder);
        EasyMock.expect(mockCleanableProcessBuilder.build()).andReturn(mockCleanableProcess);

        mockCleanableProcess.processExtensionByReference(mockRefset);
        EasyMock.expectLastCall().andThrow(new IllegalStateException());

        mockCleanableProcess.clean();
    }
}
