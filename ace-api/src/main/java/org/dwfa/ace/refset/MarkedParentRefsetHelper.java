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
package org.dwfa.ace.refset;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.dwfa.ace.api.BeanPropertyMap;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LineageHelper.FirstRelationOnly;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.ThinExtByRefPartProperty;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.TerminologyException;

public class MarkedParentRefsetHelper extends RefsetHelper {

    protected RefsetHelper refsetHelper;

    private Logger logger = Logger.getLogger(MarkedParentRefsetHelper.class.getName());

    private int refsetId;
    private int memberTypeId;
    private int parentRefsetId;
    private int parentMemberTypeId;

    public MarkedParentRefsetHelper(int refsetId, int memberTypeId) throws Exception {
        super();
        this.refsetId = refsetId;
        this.memberTypeId = memberTypeId;
        this.refsetHelper = new RefsetHelper(termFactory);
        this.parentMemberTypeId = termFactory.getConcept(RefsetAuxiliary.Concept.MARKED_PARENT.getUids())
            .getConceptId();
        this.parentRefsetId = getParentRefset();
    }

    public void addParentMembers(Integer... conceptIds) throws Exception {

        Condition[] traversingConditions = new Condition[] { new NotAlreadyVisited() };

        Set<I_GetConceptData> ancestors = new HashSet<I_GetConceptData>();
        for (Integer conceptId : conceptIds) {
            ancestors.addAll(getAllAncestors(termFactory.getConcept(conceptId), traversingConditions));
        }

        for (I_GetConceptData concept : ancestors) {
            newRefsetExtension(parentRefsetId, concept.getConceptId(), I_ThinExtByRefPartConcept.class,
                new BeanPropertyMap().with(ThinExtByRefPartProperty.CONCEPT_ONE, parentMemberTypeId));
        }
    }

    public void removeParentMembers(Integer... conceptIds) throws Exception {
        Condition[] traversingConditions = new Condition[] { new HasExtension(parentRefsetId, parentMemberTypeId),
                                                            new NotAlreadyVisited() };

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
            for (I_GetConceptData child : getAllDescendants(termFactory.getConcept(parentId), new FirstRelationOnly())) {
                Integer childId = child.getConceptId();
                if (!toBeRetired.contains(childId) && (isMarkedParent(childId) || isMember(childId))) {
                    lineageToExclude.add(childId);
                }
            }
        }

        // Reset memory of visited concepts
        traversingConditions = new Condition[] { new HasExtension(parentRefsetId, parentMemberTypeId),
                                                new NotAlreadyVisited() };

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
            retireRefsetExtension(parentRefsetId, markedParentId, new BeanPropertyMap().with(
                ThinExtByRefPartProperty.CONCEPT_ONE, parentMemberTypeId));
        }
    }

    public boolean isMarkedParent(int conceptId) throws Exception {
        return hasCurrentRefsetExtension(parentRefsetId, conceptId, new BeanPropertyMap().with(
            ThinExtByRefPartProperty.CONCEPT_ONE, parentMemberTypeId));
    }

    private boolean isMember(int conceptId) throws Exception {
        return hasCurrentRefsetExtension(refsetId, conceptId, new BeanPropertyMap().with(
            ThinExtByRefPartProperty.CONCEPT_ONE, memberTypeId));
    }

    public int getParentRefset() throws Exception {

        I_GetConceptData memberRefset = termFactory.getConcept(refsetId);

        I_IntSet allowedType = termFactory.newIntSet();
        allowedType.add(termFactory.getConcept(RefsetAuxiliary.Concept.MARKED_PARENT_REFSET.getUids()).getConceptId());

        Set<I_GetConceptData> targetParentRefsets = memberRefset.getSourceRelTargets(getAllowedStatuses(), allowedType,
            null, false, true);

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
                Set<I_GetConceptData> requiredIsAType = memberRefset.getSourceRelTargets(getAllowedStatuses(),
                    isATypes, null, false, true);

                if (requiredIsAType != null && requiredIsAType.size() > 0) {
                    // relationship exists so use the is-a specified by the
                    // marked-parent-is-a relationship
                    this.isARelTypes = termFactory.newIntSet();
                    this.isARelTypes.add(requiredIsAType.iterator().next().getConceptId());
                } else {
                    // no specified marked-parent-is-a relationship defined, so
                    // first default to using
                    // SNOMED or ArchitectonicAuxiliary is_a relationship type
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
        return super.hasCurrentRefsetExtension(parentRefsetId, conceptId, new BeanPropertyMap().with(
            ThinExtByRefPartProperty.CONCEPT_ONE, parentMemberTypeId));
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
