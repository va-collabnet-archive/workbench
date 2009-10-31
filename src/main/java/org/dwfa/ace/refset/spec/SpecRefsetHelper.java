package org.dwfa.ace.refset.spec;

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
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.AllowDataCheckSuppression;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type5UuidFactory;

@AllowDataCheckSuppression
public class SpecRefsetHelper {

    protected I_TermFactory termFactory;

    protected int currentStatusId;
    protected int retiredStatusId;
    protected int conceptTypeId;

    protected int unspecifiedUuid;

    protected Set<I_Position> viewPositions;
    protected Set<I_Path> editPaths;
    protected I_IntSet allowedStatuses;
    protected I_IntSet isARelTypes;

    private Logger logger = Logger.getLogger(SpecRefsetHelper.class.getName());

    public SpecRefsetHelper() throws Exception {
        termFactory = LocalVersionedTerminology.get();
        currentStatusId = ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid();
        retiredStatusId = ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid();
        unspecifiedUuid = ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid();
        conceptTypeId = RefsetAuxiliary.Concept.CONCEPT_EXTENSION.localize().getNid();
    }

    /**
     * Gets the last I_ThinExtByRefPartConcept that has a status of current for
     * a refset and concept.
     * 
     * @param refsetId int
     * @param conceptId int
     * @return I_ThinExtByRefPartConcept with a status of current.
     * 
     * @throws Exception if cannot get all extension for a concept id..
     */
    public I_ThinExtByRefPartConcept getCurrentRefsetExtension(int refsetId, int conceptId) throws Exception {

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

    public boolean hasCurrentRefsetExtension(int refsetId, int conceptId, int memberTypeId) throws Exception {
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
                    if (latestPart instanceof I_ThinExtByRefPartConcept) {
                        int partValue = ((I_ThinExtByRefPartConcept) latestPart).getC1id();
                        if (partValue == memberTypeId) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean hasCurrentConceptConceptRefsetExtension(int refsetId, int conceptId, int c1Id, int c2Id, int statusId)
            throws Exception {
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
                if (latestPart.getStatusId() == statusId) {
                    if (latestPart instanceof I_ThinExtByRefPartConceptConcept) {
                        int c1Value = ((I_ThinExtByRefPartConceptConcept) latestPart).getC1id();
                        int c2Value = ((I_ThinExtByRefPartConceptConcept) latestPart).getC2id();
                        int componentId = ((I_ThinExtByRefPartConceptConcept) latestPart).getConceptId();
                        if (c1Value == c1Id && c2Value == c2Id) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean hasCurrentConceptRefsetExtension(int refsetId, int componentId, int conceptId, int statusId)
            throws Exception {
        for (I_ThinExtByRefVersioned extension : termFactory.getAllExtensionsForComponent(componentId, true)) {

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
                if (latestPart.getStatusId() == statusId) {
                    if (latestPart instanceof I_ThinExtByRefPartConcept) {
                        int cValue = ((I_ThinExtByRefPartConcept) latestPart).getConceptId();
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
        for (I_ThinExtByRefVersioned extension : termFactory.getAllExtensionsForComponent(componentId, true)) {

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
                if (latestPart.getStatusId() == statusId) {
                    if (latestPart instanceof I_ThinExtByRefPartConceptString) {
                        String extensionString = ((I_ThinExtByRefPartConceptString) latestPart).getStr();
                        int currentc1Id = ((I_ThinExtByRefPartConceptString) latestPart).getC1id();
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
        for (I_ThinExtByRefVersioned extension : termFactory.getAllExtensionsForComponent(componentId, true)) {

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
                if (latestPart.getStatusId() == statusId) {
                    if (latestPart instanceof I_ThinExtByRefPartString) {
                        String extensionString = ((I_ThinExtByRefPartString) latestPart).getStringValue();
                        if (extString.equals(extensionString)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean hasCurrentIntRefsetExtension(int refsetId, int componentId, int value, int statusId)
            throws Exception {
        for (I_ThinExtByRefVersioned extension : termFactory.getAllExtensionsForComponent(componentId, true)) {

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
                if (latestPart.getStatusId() == statusId) {
                    if (latestPart instanceof I_ThinExtByRefPartInteger) {
                        int currentValue = ((I_ThinExtByRefPartInteger) latestPart).getValue();
                        if (currentValue == value) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean hasCurrentConceptConceptConceptRefsetExtension(int refsetId, int conceptId, int c1Id, int c2Id,
            int c3Id, int statusId) throws Exception {

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
                if (latestPart.getStatusId() == statusId) {
                    if (latestPart instanceof I_ThinExtByRefPartConceptConceptConcept) {
                        int c1Value = ((I_ThinExtByRefPartConceptConceptConcept) latestPart).getC1id();
                        int c2Value = ((I_ThinExtByRefPartConceptConceptConcept) latestPart).getC2id();
                        int c3Value = ((I_ThinExtByRefPartConceptConceptConcept) latestPart).getC3id();
                        if (c1Value == c1Id && c2Value == c2Id && c3Value == c3Id) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean hasCurrentConceptConceptStringRefsetExtension(int refsetId, int conceptId, int c1Id, int c2Id,
            String stringInput, int statusId) throws Exception {
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
                if (latestPart.getStatusId() == statusId) {
                    if (latestPart instanceof I_ThinExtByRefPartConceptConceptString) {
                        int c1Value = ((I_ThinExtByRefPartConceptConceptString) latestPart).getC1id();
                        int c2Value = ((I_ThinExtByRefPartConceptConceptString) latestPart).getC2id();
                        String strValue = ((I_ThinExtByRefPartConceptConceptString) latestPart).getStr();
                        if (c1Value == c1Id && c2Value == c2Id && strValue.equals(stringInput)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Add a concept to a refset (if it doesn't already exist)
     * 
     * @see {@link #newRefsetExtension(int, int, int, boolean)}
     */
    public boolean newRefsetExtension(int refsetId, int conceptId, int memberTypeId) throws Exception {
        return newRefsetExtension(refsetId, conceptId, memberTypeId, true);
    }

    /**
     * Add a concept to a refset
     * 
     * @param refsetId The subject refset
     * @param conceptId The concept to be added
     * @param memberTypeId The value of the concept extension to be added to the
     *            new member concept.
     * @param checkNotExists Is true, will only execute if the extension does
     *            not already exist.
     */
    public boolean newRefsetExtension(int refsetId, int conceptId, int memberTypeId, boolean checkNotExists)
            throws Exception {

        if (checkNotExists) {
            // check subject is not already a member
            if (hasCurrentRefsetExtension(refsetId, conceptId, memberTypeId)) {
                if (logger.isLoggable(Level.FINE)) {
                    String extValueDesc = termFactory.getConcept(memberTypeId).getInitialText();
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
                generateUuid(termFactory.getUids(refsetId).iterator().next(), termFactory.getUids(conceptId).iterator()
                    .next(), termFactory.getUids(memberTypeId).iterator().next());
        if (memberUuid == null || termFactory.hasId(memberUuid)) {
            memberUuid = UUID.randomUUID();
        }

        int newMemberId =
                termFactory.uuidToNativeWithGeneration(memberUuid, unspecifiedUuid, getEditPaths(), Integer.MAX_VALUE);

        I_ThinExtByRefVersioned newExtension =
                termFactory.newExtensionNoChecks(refsetId, newMemberId, conceptId, conceptTypeId);

        for (I_Path editPath : getEditPaths()) {

            I_ThinExtByRefPartConcept conceptExtension = termFactory.newConceptExtensionPart();

            conceptExtension.setPathId(editPath.getConceptId());
            conceptExtension.setStatusId(currentStatusId);
            conceptExtension.setVersion(Integer.MAX_VALUE);
            conceptExtension.setC1id(memberTypeId);

            newExtension.addVersion(conceptExtension);
        }

        termFactory.addUncommittedNoChecks(newExtension);
        return true;
    }

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

    public boolean newConceptConceptRefsetExtension(int refsetId, int componentId, int c1Id, int c2Id, UUID memberUuid,
            UUID pathUuid, UUID statusUuid, int effectiveTime) throws Exception {

        try {
            Collection<I_Path> paths = termFactory.getPaths();
            paths.clear();
            paths.add(termFactory.getPath(new UUID[] { pathUuid }));

            if (memberUuid == null) {
                memberUuid = UUID.randomUUID();
            }
            if (statusUuid == null) {
                statusUuid = termFactory.getConcept(currentStatusId).getUids().iterator().next();
            }

            int extTypeId = RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION.localize().getNid();

            // check subject is not already a member
            if (hasCurrentConceptConceptRefsetExtension(refsetId, componentId, c1Id, c2Id, termFactory.getConcept(
                new UUID[] { statusUuid }).getConceptId())) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Component is already a member of the refset. Skipping.");
                }
                return false;
            }

            // create a new extension (with a part for each path the user is
            // editing)

            int newMemberId = Integer.MAX_VALUE;
            if (!termFactory.hasId(memberUuid)) {
                newMemberId = termFactory.uuidToNativeWithGeneration(memberUuid, unspecifiedUuid, paths, effectiveTime);
            } else {
                newMemberId = termFactory.getId(memberUuid).getNativeId();
            }

            I_ThinExtByRefVersioned newExtension =
                    termFactory.newExtensionNoChecks(refsetId, newMemberId, componentId, extTypeId);

            for (I_Path editPath : paths) {

                I_ThinExtByRefPartConceptConcept extension = termFactory.newConceptConceptExtensionPart();

                extension.setPathId(editPath.getConceptId());
                extension.setStatusId(termFactory.getConcept(new UUID[] { statusUuid }).getConceptId());
                extension.setVersion(Integer.MAX_VALUE);
                extension.setC1id(c1Id);
                extension.setC2id(c2Id);

                newExtension.addVersion(extension);
            }

            termFactory.addUncommittedNoChecks(newExtension);
            termFactory.commit();

            // edit the existing part's effectiveDate/version - this needs to
            // occur after the part has been committed, or else the
            // effectiveDate is set to the time at commit
            int index = newExtension.getVersions().size() - 1;
            I_ThinExtByRefPartConceptConcept extension =
                    (I_ThinExtByRefPartConceptConcept) newExtension.getVersions().get(index);
            extension.setVersion(effectiveTime);
            newExtension.addVersion(extension);

            termFactory.addUncommittedNoChecks(newExtension);
            termFactory.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public boolean newStringRefsetExtension(int refsetId, int componentId, String extString, UUID memberUuid,
            UUID pathUuid, UUID statusUuid, int effectiveTime) throws Exception {
        try {
            Collection<I_Path> paths = termFactory.getPaths();
            paths.clear();
            paths.add(termFactory.getPath(new UUID[] { pathUuid }));

            if (memberUuid == null) {
                memberUuid = UUID.randomUUID();
            }
            if (statusUuid == null) {
                statusUuid = termFactory.getConcept(currentStatusId).getUids().iterator().next();
            }

            int extTypeId = RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION.localize().getNid();

            // check subject is not already a member
            if (hasCurrentStringRefsetExtension(refsetId, componentId, extString, termFactory.getConcept(
                new UUID[] { statusUuid }).getConceptId())) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Component is already a member of the refset. Skipping.");
                }
                return false;
            }

            // create a new extension (with a part for each path the user is
            // editing)

            int newMemberId = Integer.MAX_VALUE;
            if (!termFactory.hasId(memberUuid)) {
                newMemberId = termFactory.uuidToNativeWithGeneration(memberUuid, unspecifiedUuid, paths, effectiveTime);
            } else {
                newMemberId = termFactory.getId(memberUuid).getNativeId();
            }

            I_ThinExtByRefVersioned newExtension =
                    termFactory.newExtensionNoChecks(refsetId, newMemberId, componentId, extTypeId);

            for (I_Path editPath : paths) {
                I_ThinExtByRefPartString extension = termFactory.newStringExtensionPart();

                extension.setPathId(editPath.getConceptId());
                extension.setStatusId(termFactory.getConcept(new UUID[] { statusUuid }).getConceptId());
                extension.setVersion(Integer.MAX_VALUE);
                extension.setStringValue(extString);

                newExtension.addVersion(extension);
            }

            termFactory.addUncommittedNoChecks(newExtension);
            termFactory.commit();

            // edit the existing part's effectiveDate/version - this needs to
            // occur after the part has been committed, or else the
            // effectiveDate is set to the time at commit
            int index = newExtension.getVersions().size() - 1;
            I_ThinExtByRefPartString extension = (I_ThinExtByRefPartString) newExtension.getVersions().get(index);
            extension.setVersion(effectiveTime);
            newExtension.addVersion(extension);

            termFactory.addUncommittedNoChecks(newExtension);
            termFactory.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    public boolean newConceptStringRefsetExtension(int refsetId, int componentId, int c1Id, String extString,
            UUID memberUuid, UUID pathUuid, UUID statusUuid, int effectiveTime) throws Exception {
        try {
            Collection<I_Path> paths = termFactory.getPaths();
            paths.clear();
            paths.add(termFactory.getPath(new UUID[] { pathUuid }));

            if (memberUuid == null) {
                memberUuid = UUID.randomUUID();
            }
            if (statusUuid == null) {
                statusUuid = termFactory.getConcept(currentStatusId).getUids().iterator().next();
            }

            int extTypeId = RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION.localize().getNid();

            // check subject is not already a member
            if (hasCurrentConceptStringRefsetExtension(refsetId, componentId, c1Id, extString, termFactory.getConcept(
                new UUID[] { statusUuid }).getConceptId())) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Component is already a member of the refset. Skipping.");
                }
                return false;
            }

            // create a new extension (with a part for each path the user is
            // editing)

            int newMemberId = Integer.MAX_VALUE;
            if (!termFactory.hasId(memberUuid)) {
                newMemberId = termFactory.uuidToNativeWithGeneration(memberUuid, unspecifiedUuid, paths, effectiveTime);
            } else {
                newMemberId = termFactory.getId(memberUuid).getNativeId();
            }

            I_ThinExtByRefVersioned newExtension =
                    termFactory.newExtensionNoChecks(refsetId, newMemberId, componentId, extTypeId);

            for (I_Path editPath : paths) {
                I_ThinExtByRefPartConceptString extension = termFactory.newConceptStringExtensionPart();

                extension.setPathId(editPath.getConceptId());
                extension.setStatusId(termFactory.getConcept(new UUID[] { statusUuid }).getConceptId());
                extension.setVersion(Integer.MAX_VALUE);
                extension.setC1id(c1Id);
                extension.setStr(extString);

                newExtension.addVersion(extension);
            }

            termFactory.addUncommittedNoChecks(newExtension);
            termFactory.commit();

            // edit the existing part's effectiveDate/version - this needs to
            // occur after the part has been committed, or else the
            // effectiveDate is set to the time at commit
            int index = newExtension.getVersions().size() - 1;
            I_ThinExtByRefPartConceptString extension =
                    (I_ThinExtByRefPartConceptString) newExtension.getVersions().get(index);
            extension.setVersion(effectiveTime);
            newExtension.addVersion(extension);

            termFactory.addUncommittedNoChecks(newExtension);
            termFactory.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    public boolean newIntRefsetExtension(int refsetId, int componentId, int value, UUID memberUuid, UUID pathUuid,
            UUID statusUuid, int effectiveTime) throws Exception {

        Collection<I_Path> paths = termFactory.getPaths();
        paths.clear();
        paths.add(termFactory.getPath(new UUID[] { pathUuid }));

        if (memberUuid == null) {
            memberUuid = UUID.randomUUID();
        }
        if (statusUuid == null) {
            statusUuid = termFactory.getConcept(currentStatusId).getUids().iterator().next();
        }

        int extTypeId = RefsetAuxiliary.Concept.INT_EXTENSION.localize().getNid();

        // check subject is not already a member
        if (hasCurrentIntRefsetExtension(refsetId, componentId, value, termFactory
            .getConcept(new UUID[] { statusUuid }).getConceptId())) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Component is already a member of the refset. Skipping.");
            }
            return false;
        }

        // create a new extension (with a part for each path the user is
        // editing)
        int newMemberId = termFactory.uuidToNativeWithGeneration(memberUuid, unspecifiedUuid, paths, effectiveTime);

        I_ThinExtByRefVersioned newExtension =
                termFactory.newExtensionNoChecks(refsetId, newMemberId, componentId, extTypeId);

        for (I_Path editPath : paths) {

            I_ThinExtByRefPartInteger extension = termFactory.newIntegerExtensionPart();

            extension.setPathId(editPath.getConceptId());
            extension.setStatusId(termFactory.getConcept(new UUID[] { statusUuid }).getConceptId());
            extension.setVersion(effectiveTime);
            extension.setValue(value);

            newExtension.addVersion(extension);
        }

        termFactory.addUncommittedNoChecks(newExtension);
        termFactory.commit();

        // edit the existing part's effectiveDate/version - this needs to occur
        // after the part has been committed, or else the effectiveDate is set
        // to the time at commit
        int index = newExtension.getVersions().size() - 1;
        I_ThinExtByRefPartInteger extension = (I_ThinExtByRefPartInteger) newExtension.getVersions().get(index);
        extension.setVersion(effectiveTime);
        newExtension.addVersion(extension);

        termFactory.addUncommittedNoChecks(newExtension);
        termFactory.commit();
        return true;
    }

    public boolean newConceptRefsetExtension(int refsetId, int componentId, int conceptId, UUID memberUuid,
            UUID pathUuid, UUID statusUuid, int effectiveTime) throws Exception {

        Collection<I_Path> paths = termFactory.getPaths();
        paths.clear();
        paths.add(termFactory.getPath(new UUID[] { pathUuid }));

        if (memberUuid == null) {
            memberUuid = UUID.randomUUID();
        }
        if (statusUuid == null) {
            statusUuid = termFactory.getConcept(currentStatusId).getUids().iterator().next();
        }

        int extTypeId = RefsetAuxiliary.Concept.CONCEPT_EXTENSION.localize().getNid();

        // check subject is not already a member
        if (hasCurrentConceptRefsetExtension(refsetId, componentId, conceptId, termFactory.getConcept(
            new UUID[] { statusUuid }).getConceptId())) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Component is already a member of the refset. Skipping.");
            }
            return false;
        }

        // create a new extension (with a part for each path the user is
        // editing)
        int newMemberId = termFactory.uuidToNativeWithGeneration(memberUuid, unspecifiedUuid, paths, effectiveTime);

        I_ThinExtByRefVersioned newExtension =
                termFactory.newExtensionNoChecks(refsetId, newMemberId, componentId, extTypeId);

        for (I_Path editPath : paths) {

            I_ThinExtByRefPartConcept extension = termFactory.newConceptExtensionPart();

            extension.setPathId(editPath.getConceptId());
            extension.setStatusId(termFactory.getConcept(new UUID[] { statusUuid }).getConceptId());
            extension.setVersion(effectiveTime);
            extension.setConceptId(conceptId);

            newExtension.addVersion(extension);
        }

        termFactory.addUncommittedNoChecks(newExtension);
        termFactory.commit();

        // edit the existing part's effectiveDate/version - this needs to occur
        // after the part has been committed, or else the effectiveDate is set
        // to the time at commit
        int index = newExtension.getVersions().size() - 1;
        I_ThinExtByRefPartConcept extension = (I_ThinExtByRefPartConcept) newExtension.getVersions().get(index);
        extension.setVersion(effectiveTime);
        newExtension.addVersion(extension);

        termFactory.addUncommittedNoChecks(newExtension);
        termFactory.commit();
        return true;
    }

    public boolean newConceptConceptConceptRefsetExtension(int refsetId, int componentId, int c1Id, int c2Id, int c3Id,
            UUID memberUuid, UUID pathUuid, UUID statusUuid, int effectiveTime) throws Exception {

        Collection<I_Path> paths = termFactory.getPaths();
        paths.clear();
        paths.add(termFactory.getPath(new UUID[] { pathUuid }));

        int extTypeId = RefsetAuxiliary.Concept.CONCEPT_CONCEPT_CONCEPT_EXTENSION.localize().getNid();

        if (memberUuid == null) {
            memberUuid = UUID.randomUUID();
        }
        if (statusUuid == null) {
            statusUuid = termFactory.getConcept(currentStatusId).getUids().iterator().next();
        }

        // check subject is not already a member
        if (hasCurrentConceptConceptConceptRefsetExtension(refsetId, componentId, c1Id, c2Id, c3Id, termFactory
            .getConcept(new UUID[] { statusUuid }).getConceptId())) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Component is already a member of the refset. Skipping.");
            }
            return false;
        }

        // create a new extension (with a part for each path the user is
        // editing)
        int newMemberId = Integer.MAX_VALUE;
        if (!termFactory.hasId(memberUuid)) {
            newMemberId = termFactory.uuidToNativeWithGeneration(memberUuid, unspecifiedUuid, paths, effectiveTime);
        } else {
            newMemberId = termFactory.getId(memberUuid).getNativeId();
        }

        I_ThinExtByRefVersioned newExtension =
                termFactory.newExtensionNoChecks(refsetId, newMemberId, componentId, extTypeId);

        // create a new part
        for (I_Path editPath : paths) {
            I_ThinExtByRefPartConceptConceptConcept extension = termFactory.newConceptConceptConceptExtensionPart();
            extension.setPathId(editPath.getConceptId());
            extension.setStatusId(termFactory.getConcept(new UUID[] { statusUuid }).getConceptId());
            extension.setVersion(Integer.MAX_VALUE);
            extension.setC1id(c1Id);
            extension.setC2id(c2Id);
            extension.setC3id(c3Id);

            newExtension.addVersion(extension);
        }

        termFactory.addUncommittedNoChecks(newExtension);
        termFactory.commit();

        // edit the existing part's effectiveDate/version - this needs to occur
        // after the part has been committed, or else the effectiveDate is set
        // to the time at commit
        int index = newExtension.getVersions().size() - 1;
        I_ThinExtByRefPartConceptConceptConcept extension =
                (I_ThinExtByRefPartConceptConceptConcept) newExtension.getVersions().get(index);
        extension.setVersion(effectiveTime);
        newExtension.addVersion(extension);

        termFactory.addUncommittedNoChecks(newExtension);
        termFactory.commit();
        return true;
    }

    public boolean newConceptConceptStringRefsetExtension(int refsetId, int componentId, int c1Id, int c2Id,
            String stringValue, UUID memberUuid, UUID pathUuid, UUID statusUuid, int effectiveTime) throws Exception {

        Collection<I_Path> paths = termFactory.getPaths();
        paths.clear();
        paths.add(termFactory.getPath(new UUID[] { pathUuid }));

        int extTypeId = RefsetAuxiliary.Concept.CONCEPT_CONCEPT_STRING_EXTENSION.localize().getNid();

        // check subject is not already a member
        if (hasCurrentConceptConceptStringRefsetExtension(refsetId, componentId, c1Id, c2Id, stringValue, termFactory
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
            statusUuid = termFactory.getConcept(currentStatusId).getUids().iterator().next();
        }

        // create a new extension (with a part for each path the user is
        // editing)

        int newMemberId = termFactory.uuidToNativeWithGeneration(memberUuid, unspecifiedUuid, paths, effectiveTime);

        I_ThinExtByRefVersioned newExtension =
                termFactory.newExtensionNoChecks(refsetId, newMemberId, componentId, extTypeId);

        for (I_Path editPath : paths) {

            I_ThinExtByRefPartConceptConceptString extension = termFactory.newConceptConceptStringExtensionPart();

            extension.setPathId(editPath.getConceptId());
            extension.setStatusId(termFactory.getConcept(new UUID[] { statusUuid }).getConceptId());
            extension.setVersion(effectiveTime);
            extension.setC1id(c1Id);
            extension.setC2id(c2Id);
            extension.setStr(stringValue);

            newExtension.addVersion(extension);
        }

        termFactory.addUncommittedNoChecks(newExtension);
        termFactory.commit();

        // edit the existing part's effectiveDate/version - this needs to occur
        // after the part has been committed, or else the effectiveDate is set
        // to the time at commit
        int index = newExtension.getVersions().size() - 1;
        I_ThinExtByRefPartConceptConceptString extension =
                (I_ThinExtByRefPartConceptConceptString) newExtension.getVersions().get(index);
        extension.setVersion(effectiveTime);
        newExtension.addVersion(extension);

        termFactory.addUncommittedNoChecks(newExtension);
        termFactory.commit();
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
    public boolean retireRefsetExtension(int refsetId, int conceptId, int memberTypeId) throws Exception {

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

                // confirm its the right extension value and its status is
                // current
                if (latestPart.getStatusId() == currentStatusId) {
                    if (latestPart instanceof I_ThinExtByRefPartConcept) {
                        int partValue = ((I_ThinExtByRefPartConcept) latestPart).getC1id();
                        if (partValue == memberTypeId) {
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
            }
        }
        return false;
    }

    /**
     * Get all the descendants (children, children of children, etc) of a
     * particular concept.
     */
    public Set<I_GetConceptData> getAllDescendants(I_GetConceptData concept, Condition... conditions) throws Exception {
        return getAllDescendants(concept, concept, conditions);
    }

    /**
     * Get all the descendants (children, children of children, etc) of a
     * particular concept.
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
            I_IntSet allowedStatuses, I_IntSet allowedTypes, Set<I_Position> positions, Condition... conditions)
            throws Exception {

        ITERATE_CHILDREN: for (I_RelTuple childTuple : parent.getDestRelTuples(allowedStatuses, allowedTypes,
            positions, false, true)) {
            I_GetConceptData childConcept = termFactory.getConcept(childTuple.getC1Id());
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

    /**
     * Get all the ancestors (parents, parents of parents, etc) of a particular
     * concept.
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
            I_IntSet allowedStatuses, I_IntSet allowedTypes, Set<I_Position> positions, Condition... conditions)
            throws Exception {

        ITERATE_PARENTS: for (I_RelTuple childTuple : child.getSourceRelTuples(allowedStatuses, allowedTypes,
            positions, false, true)) {
            I_GetConceptData parentConcept = termFactory.getConcept(childTuple.getC2Id());
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
     * @return The view positions from the active config.
     *         Returns null if no config set or config contains no view
     *         positions.
     */
    protected Set<I_Position> getViewPositions() throws Exception {
        if (this.viewPositions == null) {
            I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();

            if (config != null) {
                this.viewPositions = config.getViewPositionSet();
            }

            if (this.viewPositions == null) {
                this.viewPositions = new HashSet<I_Position>();
            }
        }

        return (this.viewPositions.isEmpty()) ? null : this.viewPositions;
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

        return (this.editPaths.isEmpty()) ? null : this.editPaths;
    }

    /**
     * @return The allowed status from the active config.
     *         Returns just "CURRENT" if no config set.
     */
    protected I_IntSet getAllowedStatuses() throws Exception {
        if (this.allowedStatuses == null) {
            I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();

            if (config != null) {
                this.allowedStatuses = config.getAllowedStatus();
            } else {
                this.allowedStatuses = termFactory.newIntSet();
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
        if (this.isARelTypes == null) {
            this.isARelTypes = termFactory.newIntSet();
            this.isARelTypes.add(SNOMED.Concept.IS_A.localize().getNid());
            this.isARelTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
        }
        return this.isARelTypes;
    }

    /**
     * A simple template for logic that defines if a process should be executed
     * on a particular subject (concept).
     */
    public interface Condition {
        public boolean evaluate(I_GetConceptData concept) throws Exception;
    }

    public int getConceptTypeId() {
        return conceptTypeId;
    }

    public void setConceptTypeId(int conceptTypeId) {
        this.conceptTypeId = conceptTypeId;
    }

    public boolean newConceptExtensionPart(int refsetId, int componentId, int c1Id) {
        return newConceptExtensionPart(refsetId, componentId, c1Id, currentStatusId);
    }

    public boolean newConceptExtensionPart(int refsetId, int componentId, int c1Id, int statusId) {
        I_TermFactory termFactory = LocalVersionedTerminology.get();
        try {
            for (I_ThinExtByRefVersioned extension : termFactory.getAllExtensionsForComponent(componentId)) {
                if (extension.getRefsetId() == refsetId) {
                    // get the latest version
                    I_ThinExtByRefPart latestPart = null;
                    for (I_ThinExtByRefPart part : extension.getVersions()) {
                        if ((latestPart == null) || (part.getVersion() >= latestPart.getVersion())) {
                            latestPart = part;
                        }
                    }
                    if (latestPart == null) {
                        return false;
                    }

                    if (latestPart instanceof I_ThinExtByRefPartConcept) {
                        // found a member to retire
                        I_ThinExtByRefPartConcept clone = (I_ThinExtByRefPartConcept) latestPart.duplicate();
                        clone.setStatusId(statusId);
                        clone.setVersion(Integer.MAX_VALUE);
                        clone.setC1id(c1Id);
                        extension.addVersion(clone);
                        termFactory.addUncommittedNoChecks(extension);
                        termFactory.commit();
                        return true;
                    }
                }
            }
        }

        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public boolean retireConceptExtension(int refsetId, int componentId) {
        I_TermFactory termFactory = LocalVersionedTerminology.get();
        try {
            for (I_ThinExtByRefVersioned extension : termFactory.getAllExtensionsForComponent(componentId)) {
                if (extension.getRefsetId() == refsetId) {
                    // get the latest version
                    I_ThinExtByRefPart latestPart = null;
                    for (I_ThinExtByRefPart part : extension.getVersions()) {
                        if ((latestPart == null) || (part.getVersion() >= latestPart.getVersion())) {
                            latestPart = part;
                        }
                    }
                    if (latestPart == null) {
                        return false;
                    }

                    // confirm its the right extension value and its status is
                    // current
                    if (latestPart.getStatusId() == currentStatusId) {
                        if (latestPart instanceof I_ThinExtByRefPartConcept) {
                            // found a member to retire
                            I_ThinExtByRefPartConcept clone = (I_ThinExtByRefPartConcept) latestPart.duplicate();
                            clone.setStatusId(retiredStatusId);
                            clone.setVersion(Integer.MAX_VALUE);
                            extension.addVersion(clone);
                            termFactory.addUncommittedNoChecks(extension);
                            termFactory.commit();
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public List<I_GetConceptData> filterListByConceptType(List<I_ThinExtByRefVersioned> allExtensions,
            I_GetConceptData requiredPromotionStatusConcept) throws Exception {

        List<I_GetConceptData> filteredList = new ArrayList<I_GetConceptData>();

        for (I_ThinExtByRefVersioned extension : allExtensions) {
            I_ThinExtByRefPart latestMemberPart = getLatestCurrentPart(extension);
            if (latestMemberPart == null) {
                throw new Exception("Member extension exists with no parts.");
            }
            I_GetConceptData promotionStatus = null;
            if (extension != null) {
                promotionStatus = getPromotionStatus(extension);
            }
            if (promotionStatus != null && promotionStatus.equals(requiredPromotionStatusConcept)) {
                filteredList.add(termFactory.getConcept(extension.getComponentId()));
            }
        }
        return filteredList;
    }

    private I_GetConceptData getPromotionStatus(I_ThinExtByRefVersioned promotionExtension) throws Exception {
        I_ThinExtByRefPart latestPart = getLatestCurrentPart(promotionExtension);
        if (latestPart == null) {
            return null;
        } else {
            if (latestPart instanceof I_ThinExtByRefPartConcept) {
                I_ThinExtByRefPartConcept latestConceptPart = (I_ThinExtByRefPartConcept) latestPart;
                return termFactory.getConcept(latestConceptPart.getC1id());
            } else {
                throw new Exception("Don't know how to handle promotion ext of type : " + latestPart);
            }
        }
    }

    private I_ThinExtByRefPart getLatestCurrentPart(I_ThinExtByRefVersioned memberExtension)
            throws TerminologyException, IOException {
        I_ThinExtByRefPart latestPart = null;
        I_GetConceptData currentStatusConcept =
                termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());

        for (I_ThinExtByRefPart part : memberExtension.getVersions()) {
            if ((latestPart == null) || (part.getVersion() >= latestPart.getVersion())) {
                if (part.getStatusId() == currentStatusConcept.getConceptId()) {
                    latestPart = part;
                }
            }
        }
        return latestPart;
    }

}
