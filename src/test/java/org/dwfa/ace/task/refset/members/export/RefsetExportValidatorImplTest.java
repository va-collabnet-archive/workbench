package org.dwfa.ace.task.refset.members.export;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.task.refset.members.RefsetUtil;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import org.junit.Assert;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public final class RefsetExportValidatorImplTest {

    private RefsetExportValidator exportValidator;
    private RefsetUtil mockRefsetUtil;
    private I_GetConceptData mockGetConceptData;
    private IMocksControl mockControl;

    @Before
    public void setup() {
        mockControl = EasyMock.createControl();
        mockRefsetUtil = mockControl.createMock(RefsetUtil.class);
        mockGetConceptData = mockControl.createMock(I_GetConceptData.class);
        exportValidator = new RefsetExportValidatorImpl();
    }
    
    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    @Test
    public void shouldThrowAnExceptionIfValidateIsCurrentThrowsAnException() throws Exception {
        EasyMock.expect(mockRefsetUtil.getLocalizedCurrentConceptNid()).andThrow(new IllegalStateException());                
        mockControl.replay();

        try {
            exportValidator.validateIsCurrent(mockGetConceptData, mockRefsetUtil);
        } catch (RefsetExportValidationException e) {
            assertThat("An exception was thrown while validating current concept.", equalTo(e.getMessage()));
            assertThat(e.getCause(), notNullValue());
            assertTrue("Expected IllegalStateException.", IllegalStateException.class == e.getCause().getClass());
            mockControl.verify();
        }
    }

    @Test (expected = RefsetExportValidationException.class)
    public void shouldThrowAnExceptionIfTheLatestRefsetConceptIsNull() throws Exception {
        EasyMock.expect(mockRefsetUtil.getLastestAttributePart(mockGetConceptData)).andReturn(null);
        EasyMock.expect(mockRefsetUtil.getLocalizedCurrentConceptNid()).andReturn(6);
        mockControl.replay();

        exportValidator.validateIsCurrent(mockGetConceptData, mockRefsetUtil);
    }

    @Test (expected = RefsetExportValidationException.class)
    public void  shouldThrowAnExceptionIfTheLatestRefsetConceptIsNotCurrent() throws Exception {
        I_ConceptAttributePart mockConceptAttributePart = mockControl.createMock(I_ConceptAttributePart.class);
        EasyMock.expect(mockRefsetUtil.getLastestAttributePart(mockGetConceptData)).andReturn(mockConceptAttributePart);
        EasyMock.expect(mockConceptAttributePart.getStatusId()).andReturn(2);
        EasyMock.expect(mockRefsetUtil.getLocalizedCurrentConceptNid()).andReturn(3);
        mockControl.replay();

        exportValidator.validateIsCurrent(mockGetConceptData, mockRefsetUtil);
    }
        
    @Test
    public void shouldAcceptTheLatestRefsetConceptIfCurrent() throws Exception {
        I_ConceptAttributePart mockConceptAttributePart = mockControl.createMock(I_ConceptAttributePart.class);
        EasyMock.expect(mockRefsetUtil.getLastestAttributePart(mockGetConceptData)).andReturn(mockConceptAttributePart);
        EasyMock.expect(mockConceptAttributePart.getStatusId()).andReturn(5);
        EasyMock.expect(mockRefsetUtil.getLocalizedCurrentConceptNid()).andReturn(5);
        mockControl.replay();

        exportValidator.validateIsCurrent(mockGetConceptData, mockRefsetUtil);

        mockControl.verify();
    }
    
    @Test (expected = RefsetExportValidationException.class)
    public void shouldThrowAnExceptionIfConceptSuppliedIsNotAConceptExtension() throws Exception {
        EasyMock.expect(mockRefsetUtil.getLocalizedConceptExtensionNid()).andReturn(2000);
        mockControl.replay();

        exportValidator.validateIsConceptExtension(1000, mockRefsetUtil);        
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    @Test
    public void shouldThrowAnExceptionIfValidateIsConceptExtensionThrowsAnException() throws Exception {
        EasyMock.expect(mockRefsetUtil.getLocalizedConceptExtensionNid()).andThrow(new IllegalArgumentException(""));
        mockControl.replay();

        try {
            exportValidator.validateIsConceptExtension(1000, mockRefsetUtil);
        } catch (RefsetExportValidationException e) {
            assertThat("An exception was thrown while validating concept extension.", equalTo(e.getMessage()));
            assertThat(e.getCause(), notNullValue());
            Assert.assertTrue("Expected IllegalArgumentException.",
                    IllegalArgumentException.class == e.getCause().getClass());
            mockControl.verify();
        }
    }
}
