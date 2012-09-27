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

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.task.util.Logger;
import org.dwfa.bpa.process.I_DefineTask;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public final class WriteSingleRefsetDescriptionWithFailuresTest {

    private static final String WORKING_REFSET_KEY = "REFSET_KEY";
    private static final String DIRECTORY_KEY = "DIR_KEY";
    private static final int REFSET_CONCEPT_ID = 2345;

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

    @SuppressWarnings( { "ThrowableInstanceNeverThrown" })
    @Test
    @Ignore
    public void shouldRethrowAnExceptionIfTheTaskFails() throws Exception {
        I_EncodeBusinessProcess mockBusinessProcess = mockControl.createMock(I_EncodeBusinessProcess.class);
        I_Work mockWork = mockControl.createMock(I_Work.class);
        CleanableProcessExtByRefBuilder mockCleanableProcessBuilder = mockControl.createMock(CleanableProcessExtByRefBuilder.class);
        EasyMock.expect(mockBusinessProcess.getProperty(DIRECTORY_KEY)).andThrow(new IllegalArgumentException());
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
    @Ignore
    public void shouldCleanupResourcesIfAProcessorFails() throws Exception {
        I_EncodeBusinessProcess mockBusinessProcess = mockControl.createMock(I_EncodeBusinessProcess.class);
        I_Work mockWork = mockControl.createMock(I_Work.class);
        CleanableProcessExtByRef mockCleanableProcess = mockControl.createMock(CleanableProcessExtByRef.class);
        CleanableProcessExtByRefBuilder mockCleanableProcessBuilder = mockControl.createMock(CleanableProcessExtByRefBuilder.class);

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
        return new WriteSingleRefsetDescription(WORKING_REFSET_KEY, mockCleanableProcessBuilder, DIRECTORY_KEY,
            mockPropertyValidator, mockTerminologyWrapper);
    }

    @SuppressWarnings( { "ThrowableInstanceNeverThrown" })
    private void expectEvaluateCalled(final I_EncodeBusinessProcess mockBusinessProcess,
            final CleanableProcessExtByRef mockCleanableProcess,
            final CleanableProcessExtByRefBuilder mockCleanableProcessBuilder) throws Exception {

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
        I_ExtendByRef mockExt1 = mockControl.createMock(I_ExtendByRef.class);
        I_ExtendByRef mockExt2 = mockControl.createMock(I_ExtendByRef.class);
        EasyMock.expect(mockRefset.getConceptNid()).andReturn(REFSET_CONCEPT_ID);
        // TODO fix this one EasyMock.expect(mockTermFactory.getRefsetExtensionMembers(REFSET_CONCEPT_ID)).andReturn(Arrays.asList(mockExt1, mockExt2));

        mockCleanableProcess.processExtensionByReference(mockExt1);
        mockCleanableProcess.processExtensionByReference(mockExt2);
        EasyMock.expectLastCall().andThrow(new IllegalStateException());

        mockCleanableProcess.clean();
    }
}
