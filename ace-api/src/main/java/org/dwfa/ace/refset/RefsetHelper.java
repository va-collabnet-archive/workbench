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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.BeanPropertyMap;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LineageHelper;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.StatusHelper;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.api.ebr.ThinExtByRefPartProperty;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.AllowDataCheckSuppression;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.TerminologyRuntimeException;

@AllowDataCheckSuppression
public class RefsetHelper extends LineageHelper {

    protected int currentStatusId;
    protected int retiredStatusId;

    protected Set<Integer> activeStatusIds;
    protected Set<Integer> inactiveStatusIds;

    protected int unspecifiedUuid;

    protected Set<I_Path> editPaths;

    public RefsetHelper() {
        super();

        try {
            currentStatusId = ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid();
            retiredStatusId = ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid();
            unspecifiedUuid = ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid();
        } catch (Exception e) {
            throw new TerminologyRuntimeException(e);
        }

        StatusHelper statusHelper = new StatusHelper();
        activeStatusIds = statusHelper.getActiveStatuses();
        inactiveStatusIds = statusHelper.getInactiveStatuses();
    }

    /**
     * Get the latest, current concept extension part for the FIRST extension
     * matching a specific refset.
     *
     * @param refsetId int
     * @param conceptId int
     * @return I_ThinExtByRefPartConcept with a status of current.
     *
     * @throws Exception if cannot get all extension for a concept id..
     */
    public I_ThinExtByRefPartConcept getFirstCurrentRefsetExtension(int refsetId, int conceptId) throws Exception {

        for (I_ThinExtByRefVersioned extension : termFactory.getAllExtensionsForComponent(conceptId)) {

            I_ThinExtByRefPartConcept latestPart = null;
            if (extension.getRefsetId() == refsetId) {
                // get the latest version
                for (I_ThinExtByRefPart part : extension.getVersions()) {
                    if (part instanceof I_ThinExtByRefPartConcept && (latestPart == null)
                        || (part.getVersion() >= latestPart.getVersion())) {
                        latestPart = (I_ThinExtByRefPartConcept) part;
                    }
                }
            }

            // confirm its the right extension value and its status is current
            if (latestPart != null && latestPart.getStatusId() == currentStatusId) {
                return latestPart;
            }
        }
        return null;
    }

    /**
     * Obtain all current extensions (latest part only) for a particular refset
     * that exist on a
     * specific concept.
     *
     * This method is strongly typed. The caller must provide the actual type of
     * the refset.
     *
     * @param <T> the strong/concrete type of the refset extension
     * @param refsetId Only returns extensions matching this reference set
     * @param conceptId Only returns extensions that exists on this concept
     * @return All matching refset extension (latest version parts only)
     * @throws Exception if unable to complete (never returns null)
     * @throws ClassCastException if a matching refset extension is not of type
     *             T
     */
    @SuppressWarnings("unchecked")
    public <T extends I_ThinExtByRefPart> List<T> getAllCurrentRefsetExtensions(int refsetId, int conceptId)
            throws Exception {

        ArrayList<T> result = new ArrayList<T>();

        for (I_ThinExtByRefVersioned extension : termFactory.getAllExtensionsForComponent(conceptId, true)) {
            if (extension.getRefsetId() == refsetId) {

                // get the latest version
                I_ThinExtByRefPart latestPart = null;
                for (I_ThinExtByRefPart part : extension.getVersions()) {
                    if ((latestPart == null) || (part.getVersion() >= latestPart.getVersion())) {
                        latestPart = part;
                    }
                }

                // confirm its the right extension value and its status is
                // current
                if (latestPart.getStatusId() == currentStatusId) {
                    result.add((T) latestPart);
                }
            }
        }

        return result;
    }

    public <T extends I_ThinExtByRefPart> T getLatestCurrentRefsetExtensions(int refsetId, int conceptId)
            throws Exception {
        T latestPart = null;
        for (T part : this.<T> getAllCurrentRefsetExtensions(refsetId, conceptId)) {
            if (latestPart == null || part.getVersion() >= latestPart.getVersion()) {
                latestPart = part;
            }
        }
        return latestPart;
    }

