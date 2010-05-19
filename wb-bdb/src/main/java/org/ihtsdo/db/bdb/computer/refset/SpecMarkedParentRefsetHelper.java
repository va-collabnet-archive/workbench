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
package org.ihtsdo.db.bdb.computer.refset;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.refset.spec.I_HelpMarkedParentRefset;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.TerminologyException;

public class SpecMarkedParentRefsetHelper extends SpecRefsetHelper implements I_HelpMarkedParentRefset {


    private Logger logger = Logger.getLogger(SpecMarkedParentRefsetHelper.class.getName());

    private int refsetId;
    private int memberTypeId;
    private int parentRefsetId;
    private int parentMemberTypeId;

    public SpecMarkedParentRefsetHelper(I_ConfigAceFrame config, int refsetId, int memberTypeId) throws Exception {
        super(config);
        this.refsetId = refsetId;
        this.memberTypeId = memberTypeId;
        this.parentMemberTypeId =
                Terms.get().getConcept(RefsetAuxiliary.Concept.MARKED_PARENT.getUids()).getConceptId();
        this.parentRefsetId = getParentRefset();
    }

    /* (non-Javadoc)
	 * @see org.dwfa.ace.refset.spec.I_HelpMarkedParentRefset#addParentMembers(java.lang.Integer)
	 */
    public void addParentMembers(Integer... conceptIds) throws Exception {

    	LineageCondition[] traversingConditions = new LineageCondition[] { new NotAlreadyVisited() };

        Set<I_GetConceptData> ancestors = new HashSet<I_GetConceptData>();
        for (Integer conceptId : conceptIds) {
            ancestors.addAll(getAllAncestors(Terms.get().getConcept(conceptId), traversingConditions));
        }

        for (I_GetConceptData concept : ancestors) {
            newRefsetExtension(parentRefsetId, concept.getConceptId(), parentMemberTypeId);
        }
    }

    /* (non-Javadoc)
	 * @see org.dwfa.ace.refset.spec.I_HelpMarkedParentRefset#addDescriptionParentMembers(java.lang.Integer)
	 */
    public void addDescriptionParentMembers(Integer... descriptionIds) throws Exception {

    	LineageCondition[] traversingConditions = new LineageCondition[] { new NotAlreadyVisited() };

        Set<I_GetConceptData> ancestors = new HashSet<I_GetConceptData>();
        for (Integer descriptionId : descriptionIds) {
            I_GetConceptData concept =
                    Terms.get().getConcept(Terms.get().getDescription(descriptionId).getConceptId());
            ancestors.addAll(getAllAncestors(concept, traversingConditions));
            // ancestors.add(concept);
        }

        for (I_GetConceptData concept : ancestors) {
            newRefsetExtension(parentRefsetId, concept.getConceptId(), parentMemberTypeId);
        }
    }

