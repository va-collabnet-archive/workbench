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
package org.dwfa.ace.task.commit.validator.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.commit.validator.GetConceptDataValidationStrategy;
import org.dwfa.ace.task.commit.validator.ValidationException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.easymock.EasyMock;
import org.easymock.internal.MocksControl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author matt
 */
public class NotNumericConceptDataValidatorTest {

    private GetConceptDataValidationStrategy classBeingTested;
    private MocksControl mocksControl;
    private I_GetConceptData mockConcept;
    private I_GetConceptData mockNotRequiredConcept;
    private I_DescriptionPart mockDescriptionPart;
    private I_DescriptionVersioned mockVersionedDescription;
    private List<I_DescriptionVersioned> descriptions;
    private List<I_DescriptionPart> mockPartList;
    private I_TermFactory mockTermFactory;

    @Before
    public void setup() {
        mocksControl = new MocksControl(MocksControl.MockType.DEFAULT);
        mockTermFactory = mocksControl.createMock(I_TermFactory.class);
        LocalVersionedTerminology.setStealthfactory(mockTermFactory);

        mockConcept = mocksControl.createMock(I_GetConceptData.class);
        mockNotRequiredConcept = mocksControl.createMock(I_GetConceptData.class);
        descriptions = new ArrayList<I_DescriptionVersioned>();
        mockVersionedDescription = mocksControl.createMock(I_DescriptionVersioned.class);
        descriptions.add(mockVersionedDescription);
        mockPartList = new ArrayList<I_DescriptionPart>();
        mockDescriptionPart = mocksControl.createMock(I_DescriptionPart.class);
        mockPartList.add(mockDescriptionPart);

    }

    @Test
    public void passValidationWithNullValue() throws Exception {
        this.expectGetFsnConceptOnTermFactory()
            .expectGetPTConceptOnTermFactory()
            .expectUuidToNativeOnTermFactory()
            .expectCorrectTypeId(1)
            .expectCorrectConceptId(2)
            .expectNullString(1)
            .expectGetDescriptions();

        mocksControl.replay();

        mockConcept = mockTermFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());

