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

import java.io.File;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.task.refset.TaskLogger;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.easymock.IMocksControl;
import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

public final class WriteRefsetDescriptionsTest {

    private static final String DIRECTORY_KEY = "mykey";

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

        EasyMock.expect(mockTerminologyWrapper.get()).andReturn(mockTermFactory);
        EasyMock.expect(mockWork.getLogger()).andReturn(mockLogger).atLeastOnce();
        EasyMock.expect(mockLogger.isLoggable(EasyMock.isA(Level.class))).andReturn(false).atLeastOnce();
    }

    @Test
    public void shouldRunAValidTask() throws Exception {
        CleanableProcessExtByRef mockCleanableProcess = mockControl.createMock(CleanableProcessExtByRef.class);
        File directoryFile = new File("blah");
        EasyMock.expect(mockProcess.getProperty(DIRECTORY_KEY)).andReturn(directoryFile);
        expectThatCleanableProcessisBuilt(mockCleanableProcess, directoryFile);

        mockTermFactory.iterateExtByRefs(mockCleanableProcess);
        mockCleanableProcess.clean();
        mockControl.replay();

        WriteRefsetDescriptions writer = createTask();
        Condition condition = writer.evaluate(mockProcess, mockWork);
        assertThat(condition, equalTo(Condition.CONTINUE));

        mockControl.verify();
    }

    @Test
    public void shouldReturnExpectedConditions() {
        Collection<Condition> conditions = createTask().getConditions();

        assertThat(conditions, notNullValue());
        assertThat(conditions.size(), equalTo(1));
        assertThat(conditions.iterator().next(), equalTo(Condition.CONTINUE));
    }

    private WriteRefsetDescriptions createTask() {
        return new WriteRefsetDescriptions(DIRECTORY_KEY, mockTerminologyWrapper, mockBuilder);
    }

    public void shouldReturnZeroContainerIds() {
        int[] containerIds = new WriteRefsetDescriptions(DIRECTORY_KEY, mockTerminologyWrapper, mockBuilder).getDataContainerIds();

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
