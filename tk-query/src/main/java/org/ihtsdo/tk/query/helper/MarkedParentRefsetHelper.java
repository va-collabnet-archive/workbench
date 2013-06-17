/**
 * Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.ihtsdo.tk.query.helper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;

/**
 * The class SpecMarkedParentRefsetHelper contains methods for working with marked parents.
 */
public class MarkedParentRefsetHelper extends RefsetHelper{

    private static final Logger logger = Logger.getLogger(MarkedParentRefsetHelper.class.getName());
    private int refsetId;
    private int parentRefsetId;
    private int parentMemberTypeId;
    Collection<Integer> markedParentNids;
    Collection<Integer> refsetMemberNids;

    public MarkedParentRefsetHelper(ViewCoordinate viewCoordinate, EditCoordinate editCoordinate, int refsetId) throws Exception {
        super(viewCoordinate, editCoordinate);
        this.refsetId = refsetId;
        this.parentMemberTypeId =
                ts.getNidForUuids(RefsetAuxiliary.Concept.MARKED_PARENT.getUids());
        this.parentRefsetId = getParentRefsetNid();
        ConceptVersionBI refset = ts.getConceptVersion(viewCoordinate, refsetId);
        refsetMemberNids = refset.getRefsetMemberNidsActive();
    }

    public void addParentMembers(Integer... conceptNids) throws Exception {
        for (int conceptId : conceptNids) {
            newRefsetExtension(parentRefsetId, conceptId, parentMemberTypeId);
        }
    }

    public void addDescriptionParentMembers(Integer... descriptionNids) throws Exception {
        Set<Integer> ancestors = new HashSet<>();
        for (Integer descNid : descriptionNids) {
            ancestors.addAll(ts.getAncestors(ts.getConceptNidForNid(descNid), vc));
        }

        for (int conceptNid : ancestors) {
            newRefsetExtension(parentRefsetId, conceptNid, parentMemberTypeId);
        }
    }

    public void removeParentMembers(Integer... conceptIds) throws Exception {
        // Get all ancestors
        for (Integer conceptId : conceptIds) {
            RefexChronicleBI retireRefsetExtension = retireRefsetExtension(parentRefsetId, conceptId, parentMemberTypeId);
        }
    }

    public void removeDescriptionParentMembers(Integer... descriptionNids) throws Exception {
        // Get all ancestors
        Set<Integer> toBeRetired = new HashSet<Integer>();
        for (Integer descNid : descriptionNids) {
            int conceptNid = Ts.get().getConceptNidForNid(descNid);
            if (isMarkedParent(conceptNid)) {
                toBeRetired.add(conceptNid);
            }
            toBeRetired.addAll(ts.getAncestors(conceptNid, vc));
        }

        // For each ancestor, check immediate children for a member of either
        // refset (member or marked parent)
        // that is not in the existing ancestor set. This means we've found a
        // lineage that should not be modified.
        Set<Integer> lineageToExclude = new HashSet<Integer>();
        for (Integer parentId : toBeRetired) {
            for (Integer childId : ts.getChildren(parentId, vc)) {
                if (!toBeRetired.contains(childId) && (isMarkedParent(childId) || isMember(childId))) {
                    lineageToExclude.add(childId);
                }
            }
        }

        // Find all ancestors of the lineages not to be modified
        Set<Integer> ancestorIdsToExclude = new HashSet<Integer>();
        for (Integer conceptId : lineageToExclude) {
           ancestorIdsToExclude.addAll(ts.getAncestors(conceptId, vc));
        }

        // Exclude these lineages
        toBeRetired.removeAll(ancestorIdsToExclude);

        // Retire the rest
        for (Integer markedParentId : toBeRetired) {
            retireRefsetExtension(parentRefsetId, markedParentId, parentMemberTypeId);
        }
    }

    public boolean isMarkedParent(int conceptNid) throws Exception {
        ConceptVersionBI concept = ts.getConceptVersion(vc, parentRefsetId);
        return concept.hasRefsetMemberActiveForComponent(conceptNid);
    }

    private boolean isMember(int conceptNid) throws Exception {
        ConceptVersionBI concept = ts.getConceptVersion(vc, refsetId);
        return concept.hasRefsetMemberActiveForComponent(conceptNid);
    }

    public int getParentRefsetNid() throws Exception {
        if (parentRefsetId == 0) {
            ConceptVersionBI refsetConcept = ts.getConceptVersion(vc, refsetId);
            int markedParentNid = ts.getNidForUuids(RefsetAuxiliary.Concept.MARKED_PARENT_REFSET.getUids());
            //TODO: should this just get the nid and avoid getting the concept?
            Collection<? extends ConceptVersionBI> targetParentRefsets = refsetConcept.getRelationshipsOutgoingTargetConceptsActive(markedParentNid);
            if (targetParentRefsets == null || targetParentRefsets.isEmpty()) {
                throw new TerminologyException("Unable to locate parent member refset for '"
                        + refsetConcept.toUserString() + "'");
            }
            if (targetParentRefsets.size() > 1) {
                logger.log(Level.WARNING, "More than one parent member refset found for ''{0}" + "''"
                        + "Defaulting to the first one found!", refsetConcept.toUserString());
            }

            ConceptChronicleBI parentRefset = targetParentRefsets.iterator().next();
            return parentRefset.getConceptNid();
        }
        return parentRefsetId;
    }
}
