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
import junit.framework.Assert;
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
import org.junit.Before;
import org.junit.Test;

public class NotEmptyConceptDataValidatorTest {

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
    public void failValidationWithNullTest() throws Exception {
        this.expectGetFsnConceptOnTermFactory()
            .expectGetPTConceptOnTermFactory()
            .expectUuidToNativeOnTermFactory()
            .expectGetTypeIdWithCorrectValue(1)
            .expectGetConceptIdWithCorrectValue(3)
            .expectGetTextWithNull(1)
            .expectGetDescriptions();

        mocksControl.replay();

        mockConcept = mockTermFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());

        mockNotRequiredConcept = mockTermFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());

        int conceptId = mockTermFactory.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()
            .iterator()
            .next());

        Assert.assertEquals(mockConcept.getConceptId(), conceptId);

        classBeingTested = new NotEmptyConceptDataValidator(mockConcept, descriptions, mockNotRequiredConcept);
        try {
            classBeingTested.validate();
            TestCase.fail("ValidationException should be thrown");
        } catch (ValidationException ex) {
            Logger.getLogger(NotEmptyConceptDataValidatorTest.class.getName()).log(Level.INFO,
                String.format("Exception thrown as expected: %1$s", ex.getMessage()));
        }

        mocksControl.verify();
    }

    @Test
    public void failValidationWithValueTest() throws Exception {
        this.expectGetFsnConceptOnTermFactory()
            .expectGetPTConceptOnTermFactory()
            .expectUuidToNativeOnTermFactory()
            .expectGetTypeIdWithCorrectValue(1)
            .expectGetConceptIdWithCorrectValue(3)
            .expectGetTextWithoutNull(2)
            .expectGetDescriptions();

        mocksControl.replay();

        mockConcept = mockTermFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());

        mockNotRequiredConcept = mockTermFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());

        int conceptId = mockTermFactory.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()
            .iterator()
            .next());

        Assert.assertEquals(mockConcept.getConceptId(), conceptId);

        classBeingTested = new NotEmptyConceptDataValidator(mockConcept, descriptions, mockNotRequiredConcept);
        try {
            classBeingTested.validate();
            TestCase.fail("ValidationException should be thrown");
        } catch (ValidationException ex) {
            Logger.getLogger(NotEmptyConceptDataValidatorTest.class.getName()).log(Level.INFO,
                String.format("Exception thrown as expected: %1$s", ex.getMessage()));
        }

        mocksControl.verify();
    }

    @Test
    public void skipValidationIncorrectConceptType() throws Exception {
        this.expectGetFsnConceptOnTermFactory()
            .expectGetPTConceptOnTermFactory()
            .expectUuidToNativeOnTermFactory()
            .expectGetTypeIdWithWrongValue(1)
            .expectGetConceptIdWithWrongValue(2)
            .expectGetDescriptions();

        mocksControl.replay();

        mockConcept = mockTermFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());

        mockNotRequiredConcept = mockTermFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());

        int conceptId = mockTermFactory.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()
            .iterator()
            .next());

        Assert.assertEquals(mockConcept.getConceptId(), conceptId);

        classBeingTested = new NotEmptyConceptDataValidator(mockConcept, descriptions, mockNotRequiredConcept);
        try {
            classBeingTested.validate();
        } catch (ValidationException ex) {
            TestCase.fail("ValidationException should Not be thrown");
            Logger.getLogger(NotEmptyConceptDataValidatorTest.class.getName()).log(Level.INFO,
                String.format("Exception thrown unexpectedly: %1$s", ex.getMessage()));
        }

        mocksControl.verify();
    }

    @Test
    public void passValidationWithValueTest() throws Exception {
        this.expectGetFsnConceptOnTermFactory()
            .expectUuidToNativeOnTermFactory()
            .expectGetTypeIdWithCorrectValue(1)
            .expectGetConceptIdWithCorrectValue(2)
            .expectGetTextWithValue(2)
            .expectGetDescriptions();

        mocksControl.replay();

        mockConcept = mockTermFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());

        int conceptId = mockTermFactory.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()
            .iterator()
            .next());

        Assert.assertEquals(mockConcept.getConceptId(), conceptId);

        classBeingTested = new NotEmptyConceptDataValidator(mockConcept, descriptions, mockConcept);
        try {
            classBeingTested.validate();
        } catch (ValidationException ex) {
            TestCase.fail("ValidationException should Not be thrown");
            Logger.getLogger(NotEmptyConceptDataValidatorTest.class.getName()).log(Level.INFO,
                String.format("Exception thrown unexpectedly: %1$s", ex.getMessage()));
        }

        mocksControl.verify();
    }

    private NotEmptyConceptDataValidatorTest expectGetFsnConceptOnTermFactory() throws Exception {
        EasyMock.expect(
            mockTermFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()))
            .andReturn(mockConcept);
        return this;
    }

    private NotEmptyConceptDataValidatorTest expectGetPTConceptOnTermFactory() throws Exception {
        EasyMock.expect(mockTermFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()))
            .andReturn(mockConcept);
        return this;
    }

    private NotEmptyConceptDataValidatorTest expectUuidToNativeOnTermFactory() throws Exception {
        EasyMock.expect(
            mockTermFactory.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()
                .iterator()
                .next())).andReturn(Integer.MIN_VALUE);
        return this;
    }

    private NotEmptyConceptDataValidatorTest expectGetConceptIdWithCorrectValue(int times) throws Exception {
        EasyMock.expect(mockConcept.getConceptId()).andReturn(Integer.MIN_VALUE).times(times);
        return this;
    }

    private NotEmptyConceptDataValidatorTest expectGetTypeIdWithCorrectValue(int times) throws Exception {
        EasyMock.expect(mockDescriptionPart.getTypeId()).andReturn(Integer.MIN_VALUE).times(times);
        return this;
    }

    private NotEmptyConceptDataValidatorTest expectGetConceptIdWithWrongValue(int times) throws Exception {
        EasyMock.expect(mockConcept.getConceptId()).andReturn(Integer.MIN_VALUE).times(times);
        return this;
    }

    private NotEmptyConceptDataValidatorTest expectGetTypeIdWithWrongValue(int times) throws Exception {
        EasyMock.expect(mockDescriptionPart.getTypeId()).andReturn(Integer.MAX_VALUE).times(times);
        return this;
    }

    private NotEmptyConceptDataValidatorTest expectGetTextWithoutNull(int times) throws Exception {
        EasyMock.expect(mockDescriptionPart.getText()).andReturn(new String()).times(times);
        return this;
    }

    private NotEmptyConceptDataValidatorTest expectGetTextWithNull(int times) throws Exception {
        EasyMock.expect(mockDescriptionPart.getText()).andReturn(null).times(times);
        return this;
    }

    private NotEmptyConceptDataValidatorTest expectGetTextWithValue(int times) throws Exception {
        EasyMock.expect(mockDescriptionPart.getText()).andReturn("This is a test String").times(times);
        return this;
    }

    private NotEmptyConceptDataValidatorTest expectGetDescriptions() throws Exception {
        EasyMock.expect(mockVersionedDescription.getVersions()).andReturn(mockPartList);
        return this;
    }
}
