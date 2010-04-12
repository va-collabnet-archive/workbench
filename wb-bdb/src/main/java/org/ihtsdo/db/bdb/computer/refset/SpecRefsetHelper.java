package org.ihtsdo.db.bdb.computer.refset;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.RefsetPropertyMap.REFSET_PROPERTY;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidString;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidString;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartInt;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.refset.spec.I_HelpMemberRefset;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.AllowDataCheckSuppression;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.ReferenceConcepts;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

@AllowDataCheckSuppression
public class SpecRefsetHelper extends RefsetHelper implements I_HelpSpecRefset {

    protected PositionSetReadOnly viewPositions;
    protected Set<I_Path> editPaths;
    protected I_IntSet allowedStatuses;
    protected I_IntSet isARelTypes;

    private Logger logger = Logger.getLogger(SpecRefsetHelper.class.getName());

    public SpecRefsetHelper(I_ConfigAceFrame config) throws Exception {
        super(config);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.refset.spec.I_HelpSpecRefset#getCurrentRefsetExtension(int,
     * int)
     */
    public I_ExtendByRefPartCid getCurrentRefsetExtension(int refsetId, int conceptId) throws Exception {

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
            for (Integer currentStatus : getCurrentStatusIds()) {
                if (latestPart != null && latestPart.getStatusId() == currentStatus) {
                    return latestPart;
                }
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.refset.spec.I_HelpSpecRefset#hasCurrentRefsetExtension(int,
     * int, int)
     */
    public boolean hasCurrentRefsetExtension(int refsetId, int conceptId, int memberTypeId) throws Exception {
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
                for (Integer currentStatus : getCurrentStatusIds()) {
                    if (latestPart.getStatusId() == currentStatus) {
                        if (latestPart instanceof I_ExtendByRefPartCid) {
                            int partValue = ((I_ExtendByRefPartCid) latestPart).getC1id();
                            if (partValue == memberTypeId) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.refset.spec.I_HelpSpecRefset#getCurrentStatusIds()
     */
    public Set<Integer> getCurrentStatusIds() {
        Set<Integer> statuses = new HashSet<Integer>();

        try {
            statuses.add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.PENDING_MOVE.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.CONCEPT_RETIRED.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.LIMITED.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.FLAGGED_FOR_REVIEW.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.FLAGGED_FOR_DUAL_REVIEW.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.DUAL_REVIEWED.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.RESOLVED_IN_DUAL.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.ADJUDICATED.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.READY_TO_PROMOTE.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.PROMOTED.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.OPTIONAL.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.DEVELOPMENTAL.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.EXPERIMENTAL.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.FROM_SNOMED.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.REASSIGNED.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.DUAL_REVIEWED_AND_REASSIGNED.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.RESOLVED_IN_DUAL_AND_REASSIGNED.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.PROCESSED.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.DUAL_REVIEWED_AND_PROCESSED.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.RESOLVED_IN_DUAL_AND_PROCESSED.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.ADJUDICATED_AND_PROCESSED.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.DUPLICATE_PENDING_RETIREMENT.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.INTERNAL_USE_ONLY.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.DO_NOT_EDIT_INTERNAL_USE.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.DO_NOT_EDIT_FOR_RELEASE.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.FLAGGED_POTENTIAL_DUPLICATE.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.FLAGGED_POTENTIAL_REL_ERROR.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.CURRENT_UNREVIEWED.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.CURRENT_TEMP_INTERNAL_USE.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.FLAGGED_POTENTIAL_DESC_STYLE_ERROR.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.UNREVIEWED_NEW_ADDITION.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.UNREVIEWED_NEW_DELETION.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.REVIEWED_APPROVED_ADDITION.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.REVIEWED_NOT_APPROVED_ADDITION.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.REVIEWED_APPROVED_DELETION.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.REVIEWED_NOT_APPROVED_DELETION.localize().getNid());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statuses;
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.refset.spec.I_HelpSpecRefset#getCurrentStatusIntSet()
     */
    public I_IntSet getCurrentStatusIntSet() {
        I_IntSet statuses = Terms.get().newIntSet();

        try {
            statuses.add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.PENDING_MOVE.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.CONCEPT_RETIRED.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.LIMITED.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.FLAGGED_FOR_REVIEW.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.FLAGGED_FOR_DUAL_REVIEW.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.DUAL_REVIEWED.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.RESOLVED_IN_DUAL.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.ADJUDICATED.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.READY_TO_PROMOTE.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.PROMOTED.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.OPTIONAL.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.DEVELOPMENTAL.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.EXPERIMENTAL.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.FROM_SNOMED.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.REASSIGNED.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.DUAL_REVIEWED_AND_REASSIGNED.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.RESOLVED_IN_DUAL_AND_REASSIGNED.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.PROCESSED.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.DUAL_REVIEWED_AND_PROCESSED.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.RESOLVED_IN_DUAL_AND_PROCESSED.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.ADJUDICATED_AND_PROCESSED.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.DUPLICATE_PENDING_RETIREMENT.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.INTERNAL_USE_ONLY.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.DO_NOT_EDIT_INTERNAL_USE.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.DO_NOT_EDIT_FOR_RELEASE.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.FLAGGED_POTENTIAL_DUPLICATE.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.FLAGGED_POTENTIAL_REL_ERROR.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.CURRENT_UNREVIEWED.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.CURRENT_TEMP_INTERNAL_USE.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.FLAGGED_POTENTIAL_DESC_STYLE_ERROR.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.UNREVIEWED_NEW_ADDITION.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.UNREVIEWED_NEW_DELETION.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.REVIEWED_APPROVED_ADDITION.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.REVIEWED_NOT_APPROVED_ADDITION.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.REVIEWED_APPROVED_DELETION.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.REVIEWED_NOT_APPROVED_DELETION.localize().getNid());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statuses;
    }

    /*
     * (non-Javadoc)
     * @seeorg.dwfa.ace.refset.spec.I_HelpSpecRefset#
     * hasCurrentConceptConceptRefsetExtension(int, int, int, int, int)
     */
    public boolean hasCurrentConceptConceptRefsetExtension(int refsetId, int conceptId, int c1Id, int c2Id, int statusId)
            throws Exception {
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
                if (latestPart.getStatusId() == statusId) {
                    if (latestPart instanceof I_ExtendByRefPartCidCid) {
                        int c1Value = ((I_ExtendByRefPartCidCid) latestPart).getC1id();
                        int c2Value = ((I_ExtendByRefPartCidCid) latestPart).getC2id();
                        if (c1Value == c1Id && c2Value == c2Id) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.refset.spec.I_HelpSpecRefset#hasCurrentConceptRefsetExtension
     * (int, int, int, int)
     */
    public boolean hasCurrentConceptRefsetExtension(int refsetId, int componentId, int conceptId, int statusId)
            throws Exception {
        for (I_ExtendByRef extension : Terms.get().getAllExtensionsForComponent(componentId, true)) {

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
                if (latestPart.getStatusId() == statusId) {
                    if (latestPart instanceof I_ExtendByRefPartCid) {
                        int cValue = ((I_ExtendByRefPartCid) latestPart).getC1id();
                        if (cValue == conceptId) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean hasCurrentConceptStringRefsetExtension(int refsetId, int componentId, int conceptId,
            String extString, int statusId) throws Exception {
        for (I_ExtendByRef extension : Terms.get().getAllExtensionsForComponent(componentId, true)) {

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
                if (latestPart.getStatusId() == statusId) {
                    if (latestPart instanceof I_ExtendByRefPartCidString) {
                        String extensionString = ((I_ExtendByRefPartCidString) latestPart).getStringValue();
                        int currentc1Id = ((I_ExtendByRefPartCidString) latestPart).getC1id();
                        if (extString.equals(extensionString) && conceptId == currentc1Id) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean hasCurrentStringRefsetExtension(int refsetId, int componentId, String extString, int statusId)
            throws Exception {
        for (I_ExtendByRef extension : Terms.get().getAllExtensionsForComponent(componentId, true)) {

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
                if (latestPart.getStatusId() == statusId) {
                    if (latestPart instanceof I_ExtendByRefPartStr) {
                        String extensionString = ((I_ExtendByRefPartStr) latestPart).getStringValue();
                        if (extString.equals(extensionString)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.refset.spec.I_HelpSpecRefset#hasCurrentIntRefsetExtension
     * (int, int, int, int)
     */
    public boolean hasCurrentIntRefsetExtension(int refsetId, int componentId, int value, int statusId)
            throws Exception {
        for (I_ExtendByRef extension : Terms.get().getAllExtensionsForComponent(componentId, true)) {

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
                if (latestPart.getStatusId() == statusId) {
                    if (latestPart instanceof I_ExtendByRefPartInt) {
                        int currentValue = ((I_ExtendByRefPartInt) latestPart).getIntValue();
                        if (currentValue == value) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @seeorg.dwfa.ace.refset.spec.I_HelpSpecRefset#
     * hasCurrentConceptConceptConceptRefsetExtension(int, int, int, int, int,
     * int)
     */
    public boolean hasCurrentConceptConceptConceptRefsetExtension(int refsetId, int conceptId, int c1Id, int c2Id,
            int c3Id, int statusId) throws Exception {

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
                if (latestPart.getStatusId() == statusId) {
                    if (latestPart instanceof I_ExtendByRefPartCidCidCid) {
                        int c1Value = ((I_ExtendByRefPartCidCidCid) latestPart).getC1id();
                        int c2Value = ((I_ExtendByRefPartCidCidCid) latestPart).getC2id();
                        int c3Value = ((I_ExtendByRefPartCidCidCid) latestPart).getC3id();
                        if (c1Value == c1Id && c2Value == c2Id && c3Value == c3Id) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @seeorg.dwfa.ace.refset.spec.I_HelpSpecRefset#
     * hasCurrentConceptConceptStringRefsetExtension(int, int, int, int,
     * java.lang.String, int)
     */
    public boolean hasCurrentConceptConceptStringRefsetExtension(int refsetId, int conceptId, int c1Id, int c2Id,
            String stringInput, int statusId) throws Exception {
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
                if (latestPart.getStatusId() == statusId) {
                    if (latestPart instanceof I_ExtendByRefPartCidCidString) {
                        int c1Value = ((I_ExtendByRefPartCidCidString) latestPart).getC1id();
                        int c2Value = ((I_ExtendByRefPartCidCidString) latestPart).getC2id();
                        String strValue = ((I_ExtendByRefPartCidCidString) latestPart).getStringValue();
                        if (c1Value == c1Id && c2Value == c2Id && strValue.equals(stringInput)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.refset.spec.I_HelpSpecRefset#newRefsetExtension(int,
     * int, int)
     */
    public boolean newRefsetExtension(int refsetId, int conceptId, int memberTypeId) throws Exception {
        return newRefsetExtension(refsetId, conceptId, memberTypeId, true);
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.refset.spec.I_HelpSpecRefset#newRefsetExtension(int,
     * int, int, boolean)
     */
    public boolean newRefsetExtension(int refsetId, int conceptId, int memberTypeId, boolean checkNotExists)
            throws Exception {

        if (checkNotExists) {
            // check subject is not already a member
            if (hasCurrentRefsetExtension(refsetId, conceptId, memberTypeId)) {
                if (logger.isLoggable(Level.FINE)) {
                    String extValueDesc = Terms.get().getConcept(memberTypeId).getInitialText();
                    logger.fine("Concept is already a '" + extValueDesc + "' of the refset. Skipping.");
                }
                return false;
            }
        }
        // create a new extension (with a part for each path the user is
        // editing)

        // generate a UUID based on this refset's input data so that it is
        // stable in future executions
        UUID memberUuid =
                generateUuid(Terms.get().getUids(refsetId).iterator().next(), Terms.get().getUids(conceptId).iterator()
                    .next(), Terms.get().getUids(memberTypeId).iterator().next());

        RefsetPropertyMap refsetMap = new RefsetPropertyMap(REFSET_TYPES.CID);
        refsetMap.put(REFSET_PROPERTY.CID_ONE, memberTypeId);
        I_ExtendByRef newExtension = makeMemberAndSetup(refsetId, conceptId, REFSET_TYPES.CID, refsetMap, memberUuid);
        if (isAutocommitActive()) {
            Terms.get().addUncommittedNoChecks(newExtension);
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.refset.spec.I_HelpSpecRefset#generateUuid(java.util.UUID,
     * java.util.UUID, java.util.UUID)
     */
    public UUID generateUuid(UUID uuid, UUID uuid2, UUID uuid3) {
        try {
            UUID intermediateUuid = Type5UuidFactory.get(uuid, uuid2.toString());
            return Type5UuidFactory.get(intermediateUuid, uuid3.toString());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.refset.spec.I_HelpSpecRefset#newConceptConceptRefsetExtension
     * (int, int, int, int, java.util.UUID, java.util.UUID, java.util.UUID, long)
     */
    public boolean newConceptConceptRefsetExtension(int refsetId, int componentId, int c1Id, int c2Id, UUID memberUuid,
            UUID pathUuid, UUID statusUuid, long effectiveTime) throws Exception {

        try {
            Collection<I_Path> paths = Terms.get().getPaths();
            paths.clear();
            paths.add(Terms.get().getPath(new UUID[] { pathUuid }));

            if (memberUuid == null) {
                memberUuid = UUID.randomUUID();
            }
            if (statusUuid == null) {
                statusUuid =
                        Terms.get().getConcept(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid()).getUids()
                            .iterator().next();
            }
            // check subject is not already a member
            if (hasCurrentConceptConceptRefsetExtension(refsetId, componentId, c1Id, c2Id, Terms.get().getConcept(
                new UUID[] { statusUuid }).getConceptId())) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Component is already a member of the refset. Skipping.");
                }
                return false;
            }

            // create a new extension (with a part for each path the user is
            // editing)
            RefsetPropertyMap refsetMap = new RefsetPropertyMap(REFSET_TYPES.CID_CID);
            refsetMap.put(REFSET_PROPERTY.CID_ONE, c1Id);
            refsetMap.put(REFSET_PROPERTY.CID_TWO, c2Id);
            if (effectiveTime != Long.MAX_VALUE) {
                refsetMap.put(REFSET_PROPERTY.TIME, Long.MAX_VALUE);
            }
            I_ExtendByRef newExtension =
                    getOrCreateRefsetExtension(refsetId, componentId, REFSET_TYPES.CID_CID, refsetMap, memberUuid);
            if (isAutocommitActive()) {
                Terms.get().addUncommittedNoChecks(newExtension);
                Terms.get().commit();
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.refset.spec.I_HelpSpecRefset#newStringRefsetExtension(int,
     * int, java.lang.String, java.util.UUID, java.util.UUID, java.util.UUID,
     * long)
     */
    public boolean newStringRefsetExtension(int refsetId, int componentId, String extString, UUID memberUuid,
            UUID pathUuid, UUID statusUuid, long effectiveTime) throws Exception {
        try {
            Collection<I_Path> paths = Terms.get().getPaths();
            paths.clear();
            paths.add(Terms.get().getPath(new UUID[] { pathUuid }));

            if (memberUuid == null) {
                memberUuid = UUID.randomUUID();
            }
            if (statusUuid == null) {
                statusUuid =
                        Terms.get().getConcept(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid()).getUids()
                            .iterator().next();
            }

            // check subject is not already a member
            if (hasCurrentStringRefsetExtension(refsetId, componentId, extString, Terms.get().getConcept(
                new UUID[] { statusUuid }).getConceptId())) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Component is already a member of the refset. Skipping.");
                }
                return false;
            }

            // create a new extension (with a part for each path the user is
            // editing)
            RefsetPropertyMap refsetMap = new RefsetPropertyMap(REFSET_TYPES.STR);
            refsetMap.put(REFSET_PROPERTY.STRING_VALUE, extString);
            refsetMap.put(REFSET_PROPERTY.STATUS, Bdb.uuidToNid(statusUuid));
            if (effectiveTime != Long.MAX_VALUE) {
                refsetMap.put(REFSET_PROPERTY.TIME, Long.MAX_VALUE);
            }
            I_ExtendByRef newExtension =
                    getOrCreateRefsetExtension(refsetId, componentId, REFSET_TYPES.STR, refsetMap, memberUuid);
            if (isAutocommitActive()) {
                Terms.get().addUncommittedNoChecks(newExtension);
                Terms.get().commit();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.refset.spec.I_HelpSpecRefset#newConceptStringRefsetExtension
     * (int, int, int, java.lang.String, java.util.UUID, java.util.UUID,
     * java.util.UUID, long)
     */
    public boolean newConceptStringRefsetExtension(int refsetId, int componentId, int c1Id, String extString,
            UUID memberUuid, UUID pathUuid, UUID statusUuid, long effectiveTime) throws Exception {
        try {
            Collection<I_Path> paths = Terms.get().getPaths();
            paths.clear();
            paths.add(Terms.get().getPath(new UUID[] { pathUuid }));

            if (memberUuid == null) {
                memberUuid = UUID.randomUUID();
            }
            if (statusUuid == null) {
                statusUuid =
                        Terms.get().getConcept(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid()).getUids()
                            .iterator().next();
            }

            // check subject is not already a member
            if (hasCurrentConceptStringRefsetExtension(refsetId, componentId, c1Id, extString, Terms.get().getConcept(
                new UUID[] { statusUuid }).getConceptId())) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Component is already a member of the refset. Skipping.");
                }
                return false;
            }
            // create a new extension (with a part for each path the user is
            // editing)
            RefsetPropertyMap refsetMap = new RefsetPropertyMap(REFSET_TYPES.CID_STR);
            refsetMap.put(REFSET_PROPERTY.CID_ONE, c1Id);
            refsetMap.put(REFSET_PROPERTY.STRING_VALUE, extString);
            refsetMap.put(REFSET_PROPERTY.STATUS, Bdb.uuidToNid(statusUuid));
            if (effectiveTime != Long.MAX_VALUE) {
                refsetMap.put(REFSET_PROPERTY.TIME, Long.MAX_VALUE);
            }
            I_ExtendByRef newExtension =
                    getOrCreateRefsetExtension(refsetId, componentId, REFSET_TYPES.STR, refsetMap, memberUuid);
            if (isAutocommitActive()) {
                Terms.get().addUncommittedNoChecks(newExtension);
                Terms.get().commit();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.refset.spec.I_HelpSpecRefset#newIntRefsetExtension(int,
     * int, int, java.util.UUID, java.util.UUID, java.util.UUID, long)
     */
    public boolean newIntRefsetExtension(int refsetId, int componentId, int value, UUID memberUuid, UUID pathUuid,
            UUID statusUuid, long effectiveTime) throws Exception {

        Collection<I_Path> paths = Terms.get().getPaths();
        paths.clear();
        paths.add(Terms.get().getPath(new UUID[] { pathUuid }));

        if (memberUuid == null) {
            memberUuid = UUID.randomUUID();
        }
        if (statusUuid == null) {
            statusUuid =
                    Terms.get().getConcept(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid()).getUids()
                        .iterator().next();
        }

        // check subject is not already a member
        if (hasCurrentIntRefsetExtension(refsetId, componentId, value, Terms.get()
            .getConcept(new UUID[] { statusUuid }).getConceptId())) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Component is already a member of the refset. Skipping.");
            }
            return false;
        }
        // create a new extension (with a part for each path the user is
        // editing)
        RefsetPropertyMap refsetMap = new RefsetPropertyMap(REFSET_TYPES.INT);
        refsetMap.put(REFSET_PROPERTY.INTEGER_VALUE, value);
        if (effectiveTime != Long.MAX_VALUE) {
            refsetMap.put(REFSET_PROPERTY.TIME, Long.MAX_VALUE);
        }
        I_ExtendByRef newExtension =
                getOrCreateRefsetExtension(refsetId, componentId, REFSET_TYPES.INT, refsetMap, memberUuid);
        if (isAutocommitActive()) {
            Terms.get().addUncommittedNoChecks(newExtension);
            Terms.get().commit();
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.refset.spec.I_HelpSpecRefset#newConceptRefsetExtension(int,
     * int, int, java.util.UUID, java.util.UUID, java.util.UUID, long)
     */
    public boolean newConceptRefsetExtension(int refsetId, int componentId, int conceptId, UUID memberUuid,
            UUID pathUuid, UUID statusUuid, long effectiveTime) throws Exception {

        Collection<I_Path> paths = Terms.get().getPaths();
        paths.clear();
        paths.add(Terms.get().getPath(new UUID[] { pathUuid }));

        if (memberUuid == null) {
            memberUuid = UUID.randomUUID();
        }
        if (statusUuid == null) {
            statusUuid =
                    Terms.get().getConcept(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid()).getUids()
                        .iterator().next();
        }

        // check subject is not already a member
        if (hasCurrentConceptRefsetExtension(refsetId, componentId, conceptId, Terms.get().getConcept(
            new UUID[] { statusUuid }).getConceptId())) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Component is already a member of the refset. Skipping.");
            }
            return false;
        }

        // create a new extension (with a part for each path the user is
        // editing)
        RefsetPropertyMap refsetMap = new RefsetPropertyMap(REFSET_TYPES.CID);
        refsetMap.put(REFSET_PROPERTY.CID_ONE, conceptId);
        if (effectiveTime != Long.MAX_VALUE) {
            refsetMap.put(REFSET_PROPERTY.TIME, Long.MAX_VALUE);
        }
        I_ExtendByRef newExtension = makeMemberAndSetup(refsetId, componentId, REFSET_TYPES.CID, refsetMap, memberUuid);
        if (isAutocommitActive()) {
            Terms.get().addUncommittedNoChecks(newExtension);
            Terms.get().commit();
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * @seeorg.dwfa.ace.refset.spec.I_HelpSpecRefset#
     * newConceptConceptConceptRefsetExtension(int, int, int, int, int,
     * java.util.UUID, java.util.UUID, java.util.UUID, long)
     */
    public boolean newConceptConceptConceptRefsetExtension(int refsetId, int componentId, int c1Id, int c2Id, int c3Id,
            UUID memberUuid, UUID pathUuid, UUID statusUuid, long effectiveTime) throws Exception {

        Collection<I_Path> paths = Terms.get().getPaths();
        paths.clear();
        paths.add(Terms.get().getPath(new UUID[] { pathUuid }));

        if (memberUuid == null) {
            memberUuid = UUID.randomUUID();
        }
        if (statusUuid == null) {
            statusUuid =
                    Terms.get().getConcept(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid()).getUids()
                        .iterator().next();
        }

        // check subject is not already a member
        if (hasCurrentConceptConceptConceptRefsetExtension(refsetId, componentId, c1Id, c2Id, c3Id, Terms.get()
            .getConcept(new UUID[] { statusUuid }).getConceptId())) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Component is already a member of the refset. Skipping.");
            }
            return false;
        }

        // create a new extension (with a part for each path the user is
        // editing)
        RefsetPropertyMap refsetMap = new RefsetPropertyMap(REFSET_TYPES.CID_CID_CID);
        refsetMap.put(REFSET_PROPERTY.CID_ONE, c1Id);
        refsetMap.put(REFSET_PROPERTY.CID_TWO, c2Id);
        refsetMap.put(REFSET_PROPERTY.CID_THREE, c3Id);
        if (effectiveTime != Long.MAX_VALUE) {
            refsetMap.put(REFSET_PROPERTY.TIME, Long.MAX_VALUE);
        }
        I_ExtendByRef newExtension =
                getOrCreateRefsetExtension(refsetId, componentId, REFSET_TYPES.CID_CID_CID, refsetMap, memberUuid);
        if (isAutocommitActive()) {
            Terms.get().addUncommittedNoChecks(newExtension);
            Terms.get().commit();
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * @seeorg.dwfa.ace.refset.spec.I_HelpSpecRefset#
     * newConceptConceptStringRefsetExtension(int, int, int, int,
     * java.lang.String, java.util.UUID, java.util.UUID, java.util.UUID, long)
     */
    public boolean newConceptConceptStringRefsetExtension(int refsetId, int componentId, int c1Id, int c2Id,
            String stringValue, UUID memberUuid, UUID pathUuid, UUID statusUuid, long effectiveTime) throws Exception {

        Collection<I_Path> paths = Terms.get().getPaths();
        paths.clear();
        paths.add(Terms.get().getPath(new UUID[] { pathUuid }));

        // check subject is not already a member
        if (hasCurrentConceptConceptStringRefsetExtension(refsetId, componentId, c1Id, c2Id, stringValue, Terms.get()
            .getConcept(new UUID[] { statusUuid }).getConceptId())) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Component is already a member of the refset. Skipping.");
            }
            return false;
        }

        if (memberUuid == null) {
            memberUuid = UUID.randomUUID();
        }
        if (statusUuid == null) {
            statusUuid =
                    Terms.get().getConcept(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid()).getUids()
                        .iterator().next();
        }

        // create a new extension (with a part for each path the user is
        // editing)
        // create a new extension (with a part for each path the user is
        // editing)
        RefsetPropertyMap refsetMap = new RefsetPropertyMap(REFSET_TYPES.CID_CID_STR);
        refsetMap.put(REFSET_PROPERTY.CID_ONE, c1Id);
        refsetMap.put(REFSET_PROPERTY.CID_TWO, c2Id);
        refsetMap.put(REFSET_PROPERTY.STRING_VALUE, stringValue);
        if (effectiveTime != Long.MAX_VALUE) {
            refsetMap.put(REFSET_PROPERTY.TIME, Long.MAX_VALUE);
        }
        I_ExtendByRef newExtension =
                getOrCreateRefsetExtension(refsetId, componentId, REFSET_TYPES.CID_CID_STR, refsetMap, memberUuid);
        if (isAutocommitActive()) {
            Terms.get().addUncommittedNoChecks(newExtension);
            Terms.get().commit();
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.refset.spec.I_HelpSpecRefset#retireRefsetExtension(int,
     * int, int)
     */
    public boolean retireRefsetExtension(int refsetId, int conceptId, int memberTypeId) throws Exception {

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

                // confirm its the right extension value and its status is one
                // of the possible current status IDs
                for (Integer statusId : getCurrentStatusIds()) {
                    if (latestPart.getStatusId() == statusId) {
                        if (latestPart instanceof I_ExtendByRefPartCid) {
                            int partValue = ((I_ExtendByRefPartCid) latestPart).getC1id();
                            if (partValue == memberTypeId) {
                                // found a member to retire
                                for (I_Path editPath : getEditPaths()) {

                                    I_ExtendByRefPartCid clone =
                                            (I_ExtendByRefPartCid) latestPart.makeAnalog(ReferenceConcepts.RETIRED
                                                .getNid(), editPath.getConceptId(), Long.MAX_VALUE);
                                    extension.addVersion(clone);
                                    Terms.get().addUncommittedNoChecks(extension);

                                }
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.refset.spec.I_HelpSpecRefset#getAllDescendants(org.dwfa.
     * ace.api.I_GetConceptData,
     * org.dwfa.ace.refset.spec.SpecRefsetHelper.Condition)
     */
    public Set<I_GetConceptData> getAllDescendants(I_GetConceptData concept, Condition... conditions) throws Exception {
        return getAllDescendants(concept, concept, conditions);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.refset.spec.I_HelpSpecRefset#getAllDescendants(org.dwfa.
     * ace.api.I_GetConceptData, org.dwfa.ace.api.I_GetConceptData,
     * org.dwfa.ace.refset.spec.SpecRefsetHelper.Condition)
     */
    public Set<I_GetConceptData> getAllDescendants(I_GetConceptData concept, I_GetConceptData memberRefset,
            Condition... conditions) throws Exception {

        // find all the children
        Set<I_GetConceptData> descendants =
                getAllDescendants(new HashSet<I_GetConceptData>(), concept, getAllowedStatuses(), getIsARelTypes(),
                    getViewPositions(), conditions);

        logger.fine("Found " + descendants.size() + " descendants of concept '" + concept.getInitialText() + "'.");

        return descendants;
    }

    protected Set<I_GetConceptData> getAllDescendants(Set<I_GetConceptData> resultSet, I_GetConceptData parent,
            I_IntSet allowedStatuses, I_IntSet allowedTypes, PositionSetReadOnly positions, Condition... conditions)
            throws Exception {

        ITERATE_CHILDREN: for (I_RelTuple childTuple : parent.getDestRelTuples(allowedStatuses, allowedTypes,
            positions, 
            getConfig().getPrecedence(), getConfig().getConflictResolutionStrategy())) {
            I_GetConceptData childConcept = Terms.get().getConcept(childTuple.getC1Id());
            if (childConcept.getConceptId() == parent.getConceptId()) {
                continue ITERATE_CHILDREN;
            }
            if (conditions != null) {
                for (Condition condition : conditions) {
                    if (!condition.evaluate(childConcept)) {
                        continue ITERATE_CHILDREN;
                    }
                }
            }
            if (resultSet.add(childConcept)) {
                resultSet.addAll(getAllDescendants(resultSet, childConcept, allowedStatuses, allowedTypes, positions,
                    conditions));
            }
        }
        return resultSet;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.refset.spec.I_HelpSpecRefset#getAllAncestors(org.dwfa.ace
     * .api.I_GetConceptData,
     * org.dwfa.ace.refset.spec.SpecRefsetHelper.Condition)
     */
    public Set<I_GetConceptData> getAllAncestors(I_GetConceptData concept, Condition... conditions) throws Exception {

        // find all the parents
        Set<I_GetConceptData> parentConcepts =
                getAllAncestors(new HashSet<I_GetConceptData>(), concept, getAllowedStatuses(), getIsARelTypes(),
                    getViewPositions(), conditions);

        logger.fine("Found " + parentConcepts.size() + " ancestors of concept '" + concept.getInitialText() + "'.");

        return parentConcepts;
    }

    protected Set<I_GetConceptData> getAllAncestors(Set<I_GetConceptData> resultSet, I_GetConceptData child,
            I_IntSet allowedStatuses, I_IntSet allowedTypes, PositionSetReadOnly positions, Condition... conditions)
            throws Exception {

        ITERATE_PARENTS: for (I_RelTuple childTuple : child.getSourceRelTuples(allowedStatuses, allowedTypes,
            positions, 
            getConfig().getPrecedence(), getConfig().getConflictResolutionStrategy())) {
            I_GetConceptData parentConcept = Terms.get().getConcept(childTuple.getC2Id());
            if (parentConcept.getConceptId() == child.getConceptId()) {
                continue ITERATE_PARENTS;
            }
            if (conditions != null) {
                for (Condition condition : conditions) {
                    if (!condition.evaluate(parentConcept)) {
                        continue ITERATE_PARENTS;
                    }
                }
            }
            if (resultSet.add(parentConcept)) {
                resultSet.addAll(getAllAncestors(resultSet, parentConcept, allowedStatuses, allowedTypes, positions,
                    conditions));
            }
        }
        return resultSet;
    }

    /**
     * @return The view positions from the active config. Returns null if no
     *         config set or config contains no view positions.
     */
    protected PositionSetReadOnly getViewPositions() throws Exception {
        if (this.viewPositions == null) {
            I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

            if (config != null) {
                this.viewPositions = config.getViewPositionSetReadOnly();
            }

            if (this.viewPositions == null) {
                this.viewPositions = new PositionSetReadOnly(new HashSet<I_Position>());
            }
        }
        return (this.viewPositions.isEmpty()) ? null : this.viewPositions;
    }

    /**
     * @return The edit paths from the active config. Returns null if no config
     *         set or the config defines no paths for editing.
     */
    protected Set<I_Path> getEditPaths() throws Exception {
        if (this.editPaths == null) {
            I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

            if (config != null) {
                this.editPaths = config.getEditingPathSet();
            }

            if (this.editPaths == null) {
                this.editPaths = new HashSet<I_Path>();
            }
        }
        return (this.editPaths.isEmpty()) ? null : this.editPaths;
    }

    /**
     * @return The allowed status from the active config. Returns just "CURRENT"
     *         if no config set.
     */
    protected I_IntSet getAllowedStatuses() throws Exception {
        if (this.allowedStatuses == null) {
            I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

            if (config != null) {
                this.allowedStatuses = config.getAllowedStatus();
            } else {
                this.allowedStatuses = Terms.get().newIntSet();
                this.allowedStatuses.add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
            }
        }

        return this.allowedStatuses;
    }

    /**
     * @return By default (unless overridden by a subclass) will provide both
     *         the SNOMED and ArchitectonicAuxiliary IS_A concepts.
     */
    protected I_IntSet getIsARelTypes() throws Exception {
        return Terms.get().getActiveAceFrameConfig().getDestRelTypes();
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.refset.spec.I_HelpSpecRefset#getConceptTypeId()
     */
    public int getConceptTypeId() {
        return ReferenceConcepts.CONCEPT_EXTENSION.getNid();
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.refset.spec.I_HelpSpecRefset#setConceptTypeId(int)
     */
    public void setConceptTypeId(int conceptTypeId) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.refset.spec.I_HelpSpecRefset#newConceptExtensionPart(int,
     * int, int)
     */
    public boolean newConceptExtensionPart(int refsetId, int componentId, int c1Id) throws Exception {
        return newConceptExtensionPart(refsetId, componentId, c1Id, ReferenceConcepts.CURRENT.getNid());
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.refset.spec.I_HelpSpecRefset#newConceptExtensionPart(int,
     * int, int, int)
     */
    public boolean newConceptExtensionPart(int refsetId, int componentId, int c1Id, int statusId) throws Exception {
        for (I_ExtendByRef extension : Terms.get().getAllExtensionsForComponent(componentId)) {
            if (extension.getRefsetId() == refsetId) {
                // get the latest version
                I_ExtendByRefPart latestPart = null;
                for (I_ExtendByRefPart part : extension.getMutableParts()) {
                    if ((latestPart == null) || (part.getTime() >= latestPart.getTime())) {
                        latestPart = part;
                    }
                }
                if (latestPart == null) {
                    return false;
                }

                if (latestPart instanceof I_ExtendByRefPartCid) {
                    for (I_Path editPath : getEditPaths()) {
                        I_ExtendByRefPartCid clone =
                                (I_ExtendByRefPartCid) latestPart.makeAnalog(statusId, editPath.getConceptId(),
                                    Long.MAX_VALUE);
                        clone.setC1id(c1Id);
                        extension.addVersion(clone);
                        Terms.get().addUncommittedNoChecks(extension);
                    }
                    return true;
                }
            }
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.refset.spec.I_HelpSpecRefset#retireConceptExtension(int,
     * int)
     */
    public boolean retireConceptExtension(int refsetId, int componentId) throws Exception {
        for (I_ExtendByRef extension : Terms.get().getAllExtensionsForComponent(componentId)) {
            if (extension.getRefsetId() == refsetId) {
                // get the latest version
                I_ExtendByRefPart latestPart = null;
                for (I_ExtendByRefPart part : extension.getMutableParts()) {
                    if ((latestPart == null) || (part.getTime() >= latestPart.getTime())) {
                        latestPart = part;
                    }
                }
                if (latestPart == null) {
                    return false;
                }

                // confirm its the right extension value and its status is
                // current
                for (Integer currentId : getCurrentStatusIds()) {
                    if (latestPart.getStatusId() == currentId) {
                        if (latestPart instanceof I_ExtendByRefPartCid) {
                            // found a member to retire
                            for (I_Path editPath : getEditPaths()) {
                                I_ExtendByRefPartCid clone =
                                        (I_ExtendByRefPartCid) latestPart
                                            .makeAnalog(ReferenceConcepts.RETIRED.getNid(), editPath.getConceptId(),
                                                Long.MAX_VALUE);
                                extension.addVersion(clone);
                                Terms.get().addUncommittedNoChecks(extension);
                            }
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.refset.spec.I_HelpSpecRefset#filterListByConceptType(java
     * .util.List, org.dwfa.ace.api.I_GetConceptData)
     */
    public List<I_GetConceptData> filterListByConceptType(Collection<? extends I_ExtendByRef> allExtensions,
            I_GetConceptData requiredPromotionStatusConcept) throws Exception {

        List<I_GetConceptData> filteredList = new ArrayList<I_GetConceptData>();

        for (I_ExtendByRef extension : allExtensions) {
            I_ExtendByRefPart latestMemberPart = getLatestCurrentPart(extension);
            if (latestMemberPart == null) {
                throw new Exception("Member extension exists with no parts.");
            }
            I_GetConceptData promotionStatus = null;
            if (extension != null) {
                promotionStatus = getPromotionStatus(extension);
            }

            if (promotionStatus != null
                && promotionStatus.getConceptId() == requiredPromotionStatusConcept.getConceptId()) {
                if (Terms.get().hasConcept(extension.getComponentId())) {
                    filteredList.add(Terms.get().getConcept(extension.getComponentId()));
                } else {
                    filteredList.add(Terms.get().getConcept(
                        Terms.get().getDescription(extension.getComponentId()).getConceptId()));
                }
            }
        }
        return filteredList;
    }

    private I_GetConceptData getPromotionStatus(I_ExtendByRef promotionExtension) throws Exception {
        I_ExtendByRefPart latestPart = getLatestCurrentPart(promotionExtension);
        if (latestPart == null) {
            return null;
        } else {
            if (latestPart instanceof I_ExtendByRefPartCid) {
                I_ExtendByRefPartCid latestConceptPart = (I_ExtendByRefPartCid) latestPart;
                return Terms.get().getConcept(latestConceptPart.getC1id());
            } else {
                throw new Exception("Don't know how to handle promotion ext of type : " + latestPart);
            }
        }
    }

    /**
     * TODO Need to convert to use the version computer, not bypass paths...
     * 
     * @param memberExtension
     * @return
     * @throws TerminologyException
     * @throws IOException
     */
    @Deprecated
    private I_ExtendByRefPart getLatestCurrentPart(I_ExtendByRef memberExtension) throws TerminologyException,
            IOException {
        I_ExtendByRefPart latestPart = null;
        List<? extends I_ExtendByRefVersion> original = memberExtension.getTuples(null, null, 
                        getConfig().getPrecedence(), getConfig().getConflictResolutionStrategy());

        for (I_ExtendByRefVersion part : original) {
            if ((latestPart == null) || (part.getTime() >= latestPart.getTime())) {
                for (Integer currentStatus : getCurrentStatusIds()) {
                    if (part.getStatusId() == currentStatus) {
                        latestPart = part;
                    }
                }
            }
        }
        return latestPart;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.refset.spec.I_HelpSpecRefset#newConcept(org.dwfa.ace.api
     * .I_ConfigAceFrame, org.dwfa.ace.api.I_GetConceptData)
     */
    public I_GetConceptData newConcept(I_ConfigAceFrame aceConfig, I_GetConceptData status) throws Exception {
        if (status.getNid() != aceConfig.getDefaultStatus().getNid()) {
            throw new UnsupportedOperationException();
        }
        boolean isDefined = true;
        UUID conceptUuid = UUID.randomUUID();
        I_GetConceptData newConcept = Terms.get().newConcept(conceptUuid, isDefined, aceConfig);
        Terms.get().addUncommittedNoChecks(newConcept);

        return newConcept;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.refset.spec.I_HelpSpecRefset#newDescription(org.dwfa.ace
     * .api.I_GetConceptData, org.dwfa.ace.api.I_GetConceptData,
     * java.lang.String, org.dwfa.ace.api.I_ConfigAceFrame,
     * org.dwfa.ace.api.I_GetConceptData)
     */
    public void newDescription(I_GetConceptData concept, I_GetConceptData descriptionType, String description,
            I_ConfigAceFrame aceConfig, I_GetConceptData status) throws Exception {
        if (status.getNid() != aceConfig.getDefaultStatus().getNid()) {
            throw new UnsupportedOperationException();
        }
        UUID descUuid = UUID.randomUUID();
        Terms.get().newDescription(descUuid, concept, "en", description, descriptionType,
            Terms.get().getActiveAceFrameConfig());
        Terms.get().addUncommittedNoChecks(concept);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.refset.spec.I_HelpSpecRefset#newRelationship(org.dwfa.ace
     * .api.I_GetConceptData, org.dwfa.ace.api.I_GetConceptData,
     * org.dwfa.ace.api.I_GetConceptData, org.dwfa.ace.api.I_ConfigAceFrame,
     * org.dwfa.ace.api.I_GetConceptData)
     */
    public void newRelationship(I_GetConceptData concept, I_GetConceptData relationshipType,
            I_GetConceptData destination, I_ConfigAceFrame aceConfig, I_GetConceptData status) throws Exception {
        UUID relUuid = UUID.randomUUID();
        I_GetConceptData charConcept =
                Terms.get().getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids());
        I_GetConceptData refConcept =
                Terms.get().getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids());
        int group = 0;
        Terms.get().newRelationship(relUuid, concept, relationshipType, destination, charConcept, refConcept, status,
            group, Terms.get().getActiveAceFrameConfig());
        Terms.get().addUncommittedNoChecks(concept);
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.refset.spec.I_HelpSpecRefset#getAllValidUsers()
     */
    public Set<? extends I_GetConceptData> getAllValidUsers() throws Exception {
        I_IntSet allowedStatuses = getCurrentStatusIntSet();
        I_GetConceptData userParent = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER.getUids());
        I_IntSet allowedTypes = Terms.get().getActiveAceFrameConfig().getDestRelTypes();

        final Set<? extends I_GetConceptData> allUsers =
                userParent.getDestRelOrigins(allowedStatuses, allowedTypes, Terms.get().getActiveAceFrameConfig()
                    .getViewPositionSetReadOnly(), 
                    getConfig().getPrecedence(), getConfig().getConflictResolutionStrategy());

        Set<I_GetConceptData> allValidUsers = new HashSet<I_GetConceptData>();
        for (I_GetConceptData user : allUsers) {
            if (getInbox(user) != null) {
                allValidUsers.add(user);
            }
        }
        return allValidUsers;
    }

    /*
     * (non-Javadoc)
     * @seeorg.dwfa.ace.refset.spec.I_HelpSpecRefset#getInbox(org.dwfa.ace.api.
     * I_GetConceptData)
     */
    public String getInbox(I_GetConceptData concept) throws Exception {
        // find the inbox string using the concept's "user inbox" description

        I_GetConceptData descriptionType = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER_INBOX.getUids());
        I_IntSet allowedTypes = Terms.get().newIntSet();
        allowedTypes.add(descriptionType.getConceptId());
        String latestDescription = null;
        long latestVersion = Long.MIN_VALUE;

        I_HelpSpecRefset helper = new SpecRefsetHelper(Terms.get().getActiveAceFrameConfig());
        I_IntSet activeStatuses = helper.getCurrentStatusIntSet();

        List<? extends I_DescriptionTuple> descriptionResults =
                concept.getDescriptionTuples(activeStatuses, allowedTypes, Terms.get().getActiveAceFrameConfig()
                    .getViewPositionSetReadOnly(),
                    getConfig().getPrecedence(), getConfig().getConflictResolutionStrategy());
        for (I_DescriptionTuple descriptionTuple : descriptionResults) {
            if (descriptionTuple.getTime() > latestVersion) {
                latestVersion = descriptionTuple.getTime();
                latestDescription = descriptionTuple.getText();
            }
        }
        return latestDescription;
    }

    /*
     * (non-Javadoc)
     * @seeorg.dwfa.ace.refset.spec.I_HelpSpecRefset#
     * hasConceptRefsetExtensionWithAnyPromotionStatus(int, int)
     */
    public boolean hasConceptRefsetExtensionWithAnyPromotionStatus(int refsetId, int conceptId) throws IOException {
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
                for (Integer currentStatus : getCurrentStatusIds()) {
                    if (latestPart.getStatusId() == currentStatus) {
                        if (latestPart instanceof I_ExtendByRefPartCid) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public I_HelpMemberRefset getMemberHelper(int memberRefsetId, int memberTypeId) throws Exception {
        return new SpecMemberRefsetHelper(getConfig(), memberRefsetId, memberTypeId);
    }

    public I_ExtendByRefPart getLatestPart(I_ExtendByRef memberExtension) {
        I_ExtendByRefPart latestPart = null;
        for (I_ExtendByRefPart part : memberExtension.getMutableParts()) {
            if ((latestPart == null) || (part.getTime() >= latestPart.getTime())) {
                latestPart = part;
            }
        }
        return latestPart;
    }
}
