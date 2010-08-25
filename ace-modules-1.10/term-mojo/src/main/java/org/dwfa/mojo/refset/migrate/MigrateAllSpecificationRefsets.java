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
package org.dwfa.mojo.refset.migrate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.ace.refset.RefsetUtilities;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.mojo.ConceptDescriptor;

/**
 * 
 * @goal migrate-all-spec-refsets
 */
@SuppressWarnings("deprecation")
public class MigrateAllSpecificationRefsets extends AbstractMojo {

    public final String PARENT_MEMBER_HIERARCHY_NAME = "parent members";

    public final String PARENT_MEMBER_REFSET_PURPOSE_NAME = ConceptConstants.REFSET_PARENT_MEMBER_PURPOSE.getDescription();

    public final String MEMBER_REFSET_PURPOSE_NAME = ConceptConstants.REFSET_MEMBER_PURPOSE.getDescription();

    public final String PARENT_MEMBER_REFSET_RELATIONSHIP_NAME = ConceptConstants.INCLUDES_MARKED_PARENTS_REL_TYPE.getDescription();

    /**
     * @parameter
     * @required
     */
    public ConceptDescriptor editPath;

    protected I_TermFactory termFactory;

    protected RefsetHelper refsetHelper;

    protected I_ConfigAceFrame config;

    protected HashMap<String, I_GetConceptData> concepts = new HashMap<String, I_GetConceptData>();

    public MigrateAllSpecificationRefsets() throws Exception {
        termFactory = LocalVersionedTerminology.get();
        if (termFactory == null) {
            throw new RuntimeException("The LocalVersionedTerminology is not available. Please check the database.");
        }

        config = termFactory.getActiveAceFrameConfig();
        refsetHelper = new RefsetHelper(termFactory);
    }

