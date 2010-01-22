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

    /**
     * Use this constructor if you wish to input the refset spec concept.
     * 
     * @param spec
     */
    public RefsetSpec(I_GetConceptData spec) {
        this.spec = spec;
        termFactory = LocalVersionedTerminology.get();
    }

    /**
     * Use this constructor if you wish to input the member refset concept,
     * rather than the refset spec concept.
     * 
     * @param concept
     * @param memberRefsetInputted
     */
    public RefsetSpec(I_GetConceptData concept, boolean memberRefsetInputted) {
        termFactory = LocalVersionedTerminology.get();
        if (memberRefsetInputted) {
            try {
                I_GetConceptData specifiesRefsetRel =
                        termFactory.getConcept(RefsetAuxiliary.Concept.SPECIFIES_REFSET.getUids());
                this.spec = getLatestDestinationRelationshipSource(concept, specifiesRefsetRel);
                termFactory = LocalVersionedTerminology.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            this.spec = concept;
        }
    }

    public boolean isConceptComputeType() {
        try {
            I_GetConceptData refsetComputeTypeRel =
                    termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_COMPUTE_TYPE_REL.getUids());
            I_GetConceptData computeType = getLatestSourceRelationshipTarget(spec, refsetComputeTypeRel);

            if (computeType == null) {
                // backwards compatability - if no compute type has been specified, then a default compute type of
                // concept is used
                return true;
            } else {
                if (computeType.getConceptId() == RefsetAuxiliary.Concept.CONCEPT_COMPUTE_TYPE.localize().getNid()) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isDescriptionComputeType() {
        try {
            I_GetConceptData refsetComputeTypeRel =
                    termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_COMPUTE_TYPE_REL.getUids());
            I_GetConceptData computeType = getLatestSourceRelationshipTarget(spec, refsetComputeTypeRel);

            if (computeType == null) {
                // backwards compatability - if no compute type has been specified, then a default compute type of
                // concept is used
                return false;
            } else {
                if (computeType.getConceptId() == RefsetAuxiliary.Concept.DESCRIPTION_COMPUTE_TYPE.localize().getNid()) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isRelationshipComputeType() {
        try {
            I_GetConceptData refsetComputeTypeRel =
                    termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_COMPUTE_TYPE_REL.getUids());
            I_GetConceptData computeType = getLatestSourceRelationshipTarget(spec, refsetComputeTypeRel);

            if (computeType == null) {
                // backwards compatability - if no compute type has been specified, then a default compute type of
                // concept is used
                return false;
            } else {
                if (computeType.getConceptId() == RefsetAuxiliary.Concept.RELATIONSHIP_COMPUTE_TYPE.localize().getNid()) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public I_GetConceptData getRefsetSpecConcept() {
        return spec;
    }

    public I_GetConceptData getMemberRefsetConcept() {
        try {
            I_GetConceptData specifiesRefsetRel =
                    termFactory.getConcept(RefsetAuxiliary.Concept.SPECIFIES_REFSET.getUids());
            return getLatestSourceRelationshipTarget(getRefsetSpecConcept(), specifiesRefsetRel);
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

            return getLatestSourceRelationshipTarget(memberRefsetConcept, markedParentRel);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public I_GetConceptData getRefsetPurposeConcept() {
        try {
            I_GetConceptData refsetPurposeRel =
                    termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_PURPOSE_REL.getUids());
            I_GetConceptData memberRefsetConcept = getMemberRefsetConcept();
            if (memberRefsetConcept == null) {
                return null;
            }

            return getLatestSourceRelationshipTarget(memberRefsetConcept, refsetPurposeRel);
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

            return getLatestSourceRelationshipTarget(memberRefsetConcept, commentsRel);
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

            return getLatestSourceRelationshipTarget(memberRefsetConcept, promotionRel);
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
    public I_GetConceptData getLatestSourceRelationshipTarget(I_GetConceptData concept,
            I_GetConceptData relationshipType) throws Exception {

        I_GetConceptData latestTarget = null;
        int latestVersion = Integer.MIN_VALUE;

        I_IntSet allowedTypes = LocalVersionedTerminology.get().newIntSet();
        allowedTypes.add(relationshipType.getConceptId());

        List<? extends I_RelTuple> relationships = concept.getSourceRelTuples(null, allowedTypes, null, true, true);
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
    public I_GetConceptData getLatestDestinationRelationshipSource(I_GetConceptData concept,
            I_GetConceptData relationshipType) throws Exception {

        I_GetConceptData latestTarget = null;
        int latestVersion = Integer.MIN_VALUE;

        I_IntSet allowedTypes = LocalVersionedTerminology.get().newIntSet();
        allowedTypes.add(relationshipType.getConceptId());

        List<? extends I_RelTuple> relationships = concept.getDestRelTuples(null, allowedTypes, null, true, true);
        for (I_RelTuple rel : relationships) {
            if (rel.getVersion() > latestVersion) {
                latestVersion = rel.getVersion();
                latestTarget = LocalVersionedTerminology.get().getConcept(rel.getC1Id());
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

        List<? extends I_RelTuple> relationships = concept.getSourceRelTuples(null, allowedTypes, null, true, true);
        for (I_RelTuple rel : relationships) {
            if (rel.getVersion() > latestVersion) {
                latestVersion = rel.getVersion();
                latestRel = rel;
            }
        }

        return latestRel;
    }
}
