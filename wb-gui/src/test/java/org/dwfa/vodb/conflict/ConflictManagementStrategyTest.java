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
package org.dwfa.vodb.conflict;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_AmTuple;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_ImageTuple;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_ManageContradiction;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.tapi.TerminologyException;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class that tests the concrete methods of the ConflictResolutionStrategy
 * abstract class
 * 
 * @author Dion
 */
public class ConflictManagementStrategyTest {

    class TestConflictResolutionStrategy extends ContradictionManagementStrategy {

        private static final long serialVersionUID = 1L;

        public boolean isInConflict(I_GetConceptData concept) throws IOException, TerminologyException {
            return concept.getNid() == 0;
        }

        public boolean isInConflict(I_DescriptionVersioned description) throws IOException, TerminologyException {
            return description.getNid() == 0;
        }

        public boolean isInConflict(I_ExtendByRef extension) throws TerminologyException, IOException {
            return extension.getNid() == 0;
        }

        public boolean isInConflict(I_Identify id) throws IOException {
            return id.getNid() == 0;
        }

        public boolean isInConflict(I_ImageVersioned image) throws IOException, TerminologyException {
            return image.getNid() == 0;
        }

        public boolean isInConflict(I_RelVersioned relationship) throws IOException, TerminologyException {
            return relationship.getNid() == 0;
        }

        public boolean isInConflict(I_ConceptAttributeVersioned conceptAttribute) throws TerminologyException,
                IOException {
            return conceptAttribute.getNid() == 0;
        }

        public List<I_ConceptAttributePart> resolveConceptAttributeParts(List<I_ConceptAttributePart> parts) {
            throw new UnsupportedOperationException();
        }

        public List<I_ConceptAttributeTuple> resolveConceptAttributeTuples(List<I_ConceptAttributeTuple> tuples) {
            throw new UnsupportedOperationException();
        }

        public List<I_DescriptionPart> resolveDescriptionParts(List<I_DescriptionPart> parts) {
            throw new UnsupportedOperationException();
        }

        public List<I_DescriptionTuple> resolveDescriptionTuples(List<I_DescriptionTuple> tuples) {
            throw new UnsupportedOperationException();
        }

        public List<I_ExtendByRefPart> resolveExtByRefPart(List<I_ExtendByRefPart> parts) {
            throw new UnsupportedOperationException();
        }

        public List<I_ExtendByRefVersion> resolveExtByRefTuples(List<I_ExtendByRefVersion> tuples) {
            throw new UnsupportedOperationException();
        }

        public List<I_ImageTuple> resolveImageTuples(List<I_ImageTuple> tuples) {
            throw new UnsupportedOperationException();
        }

        public List<I_RelPart> resolveRelParts(List<I_RelPart> parts) {
            throw new UnsupportedOperationException();
        }

        public List<I_RelTuple> resolveRelTuples(List<I_RelTuple> tuples) {
            throw new UnsupportedOperationException();
        }

        public String getDescription() {
            return null;
        }

        public String getDisplayName() {
            return null;
        }

        @Override
        protected <T extends I_AmPart> boolean doesConflictExist(List<T> versions) {
            throw new UnsupportedOperationException();
        }

        public <T extends I_AmTuple> List<T> resolveTuples(List<T> tuples) {
            throw new UnsupportedOperationException();
        }

        public <T extends I_AmPart> List<T> resolveParts(List<T> parts) {

            throw new UnsupportedOperationException();
        }

        @Override
        public <T extends I_AmPart> List<T> resolveParts(T part1, T part2) {
            throw new UnsupportedOperationException();
        }

    }

    I_GetConceptData conceptWithoutConflict;
    I_GetConceptData conceptWithConflict;
    I_DescriptionVersioned descriptionWithoutConflict;
    I_DescriptionVersioned descriptionWithConflict;
    I_RelVersioned relationshipWithoutConflcit;
    I_RelVersioned relationshipWithConflcit;
    I_ExtendByRef extensionWithoutConflcit;
    I_ExtendByRef extensionWithConflcit;
    I_Identify idWithoutConflict;
    I_Identify idWithConflict;
    I_ImageVersioned imageWithoutConflict;
    I_ImageVersioned imageWithConflict;
    I_ManageContradiction conflictResloutionStrategy = new TestConflictResolutionStrategy();

    @Before
    public void initialise() {
    	/* TODO fix to work with generics
        conceptWithoutConflict = createMock(I_GetConceptData.class);
        expect(conceptWithoutConflict.getNid()).andReturn(1);
        conceptWithConflict = createMock(I_GetConceptData.class);
        expect(conceptWithConflict.getNid()).andReturn(0);

        descriptionWithoutConflict = createMock(I_DescriptionVersioned.class);
        expect(descriptionWithoutConflict.getNid()).andReturn(1);
        descriptionWithConflict = createMock(I_DescriptionVersioned.class);
        expect(descriptionWithConflict.getNid()).andReturn(0);

        relationshipWithoutConflcit = createMock(I_RelVersioned.class);
        expect(relationshipWithoutConflcit.getNid()).andReturn(1);
        relationshipWithConflcit = createMock(I_RelVersioned.class);
        expect(relationshipWithConflcit.getNid()).andReturn(0);

        extensionWithoutConflcit = createMock(I_ExtendByRef.class);
        expect(extensionWithoutConflcit.getNid()).andReturn(1);
        extensionWithConflcit = createMock(I_ExtendByRef.class);
        expect(extensionWithConflcit.getNid()).andReturn(0);

        idWithoutConflict = createMock(I_IdVersioned.class);
        expect(idWithoutConflict.getNid()).andReturn(1);
        idWithConflict = createMock(I_IdVersioned.class);
        expect(idWithConflict.getNid()).andReturn(0);

        imageWithoutConflict = createMock(I_ImageVersioned.class);
        expect(imageWithoutConflict.getNid()).andReturn(1);
        imageWithConflict = createMock(I_ImageVersioned.class);
        expect(imageWithConflict.getNid()).andReturn(0);
        */
    }

