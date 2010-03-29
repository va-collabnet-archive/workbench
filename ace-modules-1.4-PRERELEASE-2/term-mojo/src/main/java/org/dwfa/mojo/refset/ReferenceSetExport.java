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
package org.dwfa.mojo.refset;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.I_ConfigAceFrame.LANGUAGE_SORT_PREF;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.api.process.I_ProcessQueue;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;
import org.dwfa.mojo.ConceptConstants;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.PathReleaseDateConfig;
import org.dwfa.mojo.PositionDescriptor;
import org.dwfa.mojo.file.AceIdentifierWriter;
import org.dwfa.mojo.refset.writers.MemberRefsetHandler;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.ThinExtByRefPartConcept;
import org.dwfa.vodb.types.ThinExtByRefPartString;

/**
 *
 * This mojo exports reference sets from an ACE database
 *
 * @goal refset-export
 * @author Dion McMurtrie
 */
public class ReferenceSetExport extends AbstractMojo implements I_ProcessConcepts {
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Whether to use RF2 for the export. If not, the alternate release format
     * will be used (this is also the default).
     *
     * @parameter
     */
    boolean useRF2 = false;

    /**
     * RF2 Descriptor - this is required if useRF2 is set to true. This
     * describes the module, namespace, content sub type and country information
     * required to export in RF2.
     *
     * @parameter
     */
    RF2Descriptor rf2Descriptor;

    /**
     * Export specification that dictates which concepts are exported and which
     * are not. Only reference sets whose identifying concept is exported will
     * be exported. Only members relating to components that will be exported
     * will in turn be exported.
     * <p>
     * For example if you have a reference set identified by concept A, and
     * members B, C and D. If the export spec does not include exporting concept
     * A then none of the reference set will be exported. However if the export
     * spec does include A, but not C then the reference set will be exported
     * except it will only have members B and D - C will be omitted.
     *
     * @parameter
     * @required
     */
    ExportSpecification[] exportSpecifications;

    /**
     * Defines the directory to which the UUID based reference sets are exported
     *
     * @parameter
     * @required
     */
    File uuidRefsetOutputDirectory;

    /**
     * Defines the directory to which the SCTID based reference sets are
     * exported
     *
     * @parameter
     * @required
     */
    File sctidRefsetOutputDirectory;

    /**
     * Defines the directory to which the SCTID based structural reference sets
     * are
     * exported
     *
     * @parameter
     * @required
     */
    File sctidStructuralRefsetOutputDirectory;

    /**
     * Release version used to embed in the refset file names - if not specified
     * then the "path version" reference set is used to determine the version
     *
     * @parameter
     */
    String releaseVersion;
    /**
     * The number of threads to use.
     *
     * @parameter
     */
    int numberOfThreads = 1;

    /**
     * Auto commit every 1000 concepts.
     *
     * @parameter
     */
    boolean autoCommit = false;

    /**
     * @parameter
     */
    File aceIdentifierFile = null;
    AceIdentifierWriter aceIdentifierWriter = null;

    /**
     * @parameter
     */
    File aceStructuralIdentifierFile = null;
    AceIdentifierWriter aceStructuralIdentifierWriter = null;

    /**
     * Batch processing size - defaults to 30
     *
     * @parameter
     */
    private int batchSize = 30;

    /**
     * Used to configure the release date/version to use for given paths,
     * paths not specified will be exported with the version recorded in the
     * database.
     * Note this configuration will be ignored and overridden by the releaseDate
     * parameter
     *
     * @parameter
     */
    protected PathReleaseDateConfig[] pathReleaseDateConfig;

    protected I_TermFactory tf = LocalVersionedTerminology.get();

    protected I_IntSet allowedStatuses;

    protected Set<I_Position> positions;

    protected HashMap<String, BufferedWriter> writerMap = new HashMap<String, BufferedWriter>();

    protected HashMap<Integer, RefsetType> refsetTypeMap = new HashMap<Integer, RefsetType>();

    protected HashMap<Integer, String> pathReleaseVersions = new HashMap<Integer, String>();
    int ctv3IdMapExtension;
    int snomedIdMapExtension;
    int activeNId;
    int inActiveNId;
    int currentNId;
    int relationshipRefinability;
    int relationshipRefinabilityExtension;
    int descriptionInactivationIndicator;
    int relationshipInactivationIndicator;
    int conceptInactivationIndicator;

    /** RF2 inactive status. */
    int duplicateStatusNId;
    int ambiguousStatusNId;
    int erroneousStatusNId;
    int outdatedStatusNId;
    int inappropriateStatusNId;
    int movedElsewhereStatusNId;

    /** Ace Workbench inactive status. */
    int aceDuplicateStatusNId;
    int aceAmbiguousStatusNId;
    int aceErroneousStatusNId;
    int aceOutdatedStatusNId;
    int aceInappropriateStatusNId;
    int aceMovedElsewhereStatusNId;

    /** History relationship types to History relationship reference set map. */
    Map<Integer, Integer> historyStatusRefsetMap = new HashMap<Integer, Integer>();

    I_GetConceptData activeConcept;

    final String newLineChars = "\r\n";

    /** thread pool for processing concepts. */
    I_ProcessQueue workQueue;

    /** This is need to setup the database */
    boolean first = true;

    int refsetDescriptorType;

    /**
     * count down latch to hold processConcept until all threads have finished.
     */
    private CountDownLatch doneSignal;
    /**
     * Number of concepts to process.
     */
    private Integer conceptsToProcessCount = 0;

    private List<I_GetConceptData> currentBatch = new ArrayList<I_GetConceptData>();

    private Map<Integer, Boolean> testSpecCache = new HashMap<Integer, Boolean>();

    private Collection<Integer> positionIds = new ArrayList<Integer>();

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            activeNId = Concept.ACTIVE.localize().getNid();
            activeConcept = tf.getConcept(activeNId);
            inActiveNId = Concept.INACTIVE.localize().getNid();
            currentNId = Concept.CURRENT.localize().getNid();

            if (useRF2) {
                initRF2Constants();
            }

            aceDuplicateStatusNId = Concept.DUPLICATE.localize().getNid();
            aceAmbiguousStatusNId = Concept.AMBIGUOUS.localize().getNid();
            aceErroneousStatusNId = Concept.ERRONEOUS.localize().getNid();
            aceOutdatedStatusNId = Concept.OUTDATED.localize().getNid();
            aceInappropriateStatusNId = Concept.INAPPROPRIATE.localize().getNid();
            aceMovedElsewhereStatusNId = Concept.MOVED_ELSEWHERE.localize().getNid();