    public void init() throws Exception {
        concepts.put("DEFINING_CHARACTERISTIC",
            termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.localize().getNid()));
        concepts.put("OPTIONAL_REFINABILITY",
            termFactory.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids()));
        concepts.put("CURRENT", termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid()));
        concepts.put("RETIRED", termFactory.getConcept(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid()));

        concepts.put("SNOMED_IS_A", termFactory.getConcept(ConceptConstants.SNOMED_IS_A.localize().getNid()));
        concepts.put("REFSET_TYPE_REL", termFactory.getConcept(ConceptConstants.REFSET_TYPE_REL.localize().getNid()));
        concepts.put("EXCLUDE_MEMBERS_REL_TYPE",
            termFactory.getConcept(ConceptConstants.EXCLUDE_MEMBERS_REL_TYPE.localize().getNid()));
        concepts.put("PARENT_MARKER", termFactory.getConcept(ConceptConstants.PARENT_MARKER.localize().getNid()));

        concepts.put("REFSET_IDENTITY", termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_IDENTITY.getUids()));
        concepts.put("CONCEPT_EXTENSION", termFactory.getConcept(RefsetAuxiliary.Concept.CONCEPT_EXTENSION.localize()
            .getNid()));
        concepts.put("REFSET_PURPOSE", termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_PURPOSE.localize()
            .getNid()));
        concepts.put("REFSET_PURPOSE_REL", termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_PURPOSE_REL.localize()
            .getNid()));
        concepts.put("REFSET_RELATIONSHIP",
            termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_RELATIONSHIP.localize().getNid()));

        concepts.put("NORMAL_MEMBER",
            new ConceptDescriptor("cc624429-b17d-4ac5-a69e-0b32448aaf3c", "normal member").getVerifiedConcept());

        I_Path pathForEditing = termFactory.getPath(editPath.getVerifiedConcept().getUids());
        config.getEditingPathSet().clear();
        config.getEditingPathSet().add(pathForEditing);
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            init();

            I_GetConceptData parentRefsetHierarchy = createParentMemberHierarchy();
            I_GetConceptData parentRefsetPurpose = createParentRefsetPurpose();
            I_GetConceptData refsetPurpose = createMemberRefsetPurpose();
            I_GetConceptData parentRefsetRel = createRefsetRel();
            List<Integer> specificationRefsets = refsetHelper.getSpecificationRefsets();

            for (Integer specRefsetId : specificationRefsets) {
                I_GetConceptData specRefsetConcept = termFactory.getConcept(specRefsetId);
                I_GetConceptData memberRefsetConcept = refsetHelper.getMemberSetConcept(specRefsetId);

                addMemberRefsetPurpose(memberRefsetConcept, refsetPurpose);

                I_GetConceptData parentMemberRefset = createParentMemberRefset(parentRefsetHierarchy,
                    memberRefsetConcept, parentRefsetPurpose);

                linkMemberRefsetToParentRefset(memberRefsetConcept, parentMemberRefset, parentRefsetRel);
                copySpecRefsetExtensions(specRefsetConcept, memberRefsetConcept);
                retireSpecificationRefset(specRefsetConcept);
            }
        } catch (Exception ex) {
            throw new MojoExecutionException("Unable to migrate specification refsets", ex);
        }

    }

    /**
     * Transfer refset extension relationships from the specification refset
     * (now redundant) to the member refset concept
     */
    private void copySpecRefsetExtensions(I_GetConceptData specRefsetConcept, I_GetConceptData memberRefsetConcept)
            throws Exception {
        int exclusionExtRefset = refsetHelper.getExcludeMembersRefset(specRefsetConcept.getConceptId());
        if (exclusionExtRefset != refsetHelper.ID_NOT_FOUND) {
            termFactory.newRelationship(UUID.randomUUID(), memberRefsetConcept,
                concepts.get("EXCLUDE_MEMBERS_REL_TYPE"), termFactory.getConcept(exclusionExtRefset),
                concepts.get("DEFINING_CHARACTERISTIC"), concepts.get("OPTIONAL_REFINABILITY"),
                concepts.get("CURRENT"), 0, config);
        }
    }

    /**
     * The 'member' refset need a relationship to the new 'marked parent member'
     * refset
     */
    private void linkMemberRefsetToParentRefset(I_GetConceptData memberRefsetConcept,
            I_GetConceptData parentMemberRefset, I_GetConceptData parentRefsetRel) throws Exception {

        termFactory.newRelationship(UUID.randomUUID(), memberRefsetConcept, parentRefsetRel, parentMemberRefset,
            concepts.get("DEFINING_CHARACTERISTIC"), concepts.get("OPTIONAL_REFINABILITY"), concepts.get("CURRENT"), 0,
            config);

    }

    /**
     * Creates the new type of relationship to link a member refset with another
     * refset containing its marked parent members
     */
    private I_GetConceptData createRefsetRel() throws Exception {

        I_GetConceptData newRefsetRel = termFactory.newConcept(
            ConceptConstants.INCLUDES_MARKED_PARENTS_REL_TYPE.getUuids()[0], false, config);

        termFactory.newDescription(UUID.randomUUID(), newRefsetRel, "en", PARENT_MEMBER_REFSET_RELATIONSHIP_NAME,
            ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize(), config);

        termFactory.newDescription(UUID.randomUUID(), newRefsetRel, "en", PARENT_MEMBER_REFSET_RELATIONSHIP_NAME,
            ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize(), config);

        termFactory.newRelationship(UUID.randomUUID(), newRefsetRel, concepts.get("SNOMED_IS_A"),
            concepts.get("REFSET_RELATIONSHIP"), concepts.get("DEFINING_CHARACTERISTIC"),
            concepts.get("OPTIONAL_REFINABILITY"), concepts.get("CURRENT"), 0, config);

        return newRefsetRel;
    }

    /**
     * Creates the new purpose which can be given the marked parent member
     * refset concepts
     */
    private I_GetConceptData createParentRefsetPurpose() throws Exception {

        I_GetConceptData newRefsetPurpose = termFactory.newConcept(
            ConceptConstants.REFSET_PARENT_MEMBER_PURPOSE.getUuids()[0], false, config);

        termFactory.newDescription(UUID.randomUUID(), newRefsetPurpose, "en", PARENT_MEMBER_REFSET_PURPOSE_NAME,
            ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize(), config);

        termFactory.newDescription(UUID.randomUUID(), newRefsetPurpose, "en", PARENT_MEMBER_REFSET_PURPOSE_NAME,
            ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize(), config);

        termFactory.newRelationship(UUID.randomUUID(), newRefsetPurpose, concepts.get("SNOMED_IS_A"),
            concepts.get("REFSET_PURPOSE"), concepts.get("DEFINING_CHARACTERISTIC"),
            concepts.get("OPTIONAL_REFINABILITY"), concepts.get("CURRENT"), 0, config);

        return newRefsetPurpose;
    }

    /**
     * Creates the new purpose which can be given the marked parent member
     * refset concepts
     */
    private I_GetConceptData createMemberRefsetPurpose() throws Exception {

        I_GetConceptData newRefsetPurpose = termFactory.newConcept(
            ConceptConstants.REFSET_MEMBER_PURPOSE.getUuids()[0], false, config);

        termFactory.newDescription(UUID.randomUUID(), newRefsetPurpose, "en", MEMBER_REFSET_PURPOSE_NAME,
            ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize(), config);

        termFactory.newDescription(UUID.randomUUID(), newRefsetPurpose, "en", MEMBER_REFSET_PURPOSE_NAME,
            ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize(), config);

        termFactory.newRelationship(UUID.randomUUID(), newRefsetPurpose, concepts.get("SNOMED_IS_A"),
            concepts.get("REFSET_PURPOSE"), concepts.get("DEFINING_CHARACTERISTIC"),
            concepts.get("OPTIONAL_REFINABILITY"), concepts.get("CURRENT"), 0, config);

        return newRefsetPurpose;
    }

    /**
     * Retire the existing specification refset
     */
    private void retireSpecificationRefset(I_GetConceptData specRefsetConcept) throws Exception {

        // Attribute
        I_ConceptAttributeVersioned attribute = specRefsetConcept.getConceptAttributes();
        for (I_ConceptAttributePart attribPart : retireParts(attribute.getVersions())) {
            attribute.addVersion(attribPart);
        }

        // Descriptions
        for (I_DescriptionVersioned description : specRefsetConcept.getDescriptions()) {
            for (I_DescriptionPart retiredDesc : retireParts(description.getVersions())) {
                description.addVersion(retiredDesc);
            }
        }

        // Source Relationships
        for (I_RelVersioned relationship : specRefsetConcept.getSourceRels()) {
            for (I_RelPart retiredRel : retireParts(relationship.getVersions())) {
                relationship.addVersion(retiredRel);
            }
        }

        termFactory.addUncommitted(specRefsetConcept);
    }

    @SuppressWarnings("unchecked")
    private <T extends I_AmPart> List<T> retireParts(List<T> parts) {
        final int retiredStatusId = concepts.get("RETIRED").getConceptId();
        final int currentStatusId = concepts.get("CURRENT").getConceptId();

        ArrayList<T> newRetirees = new ArrayList<T>();

        ArrayList<T> subjects = new ArrayList<T>();
        for (T part : parts) {
            if (part.getStatusId() == currentStatusId) {
                subjects.add(part);
            }
        }

        ArrayList<T> retiredSubjects = new ArrayList<T>();
        for (T part : parts) {
            for (T subject : subjects) {
                if (part.getStatusId() == retiredStatusId) {
                    if ((subject.getPathId() == part.getPathId()) && (part.getVersion() > subject.getVersion())) {
                        retiredSubjects.add(subject);
                    }
                }
                if (part.getStatusId() == currentStatusId) {
                    if ((subject.getPathId() == part.getPathId()) && (part.getVersion() > subject.getVersion())) {
                        retiredSubjects.remove(subject);
                    }
                }
            }
        }

        subjects.removeAll(retiredSubjects);

        // Retire remaining current parts on the same path
        for (T subjectPart : subjects) {

            T newPart = (T) subjectPart.duplicate();
            newPart.setStatusId(retiredStatusId);
            newPart.setVersion(Integer.MAX_VALUE);

            newRetirees.add(newPart);
        }

        return newRetirees;
    }

    private void addMemberRefsetPurpose(I_GetConceptData memberRefsetConcept, I_GetConceptData refsetPurpose)
            throws Exception {

        termFactory.newRelationship(UUID.randomUUID(), memberRefsetConcept, concepts.get("REFSET_PURPOSE"),
            refsetPurpose, concepts.get("DEFINING_CHARACTERISTIC"), concepts.get("OPTIONAL_REFINABILITY"),
            concepts.get("CURRENT"), 0, config);
    }

    /**
     * Create a new refset to hold marked parent members
     */
    private I_GetConceptData createParentMemberRefset(I_GetConceptData parentHierarchy,
            I_GetConceptData memberRefsetConcept, I_GetConceptData refsetPurpose) throws Exception {

        String refsetName = memberRefsetConcept.getInitialText();
        if (refsetName.endsWith(" reference set")) {
            refsetName = refsetName.substring(0, refsetName.length() - 14);
        }
        if (refsetName.endsWith(" member")) {
            refsetName = refsetName.substring(0, refsetName.length() - 7);
        }
        refsetName = refsetName.concat(" parent member reference set");

        I_GetConceptData newParentMemberConcept = termFactory.newConcept(UUID.randomUUID(), false, config);

        termFactory.newDescription(UUID.randomUUID(), newParentMemberConcept, "en", refsetName,
            ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize(), config);

        termFactory.newDescription(UUID.randomUUID(), newParentMemberConcept, "en", refsetName,
            ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize(), config);

        termFactory.newRelationship(UUID.randomUUID(), newParentMemberConcept, concepts.get("SNOMED_IS_A"),
            parentHierarchy, concepts.get("DEFINING_CHARACTERISTIC"), concepts.get("OPTIONAL_REFINABILITY"),
            concepts.get("CURRENT"), 0, config);

        termFactory.newRelationship(UUID.randomUUID(), newParentMemberConcept, concepts.get("REFSET_TYPE_REL"),
            concepts.get("CONCEPT_EXTENSION"), concepts.get("DEFINING_CHARACTERISTIC"),
            concepts.get("OPTIONAL_REFINABILITY"), concepts.get("CURRENT"), 0, config);

        termFactory.newRelationship(UUID.randomUUID(), newParentMemberConcept, concepts.get("REFSET_PURPOSE"),
            refsetPurpose, concepts.get("DEFINING_CHARACTERISTIC"), concepts.get("OPTIONAL_REFINABILITY"),
            concepts.get("CURRENT"), 0, config);

        return newParentMemberConcept;
    }

    /**
     * Create a new hierarchy to group all the parent member refsets
     */
    private I_GetConceptData createParentMemberHierarchy() throws Exception {

        I_GetConceptData newHierarchyConcept = termFactory.newConcept(UUID.randomUUID(), false, config);

        termFactory.newDescription(UUID.randomUUID(), newHierarchyConcept, "en", PARENT_MEMBER_HIERARCHY_NAME,
            ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize(), config);

        termFactory.newDescription(UUID.randomUUID(), newHierarchyConcept, "en", PARENT_MEMBER_HIERARCHY_NAME,
            ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize(), config);

        termFactory.newRelationship(UUID.randomUUID(), newHierarchyConcept, concepts.get("SNOMED_IS_A"),
            concepts.get("REFSET_IDENTITY"), concepts.get("DEFINING_CHARACTERISTIC"),
            concepts.get("OPTIONAL_REFINABILITY"), concepts.get("CURRENT"), 0, config);

        return newHierarchyConcept;
    }

    /**
     * Utilises the {@link RefsetUtilities} class by injecting the db
     */
    private class RefsetHelper extends RefsetUtilities {
        public RefsetHelper(I_TermFactory termFactory) {
            super.termFactory = termFactory;
        }
    }

}