    /**
     * @param refsetId Only extensions for this refset will be evaluated
     * @param conceptId The concept to obtain extensions from
     * @param extProps The fields (being the name of the bean property) and the
     *            values to be validated
     * @return
     * @throws Exception Unable to complete
     */
    public boolean hasRefsetExtension(int refsetId, int conceptId, final BeanPropertyMap extProps) throws Exception {
        for (I_ThinExtByRefVersioned extension : termFactory.getAllExtensionsForComponent(conceptId, true)) {

            if (extension.getRefsetId() == refsetId) {

                // get the latest version
                I_ThinExtByRefPart latestPart = null;
                for (I_ThinExtByRefPart part : extension.getVersions()) {
                    if ((latestPart == null) || (part.getVersion() >= latestPart.getVersion())) {
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

    public boolean hasCurrentRefsetExtension(int refsetId, int conceptId, final BeanPropertyMap extProps)
            throws Exception {
        if (!extProps.hasProperty("statusId")) {
            extProps.with(ThinExtByRefPartProperty.STATUS, currentStatusId);
        }
        return hasRefsetExtension(refsetId, conceptId, extProps);
    }

    /**
     * Add a concept to a refset
     *
     * @param refsetId The subject refset
     * @param conceptId The concept to be added
     * @param type The class of extension to be created (must extend I_ThinExtByRefPart)
     * @param extProps Additional extension properties (refer {@link org.dwfa.ace.api.ebr.ThinExtByRefPartProperty})
     * @return True if the concept is added to the refset (new or reactivated), otherwise false
     */
    public <T extends I_ThinExtByRefPart> boolean newRefsetExtension(int refsetId, int conceptId, Class<T> type,
            final BeanPropertyMap extProps) throws Exception {

        // check subject is not already a member
        if (hasCurrentRefsetExtension(refsetId, conceptId, extProps)) {
            return false;
        }

        //check for a retired version which needs to be reactivated (rather than creating a new extension)
        I_ThinExtByRefVersioned extension = getInactiveExtension(refsetId, conceptId, extProps);

        if (extension == null) {
	        int newMemberId = termFactory.uuidToNativeWithGeneration(UUID.randomUUID(), unspecifiedUuid, getEditPaths(), Integer.MAX_VALUE);
	        extension = termFactory.newExtension(refsetId, newMemberId, conceptId, type);
        }

        for (I_Path editPath : getEditPaths()) {

            I_ThinExtByRefPart newPart = termFactory.newExtensionPart(type);

            newPart.setPathId(editPath.getConceptId());
            newPart.setStatusId(currentStatusId);
            newPart.setVersion(Integer.MAX_VALUE);

            extProps.writeTo(newPart);

            extension.addVersion(newPart);
        }

        termFactory.addUncommittedNoChecks(extension);
        return true;
    }



    private I_ThinExtByRefVersioned getInactiveExtension(int refsetId, int conceptId, final BeanPropertyMap extProps)
            throws Exception {

        I_ThinExtByRefVersioned result = null;

        // Ignore the status, just need to compare the value(s) (c1id, c2id, etc)
        BeanPropertyMap validProps = extProps.clone().without(ThinExtByRefPartProperty.STATUS);

        for (I_ThinExtByRefVersioned extension : termFactory.getAllExtensionsForComponent(conceptId, true)) {
            if (extension.getRefsetId() == refsetId) {
                I_ThinExtByRefPart part = extension.getLatestVersion();
                if (inactiveStatusIds.contains(part.getStatusId()) && validProps.validate(part)) {
                    if (result == null || part.getVersion() >= result.getLatestVersion().getVersion()) {
                        result = extension;
                    }
                }
            }
        }

        return result;
    }

    /**
     *
     * @param <T>
     * @param refsetId
     * @param conceptId
     * @param type
     * @param extProps
     * @param memberUuid
     * @param pathUuid
     * @param effectiveTime
     * @return
     * @throws Exception
     */
    @Deprecated
    public <T extends I_ThinExtByRefPart> boolean newRefsetExtension(int refsetId, int conceptId, Class<T> type,
            final BeanPropertyMap extProps, UUID memberUuid, UUID pathUuid, int effectiveTime) throws Exception {

        // check subject is not already a member
        if (hasCurrentRefsetExtension(refsetId, conceptId, extProps)) {
            return false;
        }

        I_Path path = termFactory.getPath(pathUuid);

        int newMemberId;
        if (memberUuid != null && termFactory.hasId(memberUuid)) {
            newMemberId = termFactory.getId(memberUuid).getNativeId();
        } else {
            newMemberId = termFactory.uuidToNativeWithGeneration((memberUuid == null) ? UUID.randomUUID() : memberUuid,
                unspecifiedUuid, Arrays.asList(path), effectiveTime);
        }

        I_ThinExtByRefVersioned newExtension = termFactory.newExtension(refsetId, newMemberId, conceptId, type);

        I_ThinExtByRefPart newPart = termFactory.newExtensionPart(type);

        // set defaults, may be overridden by BeanProperties
        newPart.setPathId(path.getConceptId());
        newPart.setStatusId(currentStatusId);
        newPart.setVersion(effectiveTime);

        extProps.writeTo(newPart);

        newExtension.addVersion(newPart);

        termFactory.addUncommittedNoChecks(newExtension);
        return true;
    }

    /**
     * Remove a concept from a refset
     *
     * @param refsetId The subject refset
     * @param conceptId The concept to be removed
     * @param memberTypeId The value of the concept extension to be removed (the
     *            membership type).
     */
    public boolean retireRefsetExtension(int refsetId, int conceptId, final BeanPropertyMap extProps) throws Exception {

        // check subject is not already a member
        for (I_ThinExtByRefVersioned extension : termFactory.getAllExtensionsForComponent(conceptId)) {

            if (extension.getRefsetId() == refsetId) {

                // get the latest version
                I_ThinExtByRefPart latestPart = null;
                for (I_ThinExtByRefPart part : extension.getVersions()) {
                    if ((latestPart == null) || (part.getVersion() >= latestPart.getVersion())) {
                        latestPart = part;
                    }
                }

                if (!extProps.hasProperty("statusId")) {
                    extProps.with(ThinExtByRefPartProperty.STATUS, currentStatusId);
                }

                if (extProps.validate(latestPart)) {

                    // found a member to retire

                    I_ThinExtByRefPartConcept clone = (I_ThinExtByRefPartConcept) latestPart.duplicate();
                    clone.setStatusId(retiredStatusId);
                    clone.setVersion(Integer.MAX_VALUE);
                    extension.addVersion(clone);
                    termFactory.addUncommittedNoChecks(extension);
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * @return The edit paths from the active config.
     *         Returns null if no config set or the config defines no paths for
     *         editing.
     */
    protected Set<I_Path> getEditPaths() throws Exception {
        if (this.editPaths == null) {
            I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();

            if (config != null) {
                this.editPaths = config.getEditingPathSet();
            }

            if (this.editPaths == null) {
                this.editPaths = new HashSet<I_Path>();
            }
        }

        return this.editPaths;
    }

    public void setEditPaths(I_Path... editPaths) {
        if (this.editPaths == null) {
            this.editPaths = new HashSet<I_Path>();
        } else {
            this.editPaths.clear();
        }
        Collections.addAll(this.editPaths, editPaths);
    }

    protected HashMap<Integer, HashSet<Integer>> refsetPurposeCache = new HashMap<Integer, HashSet<Integer>>();

    public boolean hasPurpose(int refsetId, int purposeId) throws Exception {
        if (refsetPurposeCache.containsKey(purposeId)) {
            return refsetPurposeCache.get(purposeId).contains(refsetId);
        } else {
            I_GetConceptData purpose = termFactory.getConcept(purposeId);

            I_IntSet allowedTypes = termFactory.newIntSet();
            allowedTypes.add(RefsetAuxiliary.Concept.REFSET_PURPOSE_REL.localize().getNid());

            Set<I_GetConceptData> destRelOrigins = purpose.getDestRelOrigins(allowedTypes, false, true);

            HashSet<Integer> cachedOrigins = new HashSet<Integer>();
            for (I_GetConceptData concept : destRelOrigins) {
                cachedOrigins.add(concept.getConceptId());
            }
            refsetPurposeCache.put(purposeId, cachedOrigins);

            return cachedOrigins.contains(refsetId);
        }
    }

    public boolean hasPurpose(int refsetId, I_ConceptualizeUniversally purposeConcept) throws Exception {
        return hasPurpose(refsetId, purposeConcept.localize().getNid());
    }

    protected class HasExtension implements Condition {
        private int refsetId;
        private int memberTypeId;

        public HasExtension(int refsetId, int memberTypeId) {
            this.refsetId = refsetId;
            this.memberTypeId = memberTypeId;
        }

        public boolean evaluate(I_GetConceptData concept, int distance) throws Exception {
            return hasCurrentRefsetExtension(this.refsetId, concept.getConceptId(), new BeanPropertyMap().with(
                ThinExtByRefPartProperty.CONCEPT_ONE, this.memberTypeId));
        }
    }

    public static Set<I_GetConceptData> getCommentsRefsetForRefset(I_GetConceptData refsetIdentityConcept,
            I_ConfigAceFrame config) throws IOException, TerminologyException {
        return getSourceRelTarget(refsetIdentityConcept, config, RefsetAuxiliary.Concept.COMMENTS_REL.localize()
            .getNid());
    }

    public static Set<I_GetConceptData> getMarkedParentRefsetForRefset(I_GetConceptData refsetIdentityConcept,
            I_ConfigAceFrame config) throws IOException, TerminologyException {
        return getSourceRelTarget(refsetIdentityConcept, config,
            RefsetAuxiliary.Concept.MARKED_PARENT_REFSET.localize().getNid());
    }

    public static Set<I_GetConceptData> getPromotionRefsetForRefset(I_GetConceptData refsetIdentityConcept,
            I_ConfigAceFrame config) throws IOException, TerminologyException {
        return getSourceRelTarget(refsetIdentityConcept, config, RefsetAuxiliary.Concept.PROMOTION_REL.localize()
            .getNid());
    }

    private static Set<I_GetConceptData> getSourceRelTarget(I_GetConceptData refsetIdentityConcept,
            I_ConfigAceFrame config, int refsetIdentityNid) throws IOException {
        I_TermFactory tf = LocalVersionedTerminology.get();
        I_IntSet allowedTypes = tf.newIntSet();
        allowedTypes.add(refsetIdentityNid);
        Set<I_GetConceptData> matchingConcepts = refsetIdentityConcept.getSourceRelTargets(config.getAllowedStatus(),
            allowedTypes, config.getViewPositionSet(), false);
        return matchingConcepts;
    }

    public static Set<I_GetConceptData> getSpecificationRefsetForRefset(I_GetConceptData refsetIdentityConcept,
            I_ConfigAceFrame config) throws IOException, TerminologyException {
        return getDestRelOrigins(refsetIdentityConcept, config, RefsetAuxiliary.Concept.SPECIFIES_REFSET.localize()
            .getNid());
    }

    private static Set<I_GetConceptData> getDestRelOrigins(I_GetConceptData refsetIdentityConcept,
            I_ConfigAceFrame config, int refsetIdentityNid) throws IOException {
        I_TermFactory tf = LocalVersionedTerminology.get();
        I_IntSet allowedTypes = tf.newIntSet();
        allowedTypes.add(refsetIdentityNid);
        Set<I_GetConceptData> matchingConcepts = refsetIdentityConcept.getDestRelOrigins(config.getAllowedStatus(),
            allowedTypes, config.getViewPositionSet(), false);
        return matchingConcepts;
    }

    /**
     * Create a new refset.
     *
     * @param refsetType
     *          The refset type will be determined by the type of extension to be added (eg I_ThinExtByRefPartConcept.class).
     *          This should be the same type intended for use in {@link #newRefsetExtension(int, int, Class, BeanPropertyMap)}.
     *
     * @param purpose
     *          The destination concept for the refset purpose relationship.
     *          Should be a descendant of {@link RefsetAuxiliary.Concept.REFSET_PURPOSE}
     *          (eg {@link RefsetAuxiliary.Concept.REFSET_MEMBER_PURPOSE}).
     *
     * @param description
     *          The name for the new refset
     *
     * @return The concept defining the new refset
     */
    public static <T extends I_ThinExtByRefPart> I_GetConceptData newRefset(Class<T> refsetType, String description, I_ConceptualizeUniversally purpose)
            throws Exception {

        I_TermFactory termFactory = LocalVersionedTerminology.get();
        I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();

        // Load references

        I_ConceptualizeLocally fullySpecifiedName =
            ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize();
        I_ConceptualizeLocally preferredTerm =
            ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize();

        I_GetConceptData isARel =
            //termFactory.getConcept(SNOMED.Concept.IS_A.localize().getNid());
            termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
        I_GetConceptData refsetIdentity =
            termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_IDENTITY.getUids());
        I_GetConceptData definingCharacteristic =
            termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.localize().getNid());
        I_GetConceptData optionalRefinability =
            termFactory.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.localize().getNid());
        I_GetConceptData currentStatus =
            termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
        I_GetConceptData refsetTypeRel =
            termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_TYPE_REL.localize().getNid());
        I_GetConceptData refsetPurposeRel =
            termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_PURPOSE.localize().getNid());

        I_GetConceptData conceptExtType =
            termFactory.getConcept(termFactory.getRefsetTypeIdByExtensionType(refsetType));
        I_GetConceptData refsetPurpose =
            termFactory.getConcept(purpose.localize().getNid());

        I_GetConceptData newRefsetConcept = termFactory.newConcept(UUID.randomUUID(), false, config);
        termFactory.newDescription(
            UUID.randomUUID(), newRefsetConcept, "en-AU", description.concat(" (refset)"), fullySpecifiedName, config);
        termFactory.newDescription(
            UUID.randomUUID(), newRefsetConcept, "en-AU", description, preferredTerm, config);
        termFactory.newRelationship(
            UUID.randomUUID(), newRefsetConcept, isARel, refsetIdentity,
            definingCharacteristic, optionalRefinability, currentStatus, 0, config);
        termFactory.newRelationship(
            UUID.randomUUID(), newRefsetConcept, refsetTypeRel, conceptExtType,
            definingCharacteristic, optionalRefinability, currentStatus, 0, config);
        termFactory.newRelationship(
            UUID.randomUUID(), newRefsetConcept, refsetPurposeRel, refsetPurpose,
            definingCharacteristic, optionalRefinability, currentStatus, 0, config);

        return newRefsetConcept;
    }

}
