package org.dwfa.ace.task.refset.members.export;

import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.select.DescriptionSelector;
import org.dwfa.ace.task.refset.members.RefsetUtil;
import org.dwfa.ace.task.util.Logger;
import org.easymock.IMocksControl;
import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public final class RefsetWriterImplTest {
    
    private static final String REFSET_NAME         = "A refset name";
    private static final String ERROR_MESSAGE       = "There was an error";
    private static final int REFSET_ID              = 1000;
    private static final int COMPONENT_ID           = 5000;
    
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

    @Test
    public void shouldWriteAValidRefset() throws Exception {
        I_ThinExtByRefPart mockVersion = mockControl.createMock(I_ThinExtByRefPart.class);
        I_ThinExtByRefVersioned mockRefset = expectRefset(mockVersion);
        mockRefsetTextWriter.writeRefset(mockConcept, conceptDescriptionList, mockWriter, mockVersion, mockNoDecWriter);
        mockControl.replay();

        RefsetWriter writer = new RefsetWriterImpl(mockRefsetParamObj, mockCommonAPIParamObj, null);
        writer.write(mockRefset);

        mockControl.verify();
    }

    @Test
    public void shouldNotWriteARefsetIfItsStatusIsNotCurrent() throws Exception {
        I_ThinExtByRefVersioned mockRefset = expectRefset(null);
        mockControl.replay();

        RefsetWriter writer = new RefsetWriterImpl(mockRefsetParamObj, mockCommonAPIParamObj, null);
        writer.write(mockRefset);

        mockControl.verify();
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    @Test
    public void shouldLogAndErrorIfAnExceptionIsThrown() throws Exception {
        I_ThinExtByRefVersioned mockRefset = mockControl.createMock(I_ThinExtByRefVersioned.class);
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

    private I_ThinExtByRefVersioned expectRefset(final I_ThinExtByRefPart mockVersion) throws Exception {
        I_ThinExtByRefVersioned mockRefset = mockControl.createMock(I_ThinExtByRefVersioned.class);
        I_DescriptionTuple mockRefsetName = mockControl.createMock(I_DescriptionTuple.class);

        List<I_DescriptionTuple> refsetDescriptionsList = Arrays.asList(mockRefsetName);
        EasyMock.expect(mockRefset.getRefsetId()).andReturn(REFSET_ID);
        EasyMock.expect(mockRefsetUtil.getFSNDescriptionsForConceptHavingCurrentStatus(mockTermFactory, REFSET_ID)).
                andReturn(refsetDescriptionsList);

        EasyMock.expect(mockRefset.getComponentId()).andReturn(COMPONENT_ID);
        EasyMock.expect(mockTermFactory.getConcept(COMPONENT_ID)).andReturn(mockConcept);
        EasyMock.expect(mockRefsetUtil.assertExactlyOne(refsetDescriptionsList)).andReturn(mockRefsetName);
        EasyMock.expect(mockRefsetUtil.getPTDescriptionsForConceptHavingCurrentStatus(mockTermFactory, COMPONENT_ID)).
                andReturn(conceptDescriptionList);

        EasyMock.expect(mockRefsetName.getText()).andReturn(REFSET_NAME);
        mockProgressLogger.logProgress(REFSET_NAME);

        EasyMock.expect(mockWriterFactory.createDescriptionFile(REFSET_NAME)).andReturn(mockWriter);
        EasyMock.expect(mockWriterFactory.createNoDescriptionFile()).andReturn(mockNoDecWriter);
        EasyMock.expect(mockRefsetUtil.getLatestVersionIfCurrent(mockRefset, mockTermFactory)).andReturn(mockVersion);
        return mockRefset;
    }
}
