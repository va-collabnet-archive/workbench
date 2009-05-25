package org.dwfa.ace.task.refset.members.export;

import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.task.refset.members.RefsetUtil;
import org.easymock.IMocksControl;
import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public final class RefsetTextWriterImplTest {

    private static final int CONCEPT_ID             = 2100;
    private static final int LOCALIZED_PARENT_NID   = 500;
    private static final int PART_CONCEPT_ID        = 3000;

    private IMocksControl mockControl;
    private RefsetUtil mockRefsetUtil;
    private I_TermFactory mockTermFactory;
    private DescriptionWriter mockDescriptionWriter;
    private NoDescriptionWriter mockNoDescriptionWriter;
    private I_ThinExtByRefPartConcept mockPart;
    private I_GetConceptData mockRefsetConcept;
    private I_GetConceptData mockPartConcept;

    @Before
    public void setup() throws Exception {
        mockControl = EasyMock.createControl();
        mockRefsetUtil = mockControl.createMock(RefsetUtil.class);
        mockTermFactory = mockControl.createMock(I_TermFactory.class);
        mockDescriptionWriter = mockControl.createMock(DescriptionWriter.class);
        mockNoDescriptionWriter = mockControl.createMock(NoDescriptionWriter.class);
        mockPart = mockControl.createMock(I_ThinExtByRefPartConcept.class);
        mockRefsetConcept = mockControl.createMock(I_GetConceptData.class);

        mockPartConcept = mockControl.createMock(I_GetConceptData.class);
        EasyMock.expect(mockPart.getConceptId()).andReturn(CONCEPT_ID);
        EasyMock.expect(mockTermFactory.getConcept(CONCEPT_ID)).andReturn(mockPartConcept);

    }

    @Test
    public void shouldUpdateTheRefsetFileIfTheRefsetHasADescription() throws Exception {
        I_DescriptionTuple mockDescriptionTuple1 = mockControl.createMock(I_DescriptionTuple.class);
        I_DescriptionTuple mockDescriptionTuple2 = mockControl.createMock(I_DescriptionTuple.class);
        List<I_DescriptionTuple> descriptionTupleList = Arrays.asList(mockDescriptionTuple1, mockDescriptionTuple2);

        EasyMock.expect(mockPartConcept.getConceptId()).andReturn(PART_CONCEPT_ID);
        EasyMock.expect(mockRefsetUtil.getLocalizedParentMarkerNid()).andReturn(LOCALIZED_PARENT_NID);
        mockDescriptionWriter.write(mockRefsetConcept, descriptionTupleList);
        mockControl.replay();

        RefsetTextWriter writer = new RefsetTextWriterImpl(mockRefsetUtil, mockTermFactory);
        writer.writeRefset(mockRefsetConcept, descriptionTupleList, mockDescriptionWriter, mockPart,
                mockNoDescriptionWriter);

        mockControl.verify();
    }

    @Test
    public void shouldUpdateTheNoDescriptsionFileIfARefsetDoesntHaveADescription() throws Exception {
        List<I_DescriptionTuple> descriptionTupleList = Arrays.asList();
        mockNoDescriptionWriter.write(mockRefsetConcept);
        mockControl.replay();

        RefsetTextWriter writer = new RefsetTextWriterImpl(mockRefsetUtil, mockTermFactory);        
        writer.writeRefset(mockRefsetConcept, descriptionTupleList, mockDescriptionWriter, mockPart,
                mockNoDescriptionWriter);

        mockControl.verify();
    }
}
