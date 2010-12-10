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
import org.dwfa.ace.api.I_HelpMarkedParentRefsets;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

public class MarkedParentRefsetHelper extends RefsetHelper implements I_HelpMarkedParentRefsets {

    private Logger logger = Logger.getLogger(MarkedParentRefsetHelper.class.getName());

    private int refsetId;
    private int memberTypeId;
    private int parentRefsetId;
    private int parentMemberTypeId;

    public MarkedParentRefsetHelper(I_ConfigAceFrame config, int refsetId, int memberTypeId) throws Exception {
        super(config);
        this.refsetId = refsetId;
        this.memberTypeId = memberTypeId;
        this.parentMemberTypeId = Terms.get().getConcept(RefsetAuxiliary.Concept.MARKED_PARENT.getUids())
            .getConceptNid();
        this.parentRefsetId = getParentRefset();
    }

    /* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.computer.refset.I_HelpMarkedParentRefsets#addParentMembers(java.lang.Integer)
	 */
    public void addParentMembers(Integer... conceptIds) throws Exception {

    	LineageCondition[] traversingConditions = new LineageCondition[] { new NotAlreadyVisited() };

        Set<I_GetConceptData> ancestors = new HashSet<I_GetConceptData>();
        for (Integer conceptId : conceptIds) {
            ancestors.addAll(getAllAncestors(Terms.get().getConcept(conceptId), traversingConditions));
        }

        for (I_GetConceptData concept : ancestors) {
            newRefsetExtension(parentRefsetId, concept.getConceptNid(), REFSET_TYPES.CID,
                new RefsetPropertyMap().with(RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, parentMemberTypeId),
                getConfig());
        }
    }

    /* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.computer.refset.I_HelpMarkedParentRefsets#removeParentMembers(java.lang.Integer)
	 */
    public void removeParentMembers(Integer... conceptIds) throws Exception {
    	LineageCondition[] traversingConditions = new LineageCondition[] { new HasExtension(parentRefsetId, parentMemberTypeId),
                                                            new NotAlreadyVisited() };

        // Get all ancestors
        Set<Integer> toBeRetired = new HashSet<Integer>();
        for (Integer conceptId : conceptIds) {
            if (isMarkedParent(conceptId)) {
                toBeRetired.add(conceptId);
            }
            for (I_GetConceptData concept : getAllAncestors(Terms.get().getConcept(conceptId), traversingConditions)) {
                toBeRetired.add(concept.getConceptNid());
            }
        }

        // For each ancestor, check immediate children for a member of either
        // refset (member or marked parent)
        // that is not in the existing ancestor set. This means we've found a
        // lineage that should not be modified.
        Set<Integer> lineageToExclude = new HashSet<Integer>();
        for (Integer parentId : toBeRetired) {
            for (Integer childId : 
            	Terms.get().getRefsetHelper(Terms.get().getActiveAceFrameConfig()).getChildrenOfConcept(parentId)) {
                if (!toBeRetired.contains(childId) && (isMarkedParent(childId) || isMember(childId))) {
                    lineageToExclude.add(childId);
                }
            }
        }

        // Reset memory of visited concepts
        traversingConditions = new LineageCondition[] { new HasExtension(parentRefsetId, parentMemberTypeId),
                                                new NotAlreadyVisited() };

        // Find all ancestors of the lineages not to be modified
        Set<Integer> ancestorIdsToExclude = new HashSet<Integer>();
        for (Integer conceptId : lineageToExclude) {
            for (I_GetConceptData concept : getAllAncestors(Terms.get().getConcept(conceptId), traversingConditions)) {
                ancestorIdsToExclude.add(concept.getConceptNid());
            }
        }

        // Exclude these lineages
        toBeRetired.removeAll(ancestorIdsToExclude);

        // Retire the rest
        for (Integer markedParentId : toBeRetired) {
            retireRefsetExtension(parentRefsetId, markedParentId, new RefsetPropertyMap().with(
            		RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, parentMemberTypeId));
        }
    }

    /* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.computer.refset.I_HelpMarkedParentRefsets#isMarkedParent(int)
	 */
    public boolean isMarkedParent(int conceptId) throws Exception {
        return hasCurrentRefsetExtension(parentRefsetId, conceptId, new RefsetPropertyMap().with(
        		RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, parentMemberTypeId));
    }

    private boolean isMember(int conceptId) throws Exception {
        return hasCurrentRefsetExtension(refsetId, conceptId, new RefsetPropertyMap().with(
        		RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, memberTypeId));
    }

    /* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.computer.refset.I_HelpMarkedParentRefsets#getParentRefset()
	 */
    public int getParentRefset() throws Exception {

        I_GetConceptData memberRefset = Terms.get().getConcept(refsetId);

        I_IntSet allowedType = Terms.get().newIntSet();
        allowedType.add(Terms.get().getConcept(RefsetAuxiliary.Concept.MARKED_PARENT_REFSET.getUids()).getConceptNid());

        Set<? extends I_GetConceptData> targetParentRefsets = memberRefset.getSourceRelTargets(getAllowedStatuses(), allowedType,
            null,
            getConfig().getPrecedence(), getConfig().getConflictResolutionStrategy());

        if (targetParentRefsets == null || targetParentRefsets.size() == 0) {
        	AceLog.getAppLog().warning("Unable to locate parent member refset for '"
                + memberRefset.getInitialText() + "'");
        }
        if (targetParentRefsets.size() > 1) {
            logger.warning("More than one parent member refset found for '" + memberRefset.getInitialText() + "'"
                + "Defaulting to the first one found!");
        }
        if (targetParentRefsets != null && !targetParentRefsets.isEmpty()) {
            I_GetConceptData parentRefset = targetParentRefsets.iterator().next();
            return parentRefset.getConceptNid();
        }
        return Integer.MIN_VALUE;
    }

    /* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.computer.refset.I_HelpMarkedParentRefsets#hasCurrentMarkedParentExtension(int)
	 */
    public boolean hasCurrentMarkedParentExtension(int conceptId) throws Exception {
        return super.hasCurrentRefsetExtension(parentRefsetId, conceptId, new RefsetPropertyMap().with(
        		RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, parentMemberTypeId));
    }

}
