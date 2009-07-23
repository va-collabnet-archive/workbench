package org.dwfa.ace.refset;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;

public class RefsetHelper {

    protected I_TermFactory termFactory;

    protected int currentStatusId;
    protected int retiredStatusId;
    protected int conceptTypeId;

    protected int unspecifiedUuid;

    private Logger logger = Logger.getLogger(RefsetHelper.class.getName());

    public RefsetHelper() throws Exception {
        termFactory = LocalVersionedTerminology.get();
        currentStatusId = ArchitectonicAuxiliary.Concept.CURRENT.localize()
                .getNid();
        retiredStatusId = ArchitectonicAuxiliary.Concept.RETIRED.localize()
                .getNid();
        unspecifiedUuid = ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID
                .localize().getNid();
        conceptTypeId = RefsetAuxiliary.Concept.CONCEPT_EXTENSION.localize()
                .getNid();
    }

    /**
     * Gets the last I_ThinExtByRefPartConcept that has a status of current for
     * a refset and concept.
     * 
     * @param refsetId
     *            int
     * @param conceptId
     *            int
     * @return I_ThinExtByRefPartConcept with a status of current.
     * 
     * @throws Exception
     *             if cannot get all extension for a concept id..
     */
    public I_ThinExtByRefPartConcept getCurrentRefsetExtension(int refsetId,
            int conceptId) throws Exception {
        I_ThinExtByRefPartConcept currentRefsetExtension = null;

        for (I_ThinExtByRefVersioned extension : termFactory
                .getAllExtensionsForComponent(conceptId)) {

            I_ThinExtByRefPartConcept latestPart = null;
            if (extension.getRefsetId() == refsetId) {
                // get the latest version
                for (I_ThinExtByRefPart part : extension.getVersions()) {
                    if (part instanceof I_ThinExtByRefPartConcept
                            && (latestPart == null)
                            || (part.getVersion() >= latestPart.getVersion())) {
                        latestPart = (I_ThinExtByRefPartConcept) part;
                    }
                }
            }

            // confirm its the right extension value and its status is current
            if (latestPart != null
                    && latestPart.getStatusId() == currentStatusId) {
                currentRefsetExtension = latestPart;
            }
        }
        return currentRefsetExtension;
    }

