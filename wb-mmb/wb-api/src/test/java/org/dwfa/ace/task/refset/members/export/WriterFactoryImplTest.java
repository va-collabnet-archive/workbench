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
package org.dwfa.ace.task.refset.members.export;

import static junit.framework.Assert.fail;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.task.refset.members.RefsetUtil;
import org.dwfa.ace.task.util.Logger;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Before;
import org.junit.Test;

public final class WriterFactoryImplTest {

    private IMocksControl mockControl;
    private Logger mockLogger;
    private I_TermFactory mockTermFactory;
    private RefsetUtil mockRefsetUtil;

    @Before
    public void setup() {
        mockControl = EasyMock.createControl();
        mockLogger = mockControl.createMock(Logger.class);
        mockTermFactory = mockControl.createMock(I_TermFactory.class);
        mockRefsetUtil = mockControl.createMock(RefsetUtil.class);
    }

    @Test
    public void shouldThrowAnExceptionIfTheOutputDirectoryIsNotSupplied() {
        mockControl.replay();
        try {
            new WriterFactoryImpl(null, mockLogger, mockTermFactory, mockRefsetUtil);
            fail();
        } catch (InvalidOutputDirectoryException e) {
            assertThat("The output directory supplied is null.", equalTo(e.getMessage()));
            mockControl.verify();
        }
    }
}
