package org.dwfa.mojo;

import org.apache.maven.plugin.logging.Log;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.task.refset.members.CleanableProcessExtByRef;
import org.dwfa.ace.task.refset.members.CleanableProcessExtByRefBuilder;
import org.dwfa.ace.task.util.Logger;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public final class WriteRefsetDescriptionsTest {

    private static final String ABSOLUTE_PATH_OF_TARGET_DIR = "blah";

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
    }
    
    @Test
    public void shouldExportAllRefsets() throws Exception {
        CleanableProcessExtByRef mockProcessor = mockControl.createMock(CleanableProcessExtByRef.class);
        EasyMock.expect(mockTargetDirectoryFile.getAbsolutePath()).andReturn(ABSOLUTE_PATH_OF_TARGET_DIR);
        EasyMock.expect(mockMojoUtilWrapper.alreadyRun(
                                                        EasyMock.isA(Log.class),
                                                        EasyMock.eq(ABSOLUTE_PATH_OF_TARGET_DIR),
                                                        EasyMock.isA(WriteRefsetDescriptions.class.getClass()),
                                                        EasyMock.eq(mockOutputDirectoryFile))).andReturn(false);
        EasyMock.expect(mockCleanableProcessExtByRefBuilder.withTermFactory(mockTermFactory)).
                andReturn(mockCleanableProcessExtByRefBuilder);
        EasyMock.expect(mockCleanableProcessExtByRefBuilder.withLogger(EasyMock.isA(Logger.class))).
                andReturn(mockCleanableProcessExtByRefBuilder);
        EasyMock.expect(mockCleanableProcessExtByRefBuilder.withSelectedDir(mockTargetDirectoryFile)).
                andReturn(mockCleanableProcessExtByRefBuilder);
        EasyMock.expect(mockCleanableProcessExtByRefBuilder.build()).andReturn(mockProcessor);
        mockTermFactory.iterateExtByRefs(mockProcessor);
        mockProcessor.clean();
        mockControl.replay();

        WriteRefsetDescriptions writer = new WriteRefsetDescriptions(mockTargetDirectoryFile, mockTermFactory,
                mockCleanableProcessExtByRefBuilder, mockOutputDirectoryFile,
                mockMojoUtilWrapper);
        writer.execute();

        mockControl.verify();
    }
}
