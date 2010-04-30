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
package org.dwfa.ace.task.refset.members;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
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
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public final class WriteSingleRefsetDescriptionTest {

    private static final String WORKING_REFSET_KEY = "REFSET_KEY";
    private static final String DIRECTORY_KEY = "DIR_KEY";
    private static final int REFSET_CONCEPT_ID = 1200;

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
    @Ignore
    public void shouldExtendAbstractTask() {
        assertTrue("WriteSingleRefsetDescription should extend AbstractTask",
            AbstractTask.class.isAssignableFrom(WriteSingleRefsetDescription.class));
    }

    @Test
    @Ignore
    public void shouldEvaluateABusinessProcess() throws Exception {
        I_EncodeBusinessProcess mockBusinessProcess = mockControl.createMock(I_EncodeBusinessProcess.class);
        CleanableProcessExtByRef mockCleanableProcess = mockControl.createMock(CleanableProcessExtByRef.class);
        CleanableProcessExtByRefBuilder mockCleanableProcessBuilder = mockControl.createMock(CleanableProcessExtByRefBuilder.class);
        PropertyValidator mockPropertyValidator = mockControl.createMock(PropertyValidator.class);
        I_Work mockWork = mockControl.createMock(I_Work.class);

        expectEvaluateCalled(mockBusinessProcess, mockCleanableProcess, mockCleanableProcessBuilder,
            mockPropertyValidator);

        mockControl.replay();

        I_DefineTask task = new WriteSingleRefsetDescription(WORKING_REFSET_KEY, mockCleanableProcessBuilder,
            DIRECTORY_KEY, mockPropertyValidator, mockTerminologyWrapper);
        Condition condition = task.evaluate(mockBusinessProcess, mockWork);
        assertThat(condition, equalTo(Condition.CONTINUE));

        mockControl.verify();
    }

    @Test
    @Ignore
    public void shouldReturnTheExpectedConditions() {
        CleanableProcessExtByRefBuilder mockCleanableProcessBuilder = mockControl.createMock(CleanableProcessExtByRefBuilder.class);
        PropertyValidator mockPropertyValidator = mockControl.createMock(PropertyValidator.class);
        mockControl.replay();

        I_DefineTask task = new WriteSingleRefsetDescription(WORKING_REFSET_KEY, mockCleanableProcessBuilder,
            DIRECTORY_KEY, mockPropertyValidator, mockTerminologyWrapper);
        assertThat(task.getConditions(), equalTo((Collection) Arrays.asList(Condition.CONTINUE)));

        mockControl.verify();
    }

    @Test
    @Ignore
    public void shouldReturnZeroContainerIds() {
        CleanableProcessExtByRefBuilder mockCleanableProcessBuilder = mockControl.createMock(CleanableProcessExtByRefBuilder.class);
        PropertyValidator mockPropertyValidator = mockControl.createMock(PropertyValidator.class);
        mockControl.replay();

        I_DefineTask task = new WriteSingleRefsetDescription(WORKING_REFSET_KEY, mockCleanableProcessBuilder,
            DIRECTORY_KEY, mockPropertyValidator, mockTerminologyWrapper);
        int[] containerIds = task.getDataContainerIds();
        assertThat(containerIds.length, equalTo(0));
    }

    @Test
    @Ignore
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
            final PropertyValidator mockPropertyValidator) throws Exception {

        File mockDirectoryFile = mockControl.createMock(File.class);
        I_GetConceptData mockRefset = mockControl.createMock(I_GetConceptData.class);

        EasyMock.expect(mockTerminologyWrapper.get()).andReturn(mockTermFactory);
        EasyMock.expect(mockBusinessProcess.getProperty(DIRECTORY_KEY)).andReturn(mockDirectoryFile);
        EasyMock.expect(mockBusinessProcess.getProperty(WORKING_REFSET_KEY)).andReturn(mockRefset);
        mockPropertyValidator.validate(mockDirectoryFile, "output directory");
        mockPropertyValidator.validate(mockRefset, "selected refset");

        EasyMock.expect(mockCleanableProcessBuilder.withTermFactory(mockTermFactory)).andReturn(
            mockCleanableProcessBuilder);
        EasyMock.expect(mockCleanableProcessBuilder.withSelectedDir(mockDirectoryFile)).andReturn(
            mockCleanableProcessBuilder);
        EasyMock.expect(mockCleanableProcessBuilder.withLogger(EasyMock.isA(Logger.class))).andReturn(
            mockCleanableProcessBuilder);
        EasyMock.expect(mockCleanableProcessBuilder.build()).andReturn(mockCleanableProcess);
        EasyMock.expect(mockRefset.getConceptId()).andReturn(REFSET_CONCEPT_ID);

        I_ExtendByRef mockExt1 = mockControl.createMock(I_ExtendByRef.class);
        I_ExtendByRef mockExt2 = mockControl.createMock(I_ExtendByRef.class);
        // TODO fix this one EasyMock.expect(mockTermFactory.getRefsetExtensionMembers(REFSET_CONCEPT_ID)).andReturn(Arrays.asList(mockExt1, mockExt2));

        mockCleanableProcess.processExtensionByReference(mockExt1);
        mockCleanableProcess.processExtensionByReference(mockExt2);

        mockCleanableProcess.clean();
    }
}
