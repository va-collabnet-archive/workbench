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
package org.dwfa.ace.refset.spec;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.refset.RefsetUtilities;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.TerminologyException;

public class SpecMarkedParentRefsetHelper extends SpecRefsetHelper {

    protected RefsetHelper refsetHelper;

    private Logger logger = Logger.getLogger(SpecMarkedParentRefsetHelper.class.getName());

    private int refsetId;
    private int memberTypeId;
    private int parentRefsetId;
    private int parentMemberTypeId;

    public SpecMarkedParentRefsetHelper(int refsetId, int memberTypeId) throws Exception {
        super();
        this.refsetId = refsetId;
        this.memberTypeId = memberTypeId;
        this.refsetHelper = new RefsetHelper(termFactory);
        this.parentMemberTypeId =
                termFactory.getConcept(RefsetAuxiliary.Concept.MARKED_PARENT.getUids()).getConceptId();
        this.parentRefsetId = getParentRefset();
    }

    public void addParentMembers(Integer... conceptIds) throws Exception {

        Condition[] traversingConditions = new Condition[] { new NotAlreadyVisited() };

        Set<I_GetConceptData> ancestors = new HashSet<I_GetConceptData>();
        for (Integer conceptId : conceptIds) {
            ancestors.addAll(getAllAncestors(termFactory.getConcept(conceptId), traversingConditions));
        }

        for (I_GetConceptData concept : ancestors) {
            newRefsetExtension(parentRefsetId, concept.getConceptId(), parentMemberTypeId);
        }
    }

    public void addDescriptionParentMembers(Integer... descriptionIds) throws Exception {

        Condition[] traversingConditions = new Condition[] { new NotAlreadyVisited() };

        Set<I_GetConceptData> ancestors = new HashSet<I_GetConceptData>();
        for (Integer descriptionId : descriptionIds) {
            UUID descriptionUuid = termFactory.getId(descriptionId).getUIDs().iterator().next();
            I_GetConceptData concept =
                    termFactory.getConcept(termFactory.getDescription(descriptionUuid.toString()).getConceptId());
            ancestors.addAll(getAllAncestors(concept, traversingConditions));
            // ancestors.add(concept);
        }

        for (I_GetConceptData concept : ancestors) {
            newRefsetExtension(parentRefsetId, concept.getConceptId(), parentMemberTypeId);
        }
    }

    public void removeParentMembers(Integer... conceptIds) throws Exception {
        Condition[] traversingConditions =
                new Condition[] { new HasExtension(parentRefsetId, parentMemberTypeId), new NotAlreadyVisited() };

        // Get all ancestors
        Set<Integer> toBeRetired = new HashSet<Integer>();
        for (Integer conceptId : conceptIds) {
            if (isMarkedParent(conceptId)) {
                toBeRetired.add(conceptId);
            }
            for (I_GetConceptData concept : getAllAncestors(termFactory.getConcept(conceptId), traversingConditions)) {
                toBeRetired.add(concept.getConceptId());
            }
        }

        // For each ancestor, check immediate children for a member of either
        // refset (member or marked parent)
        // that is not in the existing ancestor set. This means we've found a
        // lineage that should not be modified.
        Set<Integer> lineageToExclude = new HashSet<Integer>();
        for (Integer parentId : toBeRetired) {
            for (Integer childId : refsetHelper.getChildrenOfConcept(parentId)) {
                if (!toBeRetired.contains(childId) && (isMarkedParent(childId) || isMember(childId))) {
                    lineageToExclude.add(childId);
                }
            }
        }

        // Reset memory of visited concepts
        traversingConditions =
                new Condition[] { new HasExtension(parentRefsetId, parentMemberTypeId), new NotAlreadyVisited() };

        // Find all ancestors of the lineages not to be modified
        Set<Integer> ancestorIdsToExclude = new HashSet<Integer>();
        for (Integer conceptId : lineageToExclude) {
            for (I_GetConceptData concept : getAllAncestors(termFactory.getConcept(conceptId), traversingConditions)) {
                ancestorIdsToExclude.add(concept.getConceptId());
            }
        }

        // Exclude these lineages
        toBeRetired.removeAll(ancestorIdsToExclude);

        // Retire the rest
        for (Integer markedParentId : toBeRetired) {
            retireRefsetExtension(parentRefsetId, markedParentId, parentMemberTypeId);
        }
    }

    public void removeDescriptionParentMembers(Integer... descriptionIds) throws Exception {
        Condition[] traversingConditions =
                new Condition[] { new HasExtension(parentRefsetId, parentMemberTypeId), new NotAlreadyVisited() };

        // Get all ancestors
        Set<Integer> toBeRetired = new HashSet<Integer>();
        for (Integer descriptionId : descriptionIds) {
            UUID descriptionUuid = termFactory.getId(descriptionId).getUIDs().iterator().next();
            I_GetConceptData concept =
                    termFactory.getConcept(termFactory.getDescription(descriptionUuid.toString()).getConceptId());
            if (isMarkedParent(concept.getConceptId())) {
                toBeRetired.add(concept.getConceptId());
            }
            for (I_GetConceptData ancestor : getAllAncestors(termFactory.getConcept(concept.getConceptId()),
                traversingConditions)) {
                toBeRetired.add(ancestor.getConceptId());
            }
        }

        // For each ancestor, check immediate children for a member of either
        // refset (member or marked parent)
        // that is not in the existing ancestor set. This means we've found a
        // lineage that should not be modified.
        Set<Integer> lineageToExclude = new HashSet<Integer>();
        for (Integer parentId : toBeRetired) {
            for (Integer childId : refsetHelper.getChildrenOfConcept(parentId)) {
                if (!toBeRetired.contains(childId) && (isMarkedParent(childId) || isMember(childId))) {
                    lineageToExclude.add(childId);
                }
            }
        }

        // Reset memory of visited concepts
        traversingConditions =
                new Condition[] { new HasExtension(parentRefsetId, parentMemberTypeId), new NotAlreadyVisited() };

        // Find all ancestors of the lineages not to be modified
        Set<Integer> ancestorIdsToExclude = new HashSet<Integer>();
        for (Integer conceptId : lineageToExclude) {
            for (I_GetConceptData concept : getAllAncestors(termFactory.getConcept(conceptId), traversingConditions)) {
                ancestorIdsToExclude.add(concept.getConceptId());
            }
        }

        // Exclude these lineages
        toBeRetired.removeAll(ancestorIdsToExclude);

        // Retire the rest
        for (Integer markedParentId : toBeRetired) {
            retireRefsetExtension(parentRefsetId, markedParentId, parentMemberTypeId);
        }
    }

