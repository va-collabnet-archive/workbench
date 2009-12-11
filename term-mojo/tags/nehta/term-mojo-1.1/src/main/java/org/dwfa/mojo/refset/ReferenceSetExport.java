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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.dwfa.mojo.ConceptConstants;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.PositionDescriptor;
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
     * Directory where the fixed SCTID map is located
     *
     * @parameter
     * @required
     */
    File fixedMapDirectory;

    /**
     * Directory where the read/write SCTID maps are stored
     *
     * @parameter
     * @required
     */
    File readWriteMapDirectory;

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
     * Batch processing size - defaults to 30
     * @parameter
     */
    private int batchSize = 30;

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


    String newLineChars = "\r\n";

    /** thread pool for processing concepts. */
    ProcessQueue workQueue = new ProcessQueue(numberOfThreads);

    /** This is need to setup the database */
    boolean first = true;

    int refsetDescriptorType;

    /**
     * Number of processed concepts.
     */
    private int processedConceptsCount = 0;

    private List<I_GetConceptData> currentBatch = new ArrayList<I_GetConceptData>();

    private Map<Integer, Boolean> testSpecCache = new HashMap<Integer, Boolean>();

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            activeNId = org.dwfa.cement.ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid();
            inActiveNId = org.dwfa.cement.ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid();
            currentNId = org.dwfa.cement.ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid();
            relationshipRefinability = ConceptConstants.RELATIONSHIP_REFINABILITY_EXTENSION.localize().getNid();
            relationshipRefinabilityExtension = ConceptConstants.RELATIONSHIP_REFINABILITY_EXTENSION.localize().getNid();
            descriptionInactivationIndicator = ConceptConstants.DESCRIPTION_INACTIVATION_INDICATOR.localize().getNid();
            relationshipInactivationIndicator = ConceptConstants.RELATIONSHIP_INACTIVATION_INDICATOR.localize().getNid();
            conceptInactivationIndicator = ConceptConstants.CONCEPT_INACTIVATION_INDICATOR.localize().getNid();
            ctv3IdMapExtension = ConceptConstants.CTV3_ID_MAP_EXTENSION.localize().getNid();
            snomedIdMapExtension = ConceptConstants.SNOMED_ID_MAP_EXTENSION.localize().getNid();
        } catch (Exception e) {
            throw new MojoExecutionException("", e);
        }


        if (!fixedMapDirectory.exists() || !fixedMapDirectory.isDirectory() || !fixedMapDirectory.canRead()) {
            throw new MojoExecutionException("Cannot proceed, fixedMapDirectory must exist and be readable");
        }

        if (!readWriteMapDirectory.exists() || !readWriteMapDirectory.isDirectory() || !readWriteMapDirectory.canRead()) {
            throw new MojoExecutionException("Cannot proceed, readWriteMapDirectory must exist and be readable");
        }

        try {
            refsetDescriptorType = ConceptConstants.REFSET_DESCRIPTOR_TYPE.localize().getNid();
            allowedStatuses = tf.newIntSet();
            positions = new HashSet<I_Position>();
            for (ExportSpecification spec : exportSpecifications) {
                for (PositionDescriptor pd : spec.getPositionsForExport()) {
                    positions.add(pd.getPosition());
                }
                for (ConceptDescriptor status : spec.getStatusValuesForExport()) {
                    allowedStatuses.add(status.getVerifiedConcept().getConceptId());
                }
            }

            sctidRefsetOutputDirectory.mkdirs();
            uuidRefsetOutputDirectory.mkdirs();

            MemberRefsetHandler.setFixedMapDirectory(fixedMapDirectory);
            MemberRefsetHandler.setReadWriteMapDirectory(readWriteMapDirectory);
            if (rf2Descriptor != null && rf2Descriptor.getModule() != null) {
                MemberRefsetHandler.setModule(rf2Descriptor.getModule());
            }

            tf.iterateConcepts(this);

            while (!workQueue.isEmpty()) {
                Thread.sleep(5000);
            }

//          Add in after R1.0.
//            if (useRF2) {
//                HashMap<Integer, RefsetType> refsetTypeMapCopy = new HashMap<Integer, RefsetType>();
//                refsetTypeMapCopy.putAll(refsetTypeMap);
//                for (Integer refsetId : refsetTypeMapCopy.keySet()) {
//                    exportRefsetDescription(refsetId, refsetTypeMapCopy.get(refsetId));
//                }
//            }

            for (BufferedWriter writer : writerMap.values()) {
                writer.close();
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
            export(part, null, ConceptConstants.REFSET_DESCRIPTOR_TYPE.localize().getNid(), refsetId);
        } else if (part.getC1id() != latest.getVersion()) {
            // add a new row with the latest refinability
            part.setC1id(latest.getVersion());
            export((I_ThinExtByRefTuple) tuple);
        }

    }

    public void processConcept(I_GetConceptData concept) throws Exception {
        if (currentBatch.size() < batchSize ) {
            currentBatch.add(concept);
        } else {
            if(first){
                new ConceptProcessor(currentBatch).run();
            } else {
                workQueue.execute(new ConceptProcessor(currentBatch));
            }
            currentBatch.clear();
        }
    }

    /**
     * Gets the latest attribute for the concept.
     *
     * Attributes are filtered by the <code>allowedStatuses</code> and <code>positions</code> lists.
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
            if (testSpecificationWithCache(part.getTypeId()) && allowedStatuses.contains(part.getStatusId())) {

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
            exportRefsets(descId);

            // TODO commented out because it costs too many SCTIDs and we need
            // to release pathology - to be included later
            // extractStatus(latest, descId);
        }
    }

    @SuppressWarnings("deprecation")
    private void processRelationship(I_RelVersioned versionedRel) throws Exception {
        if (testSpecification(versionedRel.getC2Id())) {
            boolean exportableVersionFound = false;
            I_RelPart latest = null;
            for (I_RelPart part : versionedRel.getVersions()) {
                if (checkPath(part.getPathId())
                        && allowedStatuses.contains(part.getStatusId())
                        && testSpecificationWithCache(part.getRelTypeId())
                        && testSpecificationWithCache(part.getPathId())
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
                exportRefsets(relId);

                // TODO commented out because it costs too many SCTIDs and we
                // need to release pathology - to be included later
                // extractRelationshipRefinability(latest, relId);
                // extractStatus(latest, relId);
            }
        }
    }

    /**
     * Create/update and export string extensions for all concepts that have SCT3 ids.
     *
     * @param latest I_AmPart latest version of the concept
     * @param conceptId concept id
     * @throws Exception cannot create or export the concept.
     */
    private void exportCtv3IdMap(I_AmPart latest, int conceptId) throws Exception {
        I_ThinExtByRefTuple tuple = getCurrentExtension(conceptId, ctv3IdMapExtension);
        I_ThinExtByRefPartString part = (I_ThinExtByRefPartString) tuple;
        I_IdPart ctv3IdPart =
                getLatestVersion(tf.getConcept(conceptId).getId().getVersions(), ArchitectonicAuxiliary.Concept.CTV3_ID);
        if(ctv3IdPart != null){
            if (part == null) {
                part = new ThinExtByRefPartString();//stunt extension part.
                part.setStringValue(ctv3IdPart.getSourceId().toString());
                part.setPathId(latest.getPathId());
                part.setStatusId(org.dwfa.cement.ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
                part.setVersion(latest.getVersion());
                export(part, null, ConceptConstants.CTV3_ID_MAP_EXTENSION.localize().getNid(), conceptId);
            } else if (part.getStringValue().equals(latest.getPartComponentNids())) {
                part.setStringValue(ctv3IdPart.getSourceId().toString());
                export((I_ThinExtByRefTuple) part);
            }
        }
    }

    /**
     * Create/update and export string extensions for all concepts that have snomed ids.
     *
     * @param latest I_AmPart latest version of the concept
     * @param conceptId concept id
     * @throws Exception cannot create or export the concept.
     */
    private void exportSnomedIdMap(I_AmPart latest, int conceptId) throws Exception {
        I_ThinExtByRefTuple tuple = getCurrentExtension(conceptId, snomedIdMapExtension);
        I_ThinExtByRefPartString part = (I_ThinExtByRefPartString) tuple;
        I_IdPart snomedIdPart =
                getLatestVersion(tf.getConcept(conceptId).getId().getVersions(),
                    ArchitectonicAuxiliary.Concept.SNOMED_INT_ID);
        if(snomedIdPart != null){
            if (part == null) {
                part = new ThinExtByRefPartString();//stunt extension part.
                part.setStringValue(snomedIdPart.getSourceId().toString());
                part.setPathId(latest.getPathId());
                part.setStatusId(org.dwfa.cement.ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
                part.setVersion(latest.getVersion());
                export(part, null, ConceptConstants.SNOMED_ID_MAP_EXTENSION.localize().getNid(), conceptId);
            } else if (part.getStringValue().equals(latest.getPartComponentNids())) {
                part.setStringValue(snomedIdPart.getSourceId().toString());
                export((I_ThinExtByRefTuple) part);
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
                if (checkPath(part.getPathId())
                        && allowedStatuses.contains(part.getStatusId())
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
                extractStatus(latest, relId, relationshipInactivationIndicator);
            }
        }
    }

    /**
     * Export the descriptions for the concepts latest version
     *
     * @param versionedDesc the description to export the inactivation status for.
     *
     * @throws Exception DB errors.
     */
    private void processDescriptionInactivation(I_DescriptionVersioned versionedDesc) throws Exception {
        boolean exportableVersionFound = false;
        I_DescriptionPart latest = null;
        for (I_DescriptionPart part : versionedDesc.getVersions()) {
            if (checkPath(part.getPathId())
                    && allowedStatuses.contains(part.getStatusId())
                    && testSpecificationWithCache(part.getTypeId())) {

                exportableVersionFound = true;
                if (latest == null || latest.getVersion() < part.getVersion()) {
                    latest = part;
                }

            }
        }

        if (exportableVersionFound) {
            int descId = versionedDesc.getDescId();
            extractStatus(latest, descId, descriptionInactivationIndicator);
        }
    }

    /**
     * Create/update and export status concept extensions for all concepts that are not active, inactive
     * or current as we want to know why they are active, inactive or current.
     *
     * @param latest I_AmPart latest version of the concept
     * @param relId concept id
     * @throws Exception cannot create or export the concept.
     */
    private void extractStatus(I_AmPart latest, int relId, int inactivationRefset) throws Exception {
        I_ThinExtByRefTuple tuple = getCurrentExtension(relId, inactivationRefset);
        I_ThinExtByRefPartConcept part = (I_ThinExtByRefPartConcept) tuple;
        if (part == null) {
            // if the status is INACTIVE or ACTIVE there is no need for a
            // reason. For simplicity, CURRENT will be treated this way too,
            if (latest.getStatusId() != activeNId
                && latest.getStatusId() != inActiveNId
                && latest.getStatusId() != currentNId) {
                // no extension at all
                //part = tf.newExtensionPart(ThinExtByRefPartConcept.class);
                part = new ThinExtByRefPartConcept();//stunt extension part.
                part.setC1id(latest.getStatusId());
                part.setPathId(latest.getPathId());
                part.setStatusId(activeNId);
                part.setVersion(latest.getVersion());
                export(part, null, inactivationRefset, relId);
            }
        } else if (part.getC1id() != latest.getStatusId()) {
            // add a new row with the latest refinability
            part.setC1id(latest.getStatusId());
            export((I_ThinExtByRefTuple) part);
        }
    }

    /**
     * Create/update and export concept extensions (relationships refinability reference) for all concepts.
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
            part = new ThinExtByRefPartConcept();//stunt extension part.
            part.setC1id(latest.getRefinabilityId());
            part.setPathId(latest.getPathId());
            part.setStatusId(activeNId);
            part.setVersion(latest.getVersion());
            export(part, null, relationshipRefinability, relId);
        } else if (part.getC1id() != latest.getRefinabilityId()) {
            // add a new row with the latest refinability
            part.setC1id(latest.getRefinabilityId());
            export((I_ThinExtByRefTuple) tuple);
        }
    }

    /**
     * Get the latest version for the list of id parts with the source <code>sourceConcept</code>
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
     * Extension are filtered by the <code>allowedStatuses</code> and <code>positions</code> lists.
     *
     * @param componentId refset member concept
     * @param relationshipRefinabilityExtension refset.
     * @return I_ThinExtByRefTuple the latest extension, may be null
     * @throws IOException DB error
     * @throws TerminologyException
     */
    I_ThinExtByRefTuple getCurrentExtension(int componentId, int refsetId)
            throws IOException, TerminologyException {
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
    private void exportRefsets(int componentId) throws TerminologyException, Exception {
        List<I_ThinExtByRefVersioned> extensions = tf.getAllExtensionsForComponent(componentId);
        for (I_ThinExtByRefVersioned thinExtByRefVersioned : extensions) {
            if (testSpecificationWithCache(thinExtByRefVersioned.getRefsetId())) {
                for (I_ThinExtByRefTuple thinExtByRefTuple : thinExtByRefVersioned.getTuples(allowedStatuses,
                    positions, false, false)) {
                    export(thinExtByRefTuple);
                }
            }
        }
    }

    void export(I_ThinExtByRefTuple thinExtByRefTuple) throws Exception {
        export(thinExtByRefTuple.getPart(), thinExtByRefTuple.getMemberId(), thinExtByRefTuple.getRefsetId(),
            thinExtByRefTuple.getComponentId());
    }

    /**
     * Exports the refset to file.
     *
     * @param thinExtByRefPart The concept extension to write to file.
     * @param memberId the id for this refset member record.
     * @param refsetId the refset id
     * @param componentId the referenced component
     * @throws Exception on DB errors or file write errors.
     */
    void export(I_ThinExtByRefPart thinExtByRefPart, Integer memberId, int refsetId, int componentId)
            throws Exception {

        RefsetType refsetType;
        synchronized (refsetTypeMap) {
            refsetType = refsetTypeMap.get(refsetId);
            if (refsetType == null) {
                try {
                    refsetType = RefsetType.findByExtension(thinExtByRefPart);
                } catch (EnumConstantNotPresentException e) {
                    getLog()
                        .warn("No handler for tuple " + thinExtByRefPart + " of type " + thinExtByRefPart.getClass(), e);
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

        writeToSctIdFile(sctIdRefsetWriter, thinExtByRefPart, memberId, refsetId, componentId);

        if (!useRF2) {
            writeToUuidFile(uuidRefsetWriter, thinExtByRefPart, memberId, refsetId, componentId);
        }
        first = false;
    }

    synchronized private void writeToSctIdFile(BufferedWriter sctIdRefsetWriter, I_ThinExtByRefPart thinExtByRefPart,
            Integer memberId, int refsetId, int componentId) throws IOException, TerminologyException,
            InstantiationException, IllegalAccessException, Exception {
        if (useRF2) {
            sctIdRefsetWriter.write(refsetTypeMap.get(refsetId).getRefsetHandler().formatRefsetLineRF2(tf,
                thinExtByRefPart, memberId, refsetId, componentId, true, useRF2));
        } else {
            sctIdRefsetWriter.write(refsetTypeMap.get(refsetId).getRefsetHandler().formatRefsetLine(tf,
                thinExtByRefPart, memberId, refsetId, componentId, true, useRF2));
        }
        sctIdRefsetWriter.write(newLineChars);
    }

    synchronized private void writeToUuidFile(BufferedWriter uuidRefsetWriter, I_ThinExtByRefPart thinExtByRefPart,
            Integer memberId, int refsetId, int componentId) throws IOException, TerminologyException,
            InstantiationException, IllegalAccessException, Exception {
        uuidRefsetWriter.write(refsetTypeMap.get(refsetId).getRefsetHandler().formatRefsetLine(tf, thinExtByRefPart,
            memberId, refsetId, componentId, false, useRF2));
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
                String fileName =
                        refsetName + rf2Descriptor.getContentSubType() + "_" + rf2Descriptor.getCountryCode()
                            + rf2Descriptor.getNamespace() + "_" + releaseVersion + ".txt";
                sctIdRefsetWriter =
                        new BufferedWriter(new FileWriter(new File(sctidRefsetOutputDirectory, sctIdFilePrefix
                            + fileName)));
                sctIdRefsetWriter.write(refsetTypeMap.get(refsetId).getRefsetHandler().getRF2HeaderLine());
            } else {
                sctIdRefsetWriter =
                    new BufferedWriter(new FileWriter(new File(sctidRefsetOutputDirectory, "SCTID_" + refsetName
                        + "_" + releaseVersion + ".txt")));
                sctIdRefsetWriter.write(refsetTypeMap.get(refsetId).getRefsetHandler().getHeaderLine());
            }
            sctIdRefsetWriter.write(newLineChars);
            writerMap.put(refsetId + "SCTID", sctIdRefsetWriter);
        }

        return sctIdRefsetWriter;
    }

    synchronized private BufferedWriter getUuidRefsetWriter(int refsetId) throws Exception {
        BufferedWriter uuidRefsetWriter = writerMap.get(refsetId + "UUID");

        if(uuidRefsetWriter == null){
            I_GetConceptData refsetConcept = tf.getConcept(refsetId);
            String refsetName = getPreferredTerm(refsetConcept);
            refsetName = refsetName.replace("/", "-");
            refsetName = refsetName.replace("'", "_");

            uuidRefsetWriter =
                new BufferedWriter(new FileWriter(new File(uuidRefsetOutputDirectory, "UUID_" + refsetName
                    + "_" + releaseVersion + ".txt")));

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

                int pathVersionRefsetNid =
                        tf.uuidToNative(org.dwfa.ace.refset.ConceptConstants.PATH_VERSION_REFSET.getUuids()[0]);
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
        for (I_Position position : positions) {
            if (position.getPath().getConceptId() == pathId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the concepts preferred term filtered by <code>statusSet</code> sorted by <code>TYPE_B4_LANG</code>
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

        I_DescriptionTuple descTuple =
                conceptData.getDescTuple(descTypeList, null, statusSet, positions, LANGUAGE_SORT_PREF.TYPE_B4_LANG);
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

    private class ConceptProcessor implements Runnable {
        private List<I_GetConceptData> conceptsToProcess = new ArrayList<I_GetConceptData>();

        ConceptProcessor(List<I_GetConceptData> conceptsToProcess) {
            this.conceptsToProcess = new ArrayList<I_GetConceptData>(conceptsToProcess);
        }

        @Override
        public void run() {
            try {
                for (I_GetConceptData conceptToProcess : conceptsToProcess) {
                    processConcept(conceptToProcess);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        void processConcept(I_GetConceptData concept) throws Exception {
            processedConceptsCount++;

            if (!tf.getAllExtensionsForComponent(concept.getConceptId()).isEmpty() && testSpecification(concept)) {

                I_ConceptAttributePart latest = getLatestAttributePart(concept);
                if (latest == null) {
                    getLog().warn(
                        "Concept " + concept + " is exportable for specification " + exportSpecifications
                            + " but has no parts valid for statuses " + allowedStatuses + " and positions " + positions);
                    return;
                }

                exportRefsets(concept.getConceptId());
                exportCtv3IdMap(latest, concept.getConceptId());
                exportSnomedIdMap(latest, concept.getConceptId());

                for (I_RelVersioned rel : concept.getSourceRels()) {
                    processRelationship(rel);
                    processRelationshipInactivation(rel);
                }

                for (I_DescriptionVersioned desc : concept.getDescriptions()) {
                    processDescription(desc);
                    processDescriptionInactivation(desc);
                }
                extractStatus(latest, concept.getConceptId(), conceptInactivationIndicator);

            }

            if (testSpecification(concept)) {
                I_ConceptAttributePart latest = getLatestAttributePart(concept);

                exportCtv3IdMap(latest, concept.getConceptId());
                exportSnomedIdMap(latest, concept.getConceptId());

                for (I_RelVersioned rel : concept.getSourceRels()) {
                    processRelationship(rel);
                    processRelationshipInactivation(rel);
                }

                for (I_DescriptionVersioned desc : concept.getDescriptions()) {
                    processDescription(desc);
                    processDescriptionInactivation(desc);
                }
            }
            if(processedConceptsCount % 1000 == 0){
                logger.info("Processed " + processedConceptsCount + " Concepts");
            }
        }
    }
}
