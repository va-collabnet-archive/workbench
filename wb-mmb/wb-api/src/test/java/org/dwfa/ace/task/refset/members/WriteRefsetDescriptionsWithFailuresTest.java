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
import static org.junit.Assert.fail;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.task.refset.TaskLogger;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.easymock.IMocksControl;
import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

public final class WriteRefsetDescriptionsWithFailuresTest {

    private static final String DIRECTORY_KEY = "mykey";
    private static final String EXCEPTION_MESSAGE = "Exception message";

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

    @Test
    public void shouldHaveTheCorrectBeanAnnotations() {
        BeanList beanListAnnotation = WriteRefsetDescriptions.class.getAnnotation(BeanList.class);
        assertThat(beanListAnnotation, notNullValue());
        Spec[] specs = beanListAnnotation.specs();

        assertThat(specs.length, equalTo(1));
        Spec spec = specs[0];
        assertThat(spec.directory(), equalTo("tasks/ide/refset/membership"));
        assertThat(spec.type(), equalTo(BeanType.TASK_BEAN));
    }

    @Test
    public void shouldThrowAnExceptionIfTheTasksFails() throws Exception {
        EasyMock.expect(mockProcess.getProperty(DIRECTORY_KEY)).andThrow(
            new IllegalArgumentException(EXCEPTION_MESSAGE));
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

    @SuppressWarnings( { "ThrowableInstanceNeverThrown" })
    @Test
    public void shouldCloseOpenFilesIfATaskFailsAfterWritingFiles() throws Exception {
        EasyMock.expect(mockTerminologyWrapper.get()).andReturn(mockTermFactory);
        CleanableProcessExtByRef mockCleanableProcess = mockControl.createMock(CleanableProcessExtByRef.class);

        File directoryFile = new File("blah");
        EasyMock.expect(mockProcess.getProperty(DIRECTORY_KEY)).andReturn(directoryFile);
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
        return new WriteRefsetDescriptions(DIRECTORY_KEY, mockTerminologyWrapper, mockBuilder);
    }
}