            historyStatusRefsetMap.put(ConceptConstants.MOVED_FROM_HISTORY.localize().getNid(),
                ConceptConstants.MOVED_FROM_HISTORY_REFSET.localize().getNid());
            historyStatusRefsetMap.put(ConceptConstants.MOVED_TO_HISTORY.localize().getNid(),
                ConceptConstants.MOVED_TO_HISTORY_REFSET.localize().getNid());
            historyStatusRefsetMap.put(ConceptConstants.REPLACED_BY_HISTORY.localize().getNid(),
                ConceptConstants.REPLACED_BY_HISTORY_REFSET.localize().getNid());
            historyStatusRefsetMap.put(ConceptConstants.SAME_AS_HISTORY.localize().getNid(),
                ConceptConstants.SAME_AS_HISTORY_REFSET.localize().getNid());
            historyStatusRefsetMap.put(ConceptConstants.WAS_A_HISTORY.localize().getNid(),
                ConceptConstants.WAS_A_HISTORY_REFSET.localize().getNid());

            workQueue = LocalVersionedTerminology.get().newProcessQueue(numberOfThreads);

        } catch (Exception e) {
            throw new MojoExecutionException("", e);
        }

        if (useRF2 && (aceIdentifierFile == null || aceStructuralIdentifierFile == null)) {
            throw new MojoExecutionException(
                "Cannot proceed, RF2 requires a aceIdentifierFile and aceStructuralIdentifierFile");
        }
        if (useRF2 && (aceIdentifierFile != null && aceStructuralIdentifierFile != null)) {
            try {
                aceIdentifierWriter = new AceIdentifierWriter(aceIdentifierFile, true);
                aceStructuralIdentifierWriter = new AceIdentifierWriter(aceStructuralIdentifierFile, true);
            } catch (IOException e) {
                throw new MojoExecutionException("cannot open id file", e);
            }
        }

        try {
            refsetDescriptorType = ConceptConstants.REFSET_DESCRIPTOR_TYPE.localize().getNid();
            allowedStatuses = tf.newIntSet();
            positions = new HashSet<I_Position>();
            for (ExportSpecification spec : exportSpecifications) {
                for (PositionDescriptor pd : spec.getPositionsForExport()) {
                    positions.add(pd.getPosition());
                    positionIds.add(pd.getPosition().getPath().getConceptId());
                }
                for (ConceptDescriptor status : spec.getStatusValuesForExport()) {
                    allowedStatuses.add(status.getVerifiedConcept().getConceptId());
                }
            }

            sctidRefsetOutputDirectory.mkdirs();
            if (useRF2) {
                sctidStructuralRefsetOutputDirectory.mkdirs();
            }
            uuidRefsetOutputDirectory.mkdirs();

            MemberRefsetHandler.setPathReleaseDateConfig(pathReleaseDateConfig);
            if (useRF2 && rf2Descriptor != null && rf2Descriptor.getModule() != null) {
                MemberRefsetHandler.setModule(rf2Descriptor.getModule());
            }

            doneSignal = new CountDownLatch(1);

            tf.iterateConcepts(this);
            workQueue.execute(new ConceptProcessor(currentBatch));
            if (!first && getConceptsToProcessCount() > 0) {
                doneSignal.await();
            }

            // Add in after R1.0.
            // if (useRF2) {
            // HashMap<Integer, RefsetType> refsetTypeMapCopy = new
            // HashMap<Integer, RefsetType>();
            // refsetTypeMapCopy.putAll(refsetTypeMap);
            // for (Integer refsetId : refsetTypeMapCopy.keySet()) {
            // exportRefsetDescription(refsetId,
            // refsetTypeMapCopy.get(refsetId));
            // }
            // }

            for (BufferedWriter writer : writerMap.values()) {
                writer.close();
            }

            if (useRF2 && aceIdentifierFile != null && aceStructuralIdentifierFile != null) {
                aceIdentifierWriter.close();
                aceStructuralIdentifierWriter.close();
            }

            MemberRefsetHandler.cleanup();

        } catch (Exception e) {
            throw new MojoExecutionException("exporting reference sets failed for specification "
                + exportSpecifications, e);
        }
    }

    /**
     * NB. Activate after R1.0
     * Export the refset description for the exported refsets.
     *
     * @param refsetId the exported refset
     * @param refsetType the type of refset
     * @throws Exception DB or file errors
     */
    private void exportRefsetDescription(Integer refsetId, RefsetType refsetType) throws Exception {
        I_ConceptAttributePart latest = getLatestAttributePart(tf.getConcept(refsetId));
        I_ThinExtByRefTuple tuple = getCurrentExtension(refsetId, refsetDescriptorType);
        I_ThinExtByRefPartConcept part = (I_ThinExtByRefPartConcept) tuple;
        if (part == null) {
            // no extension at all
            part = tf.newExtensionPart(ThinExtByRefPartConcept.class);
            part.setC1id(refsetId);
            part.setPathId(latest.getPathId());
            part.setStatusId(org.dwfa.cement.ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
            part.setVersion(latest.getVersion());
            export(part, null, ConceptConstants.REFSET_DESCRIPTOR_TYPE.localize().getNid(), refsetId, TYPE.DESCRIPTION);
        } else if (part.getC1id() != latest.getVersion()) {
            // add a new row with the latest refinability
            part.setC1id(latest.getVersion());
            export((I_ThinExtByRefTuple) tuple, TYPE.DESCRIPTION);
        }
    }

    /**
     * Set the RF2 only meta data constants.
     *
     * @throws IOException DB error
     * @throws TerminologyException Missing/invalid concept
     */
    private void initRF2Constants() throws IOException, TerminologyException {
        relationshipRefinability = ConceptConstants.RELATIONSHIP_REFINABILITY_EXTENSION.localize().getNid();
        relationshipRefinabilityExtension = ConceptConstants.RELATIONSHIP_REFINABILITY_EXTENSION.localize().getNid();
        descriptionInactivationIndicator = ConceptConstants.DESCRIPTION_INACTIVATION_INDICATOR.localize().getNid();
        relationshipInactivationIndicator = ConceptConstants.RELATIONSHIP_INACTIVATION_INDICATOR.localize().getNid();
        conceptInactivationIndicator = ConceptConstants.CONCEPT_INACTIVATION_INDICATOR.localize().getNid();
        ctv3IdMapExtension = ConceptConstants.CTV3_ID_MAP_EXTENSION.localize().getNid();
        snomedIdMapExtension = ConceptConstants.SNOMED_ID_MAP_EXTENSION.localize().getNid();

        duplicateStatusNId = ConceptConstants.DUPLICATE_STATUS.localize().getNid();
        ambiguousStatusNId = ConceptConstants.AMBIGUOUS_STATUS.localize().getNid();
        erroneousStatusNId = ConceptConstants.ERRONEOUS_STATUS.localize().getNid();
        outdatedStatusNId = ConceptConstants.OUTDATED_STATUS.localize().getNid();
        inappropriateStatusNId = ConceptConstants.INAPPROPRIATE_STATUS.localize().getNid();
        movedElsewhereStatusNId = ConceptConstants.MOVED_ELSEWHERE_STATUS.localize().getNid();
        refsetDescriptorType = ConceptConstants.REFSET_DESCRIPTOR_TYPE.localize().getNid();

        historyStatusRefsetMap.put(ConceptConstants.MOVED_FROM_HISTORY.localize().getNid(),
            ConceptConstants.MOVED_FROM_HISTORY_REFSET.localize().getNid());
        historyStatusRefsetMap.put(ConceptConstants.MOVED_TO_HISTORY.localize().getNid(),
            ConceptConstants.MOVED_TO_HISTORY_REFSET.localize().getNid());
        historyStatusRefsetMap.put(ConceptConstants.REPLACED_BY_HISTORY.localize().getNid(),
            ConceptConstants.REPLACED_BY_HISTORY_REFSET.localize().getNid());
        historyStatusRefsetMap.put(ConceptConstants.SAME_AS_HISTORY.localize().getNid(),
            ConceptConstants.SAME_AS_HISTORY_REFSET.localize().getNid());
        historyStatusRefsetMap.put(ConceptConstants.WAS_A_HISTORY.localize().getNid(),
            ConceptConstants.WAS_A_HISTORY_REFSET.localize().getNid());
    }

    /**
     * Add a concept to the list of concepts to process.
     */
    public void processConcept(I_GetConceptData concept) throws Exception {
        if (first) {
            new ConceptProcessor().processConcept(concept);
        } else {
            updateConceptsToProcessCount(1);
            currentBatch.add(concept);
            if (currentBatch.size() == batchSize) {
                workQueue.execute(new ConceptProcessor(currentBatch));
                currentBatch.clear();
            }
        }
    }

    /**
     * Gets the latest attribute for the concept.
     *
     * Attributes are filtered by the <code>allowedStatuses</code> and
     * <code>positions</code> lists.
     *
     * @param concept the concept to get the latest attribute for.
     * @return latest I_ConceptAttributePart may be null.
     * @throws IOException looking up I_ConceptAttributePart
     * @throws IOException on lookup/DB errors
     * @throws TerminologyException on lookup/DB errors
     */
    I_ConceptAttributePart getLatestAttributePart(I_GetConceptData concept) throws IOException, TerminologyException {
        I_ConceptAttributePart latest = null;
        for (I_ConceptAttributeTuple tuple : concept.getConceptAttributeTuples(allowedStatuses, positions, false, true)) {
            if (latest == null || latest.getVersion() < tuple.getVersion()) {
                latest = tuple.getPart();
            }
        }
        return latest;
    }

    private void processDescription(I_DescriptionVersioned versionedDesc) throws Exception {
        boolean exportableVersionFound = false;
        I_DescriptionPart latest = null;
        for (I_DescriptionPart part : versionedDesc.getVersions()) {
            if (checkPath(part.getPathId()) && testSpecificationWithCache(part.getTypeId())
                && allowedStatuses.contains(part.getStatusId())) {

                exportableVersionFound = true;
                if (latest == null || latest.getVersion() < part.getVersion()) {
                    latest = part;
                }

            }
        }

        if (exportableVersionFound) {
            // found a valid version of this relationship for export
            // therefore export its extensions
            int descId = versionedDesc.getDescId();
            exportRefsets(descId, TYPE.DESCRIPTION);
        }
    }

    private void processRelationship(I_RelVersioned versionedRel) throws Exception {
        if (testSpecification(versionedRel.getC2Id())) {
            boolean exportableVersionFound = false;
            I_RelPart latest = null;
            for (I_RelPart part : versionedRel.getVersions()) {
                if (checkPath(part.getPathId()) && allowedStatuses.contains(part.getStatusId())
                    && testSpecificationWithCache(part.getTypeId()) && testSpecificationWithCache(part.getPathId())
                    && testSpecificationWithCache(part.getRefinabilityId())
                    && testSpecificationWithCache(part.getCharacteristicId())) {

                    exportableVersionFound = true;
                    if (latest == null || latest.getVersion() < part.getVersion()) {
                        latest = part;
                    }
                }
            }
            if (exportableVersionFound) {
                // found a valid version of this relationship for export
                // therefore export its extensions
                int relId = versionedRel.getRelId();
                exportRefsets(relId, TYPE.RELATIONSHIP);
            }
        }
    }

    /**
     * Checks if the I_RelVersioned a history relationship type, if so export
     * the details to corresponding history type refset.
     *
     * All versions of the relationship are exported.
     *
     * @param versionedRel I_RelVersioned to create the history refset for.
     * @throws Exception
     */
    private void exportConceptHistory(I_RelVersioned versionedRel) throws Exception {
        if (testSpecification(versionedRel.getC2Id())) {
            List<I_ThinExtByRefVersioned> allExtensions = tf.getAllExtensionsForComponent(versionedRel.getC1Id());

            for (I_RelPart versionPart : versionedRel.getVersions()) {
                if (historyStatusRefsetMap.containsKey(versionPart.getTypeId())
                    && testSpecificationWithCache(versionPart.getPathId())) {

                    if (allExtensions.isEmpty()) {
                        UUID uuid = UUID.nameUUIDFromBytes(("org.dwfa." + tf.getUids(versionedRel.getC2Id()) + tf.getUids(historyStatusRefsetMap.get(versionPart.getTypeId()))).getBytes("8859_1"));

                        I_ThinExtByRefPartConcept part = createConceptExtension(versionedRel, versionPart);
                        export(part, uuid, historyStatusRefsetMap.get(versionPart.getTypeId()), versionedRel.getC1Id(),
                            TYPE.CONCEPT);
                    } else {
                        for (I_ThinExtByRefVersioned ext : allExtensions) {
                            if (ext.getRefsetId() == historyStatusRefsetMap.get(versionPart.getTypeId())) {
                                I_ThinExtByRefPartConcept part = (I_ThinExtByRefPartConcept) getVersion(
                                    ext.getTuples(allowedStatuses, positions, false, false), versionPart.getVersion()).getPart();

                                if (part == null) {
                                    part = createConceptExtension(versionedRel, versionPart);
                                }
                                export(part, getMemberUuid(ext.getMemberId(), ext.getComponentId(), ext.getRefsetId()),
                                    historyStatusRefsetMap.get(versionPart.getTypeId()), versionedRel.getC1Id(),
                                    TYPE.CONCEPT);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates a new I_ThinExtByRefPartConcept.
     *
     * @param versionedRel I_RelVersioned
     * @param versionPart I_RelPart
     * @return I_ThinExtByRefPartConceptConcept
     */
    private I_ThinExtByRefPartConcept createConceptExtension(I_RelVersioned versionedRel, I_RelPart versionPart) {
        I_ThinExtByRefPartConcept part = new ThinExtByRefPartConcept();
        part.setC1id(versionedRel.getC2Id());
        part.setPathId(versionPart.getPathId());
        part.setStatusId(versionPart.getStatusId());
        part.setVersion(versionPart.getVersion());
        return part;
    }

    /**
     * Get the extension for the version from the list of extensions.
     *
     * @param extensionList List of I_ThinExtByRefTuple
     * @param version int
     * @return I_ThinExtByRefTuple
     */
    private I_ThinExtByRefTuple getVersion(List<I_ThinExtByRefTuple> extensionList, int version) {
        I_ThinExtByRefTuple extByRefVersioned = null;
        for (I_ThinExtByRefTuple extByRefTuple : extensionList) {
            if (extByRefTuple.getVersion() == version) {
                extByRefVersioned = extByRefTuple;
                break;
            }
        }

        return extByRefVersioned;
    }

    /**
     * Create/update and export string extensions for all concepts that have
     * SCT3 ids.
     *
     * @param latest I_AmPart latest version of the concept
     * @param conceptId concept id
     * @throws Exception cannot create or export the concept.
     */
    private void exportCtv3IdMap(I_AmPart latest, int conceptId) throws Exception {
        I_ThinExtByRefTuple tuple = getCurrentExtension(conceptId, ctv3IdMapExtension);
        I_ThinExtByRefPartString part = (I_ThinExtByRefPartString) tuple;
        I_IdPart ctv3IdPart = getLatestVersion(tf.getConcept(conceptId).getId().getVersions(),
            ArchitectonicAuxiliary.Concept.CTV3_ID);
        if (ctv3IdPart != null) {
            if (part == null) {
                part = new ThinExtByRefPartString();// stunt extension part.
                part.setStringValue(ctv3IdPart.getSourceId().toString());
                part.setPathId(latest.getPathId());
                part.setStatusId(org.dwfa.cement.ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
                part.setVersion(latest.getVersion());
                export(part, null, ConceptConstants.CTV3_ID_MAP_EXTENSION.localize().getNid(), conceptId, TYPE.CONCEPT);
            } else if (part.getStringValue().equals(latest.getPartComponentNids())) {
                part.setStringValue(ctv3IdPart.getSourceId().toString());
                export((I_ThinExtByRefTuple) part, TYPE.CONCEPT);
            }
        }
    }

    /**
     * Create/update and export string extensions for all concepts that have
     * snomed ids.
     *
     * @param latest I_AmPart latest version of the concept
     * @param conceptId concept id
     * @throws Exception cannot create or export the concept.
     */
    private void exportSnomedIdMap(I_AmPart latest, int conceptId) throws Exception {
        I_ThinExtByRefTuple tuple = getCurrentExtension(conceptId, snomedIdMapExtension);
        I_ThinExtByRefPartString part = (I_ThinExtByRefPartString) tuple;
        I_IdPart snomedIdPart = getLatestVersion(tf.getConcept(conceptId).getId().getVersions(),
            ArchitectonicAuxiliary.Concept.SNOMED_RT_ID);
        if (snomedIdPart != null) {
            if (part == null) {
                part = new ThinExtByRefPartString();// stunt extension part.
                part.setStringValue(snomedIdPart.getSourceId().toString());
                part.setPathId(latest.getPathId());
                part.setStatusId(activeNId);
                part.setVersion(latest.getVersion());
                export(part, null, snomedIdMapExtension, conceptId, TYPE.CONCEPT);
            } else if (part.getStringValue().equals(latest.getPartComponentNids())) {
                part.setStringValue(snomedIdPart.getSourceId().toString());
                export((I_ThinExtByRefTuple) part, TYPE.CONCEPT);
            }
        }
    }

    /**
     * Export the relationships for the concepts latest version
     *
     * @param versionedRel the concept to export the relationships for.
     *
     * @throws Exception DB errors.
     */
    private void processRelationshipInactivation(I_RelVersioned versionedRel) throws Exception {
        if (testSpecification(versionedRel.getC2Id())) {
            boolean exportableVersionFound = false;
            I_RelPart latest = null;
            for (I_RelPart part : versionedRel.getVersions()) {
                if (checkPath(part.getPathId()) && allowedStatuses.contains(part.getStatusId())
                    && testSpecificationWithCache(part.getCharacteristicId())
                    && testSpecificationWithCache(part.getPathId())
                    && testSpecificationWithCache(part.getRefinabilityId())) {

                    exportableVersionFound = true;
                    if (latest == null || latest.getVersion() < part.getVersion()) {
                        latest = part;
                    }
                }
            }
            if (exportableVersionFound) {
                // found a valid version of this relationship for export
                int relId = versionedRel.getRelId();
                extractRelationshipRefinability(latest, relId);
                extractStatus(latest, relId, relationshipInactivationIndicator, TYPE.RELATIONSHIP);
            }
        }
    }

    /**
     * Export the descriptions for the concepts latest version
     *
     * @param versionedDesc the description to export the inactivation status
     *            for.
     *
     * @throws Exception DB errors.
     */
    private void processDescriptionInactivation(I_DescriptionVersioned versionedDesc) throws Exception {
        boolean exportableVersionFound = false;
        I_DescriptionPart latest = null;
        for (I_DescriptionPart part : versionedDesc.getVersions()) {
            if (checkPath(part.getPathId()) && allowedStatuses.contains(part.getStatusId())
                && testSpecificationWithCache(part.getTypeId())) {

                exportableVersionFound = true;
                if (latest == null || latest.getVersion() < part.getVersion()) {
                    latest = part;
                }

            }
        }

        if (exportableVersionFound) {
            int descId = versionedDesc.getDescId();
            extractStatus(latest, descId, descriptionInactivationIndicator, TYPE.DESCRIPTION);
        }
    }

    /**
     * Create/update and export status concept extensions for all concepts that
     * are not active, inactive
     * or current as we want to know why they are active, inactive or current.
     *
     * @param latest I_AmPart latest version of the concept
     * @param relId concept id
     * @throws Exception cannot create or export the concept.
     */
    private void extractStatus(I_AmPart latest, int relId, int inactivationRefset, TYPE type) throws Exception {
        I_ThinExtByRefTuple tuple = getCurrentExtension(relId, inactivationRefset);
        I_ThinExtByRefPartConcept part = (I_ThinExtByRefPartConcept) tuple;
        int rf2InactiveStatus = getRf2Status(latest.getStatusId());
        if (rf2InactiveStatus != -1 && part == null) {
            // if the status is INACTIVE or ACTIVE there is no need for a
            // reason. For simplicity, CURRENT will be treated this way too,
            if (latest.getStatusId() != activeNId && latest.getStatusId() != inActiveNId
                && latest.getStatusId() != currentNId) {
                // no extension at all
                // part = tf.newExtensionPart(ThinExtByRefPartConcept.class);
                part = new ThinExtByRefPartConcept();// stunt extension part.
                part.setC1id(rf2InactiveStatus);
                part.setPathId(latest.getPathId());
                part.setStatusId(activeNId);
                part.setVersion(latest.getVersion());
                export(part, null, inactivationRefset, relId, type);
            }
        } else if (rf2InactiveStatus != -1 && part.getC1id() != latest.getStatusId()) {
            // add a new row with the latest refinability
            part.setC1id(latest.getStatusId());
            export((I_ThinExtByRefTuple) part, type);
        }
    }

    /**
     * covert the snomed CT component status to rf2 meta-data status
     *
     * @param statusId int
     * @return int rf2 status or -1 if no map found.
     */
    private int getRf2Status(int statusId) {
        int rf2Status = -1;

        if (statusId == aceDuplicateStatusNId) {
            rf2Status = duplicateStatusNId;
        } else if (statusId == aceAmbiguousStatusNId) {
            rf2Status = ambiguousStatusNId;
        } else if (statusId == aceErroneousStatusNId) {
            rf2Status = erroneousStatusNId;
        } else if (statusId == aceOutdatedStatusNId) {
            rf2Status = outdatedStatusNId;
        } else if (statusId == aceInappropriateStatusNId) {
            rf2Status = inappropriateStatusNId;
        } else if (statusId == aceMovedElsewhereStatusNId) {
            rf2Status = movedElsewhereStatusNId;
        }

        return rf2Status;
    }

    /**
     * Create/update and export concept extensions (relationships refinability
     * reference) for all concepts.
     *
     * @param latest I_AmPart latest version of the concept
     * @param relId concept id
     * @throws Exception cannot create or export the concept.
     */
    private void extractRelationshipRefinability(I_RelPart latest, int relId) throws Exception {
        I_ThinExtByRefTuple tuple = getCurrentExtension(relId, relationshipRefinabilityExtension);
        I_ThinExtByRefPartConcept part = (I_ThinExtByRefPartConcept) tuple;
        if (part == null) {
            // no extension at all
            part = new ThinExtByRefPartConcept();// stunt extension part.
            part.setC1id(latest.getRefinabilityId());
            part.setPathId(latest.getPathId());
            part.setStatusId(activeNId);
            part.setVersion(latest.getVersion());
            export(part, null, relationshipRefinability, relId, TYPE.RELATIONSHIP);
        } else if (part.getC1id() != latest.getRefinabilityId()) {
            // add a new row with the latest refinability
            part.setC1id(latest.getRefinabilityId());
            export((I_ThinExtByRefTuple) tuple, TYPE.RELATIONSHIP);
        }
    }

    /**
     * Get the latest version for the list of id parts with the source
     * <code>sourceConcept</code>
     *
     * @param sourceConcept Concept eg SNOMED_T3_UUID, SNOMED_INT_ID etc
     * @return I_IdPart latest Id version for the sourceConcept.
     * @throws IOException DB errors
     * @throws TerminologyException DB errors
     */
    private I_IdPart getLatestVersion(List<I_IdPart> idParts, Concept sourceConcept) throws TerminologyException,
            IOException {
        I_IdPart latestVersion = null;

        for (I_IdPart iIdPart : idParts) {
            if (iIdPart.getSource() == tf.uuidToNative(sourceConcept.getUids())
                && (latestVersion == null || iIdPart.getVersion() > latestVersion.getVersion())) {
                latestVersion = iIdPart;
            }
        }

        return latestVersion;
    }

    synchronized boolean testSpecificationWithCache(int id) throws TerminologyException, IOException, Exception {
        Boolean result = testSpecCache.get(id);
        if (result == null) {
            result = testSpecification(id);
            testSpecCache.put(id, result);
        }
        return result;
    }

    /**
     * Gets the latest extension for the concept and refset id.
     *
     * Extension are filtered by the <code>allowedStatuses</code> and
     * <code>positions</code> lists.
     *
     * @param componentId refset member concept
     * @param relationshipRefinabilityExtension refset.
     * @return I_ThinExtByRefTuple the latest extension, may be null
     * @throws IOException DB error
     * @throws TerminologyException
     */
    I_ThinExtByRefTuple getCurrentExtension(int componentId, int refsetId) throws IOException, TerminologyException {
        I_ThinExtByRefTuple latest = null;
        for (I_ThinExtByRefVersioned ext : tf.getAllExtensionsForComponent(componentId)) {
            if (ext.getRefsetId() == refsetId) {
                for (I_ThinExtByRefTuple tuple : ext.getTuples(allowedStatuses, positions, false, false)) {
                    if (latest == null || latest.getVersion() < tuple.getVersion()) {
                        latest = tuple;
                    }
                }
            }
        }
        return latest;
    }

    /**
     * Exports the refset to file.
     *
     * @param thinExtByRefTuple The concept extension to write to file.
     *
     * @throws Exception on DB or file error.
     */
    private void exportRefsets(int componentId, TYPE type) throws TerminologyException, Exception {
        List<I_ThinExtByRefVersioned> extensions = tf.getAllExtensionsForComponent(componentId);
        for (I_ThinExtByRefVersioned thinExtByRefVersioned : extensions) {
            if (testSpecificationWithCache(thinExtByRefVersioned.getRefsetId())) {
                if (!isLive(thinExtByRefVersioned) && !isActive(thinExtByRefVersioned.getLatestVersion().getStatusId())) {
                    // move on to the next extension - don't export
                    continue;
                }

                for (I_ThinExtByRefTuple thinExtByRefTuple : thinExtByRefVersioned.getTuples(allowedStatuses,
                    positions, false, false)) {
                    export(thinExtByRefTuple, type);
                }
            }
        }
    }

    void export(I_ThinExtByRefTuple thinExtByRefTuple, TYPE type) throws Exception {
        UUID memberUuid = getMemberUuid(thinExtByRefTuple.getMemberId(), thinExtByRefTuple.getComponentId(),
            thinExtByRefTuple.getRefsetId());

        export(thinExtByRefTuple.getPart(), memberUuid, thinExtByRefTuple.getRefsetId(),
            thinExtByRefTuple.getComponentId(), type);
    }

    /**
     * Exports the refset to file.
     *
     * @param thinExtByRefPart The concept extension to write to file.
     * @param memberUuid the id for this refset member record.
     * @param refsetId the refset id
     * @param componentId the referenced component
     * @throws Exception on DB errors or file write errors.
     */
    void export(I_ThinExtByRefPart thinExtByRefPart, UUID memberUuid, int refsetId, int componentId, TYPE type)
            throws Exception {

        RefsetType refsetType;
        synchronized (refsetTypeMap) {
            refsetType = refsetTypeMap.get(refsetId);
            if (refsetType == null) {
                try {
                    refsetType = RefsetType.findByExtension(thinExtByRefPart);
                } catch (EnumConstantNotPresentException e) {
                    getLog().warn(
                        "No handler for tuple " + thinExtByRefPart + " of type " + thinExtByRefPart.getClass(), e);
                    return;
                }
                refsetTypeMap.put(refsetId, refsetType);
            }
        }

        BufferedWriter uuidRefsetWriter = null;
        if (!useRF2) {
            uuidRefsetWriter = getUuidRefsetWriter(refsetId);
        }
        BufferedWriter sctIdRefsetWriter = getSctIdWriter(refsetId);

        if (memberUuid == null) {
            memberUuid = UUID.nameUUIDFromBytes(("org.dwfa." + tf.getUids(componentId) + tf.getUids(refsetId)).getBytes("8859_1"));
        }
        writeToSctIdFile(sctIdRefsetWriter, thinExtByRefPart, memberUuid, refsetId, componentId, type);

        if (!useRF2) {
            writeToUuidFile(uuidRefsetWriter, thinExtByRefPart, memberUuid, refsetId, componentId);
        }
        first = false;
    }

    /**
     * Is an extension "live", that is has the extension ever been
     * released/exported before.
     */
    private boolean isLive(I_ThinExtByRefVersioned extension) {

        // TODO
        // This being developed for a first release this method is being
        // jury-rigged to save development effort.
        // In subsequent releases we will need to look at the parts contain in
        // the extension to identify any that
        // are on a previous "public" release path.

        return false;
    }

    /**
     * Gets the id file for the reference set type.
     *
     * @param refsetId int
     * @return AceIdentifierWriter for the clinical or structural refset
     */
    private AceIdentifierWriter getAceIdentifierFile(int refsetId) {
        AceIdentifierWriter aceIdentifierFile = aceIdentifierWriter;

        if (isStructuralRefset(refsetId)) {
            aceIdentifierFile = aceStructuralIdentifierWriter;
        }

        return aceIdentifierFile;
    }

    /**
     * Is the reference set clinical or structural
     *
     * @param refsetId int
     * @return true if reference set is structural
     */
    boolean isStructuralRefset(int refsetId) {
        boolean isStructuralRefset = refsetId == ctv3IdMapExtension || refsetId == snomedIdMapExtension
            || refsetId == relationshipRefinability || refsetId == relationshipRefinabilityExtension
            || refsetId == descriptionInactivationIndicator || refsetId == relationshipInactivationIndicator
            || refsetId == conceptInactivationIndicator;
        return isStructuralRefset;
    }

    synchronized private void writeToSctIdFile(BufferedWriter sctIdRefsetWriter, I_ThinExtByRefPart thinExtByRefPart,
            UUID memberUuid, int refsetId, int componentId, TYPE type) throws IOException, TerminologyException,
            InstantiationException, IllegalAccessException, Exception {
        if (useRF2) {
            refsetTypeMap.get(refsetId).getRefsetHandler().setAceIdentifierFile(getAceIdentifierFile(refsetId));
            sctIdRefsetWriter.write(refsetTypeMap.get(refsetId).getRefsetHandler().formatRefsetLineRF2(tf,
                thinExtByRefPart, memberUuid, refsetId, componentId, true, useRF2, type));
        } else {
            sctIdRefsetWriter.write(refsetTypeMap.get(refsetId).getRefsetHandler().formatRefsetLine(tf,
                thinExtByRefPart, memberUuid, refsetId, componentId, true, useRF2));
        }
        sctIdRefsetWriter.write(newLineChars);
    }

    synchronized private void writeToUuidFile(BufferedWriter uuidRefsetWriter, I_ThinExtByRefPart thinExtByRefPart,
            UUID memberUuid, int refsetId, int componentId) throws IOException, TerminologyException,
            InstantiationException, IllegalAccessException, Exception {
        uuidRefsetWriter.write(refsetTypeMap.get(refsetId).getRefsetHandler().formatRefsetLine(tf, thinExtByRefPart,
            memberUuid, refsetId, componentId, false, useRF2));
        uuidRefsetWriter.write(newLineChars);
    }

    synchronized private BufferedWriter getSctIdWriter(int refsetId) throws Exception {
        BufferedWriter sctIdRefsetWriter = writerMap.get(refsetId + "SCTID");

        if (sctIdRefsetWriter == null) {
            I_GetConceptData refsetConcept = tf.getConcept(refsetId);
            String refsetName = getPreferredTerm(refsetConcept);
            refsetName = refsetName.replace("/", "-");
            refsetName = refsetName.replace("'", "_");

            if (useRF2) {
                refsetName = convertToCamelCase(refsetName);

                String sctIdFilePrefix = "der2_cRefset_";
                String fileName = refsetName + rf2Descriptor.getContentSubType() + "_" + rf2Descriptor.getCountryCode()
                    + rf2Descriptor.getNamespace() + "_" + releaseVersion + ".txt";
                if (isStructuralRefset(refsetId)) {
                    sctIdRefsetWriter = new BufferedWriter(new FileWriter(new File(
                        sctidStructuralRefsetOutputDirectory, sctIdFilePrefix + fileName)));
                } else {
                    sctIdRefsetWriter = new BufferedWriter(new FileWriter(new File(sctidRefsetOutputDirectory,
                        sctIdFilePrefix + fileName)));
                }
                sctIdRefsetWriter.write(refsetTypeMap.get(refsetId).getRefsetHandler().getRF2HeaderLine());
            } else {
                sctIdRefsetWriter = new BufferedWriter(new FileWriter(new File(sctidRefsetOutputDirectory, "SCTID_"
                    + refsetName + "_" + releaseVersion + ".txt")));
                sctIdRefsetWriter.write(refsetTypeMap.get(refsetId).getRefsetHandler().getHeaderLine());
            }
            sctIdRefsetWriter.write(newLineChars);
            writerMap.put(refsetId + "SCTID", sctIdRefsetWriter);
        }

        return sctIdRefsetWriter;
    }

    synchronized private BufferedWriter getUuidRefsetWriter(int refsetId) throws Exception {
        BufferedWriter uuidRefsetWriter = writerMap.get(refsetId + "UUID");

        if (uuidRefsetWriter == null) {
            I_GetConceptData refsetConcept = tf.getConcept(refsetId);
            String refsetName = getPreferredTerm(refsetConcept);
            refsetName = refsetName.replace("/", "-");
            refsetName = refsetName.replace("'", "_");

            uuidRefsetWriter = new BufferedWriter(new FileWriter(new File(uuidRefsetOutputDirectory, "UUID_"
                + refsetName + "_" + releaseVersion + ".txt")));

            uuidRefsetWriter.write(refsetTypeMap.get(refsetId).getRefsetHandler().getHeaderLine());
            uuidRefsetWriter.write(newLineChars);

            writerMap.put(refsetId + "UUID", uuidRefsetWriter);
        }

        return uuidRefsetWriter;
    }

    /**
     * Camel case with first letter capped.
     *
     * @param string String
     *
     * @return String
     */
    private String convertToCamelCase(String string) {
        StringBuffer sb = new StringBuffer();
        String[] str = string.split(" ");
        for (String temp : str) {
            sb.append(Character.toUpperCase(temp.charAt(0)));
            sb.append(temp.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    /**
     * Gets the release version for the path concept.
     *
     * @param refsetConcept path refset concept.
     * @return String release preferred term.
     * @throws Exception DB error
     */
    protected String getReleaseVersion(I_GetConceptData refsetConcept) throws Exception {

        if (pathReleaseVersions.containsKey(refsetConcept.getConceptId())) {
            return pathReleaseVersions.get(refsetConcept.getConceptId());
        } else {
            int pathid = getLatestAttributePart(refsetConcept).getPathId();

            String pathUuidStr = Integer.toString(pathid);
            try {
                String pathVersion = null;
                pathUuidStr = tf.getUids(pathid).iterator().next().toString();

                int pathVersionRefsetNid = tf.uuidToNative(org.dwfa.ace.refset.ConceptConstants.PATH_VERSION_REFSET.getUuids()[0]);
                int currentStatusId = tf.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
                for (I_ThinExtByRefVersioned extension : tf.getAllExtensionsForComponent(pathid)) {
                    if (extension.getRefsetId() == pathVersionRefsetNid) {
                        I_ThinExtByRefPart latestPart = getLatestVersion(extension);
                        if (latestPart.getStatusId() == currentStatusId) {

                            if (pathVersion != null) {
                                throw new TerminologyException("Concept contains multiple extensions for refset"
                                    + org.dwfa.ace.refset.ConceptConstants.PATH_VERSION_REFSET.getDescription());
                            }

                            pathVersion = ((I_ThinExtByRefPartString) latestPart).getStringValue();
                        }
                    }
                }

                if (pathVersion == null) {
                    throw new TerminologyException("Concept not a member of "
                        + org.dwfa.ace.refset.ConceptConstants.PATH_VERSION_REFSET.getDescription());
                }

                String releaseVersion = getPreferredTerm(tf.getConcept(pathid)) + "_" + pathVersion;
                pathReleaseVersions.put(refsetConcept.getConceptId(), releaseVersion);
                return releaseVersion;

            } catch (Exception e) {
                throw new RuntimeException("Failed to obtain the release version for the path " + pathUuidStr, e);
            }
        }
    }

    /**
     * Gets the latest version for this extension.
     *
     * @param extension I_ThinExtByRefVersioned
     * @return I_ThinExtByRefPart latest version. may be null
     */
    private I_ThinExtByRefPart getLatestVersion(I_ThinExtByRefVersioned extension) {
        I_ThinExtByRefPart latestPart = null;
        for (I_ThinExtByRefPart part : extension.getVersions()) {
            if (latestPart == null || part.getVersion() >= latestPart.getVersion()) {
                latestPart = part;
            }
        }
        return latestPart;
    }

    /**
     * Does the concept match the <code>exportSpecifications</code>
     *
     * @param concept I_GetConceptData
     * @return true if a matching concept.
     * @throws Exception DB error
     */
    boolean testSpecification(I_GetConceptData concept) throws Exception {
        for (ExportSpecification spec : exportSpecifications) {
            if (spec.test(concept) && getLatestAttributePart(concept) != null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Does the concept match the <code>exportSpecifications</code>
     *
     * @param id concept it
     * @return true if a matching concept.
     * @throws TerminologyException DB error
     * @throws IOException DB error
     * @throws Exception DB error
     */
    boolean testSpecification(int id) throws TerminologyException, IOException, Exception {
        return testSpecification(tf.getConcept(id));
    }

    /**
     * Is the path id in the list of <code>positions</code>
     *
     * @param pathId int
     * @return true if pathId in <code>positions</code> list
     */
    boolean checkPath(int pathId) {
        return positionIds.contains(pathId);
    }

    /**
     * THIS IS A HACK REMOVE.
     *
     * Get the namespace for the I_Path.
     *
     * @param forPath I_path
     * @return NAMESPACE
     */
    private NAMESPACE getNamespace(I_Path forPath) {
        NAMESPACE namespace = NAMESPACE.NEHTA;

        if (forPath != null && forPath.toString().equals("SNOMED Core")) {
            namespace = NAMESPACE.SNOMED_META_DATA;
        }

        return namespace;
    }

    private UUID getMemberUuid(Integer memberNid, int componentNid, int refsetNid) throws UnsupportedEncodingException,
            TerminologyException, IOException {
        UUID uuid;
        if (memberNid == null) {
            // generate new id
            uuid = UUID.nameUUIDFromBytes(("org.dwfa." + tf.getUids(componentNid) + tf.getUids(refsetNid)).getBytes("8859_1"));
        } else {
            if (tf.getUids(memberNid) == null) {
                logger.warning("No UUID for member, component and refset ids.");
                uuid = UUID.nameUUIDFromBytes(("org.dwfa." + tf.getUids(componentNid) + tf.getUids(refsetNid)).getBytes("8859_1"));
            } else {
                uuid = tf.getUids(memberNid).iterator().next();
            }
        }
        return uuid;
    }

    /**
     * Gets the concepts preferred term filtered by <code>statusSet</code>
     * sorted by <code>TYPE_B4_LANG</code>
     *
     * @param conceptData I_GetConceptData to get the preferred term for
     * @return String preferred term
     * @throws Exception DB error
     */
    String getPreferredTerm(I_GetConceptData conceptData) throws Exception {
        I_IntList descTypeList = tf.newIntList();
        descTypeList.add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());

        I_IntSet statusSet = tf.newIntSet();
        statusSet.add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
        statusSet.add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
        statusSet.add(ArchitectonicAuxiliary.Concept.CURRENT_UNREVIEWED.localize().getNid());
        statusSet.add(ArchitectonicAuxiliary.Concept.READY_TO_PROMOTE.localize().getNid());
        statusSet.add(ArchitectonicAuxiliary.Concept.PROMOTED.localize().getNid());

        I_DescriptionTuple descTuple = conceptData.getDescTuple(descTypeList, null, statusSet, positions,
            LANGUAGE_SORT_PREF.TYPE_B4_LANG);
        if (descTuple == null) {
            getLog().info("Cannot find Preferred Term using status and poistions, any current Preferred Term");
            descTuple = conceptData.getDescTuple(descTypeList, null, statusSet, null, LANGUAGE_SORT_PREF.TYPE_B4_LANG);
            if (descTuple == null) {
                getLog().info("Cannot find Preferred Term using status and poistions, any Preferred Term");
                descTuple = conceptData.getDescTuple(descTypeList, null, null, null, LANGUAGE_SORT_PREF.TYPE_B4_LANG);
            }
        }

        return descTuple.getText();
    }

    private synchronized void updateConceptsToProcessCount(int amount) {
        conceptsToProcessCount += amount;
    }

    private synchronized int getConceptsToProcessCount() {
        return conceptsToProcessCount;
    }

    private class ConceptProcessor implements Runnable {
        private List<I_GetConceptData> conceptsToProcess = new ArrayList<I_GetConceptData>();

        /**
         * Default constructor
         *
         * Doesn't update latch.
         */
        ConceptProcessor() {
        }

        ConceptProcessor(List<I_GetConceptData> conceptsToProcess) {
            this.conceptsToProcess = new ArrayList<I_GetConceptData>(conceptsToProcess);
        }

        @Override
        public void run() {
            try {
                for (I_GetConceptData conceptToProcess : conceptsToProcess) {
                    processConcept(conceptToProcess);
                    updateConceptsToProcessCount(-1);

                    if (getConceptsToProcessCount() % 1000 == 0) {
                        logger.info(getConceptsToProcessCount() + " Concepts left to process.");
                    }
                    if (getConceptsToProcessCount() == 0) {
                        doneSignal.countDown();
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        void processConcept(I_GetConceptData concept) throws Exception {
            if (testSpecification(concept)) {

                I_ConceptAttributePart latest = getLatestAttributePart(concept);
                if (latest == null) {
                    getLog().warn(
                        "Concept " + concept + " is exportable for specification " + exportSpecifications
                            + " but has no parts valid for statuses " + allowedStatuses + " and positions " + positions);
                    return;
                }

                exportCtv3IdMap(latest, concept.getConceptId());
                exportSnomedIdMap(latest, concept.getConceptId());
                extractStatus(latest, concept.getConceptId(), conceptInactivationIndicator, TYPE.CONCEPT);
                for (I_RelVersioned rel : concept.getSourceRels()) {
                    processRelationshipInactivation(rel);
                    exportConceptHistory(rel);
                }
                for (I_DescriptionVersioned desc : concept.getDescriptions()) {
                    processDescriptionInactivation(desc);
                }

                if (!tf.getAllExtensionsForComponent(concept.getConceptId()).isEmpty()) {
                    exportRefsets(concept.getConceptId(), TYPE.CONCEPT);
                    for (I_RelVersioned rel : concept.getSourceRels()) {
                        processRelationship(rel);
                    }

                    for (I_DescriptionVersioned desc : concept.getDescriptions()) {
                        processDescription(desc);
                    }
                }
            }
        }
    }

    /**
     * Is the status ACTIVE or a child of ACTIVE?
     *
     * @param statusId The status to evaluate
     * @return True is considered an ACTIVE status type
     * @throws IOException
     * @throws TerminologyException
     */
    private boolean isActive(int statusId) throws IOException, TerminologyException {
        return (statusId == activeNId || activeConcept.isParentOf(tf.getConcept(statusId), false));
    }
}
