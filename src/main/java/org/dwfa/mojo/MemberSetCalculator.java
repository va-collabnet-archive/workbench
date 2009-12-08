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
package org.dwfa.mojo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.maven.plugin.logging.Log;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.spec.ConceptSpec;

public class MemberSetCalculator extends Thread implements I_ProcessConcepts {

    /**
     * The ids of the concepts which may be included in the member set (due to
     * lineage).
     * These may be excluded if they explicitly state a refset exclusion.
     */
    private Set<Integer> includedLineage;

    /**
     * The ids of the concepts which may be excluded from the member set (due to
     * lineage).
     * These may be included if they explicitly state a refset inclusion.
     */
    private Set<Integer> excludedLineage;

    /**
     * The ids of the concepts which have already been added to member set.
     */
    private Set<Integer> includedMemberSet;

    /**
     * The ids of the concepts which have be explicitly exclude from the member
     * set.
     */
    private Set<Integer> excludedMemberSet;

    /**
     * The path that the member set will be written on.
     */
    private I_Path memberSetPath;

    /**
     * The id of the member set.
     */
    private int memberSetId;

    private I_TermFactory termFactory;
    private int memberSetCount;
    private int referenceSetId;
    private int includeLineageId;
    private int includeIndividualId;
    private int excludeLineageId;
    private int excludeIndividualId;
    private int conceptTypeId;
    private int retiredConceptId;
    private int typeId;
    private int currentStatusId;
    private I_GetConceptData root;
    private I_GetConceptData refConcept;

    private int processedConcepts = 0;

    private File refsetInclusionsOutputFile;
    private File refsetExclusionsOutputFile;
    private Log log;

    public Log getLog() {
        return log;
    }

