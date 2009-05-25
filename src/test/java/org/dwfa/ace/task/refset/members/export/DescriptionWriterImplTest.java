package org.dwfa.ace.task.refset.members.export;

import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.task.refset.members.RefsetUtil;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import java.io.Writer;
import java.util.Arrays;

public final class DescriptionWriterImplTest {

    private static final int CONCEPT_ID                 = 1000;
    private static final String SNOMED_ID               = "123456";
    private static final String TAB_SEPARATOR           = "\t";
    private static final String LINE_SEPARATOR          = "\n";
    private static final String DESCRIPTION_TUBLE_TEXT  = "Description Tuble Text";
    private static final String SOME_TEXT               = "some text";

    private IMocksControl mockControl;
    private Writer mockWriter;
    private I_TermFactory mockTermFactory;
    private RefsetUtil mockRefsetUtil;

    @Before
    public void setup() {
        mockControl = EasyMock.createControl();
        mockWriter = mockControl.createMock(Writer.class);
        mockTermFactory = mockControl.createMock(I_TermFactory.class);
        mockRefsetUtil = mockControl.createMock(RefsetUtil.class);
    }

    @Test
    public void shouldWriteTheDescriptionOfTheSuppliedRefset() throws Exception {
        I_GetConceptData mockConcept = mockControl.createMock(I_GetConceptData.class);
        I_DescriptionTuple mockDescriptionTuple = mockControl.createMock(I_DescriptionTuple.class);

        EasyMock.expect(mockWriter.append(LINE_SEPARATOR)).andReturn(mockWriter);
        EasyMock.expect(mockConcept.getConceptId()).andReturn(CONCEPT_ID);
        EasyMock.expect(mockRefsetUtil.getSnomedId(CONCEPT_ID, mockTermFactory)).andReturn(SNOMED_ID);
        EasyMock.expect(mockWriter.append(SNOMED_ID)).andReturn(mockWriter);
        EasyMock.expect(mockWriter.append(TAB_SEPARATOR)).andReturn(mockWriter);
        EasyMock.expect(mockDescriptionTuple.getText()).andReturn(DESCRIPTION_TUBLE_TEXT);
        EasyMock.expect(mockWriter.append(DESCRIPTION_TUBLE_TEXT)).andReturn(mockWriter);
        mockControl.replay();

        DescriptionWriter writer = new DescriptionWriterImpl(mockWriter, mockTermFactory, mockRefsetUtil,
                LINE_SEPARATOR);
        writer.write(mockConcept, Arrays.asList(mockDescriptionTuple));

        mockControl.verify();
    }
    
    @Test
    public void shouldAppendSuppliedText() throws Exception {
        EasyMock.expect(mockWriter.append(SOME_TEXT)).andReturn(mockWriter);
        mockControl.replay();

        DescriptionWriter writer = new DescriptionWriterImpl(mockWriter, mockTermFactory, mockRefsetUtil,
                LINE_SEPARATOR);
        writer.append(SOME_TEXT);

        mockControl.verify();
    }

    @Test
    public void shouldCloseFiles() throws Exception {
        mockWriter.flush();
        mockWriter.close();
        mockControl.replay();

        DescriptionWriter writer = new DescriptionWriterImpl(mockWriter, mockTermFactory, mockRefsetUtil,
                LINE_SEPARATOR);
        writer.close();

        mockControl.verify();
    }
}
