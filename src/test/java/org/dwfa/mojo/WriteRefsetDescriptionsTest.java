package org.dwfa.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.task.refset.members.CleanableProcessExtByRef;
import org.dwfa.ace.task.refset.members.CleanableProcessExtByRefBuilder;
import org.dwfa.ace.task.util.Logger;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

import java.io.File;

public final class WriteRefsetDescriptionsTest {

    private static final String ABSOLUTE_PATH_OF_TARGET_DIR = "blah";
    private static final String EXCEPTION_MESSAGE           = "An exception was thrown.";
    
    private IMocksControl mockControl;
    private I_TermFactory mockTermFactory;
    private CleanableProcessExtByRefBuilder mockCleanableProcessExtByRefBuilder;
    private MojoUtilWrapper mockMojoUtilWrapper;
    private File mockOutputDirectoryFile;
    private File mockTargetDirectoryFile;

    @Before
    public void setup() {
        mockControl = EasyMock.createControl();
        mockTermFactory = mockControl.createMock(I_TermFactory.class);
        mockCleanableProcessExtByRefBuilder = mockControl.createMock(CleanableProcessExtByRefBuilder.class);
        mockMojoUtilWrapper = mockControl.createMock(MojoUtilWrapper.class);
        mockOutputDirectoryFile = mockControl.createMock(File.class);
        mockTargetDirectoryFile = mockControl.createMock(File.class);

        EasyMock.expect(mockOutputDirectoryFile.getAbsolutePath()).andReturn(ABSOLUTE_PATH_OF_TARGET_DIR);
    }
    
    @Test
    public void shouldExportAllRefsets() throws Exception {
        CleanableProcessExtByRef mockProcessor = mockControl.createMock(CleanableProcessExtByRef.class);
        expectMojoToHaveRunBefore(false);
        EasyMock.expect(mockCleanableProcessExtByRefBuilder.withTermFactory(mockTermFactory)).
                andReturn(mockCleanableProcessExtByRefBuilder);
        EasyMock.expect(mockCleanableProcessExtByRefBuilder.withLogger(EasyMock.isA(Logger.class))).
                andReturn(mockCleanableProcessExtByRefBuilder);
        EasyMock.expect(mockCleanableProcessExtByRefBuilder.withSelectedDir(mockOutputDirectoryFile)).
                andReturn(mockCleanableProcessExtByRefBuilder);
        EasyMock.expect(mockCleanableProcessExtByRefBuilder.build()).andReturn(mockProcessor);
        mockTermFactory.iterateExtByRefs(mockProcessor);
        mockProcessor.clean();
        mockControl.replay();

        WriteRefsetDescriptions writer = new WriteRefsetDescriptions(mockOutputDirectoryFile, mockTermFactory,
                mockCleanableProcessExtByRefBuilder, mockTargetDirectoryFile, mockMojoUtilWrapper);
        writer.execute();

        mockControl.verify();
    }
    
    @Test
    public void shouldNotExportRefsetsIfTheExportHasRunPreviously() throws Exception {
        expectMojoToHaveRunBefore(true);
        mockControl.replay();
        
        WriteRefsetDescriptions writer = new WriteRefsetDescriptions(mockOutputDirectoryFile, mockTermFactory,
                mockCleanableProcessExtByRefBuilder, mockTargetDirectoryFile, mockMojoUtilWrapper);
        writer.execute();

        mockControl.verify();
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    @Test
    public void shouldCloseOpenFilesAndThrowAnExceptionIfTheExportFails() throws Exception {
        CleanableProcessExtByRef mockProcessor = mockControl.createMock(CleanableProcessExtByRef.class);
        expectMojoToHaveRunBefore(false);
        EasyMock.expect(mockCleanableProcessExtByRefBuilder.withTermFactory(mockTermFactory)).
                andReturn(mockCleanableProcessExtByRefBuilder);
        EasyMock.expect(mockCleanableProcessExtByRefBuilder.withLogger(EasyMock.isA(Logger.class))).
                andReturn(mockCleanableProcessExtByRefBuilder);
        EasyMock.expect(mockCleanableProcessExtByRefBuilder.withSelectedDir(mockOutputDirectoryFile)).
                andReturn(mockCleanableProcessExtByRefBuilder);
        EasyMock.expect(mockCleanableProcessExtByRefBuilder.build()).andReturn(mockProcessor);
        mockTermFactory.iterateExtByRefs(mockProcessor);
        EasyMock.expectLastCall().andThrow(new IllegalStateException(EXCEPTION_MESSAGE));
        mockProcessor.clean();
        mockControl.replay();

        WriteRefsetDescriptions writer = new WriteRefsetDescriptions(mockOutputDirectoryFile, mockTermFactory,
                mockCleanableProcessExtByRefBuilder, mockTargetDirectoryFile, mockMojoUtilWrapper);
        try {
            writer.execute();
            fail();
        } catch (MojoExecutionException e) {
            assertThat(e.getCause(), notNullValue());
            assertThat(e.getCause().getClass(), equalTo((Class) IllegalStateException.class));
            assertThat(e.getLocalizedMessage(), equalTo(EXCEPTION_MESSAGE));
            mockControl.verify();
        }
    }

    private void expectMojoToHaveRunBefore(final boolean runBefore) throws Exception {
        EasyMock.expect(mockMojoUtilWrapper.alreadyRun(
                EasyMock.isA(Log.class),
                EasyMock.eq(ABSOLUTE_PATH_OF_TARGET_DIR),
                EasyMock.isA(WriteRefsetDescriptions.class.getClass()),
                EasyMock.eq(mockTargetDirectoryFile))).andReturn(runBefore);
    }
}