        mockNotRequiredConcept = mockTermFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());

        int conceptId = mockTermFactory.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()
            .iterator()
            .next());

        Assert.assertEquals(mockConcept.getConceptId(), conceptId);

        classBeingTested = new NotNumericConceptDataValidator(mockConcept, descriptions, mockConcept);
        try {
            classBeingTested.validate();
        } catch (ValidationException ex) {
            Logger.getLogger(NotNumericConceptDataValidatorTest.class.getName()).log(Level.INFO,
                String.format("Exception thrown unexpectedly: %1$s", ex.getMessage()));
            TestCase.fail("ValidationException should NOT be thrown with a null value");
        }

        mocksControl.verify();
    }

    @Test
    public void failValidationWithNumericValue() throws Exception {
        this.expectGetFsnConceptOnTermFactory()
            .expectGetPTConceptOnTermFactory()
            .expectUuidToNativeOnTermFactory()
            .expectCorrectTypeId(1)
            .expectCorrectConceptId(3)
            .expectNumericValue(2)
            .expectGetDescriptions();

        mocksControl.replay();

        mockConcept = mockTermFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());

        mockNotRequiredConcept = mockTermFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());

        int conceptId = mockTermFactory.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()
            .iterator()
            .next());

        Assert.assertEquals(mockConcept.getConceptId(), conceptId);

        classBeingTested = new NotNumericConceptDataValidator(mockConcept, descriptions, mockNotRequiredConcept);
        try {
            classBeingTested.validate();
            TestCase.fail("ValidationException should be thrown");
        } catch (ValidationException ex) {
            Logger.getLogger(NotNumericConceptDataValidatorTest.class.getName()).log(Level.INFO,
                String.format("Exception thrown as expected: %1$s", ex.getMessage()));
        }

        mocksControl.verify();
    }

    @Test
    public void skipValidationWithNotMatchingConceptType() throws Exception {
        this.expectGetFsnConceptOnTermFactory()
            .expectGetPTConceptOnTermFactory()
            .expectUuidToNativeOnTermFactory()
            .expectWrongTypeId(1)
            .expectWrongConceptId(2)
            .expectGetDescriptions();

        mocksControl.replay();

        mockConcept = mockTermFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());

        mockNotRequiredConcept = mockTermFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());

        int conceptId = mockTermFactory.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()
            .iterator()
            .next());

        Assert.assertEquals(mockConcept.getConceptId(), conceptId);

        classBeingTested = new NotNumericConceptDataValidator(mockConcept, descriptions, mockNotRequiredConcept);
        try {
            classBeingTested.validate();
        } catch (ValidationException ex) {
            TestCase.fail("ValidationException should Not be thrown");
            Logger.getLogger(NotNumericConceptDataValidatorTest.class.getName()).log(Level.INFO,
                String.format("Exception thrown unexpectedly: %1$s", ex.getMessage()));
        }

        mocksControl.verify();
    }

    @Test
    public void passValidationWithAlphaNumericValue() throws Exception {
        this.expectGetFsnConceptOnTermFactory()
            .expectUuidToNativeOnTermFactory()
            .expectCorrectTypeId(1)
            .expectCorrectConceptId(2)
            .expectAlphaNumericString(2)
            .expectGetDescriptions();

        mocksControl.replay();

        mockConcept = mockTermFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());

        int conceptId = mockTermFactory.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()
            .iterator()
            .next());

        Assert.assertEquals(mockConcept.getConceptId(), conceptId);

        classBeingTested = new NotNumericConceptDataValidator(mockConcept, descriptions, mockConcept);
        try {
            classBeingTested.validate();
        } catch (ValidationException ex) {
            TestCase.fail("ValidationException should Not be thrown");
            Logger.getLogger(NotNumericConceptDataValidatorTest.class.getName()).log(Level.INFO,
                String.format("Exception thrown unexpectedly: %1$s", ex.getMessage()));
        }

        mocksControl.verify();
    }

    private NotNumericConceptDataValidatorTest expectGetFsnConceptOnTermFactory() throws Exception {
        EasyMock.expect(
            mockTermFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()))
            .andReturn(mockConcept);
        return this;
    }

    private NotNumericConceptDataValidatorTest expectGetPTConceptOnTermFactory() throws Exception {
        EasyMock.expect(mockTermFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()))
            .andReturn(mockConcept);
        return this;
    }

    private NotNumericConceptDataValidatorTest expectUuidToNativeOnTermFactory() throws Exception {
        EasyMock.expect(
            mockTermFactory.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()
                .iterator()
                .next())).andReturn(Integer.MIN_VALUE);
        return this;
    }

    private NotNumericConceptDataValidatorTest expectCorrectConceptId(int times) throws Exception {
        EasyMock.expect(mockConcept.getConceptId()).andReturn(Integer.MIN_VALUE).times(times);
        return this;
    }

    private NotNumericConceptDataValidatorTest expectCorrectTypeId(int times) throws Exception {
        EasyMock.expect(mockDescriptionPart.getTypeId()).andReturn(Integer.MIN_VALUE).times(times);
        return this;
    }

    private NotNumericConceptDataValidatorTest expectWrongConceptId(int times) throws Exception {
        EasyMock.expect(mockConcept.getConceptId()).andReturn(Integer.MIN_VALUE).times(times);
        return this;
    }

    private NotNumericConceptDataValidatorTest expectWrongTypeId(int times) throws Exception {
        EasyMock.expect(mockDescriptionPart.getTypeId()).andReturn(Integer.MAX_VALUE).times(times);
        return this;
    }

    private NotNumericConceptDataValidatorTest expectNumericValue(int times) throws Exception {
        EasyMock.expect(mockDescriptionPart.getText()).andReturn("-2147483648").times(times);
        return this;
    }

    private NotNumericConceptDataValidatorTest expectNullString(int times) throws Exception {
        EasyMock.expect(mockDescriptionPart.getText()).andReturn(null).times(times);
        return this;
    }

    private NotNumericConceptDataValidatorTest expectAlphaNumericString(int times) throws Exception {
        EasyMock.expect(mockDescriptionPart.getText()).andReturn("This is a test String -5464666").times(times);
        return this;
    }

    private NotNumericConceptDataValidatorTest expectGetDescriptions() throws Exception {
        EasyMock.expect(mockVersionedDescription.getVersions()).andReturn(mockPartList);
        return this;
    }
}
