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

import java.util.Arrays;
import java.util.List;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.task.refset.members.CleanableProcessExtByRef;
import org.dwfa.ace.task.refset.members.RefsetUtil;
import org.dwfa.ace.task.refset.members.WriteRefsetDescriptionsProcessExtByRef;
import org.dwfa.ace.task.util.Logger;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

public final class WriteRefsetDescriptionsProcessExtByRefTest {

    private static final int REFSET_ID = 1000;
    private static final int REFSET_TYPE_ID = 2000;
    private static final String EXCEPTION_MESSAGE = "An exception was thrown.";
    private static final String CLEANING_RESOURCES_TEXT = "Cleaning resources.";

    private IMocksControl mockControl;
    private RefsetExportValidator mockRefsetExportValidator;
    private RefsetWriter mockRefsetWriter;
    private RefsetUtil mockRefsetUtil;
    private I_TermFactory mockTermFactory;
    private Logger mockLogger;

    @Before
    public void setup() {
        mockControl = EasyMock.createControl();
        mockRefsetExportValidator = mockControl.createMock(RefsetExportValidator.class);
        mockRefsetWriter = mockControl.createMock(RefsetWriter.class);
        mockRefsetUtil = mockControl.createMock(RefsetUtil.class);
        mockTermFactory = mockControl.createMock(I_TermFactory.class);
        mockLogger = mockControl.createMock(Logger.class);
    }

    @Test
    public void shouldProcessASuppliedReferenceSet() throws Exception {
        I_ExtendByRef mockRefset = mockControl.createMock(I_ExtendByRef.class);
        expectThatRefsetsAreWritten(mockRefset, REFSET_ID, REFSET_TYPE_ID);
        mockControl.replay();

        CleanableProcessExtByRef processor = new WriteRefsetDescriptionsProcessExtByRef(mockRefsetExportValidator,
            mockRefsetWriter, mockRefsetUtil, mockTermFactory, mockLogger);
        processor.processExtensionByReference(mockRefset);

        mockControl.verify();
    }

    @Test
    public void shouldProcessMultipleSuppliedReferenceSets() throws Exception {
        List<I_ExtendByRef> refsetList = Arrays.asList(mockControl.createMock(I_ExtendByRef.class),
            mockControl.createMock(I_ExtendByRef.class),
            mockControl.createMock(I_ExtendByRef.class));

        for (int index = 0; index < refsetList.size(); index++) {
            expectThatRefsetsAreWritten(refsetList.get(index), (REFSET_ID + index), (REFSET_TYPE_ID + index));
        }
        mockControl.replay();

        CleanableProcessExtByRef processor = new WriteRefsetDescriptionsProcessExtByRef(mockRefsetExportValidator,
            mockRefsetWriter, mockRefsetUtil, mockTermFactory, mockLogger);

        for (I_ExtendByRef refset : refsetList) {
            processor.processExtensionByReference(refset);
        }
        mockControl.verify();
    }

    @SuppressWarnings( { "ThrowableInstanceNeverThrown" })
    @Test
    public void shouldLogAValidationException() throws Exception {
        I_ExtendByRef mockRefset = mockControl.createMock(I_ExtendByRef.class);
        I_GetConceptData mockConcept = mockControl.createMock(I_GetConceptData.class);
        EasyMock.expect(mockRefset.getRefsetId()).andReturn(REFSET_ID);
        EasyMock.expect(mockRefset.getTypeId()).andReturn(REFSET_TYPE_ID);

        EasyMock.expect(mockTermFactory.getConcept(REFSET_ID)).andReturn(mockConcept);
        mockRefsetExportValidator.validateIsConceptExtension(REFSET_TYPE_ID, mockRefsetUtil);
        EasyMock.expectLastCall().andThrow(new RefsetExportValidationException(EXCEPTION_MESSAGE));
        mockLogger.logWarn(EXCEPTION_MESSAGE);
        mockControl.replay();

        CleanableProcessExtByRef processor = new WriteRefsetDescriptionsProcessExtByRef(mockRefsetExportValidator,
            mockRefsetWriter, mockRefsetUtil, mockTermFactory, mockLogger);
        processor.processExtensionByReference(mockRefset);

        mockControl.verify();
    }

    @SuppressWarnings( { "ThrowableInstanceNeverThrown" })
    @Test(expected = RuntimeException.class)
    public void shouldRethrowNonValidationExceptions() throws Exception {
        I_ExtendByRef mockRefset = mockControl.createMock(I_ExtendByRef.class);
        EasyMock.expect(mockRefset.getRefsetId()).andReturn(REFSET_ID);
        EasyMock.expect(mockTermFactory.getConcept(REFSET_ID)).andThrow(new RuntimeException(EXCEPTION_MESSAGE));
        mockControl.replay();

        CleanableProcessExtByRef processor = new WriteRefsetDescriptionsProcessExtByRef(mockRefsetExportValidator,
            mockRefsetWriter, mockRefsetUtil, mockTermFactory, mockLogger);
        processor.processExtensionByReference(mockRefset);
    }

    @Test
    public void shouldCleanResources() throws Exception {
        mockLogger.logInfo(CLEANING_RESOURCES_TEXT);
        mockRefsetWriter.closeFiles();
        mockControl.replay();

        CleanableProcessExtByRef processor = new WriteRefsetDescriptionsProcessExtByRef(mockRefsetExportValidator,
            mockRefsetWriter, mockRefsetUtil, mockTermFactory, mockLogger);
        processor.clean();

        mockControl.verify();
    }

    private void expectThatRefsetsAreWritten(final I_ExtendByRef mockRefset, final int refsetId,
            final int refsetTypeId) throws Exception {
        I_GetConceptData mockConcept = mockControl.createMock(I_GetConceptData.class);
        EasyMock.expect(mockRefset.getRefsetId()).andReturn(refsetId);
        EasyMock.expect(mockTermFactory.getConcept(refsetId)).andReturn(mockConcept);
        EasyMock.expect(mockRefset.getTypeId()).andReturn(refsetTypeId);
        mockRefsetExportValidator.validateIsConceptExtension(refsetTypeId, mockRefsetUtil);
        mockRefsetExportValidator.validateIsCurrent(mockConcept, mockRefsetUtil);
        mockRefsetWriter.write(mockRefset);
    }
}
