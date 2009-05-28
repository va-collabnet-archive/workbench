package org.dwfa.ace.task.refset.members;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.task.util.Logger;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_DefineTask;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

public final class WriteSingleRefsetDescriptionTest {

    private static final String WORKING_REFSET_KEY  = "REFSET_KEY";
    private static final String DIRECTORY_KEY       = "DIR_KEY";

    private IMocksControl mockControl;
    private LocalVersionedTerminologyWrapper mockTerminologyWrapper;
    private I_TermFactory mockTermFactory;

    @Before
    public void setup() {
        mockControl = EasyMock.createControl();
        mockTermFactory = mockControl.createMock(I_TermFactory.class);
        mockTerminologyWrapper = mockControl.createMock(LocalVersionedTerminologyWrapper.class);
    }
    
    @Test
    public void shouldExtendAbstractTask() {
        assertTrue("WriteSingleRefsetDescription should extend AbstractTask",
                AbstractTask.class.isAssignableFrom(WriteSingleRefsetDescription.class));
    }

    @Test
    public void shouldEvaluateABusinessProcess() throws Exception {
        I_EncodeBusinessProcess mockBusinessProcess = mockControl.createMock(I_EncodeBusinessProcess.class);
        CleanableProcessExtByRef mockCleanableProcess = mockControl.createMock(CleanableProcessExtByRef.class);
        CleanableProcessExtByRefBuilder mockCleanableProcessBuilder =
                mockControl.createMock(CleanableProcessExtByRefBuilder.class);
        PropertyValidator mockPropertyValidator = mockControl.createMock(PropertyValidator.class);
        I_Work mockWork = mockControl.createMock(I_Work.class);

        expectEvaluateCalled(mockBusinessProcess, mockCleanableProcess,  mockCleanableProcessBuilder,
                mockPropertyValidator);

        mockControl.replay();

        I_DefineTask task = new WriteSingleRefsetDescription(WORKING_REFSET_KEY, mockCleanableProcessBuilder,
                DIRECTORY_KEY, mockPropertyValidator, mockTerminologyWrapper);
        Condition condition = task.evaluate(mockBusinessProcess, mockWork);
        assertThat(condition, equalTo(Condition.CONTINUE));

        mockControl.verify();
    }

    @Test
    public void shouldReturnTheExpectedConditions() {
        CleanableProcessExtByRefBuilder mockCleanableProcessBuilder =
                mockControl.createMock(CleanableProcessExtByRefBuilder.class);
        PropertyValidator mockPropertyValidator = mockControl.createMock(PropertyValidator.class);
        mockControl.replay();

        I_DefineTask task = new WriteSingleRefsetDescription(WORKING_REFSET_KEY, mockCleanableProcessBuilder,
                DIRECTORY_KEY, mockPropertyValidator, mockTerminologyWrapper);
        assertThat(task.getConditions(), equalTo((Collection) Arrays.asList(Condition.CONTINUE)));

        mockControl.verify();
    }

    @Test
    public void shouldReturnZeroContainerIds() {
        CleanableProcessExtByRefBuilder mockCleanableProcessBuilder =
                mockControl.createMock(CleanableProcessExtByRefBuilder.class);
        PropertyValidator mockPropertyValidator = mockControl.createMock(PropertyValidator.class);
        mockControl.replay();

        I_DefineTask task = new WriteSingleRefsetDescription(WORKING_REFSET_KEY,
                mockCleanableProcessBuilder, DIRECTORY_KEY, mockPropertyValidator, mockTerminologyWrapper);
        int[] containerIds = task.getDataContainerIds();
        assertThat(containerIds.length, equalTo(0));
    }

    @Test
    public void shouldHaveTheCorrectBeanAnnotations() {
        BeanList beanListAnnotation = WriteSingleRefsetDescription.class.getAnnotation(BeanList.class);
        assertThat(beanListAnnotation, notNullValue());
        Spec[] specs = beanListAnnotation.specs();

        assertThat(specs.length, equalTo(1));
        Spec spec = specs[0];
        assertThat(spec.directory(), equalTo("tasks/ide/refset/membership"));
        assertThat(spec.type(), equalTo(BeanType.TASK_BEAN));
    }

    private void expectEvaluateCalled(final I_EncodeBusinessProcess mockBusinessProcess,
                                      final CleanableProcessExtByRef mockCleanableProcess,
                                      final CleanableProcessExtByRefBuilder mockCleanableProcessBuilder,
                                      final PropertyValidator mockPropertyValidator)
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
        mockCleanableProcess.clean();
    }
}
