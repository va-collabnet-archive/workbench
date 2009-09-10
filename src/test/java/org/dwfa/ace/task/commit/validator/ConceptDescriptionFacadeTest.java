/**
 *  Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.dwfa.ace.task.commit.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.commit.AbstractConceptTest;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.easymock.internal.MocksControl;
import org.junit.Before;
import org.junit.Test;
import static org.easymock.EasyMock.expect;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertEquals;

public class ConceptDescriptionFacadeTest {

    private I_GetConceptData mockConcept;
    private MocksControl mocksControl;
    private I_TermFactory mockTermFactory;
    private AbstractConceptTest mockConceptTest;
    private ConceptDescriptionFacade classBeingTested;
    private I_ConfigAceFrame mockAceFrame;
    private I_IntSet mockAllowedStatus;
    private Set<I_Position> mockPositions;
    private List<I_DescriptionTuple> descriptionTuples;
    private List<I_DescriptionVersioned> uncommittedDescriptions;
    private I_DescriptionTuple mockDescriptionTuple;
    private I_DescriptionVersioned mockVersionedDescription;
    private int numExpectedDescriptions;

    @Before
    public void setup() throws Exception {
        mocksControl = new MocksControl(MocksControl.MockType.DEFAULT);
        mockTermFactory = mocksControl.createMock(I_TermFactory.class);
        mockConcept = mocksControl.createMock(I_GetConceptData.class);
        mockAceFrame = mocksControl.createMock(I_ConfigAceFrame.class);
        mockAllowedStatus = mocksControl.createMock(I_IntSet.class);
        mockPositions = mocksControl.createMock(Set.class);
        mockDescriptionTuple = mocksControl.createMock(I_DescriptionTuple.class);

        descriptionTuples = new ArrayList<I_DescriptionTuple>();
        //Add First Versioned Description
        descriptionTuples.add(mockDescriptionTuple);
        numExpectedDescriptions++;

        uncommittedDescriptions = new ArrayList<I_DescriptionVersioned>();
        //Add second uncommitted Description
        uncommittedDescriptions.add(mockVersionedDescription);
        numExpectedDescriptions++;

        mockConceptTest = org.easymock.classextension.EasyMock.createMock(MockConceptTest.class);
        mockVersionedDescription = mocksControl.createMock(I_DescriptionVersioned.class);
        LocalVersionedTerminology.setStealthfactory(mockTermFactory);
        classBeingTested = new ConceptDescriptionFacade(mockTermFactory, mockConceptTest);
    }

    @Test
    public void getAllDescriptionsTest() throws Exception {

        this.expectGetConceptFromTermFactory().expectGetConceptIdFromMockConcept().
                expectGetActiveAceFrameConfigFromTermFactory().
                expectGetPositionsFromTermFactory().expectGetAllowedStatusOnAceConfigFrame().
                expectGetDescTuplesWithMockStatusOnConcept().expectGetDescVersionedOnMockDescTuple().
                expectGetUncommittedDescOnConcept();


        mocksControl.replay();
        mockConcept = mockTermFactory.getConcept(
                ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());
        List<I_DescriptionVersioned> descriptions = classBeingTested.getAllDescriptions(mockConcept);
        assertEquals(descriptions.size(), numExpectedDescriptions);

        mocksControl.verify();
    }

    private ConceptDescriptionFacadeTest expectGetConceptFromTermFactory() throws Exception {
        expect(mockTermFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids())).
                andReturn(mockConcept);
        return this;
    }

    private ConceptDescriptionFacadeTest expectGetConceptIdFromMockConcept() throws Exception {
        expect(mockConcept.getConceptId()).andReturn(Integer.MIN_VALUE);
        return this;
    }

    private ConceptDescriptionFacadeTest expectGetActiveAceFrameConfigFromTermFactory() throws Exception {
        expect(mockTermFactory.getActiveAceFrameConfig()).andReturn(mockAceFrame);
        return this;
    }

    private ConceptDescriptionFacadeTest expectGetPositionsFromTermFactory() throws Exception {
        expect(mockConceptTest.getPositions(mockTermFactory)).andReturn(mockPositions);
        return this;
    }

    private ConceptDescriptionFacadeTest expectGetAllowedStatusOnAceConfigFrame() throws Exception {
        expect(mockAceFrame.getAllowedStatus()).andReturn(mockAllowedStatus);
        return this;
    }

    private ConceptDescriptionFacadeTest expectGetDescTuplesWithMockStatusOnConcept() throws Exception {
        expect(mockConcept.getDescriptionTuples(mockAllowedStatus, null, null, true)).andReturn(descriptionTuples);
        return this;
    }

    private ConceptDescriptionFacadeTest expectGetDescVersionedOnMockDescTuple() throws Exception {
        expect(mockDescriptionTuple.getDescVersioned()).andReturn(mockVersionedDescription).anyTimes();
        return this;
    }

    private ConceptDescriptionFacadeTest expectGetUncommittedDescOnConcept() throws Exception {
        expect(mockConcept.getUncommittedDescriptions()).andReturn(uncommittedDescriptions).once();
        return this;
    }

    private void initExpectations() throws Exception {

        expect(mockConcept.getUncommittedDescriptions()).andReturn(uncommittedDescriptions).once();
    }

    private class MockConceptTest extends AbstractConceptTest {

        @Override
        public List<AlertToDataConstraintFailure> test(I_GetConceptData concept, boolean forCommit) throws
                TaskFailedException {
            return new ArrayList<AlertToDataConstraintFailure>();
        }
    }
}