    /* (non-Javadoc)
	 * @see org.dwfa.ace.refset.spec.I_HelpMarkedParentRefset#removeParentMembers(java.lang.Integer)
	 */
    public void removeParentMembers(Integer... conceptIds) throws Exception {
    	LineageCondition[] traversingConditions =
                new LineageCondition[] { new HasExtension(parentRefsetId, parentMemberTypeId), new NotAlreadyVisited() };

        // Get all ancestors
        Set<Integer> toBeRetired = new HashSet<Integer>();
        for (Integer conceptId : conceptIds) {
            if (isMarkedParent(conceptId)) {
                toBeRetired.add(conceptId);
            }
            for (I_GetConceptData concept : getAllAncestors(Terms.get().getConcept(conceptId), traversingConditions)) {
                toBeRetired.add(concept.getConceptId());
            }
        }

        // For each ancestor, check immediate children for a member of either
        // refset (member or marked parent)
        // that is not in the existing ancestor set. This means we've found a
        // lineage that should not be modified.
        Set<Integer> lineageToExclude = new HashSet<Integer>();
        for (Integer parentId : toBeRetired) {
            for (Integer childId : Terms.get().getRefsetHelper(Terms.get().getActiveAceFrameConfig()).getChildrenOfConcept(parentId)) {
                if (!toBeRetired.contains(childId) && (isMarkedParent(childId) || isMember(childId))) {
                    lineageToExclude.add(childId);
                }
            }
        }

        // Reset memory of visited concepts
        traversingConditions =
                new LineageCondition[] { new HasExtension(parentRefsetId, parentMemberTypeId), new NotAlreadyVisited() };

        // Find all ancestors of the lineages not to be modified
        Set<Integer> ancestorIdsToExclude = new HashSet<Integer>();
        for (Integer conceptId : lineageToExclude) {
            for (I_GetConceptData concept : getAllAncestors(Terms.get().getConcept(conceptId), traversingConditions)) {
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

    /* (non-Javadoc)
	 * @see org.dwfa.ace.refset.spec.I_HelpMarkedParentRefset#removeDescriptionParentMembers(java.lang.Integer)
	 */
    public void removeDescriptionParentMembers(Integer... descriptionIds) throws Exception {
    	LineageCondition[] traversingConditions =
                new LineageCondition[] { new HasExtension(parentRefsetId, parentMemberTypeId), new NotAlreadyVisited() };

        // Get all ancestors
        Set<Integer> toBeRetired = new HashSet<Integer>();
        for (Integer descriptionId : descriptionIds) {
            I_GetConceptData concept =
                    Terms.get().getConcept(Terms.get().getDescription(descriptionId).getConceptId());
            if (isMarkedParent(concept.getConceptId())) {
                toBeRetired.add(concept.getConceptId());
            }
            for (I_GetConceptData ancestor : getAllAncestors(Terms.get().getConcept(concept.getConceptId()),
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
            for (Integer childId : Terms.get().getRefsetHelper(getConfig()).getChildrenOfConcept(parentId)) {
                if (!toBeRetired.contains(childId) && (isMarkedParent(childId) || isMember(childId))) {
                    lineageToExclude.add(childId);
                }
            }
        }

        // Reset memory of visited concepts
        traversingConditions =
                new LineageCondition[] { new HasExtension(parentRefsetId, parentMemberTypeId), new NotAlreadyVisited() };

        // Find all ancestors of the lineages not to be modified
        Set<Integer> ancestorIdsToExclude = new HashSet<Integer>();
        for (Integer conceptId : lineageToExclude) {
            for (I_GetConceptData concept : getAllAncestors(Terms.get().getConcept(conceptId), traversingConditions)) {
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

    /* (non-Javadoc)
	 * @see org.dwfa.ace.refset.spec.I_HelpMarkedParentRefset#isMarkedParent(int)
	 */
    public boolean isMarkedParent(int conceptId) throws Exception {
        return hasCurrentRefsetExtension(parentRefsetId, conceptId, parentMemberTypeId);
    }

    private boolean isMember(int conceptId) throws Exception {
        return hasCurrentRefsetExtension(refsetId, conceptId, memberTypeId);
    }

    /* (non-Javadoc)
	 * @see org.dwfa.ace.refset.spec.I_HelpMarkedParentRefset#getParentRefset()
	 */
    public int getParentRefset() throws Exception {

        I_GetConceptData memberRefset = Terms.get().getConcept(refsetId);

        I_IntSet allowedType = Terms.get().newIntSet();
        allowedType.add(Terms.get().getConcept(RefsetAuxiliary.Concept.MARKED_PARENT_REFSET.getUids()).getConceptId());

        Set<? extends I_GetConceptData> targetParentRefsets =
                memberRefset.getSourceRelTargets(getAllowedStatuses(), allowedType, null,
                    getConfig().getPrecedence(), getConfig().getConflictResolutionStrategy());

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
                I_IntSet isATypes = Terms.get().newIntSet();
                isATypes.add(Terms.get().getConcept(RefsetAuxiliary.Concept.MARKED_PARENT_IS_A_TYPE.getUids())
                    .getConceptId());
                I_GetConceptData memberRefset = Terms.get().getConcept(this.refsetId);
                Set<? extends I_GetConceptData> requiredIsAType =
                        memberRefset.getSourceRelTargets(getAllowedStatuses(), isATypes, null,
                            getConfig().getPrecedence(), getConfig().getConflictResolutionStrategy());

                if (requiredIsAType != null && requiredIsAType.size() > 0) {
                    // relationship exists so use the is-a specified by the
                    // marked-parent-is-a relationship
                    this.isARelTypes = Terms.get().newIntSet();
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

    /* (non-Javadoc)
	 * @see org.dwfa.ace.refset.spec.I_HelpMarkedParentRefset#hasCurrentMarkedParentExtension(int)
	 */
    public boolean hasCurrentMarkedParentExtension(int conceptId) throws Exception {
        return super.hasCurrentRefsetExtension(parentRefsetId, conceptId, parentMemberTypeId);
    }


    private class HasExtension implements LineageCondition {
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

    private class NotAlreadyVisited implements LineageCondition {
        private HashSet<Integer> visited = new HashSet<Integer>();

        public boolean evaluate(I_GetConceptData concept) throws Exception {
            return visited.add(concept.getConceptId());
        }
    }
}
