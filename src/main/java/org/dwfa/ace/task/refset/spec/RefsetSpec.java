package org.dwfa.ace.task.refset.spec;

import java.util.List;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.RefsetAuxiliary;

public class RefsetSpec {

    private I_GetConceptData spec;
    private I_TermFactory termFactory;

    public RefsetSpec(I_GetConceptData spec) {
        this.spec = spec;
        termFactory = LocalVersionedTerminology.get();
    }

    public I_GetConceptData getRefsetSpecConcept() {
        return spec;
    }

    public I_GetConceptData getMemberRefsetConcept() {
        try {
            I_GetConceptData specifiesRefsetRel =
                    termFactory.getConcept(RefsetAuxiliary.Concept.SPECIFIES_REFSET.getUids());
            return getLatestRelationshipTarget(getRefsetSpecConcept(), specifiesRefsetRel);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public I_GetConceptData getMarkedParentRefsetConcept() {
        try {
            I_GetConceptData markedParentRel =
                    termFactory.getConcept(RefsetAuxiliary.Concept.MARKED_PARENT_REFSET.getUids());
            I_GetConceptData memberRefsetConcept = getMemberRefsetConcept();
            if (memberRefsetConcept == null) {
                return null;
            }

            return getLatestRelationshipTarget(memberRefsetConcept, markedParentRel);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public I_GetConceptData getCommentsRefsetConcept() {
        try {
            I_GetConceptData commentsRel = termFactory.getConcept(RefsetAuxiliary.Concept.COMMENTS_REL.getUids());
            I_GetConceptData memberRefsetConcept = getMemberRefsetConcept();
            if (memberRefsetConcept == null) {
                return null;
            }

            return getLatestRelationshipTarget(memberRefsetConcept, commentsRel);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public I_GetConceptData getPromotionRefsetConcept() {
        try {
            I_GetConceptData promotionRel = termFactory.getConcept(RefsetAuxiliary.Concept.PROMOTION_REL.getUids());
            I_GetConceptData memberRefsetConcept = getMemberRefsetConcept();
            if (memberRefsetConcept == null) {
                return null;
            }

            return getLatestRelationshipTarget(memberRefsetConcept, promotionRel);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets the latest specified relationship's target.
     * 
     * @param relationshipType
     * @return
     * @throws Exception
     */
    public I_GetConceptData getLatestRelationshipTarget(I_GetConceptData concept, I_GetConceptData relationshipType)
            throws Exception {

        I_GetConceptData latestTarget = null;
        int latestVersion = Integer.MIN_VALUE;

        I_IntSet allowedTypes = LocalVersionedTerminology.get().newIntSet();
        allowedTypes.add(relationshipType.getConceptId());

        List<I_RelTuple> relationships = concept.getSourceRelTuples(null, allowedTypes, null, true, true);
        for (I_RelTuple rel : relationships) {
            if (rel.getVersion() > latestVersion) {
                latestVersion = rel.getVersion();
                latestTarget = LocalVersionedTerminology.get().getConcept(rel.getC2Id());
            }
        }

        return latestTarget;
    }

    /**
     * Gets the latest specified relationship's target.
     * 
     * @param relationshipType
     * @return
     * @throws Exception
     */
    public I_RelTuple getLatestRelationship(I_GetConceptData concept, I_GetConceptData relationshipType)
            throws Exception {

        I_RelTuple latestRel = null;
        int latestVersion = Integer.MIN_VALUE;

        I_IntSet allowedTypes = LocalVersionedTerminology.get().newIntSet();
        allowedTypes.add(relationshipType.getConceptId());

        List<I_RelTuple> relationships = concept.getSourceRelTuples(null, allowedTypes, null, true, true);
        for (I_RelTuple rel : relationships) {
            if (rel.getVersion() > latestVersion) {
                latestVersion = rel.getVersion();
                latestRel = rel;
            }
        }

        return latestRel;
    }
}
