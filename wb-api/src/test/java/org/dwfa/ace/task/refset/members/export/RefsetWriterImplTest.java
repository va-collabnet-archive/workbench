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

import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.task.refset.members.RefsetUtil;
import org.dwfa.ace.task.util.Logger;
import org.easymock.IMocksControl;
import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

public final class RefsetWriterImplTest {

    private static final String REFSET_NAME = "A refset name";
    private static final String ERROR_MESSAGE = "There was an error";
    private static final int REFSET_ID = 1000;
    private static final int COMPONENT_ID = 5000;

    private RefsetWriterParameterObject mockRefsetParamObj;
    private CommonAPIParameterObject mockCommonAPIParamObj;
    private IMocksControl mockControl;
    private RefsetUtil mockRefsetUtil;
    private I_TermFactory mockTermFactory;
    private ProgressLogger mockProgressLogger;
    private RefsetTextWriter mockRefsetTextWriter;
    private WriterFactory mockWriterFactory;
    private Logger mockLogger;
    private DescriptionWriter mockWriter;
    private NoDescriptionWriter mockNoDecWriter;
    private I_GetConceptData mockConcept;
    private List<I_DescriptionTuple> conceptDescriptionList;

    @Before
    public void setup() throws Exception {
        mockControl = EasyMock.createControl();
        mockLogger = mockControl.createMock(Logger.class);
        mockRefsetParamObj = mockControl.createMock(RefsetWriterParameterObject.class);
        mockCommonAPIParamObj = mockControl.createMock(CommonAPIParameterObject.class);
        mockRefsetUtil = mockControl.createMock(RefsetUtil.class);
        mockTermFactory = mockControl.createMock(I_TermFactory.class);
        mockProgressLogger = mockControl.createMock(ProgressLogger.class);
        mockRefsetTextWriter = mockControl.createMock(RefsetTextWriter.class);
        mockWriterFactory = mockControl.createMock(WriterFactory.class);
        I_DescriptionTuple conceptDesc = mockControl.createMock(I_DescriptionTuple.class);
        mockWriter = mockControl.createMock(DescriptionWriter.class);
        mockNoDecWriter = mockControl.createMock(NoDescriptionWriter.class);
        mockConcept = mockControl.createMock(I_GetConceptData.class);
        conceptDescriptionList = Arrays.asList(conceptDesc);

        EasyMock.expect(mockCommonAPIParamObj.getLogger()).andReturn(mockLogger);
        EasyMock.expect(mockCommonAPIParamObj.getRefsetUtil()).andReturn(mockRefsetUtil);
        EasyMock.expect(mockCommonAPIParamObj.getTermFactory()).andReturn(mockTermFactory);

        EasyMock.expect(mockRefsetParamObj.getProgressLogger()).andReturn(mockProgressLogger);
        EasyMock.expect(mockRefsetParamObj.getRefsetTextWriter()).andReturn(mockRefsetTextWriter);
        EasyMock.expect(mockRefsetParamObj.getWriterFactory()).andReturn(mockWriterFactory);
    }

    //TODO update for generics @Test
    public void shouldWriteAValidRefset() throws Exception {
        I_ExtendByRefPart mockVersion = mockControl.createMock(I_ExtendByRefPart.class);
        I_ExtendByRef mockRefset = expectRefset(mockVersion);
        mockRefsetTextWriter.writeRefset(mockConcept, conceptDescriptionList, mockWriter, mockVersion, mockNoDecWriter);
        mockControl.replay();

        RefsetWriter writer = new RefsetWriterImpl(mockRefsetParamObj, mockCommonAPIParamObj, null);
        writer.write(mockRefset);

        mockControl.verify();
    }

    //TODO update for generics @Test
    public void shouldNotWriteARefsetIfItsStatusIsNotCurrent() throws Exception {
        I_ExtendByRef mockRefset = expectRefset(null);
        mockControl.replay();

        RefsetWriter writer = new RefsetWriterImpl(mockRefsetParamObj, mockCommonAPIParamObj, null);
        writer.write(mockRefset);

        mockControl.verify();
    }

    @SuppressWarnings( { "ThrowableInstanceNeverThrown" })
    @Test
    public void shouldLogAndErrorIfAnExceptionIsThrown() throws Exception {
        I_ExtendByRef mockRefset = mockControl.createMock(I_ExtendByRef.class);
        EasyMock.expect(mockRefset.getRefsetId()).andThrow(new IllegalArgumentException(ERROR_MESSAGE));
        mockLogger.logWarn(ERROR_MESSAGE);
        mockControl.replay();

        RefsetWriter writer = new RefsetWriterImpl(mockRefsetParamObj, mockCommonAPIParamObj, null);
        writer.write(mockRefset);

        mockControl.verify();
    }

    @Test
    public void shouldCloseFiles() throws Exception {
        mockWriterFactory.closeFiles();
        mockControl.replay();

        RefsetWriter writer = new RefsetWriterImpl(mockRefsetParamObj, mockCommonAPIParamObj, null);
        writer.closeFiles();

        mockControl.verify();
    }

    private I_ExtendByRef expectRefset(final I_ExtendByRefPart mockVersion) throws Exception {
        I_ExtendByRef mockRefset = mockControl.createMock(I_ExtendByRef.class);
        I_DescriptionTuple mockRefsetName = mockControl.createMock(I_DescriptionTuple.class);

        List<I_DescriptionTuple> refsetDescriptionsList = Arrays.asList(mockRefsetName);
        EasyMock.expect(mockRefset.getRefsetId()).andReturn(REFSET_ID);
        /*
        EasyMock.expect(mockRefsetUtil.getFSNDescriptionsForConceptHavingCurrentStatus(mockTermFactory, REFSET_ID))
            .andReturn(refsetDescriptionsList);
		*/
        EasyMock.expect(mockRefset.getComponentId()).andReturn(COMPONENT_ID);
        EasyMock.expect(mockTermFactory.getConcept(COMPONENT_ID)).andReturn(mockConcept);
        EasyMock.expect(mockRefsetUtil.assertExactlyOne(refsetDescriptionsList)).andReturn(mockRefsetName);
        /*
        EasyMock.expect(mockRefsetUtil.getPTDescriptionsForConceptHavingCurrentStatus(mockTermFactory, COMPONENT_ID))
            .andReturn(conceptDescriptionList);
		*/
        EasyMock.expect(mockRefsetName.getText()).andReturn(REFSET_NAME);
        mockProgressLogger.logProgress(REFSET_NAME);

        EasyMock.expect(mockWriterFactory.createDescriptionFile(REFSET_NAME)).andReturn(mockWriter);
        EasyMock.expect(mockWriterFactory.createNoDescriptionFile()).andReturn(mockNoDecWriter);
        EasyMock.expect(mockRefsetUtil.getLatestVersionIfCurrent(mockRefset, mockTermFactory)).andReturn(mockVersion);
        return mockRefset;
    }
}
