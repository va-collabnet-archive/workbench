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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.AllowDataCheckSuppression;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.BdbTermFactory;
import org.ihtsdo.db.bdb.computer.ReferenceConcepts;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.tk.api.PathBI;

@AllowDataCheckSuppression
public class RefsetHelper extends RefsetUtilities implements I_HelpRefsets {

    public RefsetHelper(I_ConfigAceFrame config) {
        super(config);
    }

    public RefsetHelper(I_ConfigAceFrame config, I_IntSet isARelTypes) {
        super(config, isARelTypes);
    }

    public I_IntSet getIsATypesForRefset(int refsetNid) throws Exception {
        access();
        I_IntSet isATypes = Terms.get().newIntSet();
        isATypes.add(ReferenceConcepts.MARKED_PARENT_IS_A_TYPE.getNid());
        Concept memberRefset = Bdb.getConceptDb().getConcept(refsetNid);
        Set<? extends I_GetConceptData> requiredIsAType =
                memberRefset.getSourceRelTargets(getAllowedStatuses(), isATypes, null, getConfig().getPrecedence(),
                    getConfig().getConflictResolutionStrategy());

        if (requiredIsAType != null && requiredIsAType.size() > 0) {
            // relationship exists so use the is-a specified by the
            // marked-parent-is-a relationship
            useConfigClone();
            getConfig().getDestRelTypes().clear();
            for (I_GetConceptData isAType : requiredIsAType) {
                getConfig().getDestRelTypes().add(isAType.getNid());
            }
        }
        return getConfig().getDestRelTypes();
    }

