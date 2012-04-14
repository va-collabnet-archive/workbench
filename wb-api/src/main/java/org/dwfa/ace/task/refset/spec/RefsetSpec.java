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

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartLong;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class RefsetSpec {

    private I_GetConceptData spec;
    private I_TermFactory termFactory;
    private I_ConfigAceFrame config;

    /**
     * Use this constructor if you wish to input the refset spec concept.
     * 
     * @param spec
     */
    public RefsetSpec(I_GetConceptData spec, I_ConfigAceFrame config) {
        this.spec = spec;
        termFactory = Terms.get();
        this.config = config;
    }

    /**
     * Use this constructor if you wish to input the member refset concept,
     * rather than the refset spec concept.
     * 
     * @param concept
     * @param memberRefsetInputted
     */
    public RefsetSpec(I_GetConceptData concept, boolean memberRefsetInputted, I_ConfigAceFrame config) {
        termFactory = Terms.get();
        this.config = config;
        if (memberRefsetInputted) {
            try {
                I_GetConceptData specifiesRefsetRel =
                        termFactory.getConcept(RefsetAuxiliary.Concept.SPECIFIES_REFSET.getUids());
                this.spec = getLatestDestinationRelationshipSource(concept, specifiesRefsetRel);
                termFactory = Terms.get();
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
                // backwards compatibility - if no compute type has been specified, then a default compute type of
                // concept is used
                return true;
            } else {
                if (computeType.getConceptNid() == Terms.get().uuidToNative(
                    RefsetAuxiliary.Concept.CONCEPT_COMPUTE_TYPE.getUids())) {
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

    public String getComputeTypeString() {
        String computeTypeString = "";
        if (isConceptComputeType()) {
            computeTypeString = "concept";
        } else if (isDescriptionComputeType()) {
            computeTypeString = "description";
        } else {
            computeTypeString = "unknown";
        }
        return computeTypeString;
    }

    public boolean isDescriptionComputeType() {
        try {
            I_GetConceptData refsetComputeTypeRel =
                    termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_COMPUTE_TYPE_REL.getUids());
            I_GetConceptData computeType = getLatestSourceRelationshipTarget(spec, refsetComputeTypeRel);

            if (computeType == null) {
                // backwards compatibility - if no compute type has been specified, then a default compute type of
                // concept is used
                return false;
            } else {
                if (computeType.getConceptNid() == Terms.get().uuidToNative(
                    RefsetAuxiliary.Concept.DESCRIPTION_COMPUTE_TYPE.getUids())) {
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
                // backwards compatibility - if no compute type has been specified, then a default compute type of
                // concept is used
                return false;
            } else {
                if (computeType.getConceptNid() == Terms.get().uuidToNative(
                    RefsetAuxiliary.Concept.RELATIONSHIP_COMPUTE_TYPE.getUids())) {
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
            I_GetConceptData specifiesRefsetRel = termFactory.getConcept(RefsetAuxiliary.Concept.SPECIFIES_REFSET.getUids());
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
            I_GetConceptData refsetPurposeRel = termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_PURPOSE_REL.getUids());
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
    public I_GetConceptData getLatestSourceRelationshipTarget(I_GetConceptData concept, I_GetConceptData relationshipType)
            throws Exception {

        I_GetConceptData latestTarget = null;
        int latestVersion = Integer.MIN_VALUE;

        I_IntSet allowedTypes = Terms.get().newIntSet();
        allowedTypes.add(relationshipType.getConceptNid());

        if (concept != null) {
            // TODO should use the version computer/handle contradiction better.
            List<? extends I_RelTuple> relationships =
                    concept.getSourceRelTuples(null, allowedTypes, config.getViewPositionSetReadOnly(), config
                        .getPrecedence(), config.getConflictResolutionStrategy());
            for (I_RelTuple rel : relationships) {
                if (rel.getVersion() > latestVersion) {
                    latestVersion = rel.getVersion();
                    latestTarget = Terms.get().getConcept(rel.getC2Id());
                }
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

        I_IntSet allowedTypes = Terms.get().newIntSet();
        allowedTypes.add(relationshipType.getConceptNid());

        if (concept != null) {
            // TODO should use the version computer/handle contradiction differently
            List<? extends I_RelTuple> relationships =
                    concept.getDestRelTuples(null, allowedTypes, config.getViewPositionSetReadOnly(),
                        config.getPrecedence(), config.getConflictResolutionStrategy());
            for (I_RelTuple rel : relationships) {
                if (rel.getVersion() > latestVersion) {
                    latestVersion = rel.getVersion();
                    latestTarget = Terms.get().getConcept(rel.getC1Id());
                }
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
    public I_RelTuple getLatestRelationship(I_GetConceptData concept, I_GetConceptData relationshipType) throws Exception {

        I_RelTuple latestRel = null;
        int latestVersion = Integer.MIN_VALUE;

        I_IntSet allowedTypes = Terms.get().newIntSet();
        allowedTypes.add(relationshipType.getConceptNid());

        if (concept != null) {
            List<? extends I_RelTuple> relationships =
                    concept.getSourceRelTuples(null, allowedTypes, config.getViewPositionSetReadOnly(), config
                        .getPrecedence(), config.getConflictResolutionStrategy());
            // TODO should use the version computer/handle contradiction differently.
            for (I_RelTuple rel : relationships) {
                if (rel.getVersion() > latestVersion) {
                    latestVersion = rel.getVersion();
                    latestRel = rel;
                }
            }
        }

        return latestRel;
    }

    public String getOverallSpecStatusString() {

        try {
            I_GetConceptData memberRefsetConcept = getMemberRefsetConcept();
            I_GetConceptData promotionRefsetConcept = getPromotionRefsetConcept();
            I_HelpSpecRefset helper = Terms.get().getSpecRefsetHelper(Terms.get().getActiveAceFrameConfig());
            I_IntSet currentStatuses = helper.getCurrentStatusIntSet();

            if (promotionRefsetConcept != null && memberRefsetConcept != null) {
                Collection<? extends I_ExtendByRef> promotionExtensions =
                        Terms.get().getRefsetExtensionMembers(promotionRefsetConcept.getConceptNid());
                for (I_ExtendByRef promotionExtension : promotionExtensions) {
                    if (promotionExtension.getComponentNid() == memberRefsetConcept.getConceptNid()) {
                        I_ExtendByRefPart latestPart = helper.getLatestPart(promotionExtension);
                        if (currentStatuses.contains(latestPart.getStatusNid())) {
                            if (latestPart instanceof I_ExtendByRefPartCid) {
                                I_ExtendByRefPartCid latestConceptPart = (I_ExtendByRefPartCid) latestPart;
                                return Terms.get().getConcept(latestConceptPart.getC1id()).getInitialText();
                            }
                        }
                    }
                }
            }

            return "none";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error while locating status";
        }
    }

    public boolean isEditableRefset() {

        try {
            I_GetConceptData memberRefsetConcept = getMemberRefsetConcept();
            I_GetConceptData promotionRefsetConcept = getPromotionRefsetConcept();
            I_HelpSpecRefset helper = Terms.get().getSpecRefsetHelper(Terms.get().getActiveAceFrameConfig());
            Set<Integer> currentIds = helper.getCurrentStatusIds();

            if (promotionRefsetConcept != null && memberRefsetConcept != null) {
                Collection<? extends I_ExtendByRef> promotionExtensions =
                        Terms.get().getRefsetExtensionMembers(promotionRefsetConcept.getConceptNid());
                for (I_ExtendByRef promotionExtension : promotionExtensions) {
                    if (promotionExtension.getComponentNid() == memberRefsetConcept.getConceptNid()) {
                        I_ExtendByRefPart latestPart = helper.getLatestPart(promotionExtension);
                        if (currentIds.contains(latestPart.getStatusNid())) {
                            if (latestPart instanceof I_ExtendByRefPartCid) {
                                I_ExtendByRefPartCid latestConceptPart = (I_ExtendByRefPartCid) latestPart;
                                I_GetConceptData status = Terms.get().getConcept(latestConceptPart.getC1id());
                                if (status.getConceptNid() == Terms.get().uuidToNative(
                                    ArchitectonicAuxiliary.Concept.CURRENT.getUids())
                                    || status.getConceptNid() == Terms.get().uuidToNative(
                                        ArchitectonicAuxiliary.Concept.IN_DEVELOPMENT.getUids())) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void modifyOverallSpecStatus(I_GetConceptData newStatus) throws Exception {
        I_HelpSpecRefset helper = Terms.get().getSpecRefsetHelper(Terms.get().getActiveAceFrameConfig());
        helper.setAutocommitActive(true);
        I_GetConceptData memberRefsetConcept = getMemberRefsetConcept();
        I_GetConceptData promotionRefsetConcept = getPromotionRefsetConcept();

        if (memberRefsetConcept != null && promotionRefsetConcept != null) {
            if (helper.hasConceptRefsetExtensionWithAnyPromotionStatus(promotionRefsetConcept.getConceptNid(),
                memberRefsetConcept.getConceptNid())) {
                helper.newConceptExtensionPart(promotionRefsetConcept.getConceptNid(), memberRefsetConcept.getConceptNid(),
                    newStatus.getConceptNid(), Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids()));
                Terms.get().commit();
            } else {
                helper.newRefsetExtension(promotionRefsetConcept.getConceptNid(), memberRefsetConcept.getConceptNid(),
                    newStatus.getConceptNid());
                Terms.get().commit();
            }
        }
    }

    public void setLastComputeTime(Long time) throws Exception {
        I_GetConceptData specConcept = getRefsetSpecConcept();
        if (specConcept == null) {
            return; // not a correct refset spec (i.e. doesn't have an associated spec)
        }

        I_GetConceptData lastComputeTimeConcept = getComputeConcept();
        if (lastComputeTimeConcept == null) {
            return; // 
        }

        I_HelpSpecRefset helper = Terms.get().getSpecRefsetHelper(config);
        boolean prevAutoCommit = helper.isAutocommitActive();
        helper.setAutocommitActive(false);

        if (specConcept != null && lastComputeTimeConcept != null) {
            helper.newLongRefsetExtension(lastComputeTimeConcept.getConceptNid(), specConcept.getConceptNid(), time);
        }
        Terms.get().addUncommittedNoChecks(lastComputeTimeConcept);
        helper.setAutocommitActive(prevAutoCommit);
    }

    public I_GetConceptData getEditConcept() throws TerminologyException, IOException {
        try {
            I_GetConceptData editTimeRel = termFactory.getConcept(RefsetAuxiliary.Concept.EDIT_TIME_REL.getUids());
            I_GetConceptData memberRefsetConcept = getMemberRefsetConcept();
            if (memberRefsetConcept == null) {
                return null;
            }

            return getLatestSourceRelationshipTarget(memberRefsetConcept, editTimeRel);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public I_GetConceptData getComputeConcept() throws TerminologyException, IOException {
        try {
            I_GetConceptData computeTimeRel = termFactory.getConcept(RefsetAuxiliary.Concept.COMPUTE_TIME_REL.getUids());
            I_GetConceptData memberRefsetConcept = getMemberRefsetConcept();
            if (memberRefsetConcept == null) {
                return null;
            }

            return getLatestSourceRelationshipTarget(memberRefsetConcept, computeTimeRel);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public void setLastEditTime(Long time) throws Exception {
        if (getRefsetSpecConcept() == null) {
            return; // not a correct refset spec (i.e. doesn't have an associated spec)
        }
        try {

            I_HelpSpecRefset helper = Terms.get().getSpecRefsetHelper(Terms.get().getActiveAceFrameConfig());
            boolean prevAutoCommit = helper.isAutocommitActive();
            helper.setAutocommitActive(false);
            I_GetConceptData specConcept = getRefsetSpecConcept();
            I_GetConceptData lastEditTimeConcept = getEditConcept();

            if (specConcept != null && lastEditTimeConcept != null) {
                helper.newLongRefsetExtension(lastEditTimeConcept.getConceptNid(), specConcept.getConceptNid(), time);
            }
            helper.setAutocommitActive(prevAutoCommit);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error setting the last edit time of refset : " + e.getLocalizedMessage());
        }
    }

    public Long getLastComputeTime() throws TerminologyException, IOException {
        if (getRefsetSpecConcept() == null) {
            return null; // not a correct refset spec (i.e. doesn't have an associated spec)
        }
        I_GetConceptData lastComputeTimeConcept = getComputeConcept();
        if (lastComputeTimeConcept == null) {
            try {
                CreateRefsetMetaDataTask task = new CreateRefsetMetaDataTask();
                task.setStatus(Terms.get().getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()));
                task.setTermFactory(Terms.get());
                String originalMemberFSN = getMemberRefsetConcept().getInitialText();
                String editedFSN = "";
                if (originalMemberFSN.indexOf(" refset") == -1) {
                    editedFSN = originalMemberFSN;
                } else {
                    editedFSN = originalMemberFSN.substring(0, originalMemberFSN.indexOf(" refset"));
                }
                String computeTimeName = editedFSN + " compute time refset";

                lastComputeTimeConcept = task.newConcept(config);
                task.newDescription(lastComputeTimeConcept, Terms.get().getConcept(
                    ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()), computeTimeName, config);
                task.newDescription(lastComputeTimeConcept, Terms.get().getConcept(
                    ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()), computeTimeName, config);

                task.newRelationship(lastComputeTimeConcept, Terms.get().getConcept(
                    ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()), Terms.get().getConcept(
                    RefsetAuxiliary.Concept.SUPPORTING_REFSETS.getUids()), config);

                task.newRelationship(getMemberRefsetConcept(), Terms.get().getConcept(
                    RefsetAuxiliary.Concept.COMPUTE_TIME_REL.getUids()), lastComputeTimeConcept, config);

                task.newRelationship(lastComputeTimeConcept, Terms.get().getConcept(
                    RefsetAuxiliary.Concept.REFSET_PURPOSE_REL.getUids()), Terms.get().getConcept(
                    RefsetAuxiliary.Concept.ANCILLARY_DATA.getUids()), config);

                Terms.get().addUncommittedNoChecks(lastComputeTimeConcept);

                Terms.get().commit();

            } catch (Exception e) {
                e.printStackTrace();
                throw new TerminologyException(e.getMessage());
            }
        }
        I_ExtendByRefPart latestPart = null;

        for (I_ExtendByRef extension : lastComputeTimeConcept.getExtensions()) {

            if (extension.getRefsetId() == lastComputeTimeConcept.getConceptNid()) {

                // get the latest version

                for (I_ExtendByRefPart part : extension.getMutableParts()) {
                    if (part instanceof I_ExtendByRefPartLong) {
                        if ((latestPart == null) || (part.getTime() >= latestPart.getTime())) {
                            latestPart = part;
                        }
                    }
                }
            }
        }

        if (latestPart != null) {
            return ((I_ExtendByRefPartLong) latestPart).getLongValue();
        } else {
            return null;
        }
    }

    private Long getLastEditTime() throws TerminologyException, IOException {
        if (getRefsetSpecConcept() == null) {
            return null; // not a correct refset spec (i.e. doesn't have an associated spec)
        }
        I_GetConceptData specConcept = getRefsetSpecConcept();
        I_GetConceptData lastEditTimeConcept = getEditConcept();
        if (lastEditTimeConcept == null) {
            try {
                CreateRefsetMetaDataTask task = new CreateRefsetMetaDataTask();
                task.setStatus(Terms.get().getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()));

                task.setTermFactory(Terms.get());
                if (getMemberRefsetConcept() != null) {
                    String originalMemberFSN = getMemberRefsetConcept().getInitialText();
                    String editedFSN = "";
                    if (originalMemberFSN.indexOf(" refset") == -1) {
                        editedFSN = originalMemberFSN;
                    } else {
                        editedFSN = originalMemberFSN.substring(0, originalMemberFSN.indexOf(" refset"));
                    }

                    String computeTimeName = editedFSN + " edit time refset";

                    lastEditTimeConcept = task.newConcept(config);
                    task.newDescription(lastEditTimeConcept, Terms.get().getConcept(
                        ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()), computeTimeName, config);
                    task.newDescription(lastEditTimeConcept, Terms.get().getConcept(
                        ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()), computeTimeName, config);

                    task.newRelationship(lastEditTimeConcept, Terms.get().getConcept(
                        ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()), Terms.get().getConcept(
                        RefsetAuxiliary.Concept.SUPPORTING_REFSETS.getUids()), config);

                    task.newRelationship(getMemberRefsetConcept(), Terms.get().getConcept(
                        RefsetAuxiliary.Concept.EDIT_TIME_REL.getUids()), lastEditTimeConcept, config);

                    task.newRelationship(lastEditTimeConcept, Terms.get().getConcept(
                        RefsetAuxiliary.Concept.REFSET_PURPOSE_REL.getUids()), Terms.get().getConcept(
                        RefsetAuxiliary.Concept.ANCILLARY_DATA.getUids()), config);

                    Terms.get().addUncommittedNoChecks(lastEditTimeConcept);

                    Terms.get().commit();
                }

            } catch (Exception e) {
                throw new IOException(e);
            }
        }
        I_ExtendByRefPart latestPart = null;
        for (I_ExtendByRef extension : Terms.get().getAllExtensionsForComponent(specConcept.getConceptNid(), true)) {

            if (extension.getRefsetId() == lastEditTimeConcept.getConceptNid()) {

                // get the latest version

                for (I_ExtendByRefPart part : extension.getMutableParts()) {
                    if ((latestPart == null) || (part.getTime() >= latestPart.getTime())
                        && (part instanceof I_ExtendByRefPartLong)) {
                        latestPart = part;
                    }
                }
            }
        }

        if (latestPart != null) {
            return ((I_ExtendByRefPartLong) latestPart).getLongValue();
        } else {
            return null;
        }
    }

    public boolean needsCompute() throws TerminologyException, IOException {
        if (getRefsetSpecConcept() == null) {
            return false; // not a correct refset spec (i.e. doesn't have an associated spec)
        }
        Long lastComputeTime = getLastComputeTime();
        Long lastEditTime = getLastEditTime();
        if (lastEditTime == null) {
            return false; // hasn't ever been edited i.e. empty spec
        }
        if (lastComputeTime == null) {
            return true; // hasn't ever been computed, but it has been edited
        }

        return lastEditTime > lastComputeTime;
    }

}