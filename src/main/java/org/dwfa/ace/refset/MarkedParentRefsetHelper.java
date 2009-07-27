package org.dwfa.ace.refset;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class MarkedParentRefsetHelper extends RefsetHelper {

    protected RefsetHelper refsetHelper;

    private Logger logger = Logger.getLogger(MarkedParentRefsetHelper.class
            .getName());

    private int refsetId;
    private int memberTypeId;
    private int parentRefsetId;
    private int parentMemberTypeId;

    public MarkedParentRefsetHelper(int refsetId, int memberTypeId)
            throws Exception {
        super();
        this.refsetId = refsetId;
        this.memberTypeId = memberTypeId;
        this.refsetHelper = new RefsetHelper(termFactory);
        // this.parentMemberTypeId =
        // ConceptConstants.PARENT_MARKER.localize().getNid();
        this.parentMemberTypeId = termFactory.getConcept(
                RefsetAuxiliary.Concept.MARKED_PARENT.getUids()).getConceptId();
        this.parentRefsetId = getParentRefset();
    }

    public void addParentMembers(Integer... conceptIds) throws Exception {

        Condition[] traversingConditions = new Condition[] { new NotAlreadyVisited() };

        Set<I_GetConceptData> ancestors = new HashSet<I_GetConceptData>();
        for (Integer conceptId : conceptIds) {
            ancestors.addAll(getAllAncestors(termFactory.getConcept(conceptId),
                    termFactory.getConcept(refsetId), traversingConditions));
        }
        for (I_GetConceptData concept : ancestors) {
            newRefsetExtension(parentRefsetId, concept.getConceptId(),
                    parentMemberTypeId);
        }
    }

    public void removeParentMembers(Integer... conceptIds) throws Exception {
        Condition[] traversingConditions = new Condition[] {
                new HasExtension(parentRefsetId, parentMemberTypeId),
                new NotAlreadyVisited() };

        // Get all ancestors
        Set<Integer> toBeRetired = new HashSet<Integer>();
        for (Integer conceptId : conceptIds) {
            if (isMarkedParent(conceptId)) {
                toBeRetired.add(conceptId);
            }
            for (I_GetConceptData concept : getAllAncestors(termFactory
                    .getConcept(conceptId), termFactory.getConcept(refsetId),
                    traversingConditions)) {
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
                if (!toBeRetired.contains(childId)
                        && (isMarkedParent(childId) || isMember(childId))) {
                    lineageToExclude.add(childId);
                }
            }
        }

        // Reset memory of visited concepts
        traversingConditions = new Condition[] {
                new HasExtension(parentRefsetId, parentMemberTypeId),
                new NotAlreadyVisited() };

        // Find all ancestors of the lineages not to be modified
        Set<Integer> ancestorIdsToExclude = new HashSet<Integer>();
        for (Integer conceptId : lineageToExclude) {
            for (I_GetConceptData concept : getAllAncestors(termFactory
                    .getConcept(conceptId), termFactory.getConcept(refsetId),
                    traversingConditions)) {
                ancestorIdsToExclude.add(concept.getConceptId());
            }
        }

        // Exclude these lineages
        toBeRetired.removeAll(ancestorIdsToExclude);

        // Retire the rest
        for (Integer markedParentId : toBeRetired) {
            retireRefsetExtension(parentRefsetId, markedParentId,
                    parentMemberTypeId);
        }
    }

    public boolean isMarkedParent(int conceptId) throws Exception {
        return hasCurrentRefsetExtension(parentRefsetId, conceptId,
                parentMemberTypeId);
    }

    private boolean isMember(int conceptId) throws Exception {
        return hasCurrentRefsetExtension(refsetId, conceptId, memberTypeId);
    }

    public int getParentRefset() throws Exception {

        I_GetConceptData memberRefset = termFactory.getConcept(refsetId);

        I_IntSet allowedStatus = termFactory.newIntSet();
        allowedStatus.add(termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.CURRENT.getUids())
                .getConceptId());

        I_IntSet allowedType = termFactory.newIntSet();
        allowedType.add(termFactory.getConcept(
                RefsetAuxiliary.Concept.MARKED_PARENT_REFSET.getUids())
                .getConceptId());

        Set<I_GetConceptData> targetParentRefsets = memberRefset
                .getSourceRelTargets(allowedStatus, allowedType, null, false);

        if (targetParentRefsets == null || targetParentRefsets.size() == 0) {
            throw new TerminologyException(
                    "Unable to locate parent member refset for '"
                            + memberRefset.getInitialText() + "'");
        }
        if (targetParentRefsets.size() > 1) {
            logger.warning("More than one parent member refset found for '"
                    + memberRefset.getInitialText() + "'"
                    + "Defaulting to the first one found!");
        }
        I_GetConceptData parentRefset = targetParentRefsets.iterator().next();
        return parentRefset.getConceptId();
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
            return hasCurrentRefsetExtension(this.refsetId, concept
                    .getConceptId(), this.memberTypeId);
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
