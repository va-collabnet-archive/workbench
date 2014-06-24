/*
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.project.refset;

import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationThreadingPolicy;

/**
 * The Class ConceptMembershipRefset.
 */
public class ConceptMembershipRefset extends ConceptRefset {

    /**
     * Instantiates a new concept membership refset.
     *
     * @param conceptMembershipRefset the concept membership refset
     * @throws Exception the exception
     */
    public ConceptMembershipRefset(I_GetConceptData conceptMembershipRefset) throws Exception {
        super();
        validateRefsetAsMembership(conceptMembershipRefset.getConceptNid());
        this.refsetConcept = conceptMembershipRefset;
        this.refsetName = conceptMembershipRefset.toString();
        this.refsetId = conceptMembershipRefset.getConceptNid();
        termFactory = Terms.get();
    }

    /**
     * Creates the new concept membership refset.
     *
     * @param name the name
     * @param parentId the parent id
     * @return the concept membership refset
     * @throws Exception the exception
     */
    public static ConceptMembershipRefset createNewConceptMembershipRefset(String name, int parentId) throws Exception {
        ConceptMembershipRefset newConceptMembershipRefset = null;
        I_GetConceptData newMembershipConcept = null;
        I_TermFactory tf = Terms.get();
        try {
            I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
            I_GetConceptData parentConcept = tf.getConcept(parentId);
            I_GetConceptData isAConcept = tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids());
            I_GetConceptData purposeRelConcept = tf.getConcept(RefsetAuxiliary.Concept.REFSET_PURPOSE_REL.getUids());
            I_GetConceptData typeRelConcept = tf.getConcept(RefsetAuxiliary.Concept.REFSET_TYPE_REL.getUids());
            I_GetConceptData memberTypeConcept = tf.getConcept(RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids());
            I_GetConceptData purposeConcept = tf.getConcept(RefsetAuxiliary.Concept.DIALECT.getUids());
            I_GetConceptData defining = tf.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids());
            I_GetConceptData refinability = tf.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids());
            I_GetConceptData current = tf.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());

            newMembershipConcept = tf.newConcept(UUID.randomUUID(), false, config);
            tf.newDescription(UUID.randomUUID(), newMembershipConcept, "en",
                    name,
                    tf.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid()),
                    config);
            tf.newDescription(UUID.randomUUID(), newMembershipConcept, "en",
                    name,
                    tf.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()),
                    config);
            tf.newRelationship(UUID.randomUUID(), newMembershipConcept, isAConcept, parentConcept, defining, refinability,
                    current, 0, config);

            tf.newRelationship(UUID.randomUUID(), newMembershipConcept, purposeRelConcept, purposeConcept, defining, refinability,
                    current, 0, config);
            tf.newRelationship(UUID.randomUUID(), newMembershipConcept, typeRelConcept, memberTypeConcept, defining, refinability,
                    current, 0, config);

            tf.addUncommittedNoChecks(newMembershipConcept);

            newMembershipConcept.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);

            newConceptMembershipRefset = new ConceptMembershipRefset(newMembershipConcept);

        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
            return null;
        }
        return newConceptMembershipRefset;
    }

    /**
     * Gets the concept spec refset concept.
     *
     * @return the concept spec refset concept
     */
    public I_GetConceptData getConceptSpecRefsetConcept() {
        try {
            I_GetConceptData specifiesRefsetRel =
                    termFactory.getConcept(RefsetAuxiliary.Concept.SPECIFIES_REFSET.getUids());
            return getLatestDestinationRelationshipSource(getRefsetConcept(), specifiesRefsetRel);
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
            return null;
        }
    }

    /**
     * Validate refset as membership.
     *
     * @param conceptRefsetId the concept refset id
     * @throws Exception the exception
     */
    private static void validateRefsetAsMembership(int conceptRefsetId) throws Exception {
        I_TermFactory tf = Terms.get();
        I_GetConceptData conceptRefsetConcept = tf.getConcept(conceptRefsetId);
        I_GetConceptData refsetTypeConcept = tf.getConcept(
                RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids());
        I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
        Set<? extends I_GetConceptData> refsetTypes = getSourceRelTarget(conceptRefsetConcept, config,
                RefsetAuxiliary.Concept.REFSET_TYPE_REL.localize().getNid());
        boolean isValid = false;
        for (I_GetConceptData refsetType : refsetTypes) {
            if (refsetType.getConceptNid() == refsetTypeConcept.getConceptNid()) {
                isValid = true;
            }
        }
        if (!isValid) {
            throw new Exception("Refset type must be a concept membership refset");
        }
        return;
    }
}