   @Test
    public void testConceptAttributeConflict() throws Exception {
    	/* TODO fix to work with generics 
        replay(conceptWithConflict);
        Assert.assertTrue(conflictResloutionStrategy.isInConflict(conceptWithConflict, false));
        replay(conceptWithoutConflict);
        Assert.assertFalse(conflictResloutionStrategy.isInConflict(conceptWithoutConflict, false));

        initialise();
        replay(conceptWithConflict);
        Assert.assertTrue(conflictResloutionStrategy.isInConflict(conceptWithConflict, true));
        expect(conceptWithoutConflict.getDescriptions()).andReturn(new ArrayList<I_DescriptionVersioned>());
        expect(conceptWithoutConflict.getSourceRels()).andReturn(new ArrayList<I_RelVersioned>());
        expect(conceptWithoutConflict.getExtensions()).andReturn(new ArrayList<I_ExtendByRef>());
        expect(conceptWithoutConflict.getImages()).andReturn(new ArrayList<I_ImageVersioned>());
        expect(conceptWithoutConflict.getId()).andReturn(idWithoutConflict);
        replay(conceptWithoutConflict, idWithoutConflict);
        Assert.assertFalse(conflictResloutionStrategy.isInConflict(conceptWithoutConflict, true));
        */
    }

  //TODO fix to work with generics @Test
    public void testDescriptionConflict() throws Exception {
        Assert.assertTrue(conflictResloutionStrategy.isInConflict(getMockConcept(false, true, false, false, false,
            false), true));
        Assert.assertFalse(conflictResloutionStrategy.isInConflict(getMockConcept(false, true, false, false, false,
            false), false));
    }

    private I_GetConceptData getMockConcept(boolean conceptConflict, boolean descriptionConflict,
            boolean replationshipConflict, boolean extensionConflict, boolean imageConflict, boolean idConflict)
            throws IOException, TerminologyException {
        I_GetConceptData concept = null;
    	/*
        initialise();

        if (conceptConflict) {
            concept = conceptWithConflict;
        } else {
            concept = conceptWithoutConflict;
        }

        ArrayList<I_DescriptionVersioned> descriptions = new ArrayList<I_DescriptionVersioned>();
        descriptions.add(descriptionWithoutConflict);
        replay(descriptionWithoutConflict);
        if (descriptionConflict) {
            descriptions.add(descriptionWithConflict);
            replay(descriptionWithConflict);
        }
        expect(concept.getDescriptions()).andReturn(descriptions);

        ArrayList<I_RelVersioned> relationships = new ArrayList<I_RelVersioned>();
        relationships.add(relationshipWithoutConflcit);
        replay(relationshipWithoutConflcit);
        if (replationshipConflict) {
            relationships.add(relationshipWithConflcit);
            replay(relationshipWithConflcit);
        }
        expect(concept.getSourceRels()).andReturn(relationships);

        ArrayList<I_ExtendByRef> extensions = new ArrayList<I_ExtendByRef>();
        extensions.add(extensionWithoutConflcit);
        replay(extensionWithoutConflcit);
        if (extensionConflict) {
            extensions.add(extensionWithConflcit);
          replay(extensionWithConflcit);
        }
        expect(concept.getExtensions()).andReturn(extensions);

        ArrayList<I_ImageVersioned> images = new ArrayList<I_ImageVersioned>();
        images.add(imageWithoutConflict);
        replay(imageWithoutConflict);
        if (imageConflict) {
            images.add(imageWithConflict);
            replay(imageWithConflict);
        }
        expect(concept.getImages()).andReturn(images);

        if (idConflict) {
            expect(concept.getId()).andReturn(idWithConflict);
            replay(idWithConflict);
        } else {
            expect(concept.getId()).andReturn(idWithoutConflict);
            replay(idWithoutConflict);
        }

        replay(concept);

        */
        return concept;
    }

  //TODO fix to work with generics @Test
    public void testExtensionConflict() throws Exception {
        Assert.assertTrue(conflictResloutionStrategy.isInConflict(getMockConcept(false, false, false, true, false,
            false), true));
        Assert.assertFalse(conflictResloutionStrategy.isInConflict(getMockConcept(false, false, false, true, false,
            false), false));
    }

  //TODO fix to work with generics @Test
    public void testRelationshipConflict() throws Exception {
        Assert.assertTrue(conflictResloutionStrategy.isInConflict(getMockConcept(false, false, true, false, false,
            false), true));
        Assert.assertFalse(conflictResloutionStrategy.isInConflict(getMockConcept(false, false, true, false, false,
            false), false));
    }

  //TODO fix to work with generics @Test
    public void testIdConflict() throws Exception {
        Assert.assertTrue(conflictResloutionStrategy.isInConflict(getMockConcept(false, false, false, false, false,
            true), true));
        Assert.assertFalse(conflictResloutionStrategy.isInConflict(getMockConcept(false, false, false, false, false,
            true), false));
    }

  //TODO fix to work with generics @Test
    public void testImageConflict() throws Exception {
        Assert.assertTrue(conflictResloutionStrategy.isInConflict(getMockConcept(false, false, false, false, true,
            false), true));
        Assert.assertFalse(conflictResloutionStrategy.isInConflict(getMockConcept(false, false, false, false, true,
            false), false));
    }
}