    /**
     * Calculates a member set given a reference set spec.
     * 
     * @param memberSetPathDescriptor
     * @param referenceSetId The id of the reference set of which we wish to
     *            calculate the member set.
     * @throws Exception
     */
    public MemberSetCalculator(I_GetConceptData refConcept, ConceptDescriptor memberSetPathDescriptor,
            ConceptDescriptor rootDescriptor, File refsetInclusionsOutputFile, File refsetExclusionsOutputFile, Log log)
            throws Exception {

        this.log = log;

        getLog().debug("MemberSetCalculator() - start");

        this.refsetInclusionsOutputFile = refsetInclusionsOutputFile;
        this.refsetExclusionsOutputFile = refsetExclusionsOutputFile;

        termFactory = LocalVersionedTerminology.get();
        this.refConcept = refConcept;
        // verify concepts
        referenceSetId = refConcept.getConceptId();

        I_GetConceptData memberSetPathConcept = memberSetPathDescriptor.getVerifiedConcept();
        memberSetPath = termFactory.getPath(memberSetPathConcept.getUids());

        I_IntSet currentIntSet = getIntSet(ArchitectonicAuxiliary.Concept.CURRENT);
        I_IntSet generatesRelIntSet = getIntSet(ConceptConstants.GENERATES_REL);

        I_GetConceptData memberSetSpecConcept = assertExactlyOne(refConcept.getSourceRelTargets(currentIntSet,
            generatesRelIntSet, null, false));

        memberSetId = memberSetSpecConcept.getConceptId();

        root = rootDescriptor.getVerifiedConcept();

        // initialise sets
        includedLineage = new HashSet<Integer>();
        excludedLineage = new HashSet<Integer>();
        includedMemberSet = new HashSet<Integer>();
        excludedMemberSet = new HashSet<Integer>();

        memberSetCount = 0;

        includeLineageId = termFactory.uuidToNative(RefsetAuxiliary.Concept.INCLUDE_LINEAGE.getUids().iterator().next());
        includeIndividualId = termFactory.uuidToNative(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.getUids()
            .iterator()
            .next());
        excludeLineageId = termFactory.uuidToNative(RefsetAuxiliary.Concept.EXCLUDE_LINEAGE.getUids().iterator().next());
        excludeIndividualId = termFactory.uuidToNative(RefsetAuxiliary.Concept.EXCLUDE_INDIVIDUAL.getUids()
            .iterator()
            .next());
        conceptTypeId = termFactory.uuidToNative(RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids().iterator().next());
        retiredConceptId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids().iterator().next());
        typeId = termFactory.uuidToNative(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.getUids().iterator().next());
        currentStatusId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next());

        getLog().debug("MemberSetCalculator() - end");
    }

    private <T> T assertExactlyOne(Collection<T> collection) {
        assert collection.size() == 1 : "Exactly one element expected, encountered " + collection;

        return collection.iterator().next();
    }

    private File renameOutputFile(String suffix, File refsetInclusionsOutputFile) {

        String filename = refsetInclusionsOutputFile.getAbsolutePath();
        int index = -1;
        if ((index = filename.lastIndexOf(".")) != -1) {
            String fileExtension = filename.substring(index, filename.length());
            filename = filename.replace(fileExtension, suffix + "." + fileExtension);
        } else {
            filename = filename + "." + suffix + ".txt";
        }

        refsetInclusionsOutputFile = new File(filename);
        return refsetInclusionsOutputFile;
    }

    public void run() {
        try {
            getLog().info("Running analysis for : " + refConcept.toString() + " \nusing root : " + getRoot());
            processConcept(getRoot());

            // write list of uuids for concepts that were included
            // in the member set
            refsetInclusionsOutputFile.getParentFile().mkdirs();

            File refsetInclusionsOutputFile_temp = renameOutputFile(refConcept.toString(), refsetInclusionsOutputFile);

            BufferedWriter uuidWriter = new BufferedWriter(new FileWriter(refsetInclusionsOutputFile_temp));
            for (int i : includedMemberSet) {
                Collection<UUID> uuids = termFactory.getUids(i);
                for (UUID uuid : uuids) {
                    uuidWriter.write(uuid.toString());
                    uuidWriter.append("\t");
                    uuidWriter.append(getFsnFromConceptId(i));
                    uuidWriter.newLine();
                }
            }
            uuidWriter.close();

            // write list of uuids for concepts that were excluded
            // from member set
            File refsetExclusionsOutputFile_temp = renameOutputFile(refConcept.toString(), refsetExclusionsOutputFile);

            refsetExclusionsOutputFile.getParentFile().mkdirs();
            uuidWriter = new BufferedWriter(new FileWriter(refsetExclusionsOutputFile_temp));
            for (int i : excludedMemberSet) {
                Collection<UUID> uuids = termFactory.getUids(i);
                for (UUID uuid : uuids) {
                    uuidWriter.write(uuid.toString());
                    uuidWriter.append("\t");
                    uuidWriter.append(getFsnFromConceptId(i));
                    uuidWriter.newLine();
                }
            }
            uuidWriter.close();

            String message = "Number of members found in reference set: " + getMemberSetCount();
            getLog().info(message);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getFsnFromConceptId(int conceptId) throws Exception {

        I_GetConceptData concept = LocalVersionedTerminology.get().getConcept(conceptId);

        List<I_DescriptionVersioned> descriptions = concept.getDescriptions();
        int fsnId = LocalVersionedTerminology.get().uuidToNative(
            ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids().iterator().next());
        for (I_DescriptionVersioned description : descriptions) {
            List<I_DescriptionPart> parts = description.getVersions();
            for (I_DescriptionPart part : parts) {
                if (fsnId == part.getTypeId()) {
                    return part.getText();
                }
            }
        }

        return "unknown";
    }

    /**
     * Processes each concept in the database. Concepts may be included
     * or excluded from the member set based on the reference set extension
     * type. Lineage (children) of the concept may also be included or excluded
     * (recursively).
     */
    public void processConcept(I_GetConceptData concept) throws Exception {

        getLog().debug(
            "processConcept(I_GetConceptData) " + concept == null ? null : concept.getDescriptions().iterator().next()
                + " - start");

        processedConcepts++;
        if (processedConcepts % 10000 == 0) {
            getLog().info("Processed synced " + processedConcepts + " concepts");
        }

        int conceptId = concept.getConceptId();

        List<I_ThinExtByRefVersioned> extensions = termFactory.getAllExtensionsForComponent(conceptId);

        // process each refset associated with this concept and work out
        // if any of them are the refset we are looking for
        int refsetCount = 0;
        I_ThinExtByRefVersioned memberSet = null;
        for (I_ThinExtByRefVersioned refSetExtension : extensions) {
            List<I_ThinExtByRefTuple> exensionParts = new ArrayList<I_ThinExtByRefTuple>();
            synchronized (termFactory) {
                refSetExtension.addTuples(getIntSet(ArchitectonicAuxiliary.Concept.CURRENT), null, exensionParts, true);
            }
            if (exensionParts.size() == 0) {
                // this is dodgy, but basically if the latest version of the
                // refset doesn't have a current status skip the refset
                continue;
            }

            if (refSetExtension.getRefsetId() == referenceSetId) {
                getLog().debug("processConcept(I_GetConceptData) - found refset spec " + referenceSetId);
                refsetCount++;
            }
            if (refSetExtension.getRefsetId() == memberSetId) {
                getLog().debug("processConcept(I_GetConceptData) - found refset membership " + memberSetId);
                memberSet = refSetExtension;
            }
        }
        boolean includedInLatestMemberSet = latestMembersetIncludesConcept(memberSet);

        if (refsetCount == 0) {
            getLog().debug("processConcept(I_GetConceptData) - no explicit refset instruction");

            // no refsets have been found so check if there are any inherited
            // conditions
            if (includedLineage.contains(conceptId)) {
                getLog().debug(
                    "processConcept(I_GetConceptData) - inherited include "
                        + getFsnFromConceptId(concept.getConceptId()));

                // this concept has an inherited condition for inclusion
                if (!includedInLatestMemberSet) {
                    addToMemberSet(conceptId, includeLineageId);
                }
            } else {
                if (excludedLineage.contains(conceptId)) {
                    getLog().debug(
                        "processConcept(I_GetConceptData) - inherited exclude "
                            + getFsnFromConceptId(concept.getConceptId()));
                } else {
                    getLog().debug(
                        "processConcept(I_GetConceptData) - no longer under a lineage include therefore retire "
                            + getFsnFromConceptId(concept.getConceptId()));
                }

                if (memberSet != null) {
                    excludedMemberSet.add(conceptId);
                    retireLatestExtension(memberSet);
                }
            }
        }

        // process each reference set extension
        for (I_ThinExtByRefVersioned extensionData : extensions) {
            List<I_ThinExtByRefTuple> exensionParts = new ArrayList<I_ThinExtByRefTuple>();
            synchronized (termFactory) {
                extensionData.addTuples(getIntSet(ArchitectonicAuxiliary.Concept.CURRENT), null, exensionParts, true);
            }
            if (exensionParts.size() == 0) {
                // this is dodgy, but basically if the latest version of the
                // refset doesn't have a current status skip the refset
                continue;
            }

            includedInLatestMemberSet = latestMembersetIncludesConcept(memberSet);
            I_ThinExtByRefVersioned part = extensionData;
            int extensionTypeId = part.getTypeId();
            getLog().debug(
                "processConcept(I_GetConceptData) - processing extensionTypeId " + extensionTypeId + " referenceSetId "
                    + extensionData.getRefsetId());

            if (extensionTypeId == conceptTypeId && extensionData.getRefsetId() == referenceSetId) {
                // only look at the ref set extensions that correspond to
                // the reference set as specified in maven plugin config
                int typeId = 0;
                getLog().debug("processConcept(I_GetConceptData) - valid type/refset, processing");

                List<? extends I_ThinExtByRefPart> versions = part.getVersions();
                for (I_ThinExtByRefPart version : versions) {
                    I_ThinExtByRefPartConcept temp = (I_ThinExtByRefPartConcept) version;
                    typeId = temp.getConceptId();
                    getLog().debug(
                        "processConcept(I_GetConceptData) - determining type version " + temp.getVersion()
                            + " type now " + typeId);
                }

                boolean include = true;
                if (typeId == includeIndividualId) {
                    if (!includedInLatestMemberSet) {
                        getLog().debug(
                            "processConcept(I_GetConceptData) - including individual "
                                + getFsnFromConceptId(concept.getConceptId()));
                        addToMemberSet(conceptId, typeId);
                    } else {
                        getLog().debug("processConcept(I_GetConceptData) - already included in last generation");
                    }
                } else if (typeId == includeLineageId) {
                    if (!includedInLatestMemberSet) {
                        getLog().debug(
                            "processConcept(I_GetConceptData) - including individual for lineage instruction "
                                + getFsnFromConceptId(concept.getConceptId()));
                        addToMemberSet(conceptId, typeId);
                    } else {
                        getLog().debug("processConcept(I_GetConceptData) - already included in last generation");
                    }
                    getLog().debug("processConcept(I_GetConceptData) - including all children");
                    markAllChildren(concept, include);
                } else if (typeId == excludeIndividualId) {
                    if (includedInLatestMemberSet) {
                        getLog().debug(
                            "processConcept(I_GetConceptData) - excluding individual "
                                + getFsnFromConceptId(concept.getConceptId()));
                        retireLatestExtension(memberSet);
                        excludedMemberSet.add(conceptId);
                    } else {
                        getLog().debug("processConcept(I_GetConceptData) - already excluded in last generation");
                    }
                } else if (typeId == excludeLineageId) {
                    if (includedInLatestMemberSet) {
                        getLog().debug(
                            "processConcept(I_GetConceptData) - excluding individual for lineage instruction "
                                + getFsnFromConceptId(concept.getConceptId()));
                        retireLatestExtension(memberSet);
                        excludedMemberSet.add(conceptId);
                    } else {
                        getLog().debug("processConcept(I_GetConceptData) - already excluded in last generation");
                    }
                    getLog().debug("processConcept(I_GetConceptData) - including all children");
                    markAllChildren(concept, !include);
                } else {
                    throw new Exception("Unknown extension type: " + typeId);
                }

            }
        }

        List<I_RelTuple> children = concept.getDestRelTuples(getIntSet(ArchitectonicAuxiliary.Concept.CURRENT,
            ArchitectonicAuxiliary.Concept.PENDING_MOVE), getIntSet(ConceptConstants.SNOMED_IS_A), null, false);

        getLog().debug("processConcept(I_GetConceptData) - processing " + children.size() + " children");

        for (I_RelTuple child : children) {
            int childId = child.getC1Id();
            processConcept(termFactory.getConcept(childId));
        }

        getLog().debug("processConcept(I_GetConceptData) - end");
    }

    /**
     * Calculates if the latest version of the extension includes the current
     * concept.
     * 
     * @param extensionPart The extension to check.
     * @return True if the member set includes the concept, false if it doesn't.
     * @throws Exception
     */
    public boolean latestMembersetIncludesConcept(I_ThinExtByRefVersioned extensionPart) throws Exception {
        getLog().debug("latestMembersetIncludesConcept(I_ThinExtByRefVersioned=" + extensionPart + ") - start"); //$NON-NLS-1$ //$NON-NLS-2$

        if (extensionPart == null) {
            getLog().debug("latestMembersetIncludesConcept(I_ThinExtByRefVersioned) - end - return value=" + false); //$NON-NLS-1$
            return false;
        }

        List<I_ThinExtByRefTuple> exensionParts = new ArrayList<I_ThinExtByRefTuple>();
        synchronized (termFactory) {
            extensionPart.addTuples(getIntSet(ArchitectonicAuxiliary.Concept.CURRENT), null, exensionParts, true);
        }

        boolean result = exensionParts.size() > 0;
        getLog().debug("latestMembersetIncludesConcept(I_ThinExtByRefVersioned) - end - return value=" + result);

        return result;
    }

    /**
     * Retires the latest version of a specified extension.
     * 
     * @param extensionPart The extension to check.
     * @throws Exception
     */
    public void retireLatestExtension(I_ThinExtByRefVersioned extensionPart) throws Exception {
        getLog().debug(
            "retireLatestExtension(I_ThinExtByRefVersioned=" + extensionPart + ") - start for concept "
                + getFsnFromConceptId(extensionPart.getComponentId()));

        if (extensionPart != null) {

            List<I_ThinExtByRefTuple> extensionParts = new ArrayList<I_ThinExtByRefTuple>();

            synchronized (termFactory) {
                extensionPart.addTuples(getIntSet(ArchitectonicAuxiliary.Concept.CURRENT), null, extensionParts, true);
            }

            if (extensionParts.size() > 0) {
                I_ThinExtByRefPart latestVersion = assertExactlyOne(extensionParts);

                I_ThinExtByRefPart clone = latestVersion.duplicatePart();
                clone.setStatus(retiredConceptId);
                clone.setVersion(Integer.MAX_VALUE);
                extensionPart.addVersion(clone);

                getLog().debug(
                    "retireLatestExtension(I_ThinExtByRefVersioned) - updated version of extension for "
                        + getFsnFromConceptId(extensionPart.getComponentId()));

                termFactory.addUncommitted(extensionPart);
            }
        }

        getLog().debug("retireLatestExtension(I_ThinExtByRefVersioned) - end"); //$NON-NLS-1$
    }

    /**
     * Adds a particular concept to the member set.
     * 
     * @param conceptId the concept id of the concept we wish to add to the
     *            member set.
     * @param includeTypeConceptId
     * @throws Exception
     */
    public void addToMemberSet(int conceptId, int includeTypeConceptId) throws Exception {
        getLog().debug("addToMemberSet(int=" + conceptId + ") - start for " + getFsnFromConceptId(conceptId)); //$NON-NLS-1$ //$NON-NLS-2$

        if (!includedMemberSet.contains(conceptId)) {
            memberSetCount++;
            includedMemberSet.add(conceptId);

            int memberId = termFactory.uuidToNativeWithGeneration(UUID.randomUUID(),
                ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(), termFactory.getPaths(),
                Integer.MAX_VALUE);

            I_ThinExtByRefVersioned newExtension;
            I_ThinExtByRefPartConcept conceptExtension;

            synchronized (termFactory) {
                newExtension = termFactory.newExtension(memberSetId, memberId, conceptId, typeId);

                conceptExtension = termFactory.newConceptExtensionPart();
            }

            conceptExtension.setPathId(memberSetPath.getConceptId());
            conceptExtension.setStatus(currentStatusId);
            conceptExtension.setVersion(Integer.MAX_VALUE);
            conceptExtension.setConceptId(getMembershipType(includeTypeConceptId));

            newExtension.addVersion(conceptExtension);
            getLog().debug(
                "addToMemberSet(int=" + conceptId + ") - start added new extension for " + getFsnFromConceptId(conceptId)); //$NON-NLS-1$ //$NON-NLS-2$

            synchronized (termFactory) {
                termFactory.addUncommitted(newExtension);
            }
        }

        getLog().debug("addToMemberSet(int) - end");
    }

    private int getMembershipType(int includeTypeConceptId) throws Exception {
        I_GetConceptData includeConcept = termFactory.getConcept(includeTypeConceptId);

        Set<I_GetConceptData> membershipTypes = includeConcept.getSourceRelTargets(
            getIntSet(ArchitectonicAuxiliary.Concept.CURRENT), getIntSet(ConceptConstants.CREATES_MEMBERSHIP_TYPE),
            null, false);

        return assertExactlyOne(membershipTypes).getConceptId();
    }

    /**
     * Finds the children of a particular concept, and includes/excludes based
     * on provided parameter. Occurs recursively so that the entire lineage is
     * calculated.
     * 
     * @param concept The concept who's children we wish to process.
     * @param includeChildren Whether children will be included or excluded when
     *            processed.
     * @throws Exception
     */
    public void markAllChildren(I_GetConceptData concept, boolean includeChildren) throws Exception {
        getLog().debug(
            "markAllChildren(I_GetConceptData=" + concept.getDescriptions().iterator().next() + ", boolean="
                + includeChildren + ") - start");

        List<I_RelTuple> children = concept.getDestRelTuples(getIntSet(ArchitectonicAuxiliary.Concept.CURRENT,
            ArchitectonicAuxiliary.Concept.PENDING_MOVE), getIntSet(ConceptConstants.SNOMED_IS_A), null, false);

        getLog().debug("markAllChildren(I_GetConceptData, boolean) - concept has " + children.size() + " children");

        for (I_RelTuple child : children) {

            int childId = child.getC1Id();

            if (includeChildren) {
                if (excludedLineage.contains(Integer.valueOf(childId))) {
                    excludedLineage.remove(Integer.valueOf(childId));
                }
                includedLineage.add(Integer.valueOf(childId));
            } else {
                if (includedLineage.contains(Integer.valueOf(childId))) {
                    includedLineage.remove(Integer.valueOf(childId));
                }
                excludedLineage.add(Integer.valueOf(childId));
            }

            getLog().debug("markAllChildren(I_GetConceptData, boolean) - include children's children");

            markAllChildren(termFactory.getConcept(childId), includeChildren);
        }

        getLog().debug("markAllChildren(I_GetConceptData, boolean) - end");
    }

    private I_IntSet getIntSet(ArchitectonicAuxiliary.Concept... concepts) throws Exception {
        I_IntSet status = termFactory.newIntSet();

        for (ArchitectonicAuxiliary.Concept concept : concepts) {
            status.add(termFactory.getConcept(concept.getUids()).getConceptId());
        }
        assert status.getSetValues().length > 0 : "getIntSet returns an empty set";
        return status;
    }

    private I_IntSet getIntSet(ConceptSpec... concepts) throws Exception {
        I_IntSet status = termFactory.newIntSet();

        for (ConceptSpec concept : concepts) {
            status.add(concept.localize().getNid());
        }

        return status;
    }

    /**
     * Gets the number of members in the specified reference set.
     * 
     * @return
     */
    public int getMemberSetCount() {

        return memberSetCount;
    }

    /**
     * Sets the number of members in the specified reference set.
     * 
     * @param memberSetCount
     */
    public void setMemberSetCount(int memberSetCount) {

        this.memberSetCount = memberSetCount;
    }

    public I_GetConceptData getRoot() {
        return root;
    }

    public void setRoot(I_GetConceptData root) {
        this.root = root;
    }
}
