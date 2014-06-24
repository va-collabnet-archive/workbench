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

import org.dwfa.bpa.process.I_DefineTask;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public final class WriteSingleRefsetDescriptionWithMissingDataTest {

    private static final String WORKING_REFSET_KEY = "REFSET_KEY";
    private static final String DIRECTORY_KEY = "DIR_KEY";
    private static final String PROPERTY_NOT_FOUND_MESSAGE = "could not find property";

    private IMocksControl mockControl;
    private PropertyValidator mockPropertyValidator;
    private LocalVersionedTerminologyWrapper mockTerminologyWrapper;

    @Before
    public void setup() {
        mockControl = EasyMock.createControl();
        mockPropertyValidator = mockControl.createMock(PropertyValidator.class);

        mockTerminologyWrapper = mockControl.createMock(LocalVersionedTerminologyWrapper.class);
    }

    @SuppressWarnings( { "ThrowableInstanceNeverThrown" })
    @Test
    @Ignore
    public void shouldFailIfTheOutputDirectoryHasNotBeenSelected() throws Exception {
        I_EncodeBusinessProcess mockBusinessProcess = mockControl.createMock(I_EncodeBusinessProcess.class);
        I_Work mockWork = mockControl.createMock(I_Work.class);
        CleanableProcessExtByRefBuilder mockCleanableProcessBuilder = mockControl.createMock(CleanableProcessExtByRefBuilder.class);

        EasyMock.expect(mockBusinessProcess.getProperty(DIRECTORY_KEY)).andReturn(null);
        mockPropertyValidator.validate(null, "output directory");
        EasyMock.expectLastCall().andThrow(new PropertyNotFoundException(PROPERTY_NOT_FOUND_MESSAGE));
        mockControl.replay();

        I_DefineTask task = createTask(mockCleanableProcessBuilder);
        try {
            task.evaluate(mockBusinessProcess, mockWork);
            fail();
        } catch (TaskFailedException e) {
            assertThat(e.getCause(), notNullValue());
            assertTrue("Expected PropertyNotFoundException. Got " + e.getCause(),
                e.getCause().getClass() == PropertyNotFoundException.class);
            assertThat(e.getCause().getMessage(), equalTo(PROPERTY_NOT_FOUND_MESSAGE));
            mockControl.verify();
        }

    }

    @SuppressWarnings( { "ThrowableInstanceNeverThrown" })
    @Test
    @Ignore
    public void shouldFailIfTheWorkingRefsetHasNotBeenSelected() throws Exception {
        I_EncodeBusinessProcess mockBusinessProcess = mockControl.createMock(I_EncodeBusinessProcess.class);
        I_Work mockWork = mockControl.createMock(I_Work.class);
        CleanableProcessExtByRefBuilder mockCleanableProcessBuilder = mockControl.createMock(CleanableProcessExtByRefBuilder.class);
        File mockDirectoryFile = mockControl.createMock(File.class);

        EasyMock.expect(mockBusinessProcess.getProperty(DIRECTORY_KEY)).andReturn(mockDirectoryFile);
        mockPropertyValidator.validate(mockDirectoryFile, "output directory");
        EasyMock.expect(mockBusinessProcess.getProperty(WORKING_REFSET_KEY)).andReturn(null);
        mockPropertyValidator.validate(null, "selected refset");
        EasyMock.expectLastCall().andThrow(new PropertyNotFoundException(PROPERTY_NOT_FOUND_MESSAGE));
        mockControl.replay();

        I_DefineTask task = createTask(mockCleanableProcessBuilder);
        try {
            task.evaluate(mockBusinessProcess, mockWork);
            fail();
        } catch (TaskFailedException e) {
            assertThat(e.getCause(), notNullValue());
            assertTrue("Expected PropertyNotFoundException. Got " + e.getCause(),
                e.getCause().getClass() == PropertyNotFoundException.class);
            assertThat(e.getCause().getMessage(), equalTo(PROPERTY_NOT_FOUND_MESSAGE));
            mockControl.verify();
        }
    }

    private WriteSingleRefsetDescription createTask(final CleanableProcessExtByRefBuilder mockCleanableProcessBuilder) {
        return new WriteSingleRefsetDescription(WORKING_REFSET_KEY, mockCleanableProcessBuilder, DIRECTORY_KEY,
            mockPropertyValidator, mockTerminologyWrapper);
    }

}