    public boolean isMarkedParent(int conceptId) throws Exception {
        return hasCurrentRefsetExtension(parentRefsetId, conceptId, parentMemberTypeId);
    }

    private boolean isMember(int conceptId) throws Exception {
        return hasCurrentRefsetExtension(refsetId, conceptId, memberTypeId);
    }

    public int getParentRefset() throws Exception {

        I_GetConceptData memberRefset = termFactory.getConcept(refsetId);

        I_IntSet allowedType = termFactory.newIntSet();
        allowedType.add(termFactory.getConcept(RefsetAuxiliary.Concept.MARKED_PARENT_REFSET.getUids()).getConceptId());

        Set<? extends I_GetConceptData> targetParentRefsets =
                memberRefset.getSourceRelTargets(getAllowedStatuses(), allowedType, null, false, true);

        if (targetParentRefsets == null || targetParentRefsets.size() == 0) {
            throw new TerminologyException("Unable to locate parent member refset for '"
                + memberRefset.getInitialText() + "'");
        }
        if (targetParentRefsets.size() > 1) {
            logger.warning("More than one parent member refset found for '" + memberRefset.getInitialText() + "'"
                + "Defaulting to the first one found!");
        }
        I_GetConceptData parentRefset = targetParentRefsets.iterator().next();
        return parentRefset.getConceptId();
    }

    /**
     * Check for a is_a relationship type defined on the member refset concept
     * otherwise default to just using
     * either a SNOMED or ArchitectonicAuxiliary is_a relationship type
     */
    @Override
    protected I_IntSet getIsARelTypes() throws Exception {
        if (this.isARelTypes == null) {
            try {
                I_IntSet isATypes = termFactory.newIntSet();
                isATypes.add(termFactory.getConcept(RefsetAuxiliary.Concept.MARKED_PARENT_IS_A_TYPE.getUids())
                    .getConceptId());
                I_GetConceptData memberRefset = termFactory.getConcept(this.refsetId);
                Set<? extends I_GetConceptData> requiredIsAType =
                        memberRefset.getSourceRelTargets(getAllowedStatuses(), isATypes, null, false, true);

                if (requiredIsAType != null && requiredIsAType.size() > 0) {
                    // relationship exists so use the is-a specified by the
                    // marked-parent-is-a relationship
                    this.isARelTypes = termFactory.newIntSet();
                    for (I_GetConceptData concept : requiredIsAType) {
                        this.isARelTypes.add(concept.getConceptId());
                    }

                    // Added for backwards compatability. All newly created refset specs will have one or more
                    // relationships specifiying the relationship types to use. e.g. if in a database with Snomed IS-a
                    // and the AA is-a, it will have 2 relationships... one to each. Previously only one relationship
                    // would have been created (to the SNOMED is-a), so this step ensures that the AA is-a is also added
                    // in SNOMED databases.
                    if (!this.isARelTypes.contains(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid())) {
                        isARelTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
                    }
                } else {
                    // no specified marked-parent-is-a relationship defined, so
                    // first default to using SNOMED and ArchitectonicAuxiliary is_a relationship type
                    super.getIsARelTypes();
                }
            } catch (NoMappingException ex) {
                // marked-parent-is-a relationship type is unknown so default
                super.getIsARelTypes();
            }
        }
        return this.isARelTypes;
    }

    public boolean hasCurrentMarkedParentExtension(int conceptId) throws Exception {
        return super.hasCurrentRefsetExtension(parentRefsetId, conceptId, parentMemberTypeId);
    }

    /**
     * Utilises the {@link RefsetUtilities} class by injecting the db
     */
    private class RefsetHelper extends RefsetUtilities {
        public RefsetHelper(I_TermFactory termFactory) {
            super.termFactory = termFactory;
        }
    }

    private class HasExtension implements Condition {
        private int refsetId;
        private int memberTypeId;

        public HasExtension(int refsetId, int memberTypeId) {
            this.refsetId = refsetId;
            this.memberTypeId = memberTypeId;
        }

        public boolean evaluate(I_GetConceptData concept) throws Exception {
            return hasCurrentRefsetExtension(this.refsetId, concept.getConceptId(), this.memberTypeId);
        }
    }

    private class NotAlreadyVisited implements Condition {
        private HashSet<Integer> visited = new HashSet<Integer>();

        public boolean evaluate(I_GetConceptData concept) throws Exception {
            return visited.add(concept.getConceptId());
        }
    }

    private class OrOperator implements Condition {
        private Condition[] conditions;

        public OrOperator(Condition... conditions) {
            this.conditions = conditions;
        }

        public boolean evaluate(I_GetConceptData concept) throws Exception {
            for (Condition condition : this.conditions) {
                if (condition.evaluate(concept)) {
                    return true;
                }
            }
            return false;
        }
    }
}