    /*
     * (non-Javadoc)
     * @seeorg.ihtsdo.db.bdb.computer.refset.I_HelpWithRefsets#
     * getFirstCurrentRefsetExtension(int, int)
     */
    public I_ExtendByRefPartCid getFirstCurrentRefsetExtension(int refsetId, int conceptId) throws Exception {

        access();
        for (I_ExtendByRef extension : Terms.get().getAllExtensionsForComponent(conceptId)) {

            I_ExtendByRefPartCid latestPart = null;
            if (extension.getRefsetId() == refsetId) {
                // get the latest version
                for (I_ExtendByRefPart part : extension.getMutableParts()) {
                    if (part instanceof I_ExtendByRefPartCid && (latestPart == null)
                        || (part.getTime() >= latestPart.getTime())) {
                        latestPart = (I_ExtendByRefPartCid) part;
                    }
                }
            }

            // confirm its the right extension value and its status is current
            if (latestPart != null && latestPart.getStatusNid() == ReferenceConcepts.CURRENT.getNid()) {
                return latestPart;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @seeorg.ihtsdo.db.bdb.computer.refset.I_HelpWithRefsets#
     * getAllCurrentRefsetExtensions(int, int)
     */
    @SuppressWarnings("unchecked")
    public <T extends I_ExtendByRefPart> List<T> getAllCurrentRefsetExtensions(int refsetId, int conceptId)
            throws Exception {

        access();
        ArrayList<T> result = new ArrayList<T>();

        for (I_ExtendByRef extension : Terms.get().getAllExtensionsForComponent(conceptId, true)) {
            if (extension.getRefsetId() == refsetId) {

                // get the latest version
                I_ExtendByRefPart latestPart = null;
                for (I_ExtendByRefPart part : extension.getMutableParts()) {
                    if ((latestPart == null) || (part.getTime() >= latestPart.getTime())) {
                        latestPart = part;
                    }
                }

                // confirm its the right extension value and its status is
                // current
                if (latestPart.getStatusNid() == ReferenceConcepts.CURRENT.getNid()) {
                    result.add((T) latestPart);
                }
            }
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.ihtsdo.db.bdb.computer.refset.I_HelpWithRefsets#hasRefsetExtension
     * (int, int, org.dwfa.ace.api.RefsetPropertyMap)
     */
    public boolean hasRefsetExtension(int refsetId, int componentNid, final RefsetPropertyMap extProps)
            throws Exception {
        access();
        List<? extends I_ExtendByRef> extensions = Terms.get().getAllExtensionsForComponent(componentNid, true);
        for (I_ExtendByRef extension : extensions) {

            if (extension == null) {
                AceLog.getAppLog().alertAndLogException(
                    new Exception("Null extension in list: " + extensions + " from component: "
                        + Bdb.getConceptForComponent(componentNid).toLongString()));
            }
            if (extension != null && extension.getRefsetId() == refsetId) {

                // get the latest version
                I_ExtendByRefPart latestPart = null;
                for (I_ExtendByRefPart part : extension.getMutableParts()) {
                    if ((latestPart == null) || (part.getTime() >= latestPart.getTime())) {
                        latestPart = part;
                    }
                }
                if (extProps.validate(latestPart)) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.ihtsdo.db.bdb.computer.refset.I_HelpWithRefsets#getRefsetExtension
     * (int, int, org.dwfa.ace.api.RefsetPropertyMap)
     */
    public I_ExtendByRef getRefsetExtension(int refsetId, int componentId, final RefsetPropertyMap extProps)
            throws Exception {
        access();
        for (I_ExtendByRef extension : Terms.get().getAllExtensionsForComponent(componentId, true)) {

            if (extension.getRefsetId() == refsetId && extProps.validate((I_ExtendByRefPart) extension)) {
                return extension;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.ihtsdo.db.bdb.computer.refset.I_HelpWithRefsets#hasCurrentRefsetExtension
     * (int, int, org.dwfa.ace.api.RefsetPropertyMap)
     */
    public boolean hasCurrentRefsetExtension(int refsetId, int conceptId, final RefsetPropertyMap extProps)
            throws Exception {
        access();
        if (!extProps.hasProperty(RefsetPropertyMap.REFSET_PROPERTY.STATUS)) {
            extProps.with(RefsetPropertyMap.REFSET_PROPERTY.STATUS, ReferenceConcepts.CURRENT.getNid());
        }
        return hasRefsetExtension(refsetId, conceptId, extProps);
    }

    /*
     * (non-Javadoc)
     * @seeorg.ihtsdo.db.bdb.computer.refset.I_HelpWithRefsets#
     * getOrCreateRefsetExtension(int, int, java.lang.Class,
     * org.dwfa.ace.api.RefsetPropertyMap)
     */
    public <T extends I_ExtendByRefPart> I_ExtendByRef getOrCreateRefsetExtension(int refsetId, int componentId,
            REFSET_TYPES type, final RefsetPropertyMap propMap, UUID memberUuid) throws Exception {

        access();
        I_ExtendByRef extension = getRefsetExtension(refsetId, componentId, propMap);
        // check subject is not already a member
        if (extension != null) {
            return extension;
        }
        // create a new extension (with a part for each path the user is
        // editing)
        return makeMemberAndSetup(refsetId, componentId, type, propMap, memberUuid);
    }

    public <T extends I_ExtendByRefPart> I_ExtendByRef createRefsetExtension(int refsetId, int componentId,
            REFSET_TYPES type, final RefsetPropertyMap propMap, UUID memberUuid) throws Exception {

        access();
        // create a new extension (with a part for each path the user is
        // editing)
        return makeMemberAndSetup(refsetId, componentId, type, propMap, memberUuid);
    }


    protected I_ExtendByRef makeMemberAndSetup(int refsetId, int referencedComponentNid, REFSET_TYPES type,
            final RefsetPropertyMap propMap, UUID memberUuid) throws IOException {

        access();
        return BdbTermFactory.createMember(memberUuid, referencedComponentNid, type, Concept.get(refsetId),
            getConfig(), propMap);
    }

    public boolean newRefsetExtension(int refsetId, int componentId, REFSET_TYPES type, RefsetPropertyMap propMap,
            I_ConfigAceFrame config) throws Exception {
        access();
        if (hasCurrentRefsetExtension(refsetId, componentId, propMap)) {
            return false;
        }
        getOrCreateRefsetExtension(refsetId, componentId, type, propMap, UUID.randomUUID());
        return true;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.ihtsdo.db.bdb.computer.refset.I_HelpWithRefsets#retireRefsetExtension
     * (int, int, org.dwfa.ace.api.RefsetPropertyMap)
     */
    public boolean retireRefsetExtension(int refsetId, int conceptId, final RefsetPropertyMap extProps)
            throws Exception {

        access();
        // check subject is not already a member
        for (I_ExtendByRef extension : Terms.get().getAllExtensionsForComponent(conceptId)) {

            if (extension.getRefsetId() == refsetId) {

                // get the latest version
                I_ExtendByRefPart latestPart = null;
                for (I_ExtendByRefPart part : extension.getMutableParts()) {
                    if ((latestPart == null) || (part.getTime() >= latestPart.getTime())) {
                        latestPart = part;
                    }
                }

                if (!extProps.hasProperty(RefsetPropertyMap.REFSET_PROPERTY.STATUS)) {
                    extProps.with(RefsetPropertyMap.REFSET_PROPERTY.STATUS, ReferenceConcepts.CURRENT.getNid());
                }

                if (extProps.validate(latestPart)) {

                    // found a member to retire

                    I_ExtendByRefPartCid clone =
                            (I_ExtendByRefPartCid) latestPart.makeAnalog(ReferenceConcepts.RETIRED.getNid(), latestPart
                                .getPathNid(), Long.MAX_VALUE);
                    extension.addVersion(clone);
                    if (isAutocommitActive()) {
                        Terms.get().addUncommittedNoChecks(extension);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return The edit paths from the active config. Returns null if no config
     *         set or the config defines no paths for editing.
     */
    protected Set<PathBI> getEditPaths() throws Exception {
        access();
        return this.getConfig().getEditingPathSet();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.ihtsdo.db.bdb.computer.refset.I_HelpWithRefsets#setEditPaths(org.
     * dwfa.ace.api.PathBI)
     */
    public void setEditPaths(PathBI... editPaths) {
        access();
        HashSet<PathBI> editPathSet = new HashSet<PathBI>();
        Collections.addAll(editPathSet, editPaths);
        useConfigClone();
        this.getConfig().getEditingPathSet().clear();
        this.getConfig().getEditingPathSet().addAll(editPathSet);
    }

    protected HashMap<Integer, HashSet<Integer>> refsetPurposeCache = new HashMap<Integer, HashSet<Integer>>();

    /*
     * (non-Javadoc)
     * @see org.ihtsdo.db.bdb.computer.refset.I_HelpWithRefsets#hasPurpose(int,
     * int)
     */
    public boolean hasPurpose(int refsetId, int purposeId) throws Exception {
        access();
        if (refsetPurposeCache.containsKey(purposeId)) {
            return refsetPurposeCache.get(purposeId).contains(refsetId);
        } else {
            I_GetConceptData purpose = Terms.get().getConcept(purposeId);

            I_IntSet allowedTypes = Terms.get().newIntSet();
            allowedTypes.add(RefsetAuxiliary.Concept.REFSET_PURPOSE_REL.localize().getNid());

            Set<? extends I_GetConceptData> destRelOrigins = purpose.getDestRelOrigins(allowedTypes);

            HashSet<Integer> cachedOrigins = new HashSet<Integer>();
            for (I_GetConceptData concept : destRelOrigins) {
                cachedOrigins.add(concept.getConceptNid());
            }
            refsetPurposeCache.put(purposeId, cachedOrigins);

            return cachedOrigins.contains(refsetId);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.ihtsdo.db.bdb.computer.refset.I_HelpWithRefsets#hasPurpose(int,
     * org.dwfa.tapi.I_ConceptualizeUniversally)
     */
    public boolean hasPurpose(int refsetId, I_ConceptualizeUniversally purposeConcept) throws Exception {
        access();
        return hasPurpose(refsetId, purposeConcept.localize().getNid());
    }

    protected class HasExtension implements LineageCondition {
        private int refsetId;
        private int memberTypeId;

        public HasExtension(int refsetId, int memberTypeId) {
            this.refsetId = refsetId;
            this.memberTypeId = memberTypeId;
        }

        public boolean evaluate(I_GetConceptData concept) throws Exception {
            return hasCurrentRefsetExtension(this.refsetId, concept.getConceptNid(), new RefsetPropertyMap().with(
                RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, this.memberTypeId));
        }
    }

    /*
     * (non-Javadoc)
     * @seeorg.ihtsdo.db.bdb.computer.refset.I_HelpWithRefsets#
     * getCommentsRefsetForRefset(org.dwfa.ace.api.I_GetConceptData,
     * org.dwfa.ace.api.I_ConfigAceFrame)
     */
    public Set<? extends I_GetConceptData> getCommentsRefsetForRefset(I_GetConceptData refsetIdentityConcept,
            I_ConfigAceFrame config) throws IOException, TerminologyException {
        access();
        return getSourceRelTarget(refsetIdentityConcept, config, RefsetAuxiliary.Concept.COMMENTS_REL.localize()
            .getNid());
    }

    /*
     * (non-Javadoc)
     * @seeorg.ihtsdo.db.bdb.computer.refset.I_HelpWithRefsets#
     * getCommentsRefsetForRefset(org.dwfa.ace.api.I_GetConceptData,
     * org.dwfa.ace.api.I_ConfigAceFrame)
     */
    public Set<? extends I_GetConceptData> getEditTimeRefsetForRefset(I_GetConceptData refsetIdentityConcept,
            I_ConfigAceFrame config) throws IOException, TerminologyException {
        access();
        return getSourceRelTarget(refsetIdentityConcept, config, RefsetAuxiliary.Concept.EDIT_TIME_REL.localize()
            .getNid());
    }

    /*
     * (non-Javadoc)
     * @seeorg.ihtsdo.db.bdb.computer.refset.I_HelpWithRefsets#
     * getCommentsRefsetForRefset(org.dwfa.ace.api.I_GetConceptData,
     * org.dwfa.ace.api.I_ConfigAceFrame)
     */
    public Set<? extends I_GetConceptData> getComputeTimeRefsetForRefset(I_GetConceptData refsetIdentityConcept,
            I_ConfigAceFrame config) throws IOException, TerminologyException {
        access();
        return getSourceRelTarget(refsetIdentityConcept, config, RefsetAuxiliary.Concept.COMPUTE_TIME_REL.localize()
            .getNid());
    }

    /*
     * (non-Javadoc)
     * @seeorg.ihtsdo.db.bdb.computer.refset.I_HelpWithRefsets#
     * getMarkedParentRefsetForRefset(org.dwfa.ace.api.I_GetConceptData,
     * org.dwfa.ace.api.I_ConfigAceFrame)
     */
    public Set<? extends I_GetConceptData> getMarkedParentRefsetForRefset(I_GetConceptData refsetIdentityConcept,
            I_ConfigAceFrame config) throws IOException, TerminologyException {
        access();
        return getSourceRelTarget(refsetIdentityConcept, config, RefsetAuxiliary.Concept.MARKED_PARENT_REFSET
            .localize().getNid());
    }

    /*
     * (non-Javadoc)
     * @seeorg.ihtsdo.db.bdb.computer.refset.I_HelpWithRefsets#
     * getPromotionRefsetForRefset(org.dwfa.ace.api.I_GetConceptData,
     * org.dwfa.ace.api.I_ConfigAceFrame)
     */
    public Set<? extends I_GetConceptData> getPromotionRefsetForRefset(I_GetConceptData refsetIdentityConcept,
            I_ConfigAceFrame config) throws IOException, TerminologyException {
        access();
        return getSourceRelTarget(refsetIdentityConcept, config, RefsetAuxiliary.Concept.PROMOTION_REL.localize()
            .getNid());
    }

    private Set<? extends I_GetConceptData> getSourceRelTarget(I_GetConceptData refsetIdentityConcept,
            I_ConfigAceFrame config, int refsetIdentityNid) throws IOException, TerminologyException {
        access();
        I_IntSet allowedTypes = Terms.get().newIntSet();
        allowedTypes.add(refsetIdentityNid);
        Set<? extends I_GetConceptData> matchingConcepts =
                refsetIdentityConcept.getSourceRelTargets(config.getAllowedStatus(), allowedTypes, config
                    .getViewPositionSetReadOnly(), getConfig().getPrecedence(), getConfig()
                    .getConflictResolutionStrategy());
        return matchingConcepts;
    }

    /*
     * (non-Javadoc)
     * @seeorg.ihtsdo.db.bdb.computer.refset.I_HelpWithRefsets#
     * getSpecificationRefsetForRefset(org.dwfa.ace.api.I_GetConceptData,
     * org.dwfa.ace.api.I_ConfigAceFrame)
     */
    public Set<? extends I_GetConceptData> getSpecificationRefsetForRefset(I_GetConceptData refsetIdentityConcept,
            I_ConfigAceFrame config) throws IOException, TerminologyException {
        access();
        return getDestRelOrigins(refsetIdentityConcept, config, RefsetAuxiliary.Concept.SPECIFIES_REFSET.localize()
            .getNid());
    }

    private Set<? extends I_GetConceptData> getDestRelOrigins(I_GetConceptData refsetIdentityConcept,
            I_ConfigAceFrame config, int refsetIdentityNid) throws IOException, TerminologyException {
        access();
        I_IntSet allowedTypes = Terms.get().newIntSet();
        allowedTypes.add(refsetIdentityNid);
        Set<? extends I_GetConceptData> matchingConcepts =
                refsetIdentityConcept.getDestRelOrigins(config.getAllowedStatus(), allowedTypes, config
                    .getViewPositionSetReadOnly(), getConfig().getPrecedence(), getConfig()
                    .getConflictResolutionStrategy());
        return matchingConcepts;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.ihtsdo.db.bdb.computer.refset.I_HelpMemberRefsets#getMemberRefsets()
     */
    public Set<Integer> getMemberRefsets() throws Exception {
        access();

        HashSet<Integer> memberRefsets = new HashSet<Integer>();
        I_TermFactory termFactory = Terms.get();

        int currentStatusId = ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid();
        int memberRefsetPurposeId = ConceptConstants.REFSET_MEMBER_PURPOSE.localize().getNid();
        int refsetIdenityId = RefsetAuxiliary.Concept.REFSET_IDENTITY.localize().getNid();

        I_IntSet statuses = termFactory.newIntSet();
        statuses.add(currentStatusId);

        I_IntSet purposeTypes = termFactory.newIntSet();
        purposeTypes.add(RefsetAuxiliary.Concept.REFSET_PURPOSE.localize().getNid());

        I_IntSet isATypes = termFactory.newIntSet();
        isATypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
        isATypes.add(ConceptConstants.SNOMED_IS_A.localize().getNid());

        I_GetConceptData memberPurpose = termFactory.getConcept(memberRefsetPurposeId);

        for (I_GetConceptData origin : memberPurpose.getDestRelOrigins(statuses, purposeTypes, null, getConfig()
            .getPrecedence(), getConfig().getConflictResolutionStrategy())) {
            // Check origin is a refset (ie. has not been retired as a refset)
            for (I_GetConceptData target : origin.getSourceRelTargets(statuses, isATypes, null, getConfig()
                .getPrecedence(), getConfig().getConflictResolutionStrategy())) {
                if (target.getConceptNid() == refsetIdenityId) {
                    memberRefsets.add(origin.getConceptNid());
                }
            }
        }

        return memberRefsets;
    }

    public List<Integer> getSpecificationRefsets() throws Exception {
        access();
        throw new UnsupportedOperationException();
    }

    public I_GetConceptData getMemberSetConcept(int refsetId) throws Exception {
        throw new UnsupportedOperationException();
    }

    public int getExcludeMembersRefset(int specRefsetConceptId) {
        throw new UnsupportedOperationException();
    }

    public List<Integer> getChildrenOfConcept(int conceptId) throws IOException, Exception {
        Concept c = Concept.get(conceptId);
        Collection<Concept> children =
                c.getDestRelOrigins(getConfig().getAllowedStatus(), getConfig().getDestRelTypes(), getConfig()
                    .getViewPositionSetReadOnly(), getConfig().getPrecedence(), getConfig()
                    .getConflictResolutionStrategy());
        List<Integer> childNids = new ArrayList<Integer>(children.size());
        for (Concept child : children) {
            childNids.add(child.getNid());
        }
        return childNids;
    }

}