    public boolean hasCurrentRefsetExtension(int refsetId, int conceptId,
            int memberTypeId) throws Exception {
        for (I_ThinExtByRefVersioned extension : termFactory
                .getAllExtensionsForComponent(conceptId, true)) {

            if (extension.getRefsetId() == refsetId) {

                // get the latest version
                I_ThinExtByRefPart latestPart = null;
                for (I_ThinExtByRefPart part : extension.getVersions()) {
                    if ((latestPart == null)
                            || (part.getVersion() >= latestPart.getVersion())) {
                        latestPart = part;
                    }
                }

                // confirm its the right extension value and its status is
                // current
                if (latestPart.getStatusId() == currentStatusId) {
                    if (latestPart instanceof I_ThinExtByRefPartConcept) {
                        int partValue = ((I_ThinExtByRefPartConcept) latestPart)
                                .getConceptId();
                        if (partValue == memberTypeId) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean hasCurrentConceptConceptRefsetExtension(int refsetId,
            int conceptId, int c1Id, int c2Id, int statusId) throws Exception {
        for (I_ThinExtByRefVersioned extension : termFactory
                .getAllExtensionsForComponent(conceptId, true)) {

            if (extension.getRefsetId() == refsetId) {

                // get the latest version
                I_ThinExtByRefPart latestPart = null;
                for (I_ThinExtByRefPart part : extension.getVersions()) {
                    if ((latestPart == null)
                            || (part.getVersion() >= latestPart.getVersion())) {
                        latestPart = part;
                    }
                }

                // confirm its the right extension value and its status is
                // current
                if (latestPart.getStatusId() == statusId) {
                    if (latestPart instanceof I_ThinExtByRefPartConceptConcept) {
                        int c1Value = ((I_ThinExtByRefPartConceptConcept) latestPart)
                                .getC1id();
                        int c2Value = ((I_ThinExtByRefPartConceptConcept) latestPart)
                                .getC2id();
                        if (c1Value == c1Id && c2Value == c2Id) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean hasCurrentConceptRefsetExtension(int refsetId,
            int componentId, int conceptId, int statusId) throws Exception {
        for (I_ThinExtByRefVersioned extension : termFactory
                .getAllExtensionsForComponent(componentId, true)) {

            if (extension.getRefsetId() == refsetId) {

                // get the latest version
                I_ThinExtByRefPart latestPart = null;
                for (I_ThinExtByRefPart part : extension.getVersions()) {
                    if ((latestPart == null)
                            || (part.getVersion() >= latestPart.getVersion())) {
                        latestPart = part;
                    }
                }

                // confirm its the right extension value and its status is
                // current
                if (latestPart.getStatusId() == statusId) {
                    if (latestPart instanceof I_ThinExtByRefPartConcept) {
                        int cValue = ((I_ThinExtByRefPartConcept) latestPart)
                                .getConceptId();
                        if (cValue == conceptId) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean hasCurrentIntRefsetExtension(int refsetId, int componentId,
            int value, int statusId) throws Exception {
        for (I_ThinExtByRefVersioned extension : termFactory
                .getAllExtensionsForComponent(componentId, true)) {

            if (extension.getRefsetId() == refsetId) {

                // get the latest version
                I_ThinExtByRefPart latestPart = null;
                for (I_ThinExtByRefPart part : extension.getVersions()) {
                    if ((latestPart == null)
                            || (part.getVersion() >= latestPart.getVersion())) {
                        latestPart = part;
                    }
                }

                // confirm its the right extension value and its status is
                // current
                if (latestPart.getStatusId() == statusId) {
                    if (latestPart instanceof I_ThinExtByRefPartInteger) {
                        int currentValue = ((I_ThinExtByRefPartInteger) latestPart)
                                .getValue();
                        if (currentValue == value) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean hasCurrentConceptConceptConceptRefsetExtension(int refsetId,
            int conceptId, int c1Id, int c2Id, int c3Id, int statusId)
            throws Exception {

        for (I_ThinExtByRefVersioned extension : termFactory
                .getAllExtensionsForComponent(conceptId, true)) {
            if (extension.getRefsetId() == refsetId) {
                // get the latest version
                I_ThinExtByRefPart latestPart = null;
                for (I_ThinExtByRefPart part : extension.getVersions()) {
                    if ((latestPart == null)
                            || (part.getVersion() >= latestPart.getVersion())) {
                        latestPart = part;
                    }
                }

                // confirm its the right extension value and its status is
                // current
                if (latestPart.getStatusId() == statusId) {
                    if (latestPart instanceof I_ThinExtByRefPartConceptConceptConcept) {
                        int c1Value = ((I_ThinExtByRefPartConceptConceptConcept) latestPart)
                                .getC1id();
                        int c2Value = ((I_ThinExtByRefPartConceptConceptConcept) latestPart)
                                .getC2id();
                        int c3Value = ((I_ThinExtByRefPartConceptConceptConcept) latestPart)
                                .getC3id();
                        if (c1Value == c1Id && c2Value == c2Id
                                && c3Value == c3Id) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean hasCurrentConceptConceptStringRefsetExtension(int refsetId,
            int conceptId, int c1Id, int c2Id, String stringInput, int statusId)
            throws Exception {
        for (I_ThinExtByRefVersioned extension : termFactory
                .getAllExtensionsForComponent(conceptId, true)) {

            if (extension.getRefsetId() == refsetId) {

                // get the latest version
                I_ThinExtByRefPart latestPart = null;
                for (I_ThinExtByRefPart part : extension.getVersions()) {
                    if ((latestPart == null)
                            || (part.getVersion() >= latestPart.getVersion())) {
                        latestPart = part;
                    }
                }

                // confirm its the right extension value and its status is
                // current
                if (latestPart.getStatusId() == statusId) {
                    if (latestPart instanceof I_ThinExtByRefPartConceptConceptString) {
                        int c1Value = ((I_ThinExtByRefPartConceptConceptString) latestPart)
                                .getC1id();
                        int c2Value = ((I_ThinExtByRefPartConceptConceptString) latestPart)
                                .getC2id();
                        String strValue = ((I_ThinExtByRefPartConceptConceptString) latestPart)
                                .getStr();
                        if (c1Value == c1Id && c2Value == c2Id
                                && strValue.equals(stringInput)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Add a concept to a refset
     * 
     * @param refsetId
     *            The subject refset
     * @param conceptId
     *            The concept to be added
     * @param memberTypeId
     *            The value of the concept extension to be added to the new
     *            member concept.
     */
    public boolean newRefsetExtension(int refsetId, int conceptId,
            int memberTypeId) throws Exception {

        Set<I_Path> userEditPaths = null;

        // check subject is not already a member
        if (hasCurrentRefsetExtension(refsetId, conceptId, memberTypeId)) {
            if (logger.isLoggable(Level.FINE)) {
                String extValueDesc = termFactory.getConcept(memberTypeId)
                        .getInitialText();
                logger.fine("Concept is already a '" + extValueDesc
                        + "' of the refset. Skipping.");
            }
            return false;
        }

        // create a new extension (with a part for each path the user is
        // editing)

        I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
        if (config != null) {
            userEditPaths = config.getEditingPathSet();
        }

        int newMemberId = termFactory.uuidToNativeWithGeneration(UUID
                .randomUUID(), unspecifiedUuid, userEditPaths,
                Integer.MAX_VALUE);

        I_ThinExtByRefVersioned newExtension = termFactory
                .newExtensionNoChecks(refsetId, newMemberId, conceptId,
                        conceptTypeId);

        for (I_Path editPath : userEditPaths) {

            I_ThinExtByRefPartConcept conceptExtension = termFactory
                    .newConceptExtensionPart();

            conceptExtension.setPathId(editPath.getConceptId());
            conceptExtension.setStatusId(currentStatusId);
            conceptExtension.setVersion(Integer.MAX_VALUE);
            conceptExtension.setConceptId(memberTypeId);

            newExtension.addVersion(conceptExtension);
        }

        termFactory.addUncommittedNoChecks(newExtension);
        return true;
    }

    public boolean newConceptConceptRefsetExtension(int refsetId,
            int componentId, int c1Id, int c2Id, UUID memberUuid,
            UUID pathUuid, UUID statusUuid, int effectiveTime) throws Exception {

        Collection<I_Path> paths = termFactory.getPaths();
        paths.clear();
        paths.add(termFactory.getPath(new UUID[] { pathUuid }));

        if (memberUuid == null) {
            memberUuid = UUID.randomUUID();
        }
        if (statusUuid == null) {
            statusUuid = termFactory.getConcept(currentStatusId).getUids()
                    .iterator().next();
        }

        int extTypeId = RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION
                .localize().getNid();

        // check subject is not already a member
        if (hasCurrentConceptConceptRefsetExtension(refsetId, componentId,
                c1Id, c2Id, termFactory.getConcept(new UUID[] { statusUuid })
                        .getConceptId())) {
            if (logger.isLoggable(Level.FINE)) {
                logger
                        .fine("Component is already a member of the refset. Skipping.");
            }
            return false;
        }

        // create a new extension (with a part for each path the user is
        // editing)

        int newMemberId = termFactory.uuidToNativeWithGeneration(memberUuid,
                unspecifiedUuid, paths, effectiveTime);

        I_ThinExtByRefVersioned newExtension = termFactory
                .newExtensionNoChecks(refsetId, newMemberId, componentId,
                        extTypeId);

        for (I_Path editPath : paths) {

            I_ThinExtByRefPartConceptConcept extension = termFactory
                    .newConceptConceptExtensionPart();

            extension.setPathId(editPath.getConceptId());
            extension.setStatusId(termFactory.getConcept(
                    new UUID[] { statusUuid }).getConceptId());
            extension.setVersion(effectiveTime);
            extension.setC1id(c1Id);
            extension.setC2id(c2Id);

            newExtension.addVersion(extension);
        }

        termFactory.addUncommittedNoChecks(newExtension);
        return true;
    }

    public boolean newIntRefsetExtension(int refsetId, int componentId,
            int value, UUID memberUuid, UUID pathUuid, UUID statusUuid,
            int effectiveTime) throws Exception {

        Collection<I_Path> paths = termFactory.getPaths();
        paths.clear();
        paths.add(termFactory.getPath(new UUID[] { pathUuid }));

        if (memberUuid == null) {
            memberUuid = UUID.randomUUID();
        }
        if (statusUuid == null) {
            statusUuid = termFactory.getConcept(currentStatusId).getUids()
                    .iterator().next();
        }

        int extTypeId = RefsetAuxiliary.Concept.INT_EXTENSION.localize()
                .getNid();

        // check subject is not already a member
        if (hasCurrentIntRefsetExtension(refsetId, componentId, value,
                termFactory.getConcept(new UUID[] { statusUuid })
                        .getConceptId())) {
            if (logger.isLoggable(Level.FINE)) {
                logger
                        .fine("Component is already a member of the refset. Skipping.");
            }
            return false;
        }

        // create a new extension (with a part for each path the user is
        // editing)
        int newMemberId = termFactory.uuidToNativeWithGeneration(memberUuid,
                unspecifiedUuid, paths, effectiveTime);

        I_ThinExtByRefVersioned newExtension = termFactory
                .newExtensionNoChecks(refsetId, newMemberId, componentId,
                        extTypeId);

        for (I_Path editPath : paths) {

            I_ThinExtByRefPartInteger extension = termFactory
                    .newIntegerExtensionPart();

            extension.setPathId(editPath.getConceptId());
            extension.setStatusId(termFactory.getConcept(
                    new UUID[] { statusUuid }).getConceptId());
            extension.setVersion(effectiveTime);
            extension.setValue(value);

            newExtension.addVersion(extension);
        }

        termFactory.addUncommittedNoChecks(newExtension);
        return true;
    }

    public boolean newConceptRefsetExtension(int refsetId, int componentId,
            int conceptId, UUID memberUuid, UUID pathUuid, UUID statusUuid,
            int effectiveTime) throws Exception {

        Collection<I_Path> paths = termFactory.getPaths();
        paths.clear();
        paths.add(termFactory.getPath(new UUID[] { pathUuid }));

        if (memberUuid == null) {
            memberUuid = UUID.randomUUID();
        }
        if (statusUuid == null) {
            statusUuid = termFactory.getConcept(currentStatusId).getUids()
                    .iterator().next();
        }

        int extTypeId = RefsetAuxiliary.Concept.CONCEPT_EXTENSION.localize()
                .getNid();

        // check subject is not already a member
        if (hasCurrentConceptRefsetExtension(refsetId, componentId, conceptId,
                termFactory.getConcept(new UUID[] { statusUuid })
                        .getConceptId())) {
            if (logger.isLoggable(Level.FINE)) {
                logger
                        .fine("Component is already a member of the refset. Skipping.");
            }
            return false;
        }

        // create a new extension (with a part for each path the user is
        // editing)
        int newMemberId = termFactory.uuidToNativeWithGeneration(memberUuid,
                unspecifiedUuid, paths, effectiveTime);

        I_ThinExtByRefVersioned newExtension = termFactory
                .newExtensionNoChecks(refsetId, newMemberId, componentId,
                        extTypeId);

        for (I_Path editPath : paths) {

            I_ThinExtByRefPartConcept extension = termFactory
                    .newConceptExtensionPart();

            extension.setPathId(editPath.getConceptId());
            extension.setStatusId(termFactory.getConcept(
                    new UUID[] { statusUuid }).getConceptId());
            extension.setVersion(effectiveTime);
            extension.setConceptId(conceptId);

            newExtension.addVersion(extension);
        }

        termFactory.addUncommittedNoChecks(newExtension);
        return true;
    }

    public boolean newConceptConceptConceptRefsetExtension(int refsetId,
            int componentId, int c1Id, int c2Id, int c3Id, UUID memberUuid,
            UUID pathUuid, UUID statusUuid, int effectiveTime) throws Exception {

        Collection<I_Path> paths = termFactory.getPaths();
        paths.clear();
        paths.add(termFactory.getPath(new UUID[] { pathUuid }));

        int extTypeId = RefsetAuxiliary.Concept.CONCEPT_CONCEPT_CONCEPT_EXTENSION
                .localize().getNid();

        if (memberUuid == null) {
            memberUuid = UUID.randomUUID();
        }
        if (statusUuid == null) {
            statusUuid = termFactory.getConcept(currentStatusId).getUids()
                    .iterator().next();
        }

        // check subject is not already a member
        if (hasCurrentConceptConceptConceptRefsetExtension(refsetId,
                componentId, c1Id, c2Id, c3Id, termFactory.getConcept(
                        new UUID[] { statusUuid }).getConceptId())) {
            if (logger.isLoggable(Level.FINE)) {
                logger
                        .fine("Component is already a member of the refset. Skipping.");
            }
            return false;
        }

        // create a new extension (with a part for each path the user is
        // editing)
        int newMemberId = termFactory.uuidToNativeWithGeneration(memberUuid,
                unspecifiedUuid, paths, effectiveTime);

        I_ThinExtByRefVersioned newExtension = termFactory
                .newExtensionNoChecks(refsetId, newMemberId, componentId,
                        extTypeId);

        for (I_Path editPath : paths) {

            I_ThinExtByRefPartConceptConceptConcept extension = termFactory
                    .newConceptConceptConceptExtensionPart();

            extension.setPathId(editPath.getConceptId());
            extension.setStatusId(termFactory.getConcept(
                    new UUID[] { statusUuid }).getConceptId());
            extension.setVersion(Integer.MAX_VALUE);
            extension.setC1id(c1Id);
            extension.setC2id(c2Id);
            extension.setC3id(c3Id);

            newExtension.addVersion(extension);
        }

        termFactory.addUncommittedNoChecks(newExtension);
        return true;
    }

    public boolean newConceptConceptStringRefsetExtension(int refsetId,
            int componentId, int c1Id, int c2Id, String stringValue,
            UUID memberUuid, UUID pathUuid, UUID statusUuid, int effectiveTime)
            throws Exception {

        Collection<I_Path> paths = termFactory.getPaths();
        paths.clear();
        paths.add(termFactory.getPath(new UUID[] { pathUuid }));

        int extTypeId = RefsetAuxiliary.Concept.CONCEPT_CONCEPT_STRING_EXTENSION
                .localize().getNid();

        // check subject is not already a member
        if (hasCurrentConceptConceptStringRefsetExtension(refsetId,
                componentId, c1Id, c2Id, stringValue, termFactory.getConcept(
                        new UUID[] { statusUuid }).getConceptId())) {
            if (logger.isLoggable(Level.FINE)) {
                logger
                        .fine("Component is already a member of the refset. Skipping.");
            }
            return false;
        }

        if (memberUuid == null) {
            memberUuid = UUID.randomUUID();
        }
        if (statusUuid == null) {
            statusUuid = termFactory.getConcept(currentStatusId).getUids()
                    .iterator().next();
        }

        // create a new extension (with a part for each path the user is
        // editing)

        int newMemberId = termFactory.uuidToNativeWithGeneration(memberUuid,
                unspecifiedUuid, paths, effectiveTime);

        I_ThinExtByRefVersioned newExtension = termFactory
                .newExtensionNoChecks(refsetId, newMemberId, componentId,
                        extTypeId);

        for (I_Path editPath : paths) {

            I_ThinExtByRefPartConceptConceptString extension = termFactory
                    .newConceptConceptStringExtensionPart();

            extension.setPathId(editPath.getConceptId());
            extension.setStatusId(termFactory.getConcept(
                    new UUID[] { statusUuid }).getConceptId());
            extension.setVersion(Integer.MAX_VALUE);
            extension.setC1id(c1Id);
            extension.setC2id(c2Id);
            extension.setStr(stringValue);

            newExtension.addVersion(extension);
        }

        termFactory.addUncommittedNoChecks(newExtension);
        return true;
    }

    /**
     * Remove a concept from a refset
     * 
     * @param refsetId
     *            The subject refset
     * @param conceptId
     *            The concept to be removed
     * @param memberTypeId
     *            The value of the concept extension to be removed (the
     *            membership type).
     */
    public boolean retireRefsetExtension(int refsetId, int conceptId,
            int memberTypeId) throws Exception {

        boolean wasRemoved = false;

        // check subject is not already a member
        for (I_ThinExtByRefVersioned extension : termFactory
                .getAllExtensionsForComponent(conceptId)) {

            if (extension.getRefsetId() == refsetId) {

                // get the latest version
                I_ThinExtByRefPart latestPart = null;
                for (I_ThinExtByRefPart part : extension.getVersions()) {
                    if ((latestPart == null)
                            || (part.getVersion() >= latestPart.getVersion())) {
                        latestPart = part;
                    }
                }

                // confirm its the right extension value and its status is
                // current
                if (latestPart.getStatusId() == currentStatusId) {
                    if (latestPart instanceof I_ThinExtByRefPartConcept) {
                        int partValue = ((I_ThinExtByRefPartConcept) latestPart)
                                .getConceptId();
                        if (partValue == memberTypeId) {
                            // found a member to retire

                            I_ThinExtByRefPartConcept clone = (I_ThinExtByRefPartConcept) latestPart
                                    .duplicate();
                            clone.setStatusId(retiredStatusId);
                            clone.setVersion(Integer.MAX_VALUE);
                            extension.addVersion(clone);
                            termFactory.addUncommittedNoChecks(extension);
                            wasRemoved = true;
                        }
                    }
                }
            }
        }
        return wasRemoved;
    }

    /**
     * Get all the descendants (children, children of children, etc) of a
     * particular concept.
     */
    public Set<I_GetConceptData> getAllDescendants(I_GetConceptData concept,
            Condition... conditions) throws Exception {

        Set<I_Position> userViewPositions = null;
        I_IntSet userViewStatuses;

        I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();

        if (config != null) {
            userViewPositions = config.getViewPositionSet();
            userViewStatuses = config.getAllowedStatus();
        } else {
            userViewStatuses = termFactory.newIntSet();
            userViewStatuses.add(ArchitectonicAuxiliary.Concept.CURRENT
                    .localize().getNid());
        }

        I_IntSet isARel = termFactory.newIntSet();

        // get the appropriate is-a type (SNOMED or architectonic), based on
        // "marked parent is-a type" rel
        isARel.add(new RefsetUtilImpl().getMarkedParentIsARelationshipTarget(
                termFactory, concept));

        // find all the children
        Set<I_GetConceptData> descendants = getAllDescendants(
                new HashSet<I_GetConceptData>(), concept, userViewStatuses,
                isARel, userViewPositions, conditions);

        logger.fine("Found " + descendants.size() + " descendants of concept '"
                + concept.getInitialText() + "'.");

        return descendants;
    }

    protected Set<I_GetConceptData> getAllDescendants(
            Set<I_GetConceptData> resultSet, I_GetConceptData parent,
            I_IntSet allowedStatuses, I_IntSet allowedTypes,
            Set<I_Position> positions, Condition... conditions)
            throws Exception {

        ITERATE_CHILDREN: for (I_RelTuple childTuple : parent.getDestRelTuples(
                allowedStatuses, allowedTypes, positions, false)) {
            I_GetConceptData childConcept = termFactory.getConcept(childTuple
                    .getC1Id());
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
                resultSet.addAll(getAllDescendants(resultSet, childConcept,
                        allowedStatuses, allowedTypes, positions, conditions));
            }
        }
        return resultSet;
    }

    /**
     * Get all the ancestors (parents, parents of parents, etc) of a particular
     * concept.
     */
    public Set<I_GetConceptData> getAllAncestors(I_GetConceptData concept,
            Condition... conditions) throws Exception {

        Set<I_Position> userViewPositions = null;
        I_IntSet userViewStatuses;

        I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();

        if (config != null) {
            userViewPositions = config.getViewPositionSet();
            userViewStatuses = config.getAllowedStatus();
        } else {
            userViewStatuses = termFactory.newIntSet();
            userViewStatuses.add(ArchitectonicAuxiliary.Concept.CURRENT
                    .localize().getNid());
        }

        I_IntSet isARel = termFactory.newIntSet();

        // get the appropriate is-a type (SNOMED or architectonic), based on
        // "marked parent is-a type" rel
        isARel.add(new RefsetUtilImpl().getMarkedParentIsARelationshipTarget(
                termFactory, concept));

        // find all the parents
        Set<I_GetConceptData> parentConcepts = getAllAncestors(
                new HashSet<I_GetConceptData>(), concept, userViewStatuses,
                isARel, userViewPositions, conditions);

        logger.fine("Found " + parentConcepts.size()
                + " ancestors of concept '" + concept.getInitialText() + "'.");

        return parentConcepts;
    }

    protected Set<I_GetConceptData> getAllAncestors(
            Set<I_GetConceptData> resultSet, I_GetConceptData child,
            I_IntSet allowedStatuses, I_IntSet allowedTypes,
            Set<I_Position> positions, Condition... conditions)
            throws Exception {

        ITERATE_PARENTS: for (I_RelTuple childTuple : child.getSourceRelTuples(
                allowedStatuses, allowedTypes, positions, false)) {
            I_GetConceptData parentConcept = termFactory.getConcept(childTuple
                    .getC2Id());
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
                resultSet.addAll(getAllAncestors(resultSet, parentConcept,
                        allowedStatuses, allowedTypes, positions, conditions));
            }
        }
        return resultSet;
    }

    public interface Condition {
        public boolean evaluate(I_GetConceptData concept) throws Exception;
    }

    public int getConceptTypeId() {
        return conceptTypeId;
    }

    public void setConceptTypeId(int conceptTypeId) {
        this.conceptTypeId = conceptTypeId;
    }
}
