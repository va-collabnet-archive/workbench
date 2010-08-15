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
package org.ihtsdo.mojo.mojo.refset;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.mojo.mojo.refset.spec.RefsetInclusionSpec;
import org.ihtsdo.mojo.mojo.refset.writers.MemberRefsetHandler;
import org.ihtsdo.tk.api.PositionBI;

/**
 * 
 * This mojo exports reference sets from an ACE database in RF2.
 * 
 * @goal rf2-export
 * @author Christine Hill
 */
public class RF2Export extends AbstractMojo implements I_ProcessConcepts {

    /**
     * RF2 Descriptor - this is required if useRF2 is set to true. This
     * describes the module, namespace, content sub type and country information
     * required to export in RF2.
     * 
     * @parameter
     */
    RF2Descriptor rf2Descriptor;

    /**
     * The namespace that this export uses. e.g. 1000036
     * 
     * @parameter
     * @required
     */
    int namespace;

    /**
     * The project that this export uses. e.g. 1000036
     * 
     * @parameter
     * @required
     */
    int project;

    /**
     * Specifies which refsets to include in the export.
     * 
     * @parameter
     * @required
     */
    RefsetInclusionSpec[] refsetInclusionSpecs;

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

    private I_TermFactory tf = Terms.get();

    private I_IntSet allowedStatuses;

    private Set<I_Position> positions;

    private HashMap<String, BufferedWriter> writerMap = new HashMap<String, BufferedWriter>();

    private HashMap<Integer, RefsetType> refsetTypeMap = new HashMap<Integer, RefsetType>();

    private ReferenceSetExport referenceSetExport = new ReferenceSetExport();

    public void execute() throws MojoExecutionException, MojoFailureException {

        if (!readWriteMapDirectory.exists() || !readWriteMapDirectory.isDirectory() || !readWriteMapDirectory.canRead()) {
            throw new MojoExecutionException("Cannot proceed, readWriteMapDirectory must exist and be readable");
        }

        try {
            allowedStatuses = null;
            positions = null;
            Set<PositionBI> refsetPositions = new HashSet<PositionBI>();
            refsetPositions.addAll(Terms.get().getActiveAceFrameConfig().getViewPositionSetReadOnly());
            referenceSetExport.setPositions(refsetPositions);

            sctidRefsetOutputDirectory.mkdirs();
            uuidRefsetOutputDirectory.mkdirs();

            MemberRefsetHandler.setFixedMapDirectory(readWriteMapDirectory);
            MemberRefsetHandler.setReadWriteMapDirectory(readWriteMapDirectory);
            if (rf2Descriptor != null && rf2Descriptor.getModule() != null) {
                MemberRefsetHandler.setModule(rf2Descriptor.getModule());
            }

            for (RefsetInclusionSpec spec : refsetInclusionSpecs) {
                processConcept(spec.refsetConcept.getVerifiedConcept());
            }

            for (BufferedWriter writer : writerMap.values()) {
                writer.close();
            }

            MemberRefsetHandler.cleanup();

        } catch (Exception e) {
            throw new MojoExecutionException("exporting reference sets failed for specification ", e);
        }
    }

    public void processConcept(I_GetConceptData concept) throws Exception {
        if (testSpecification(concept)) {
            // export the status refset for this concept
            I_ConceptAttributePart latest = referenceSetExport.getLatestAttributePart(concept);
            if (latest == null) {
                getLog().warn(
                    "Concept " + concept + " is exportable but has no parts valid for statuses " + allowedStatuses
                        + " and positions " + positions);
                return;
            }

            exportRefsets(concept.getConceptNid());

            // export relationship refsets
            for (I_RelVersioned rel : concept.getSourceRels()) {
                processRelationship(rel);
            }

            // export description refsets
            for (I_DescriptionVersioned desc : concept.getDescriptions()) {
                processDescription(desc);
            }
        } else {
            getLog().warn("Skipping : " + concept.getInitialText());
        }
    }

    private void processDescription(I_DescriptionVersioned versionedDesc) throws Exception {
        boolean exportableVersionFound = false;
        I_DescriptionPart latest = null;
        for (I_DescriptionPart part : versionedDesc.getMutableParts()) {
            if (testSpecification(part.getTypeId()) && allowedStatuses.contains(part.getStatusId())
                && referenceSetExport.checkPath(part.getPathId())) {

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
        }
    }

    private void processRelationship(I_RelVersioned versionedRel) throws Exception {
        if (testSpecification(versionedRel.getC2Id())) {
            boolean exportableVersionFound = false;
            I_RelPart latest = null;
            for (I_RelPart part : versionedRel.getMutableParts()) {
                if (testSpecification(part.getCharacteristicId()) && testSpecification(part.getPathId())
                    && testSpecification(part.getRefinabilityId()) && testSpecification(part.getTypeId())
                    && allowedStatuses.contains(part.getStatusId()) && referenceSetExport.checkPath(part.getPathId())) {

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
            }
        }
    }

    /**
     * Exports the refset to file.
     * 
     * @param thinExtByRefTuple
     *            The concept extension to write to file.
     * 
     * @throws Exception
     *             on DB or file error.
     */
    private void exportRefsets(int refsetId) throws TerminologyException, Exception {
        Collection<? extends I_ExtendByRef> extensions = tf.getRefsetExtensionMembers(refsetId);
        TreeMap<Long, I_ExtendByRefVersion> sortedExtensions = new TreeMap<Long, I_ExtendByRefVersion>();

        for (I_ExtendByRef thinExtByRefVersioned : extensions) {
            for (I_ExtendByRefVersion thinExtByRefTuple : thinExtByRefVersioned.getTuples(allowedStatuses,
                new PositionSetReadOnly(positions), null, null)) {
                RefsetType refsetType = refsetTypeMap.get(refsetId);
                if (refsetType == null) {
                    try {
                        refsetType = RefsetType.findByExtension(thinExtByRefTuple.getMutablePart());
                    } catch (EnumConstantNotPresentException e) {
                        getLog().warn(
                            "No handler for tuple " + thinExtByRefTuple.getMutablePart() + " of type "
                                + thinExtByRefTuple.getMutablePart().getClass(), e);
                        return;
                    }
                    refsetTypeMap.put(refsetId, refsetType);
                }
                Long memberId =
                        Long.parseLong(refsetType.getRefsetHandler().toId(tf, thinExtByRefTuple.getComponentId(), true,
                            namespace, project));
                if (sortedExtensions.containsKey(memberId)) {
                    getLog().warn(
                        "Refset " + tf.getConcept(refsetId).getInitialText()
                            + " export has multiple entries with same member ID : " + thinExtByRefTuple + " "
                            + memberId);
                }
                sortedExtensions.put(memberId, thinExtByRefTuple);
            }
        }

        Iterator<Long> iterator = sortedExtensions.keySet().iterator();
        while (iterator.hasNext()) {
            Long key = iterator.next();
            I_ExtendByRefVersion ext = sortedExtensions.get(key);
            export(ext);
        }
    }

    void export(I_ExtendByRefVersion thinExtByRefTuple) throws Exception {
        export(thinExtByRefTuple.getMutablePart(), thinExtByRefTuple.getMemberId(), thinExtByRefTuple.getRefsetId(),
            thinExtByRefTuple.getComponentId());
    }

    /**
     * Exports the refset to file.
     * 
     * @param thinExtByRefPart
     *            The concept extension to write to file.
     * @param memberId
     *            the id for this refset member record.
     * @param refsetId
     *            the refset id
     * @param componentId
     *            the referenced component
     * @throws Exception
     *             on DB errors or file write errors.
     */
    void export(I_ExtendByRefPart thinExtByRefPart, Integer memberId, int refsetId, int componentId) throws Exception {
        RefsetType refsetType = refsetTypeMap.get(refsetId);
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

        BufferedWriter uuidRefsetWriter = writerMap.get(refsetId + "UUID");
        BufferedWriter sctIdRefsetWriter = writerMap.get(refsetId + "SCTID");
        if (sctIdRefsetWriter == null) {
            // must not have written to this file yet
            I_GetConceptData refsetConcept = tf.getConcept(refsetId);
            String refsetName = referenceSetExport.getPreferredTerm(refsetConcept);

            refsetName = refsetName.replace("/", "-");
            refsetName = refsetName.replace("'", "_");

            if (releaseVersion == null) {
                releaseVersion = referenceSetExport.getReleaseVersion(refsetConcept);
            }

            /*
             * <FileType>_<ContentType>_<ContentSubType>_<Country|Namespace>_
             * <Date>.<ext> e.g. der2_SCTID.Activities of daily
             * living.concept.refset_National_UK1999999_20090131.txt
             */
            String sctIdFilePrefix = "der2_SCTID.";
            String uuidFilePrefix = "der2_UUID.";

            String fileName = null;
            File exportFile = getExportFile(tf.getConcept(refsetId));
            if (exportFile != null) {
                fileName = exportFile.getName();
                sctIdFilePrefix = "";
                uuidFilePrefix = "";
            }
            if (fileName == null) {
                fileName =
                        refsetName + refsetType.getFileExtension() + "_" + rf2Descriptor.getContentSubType() + "_"
                            + rf2Descriptor.getCountryCode() + namespace + "_" + releaseVersion + ".txt";
            }
            uuidRefsetWriter =
                    new BufferedWriter(new FileWriter(new File(uuidRefsetOutputDirectory, uuidFilePrefix + fileName)));
            sctIdRefsetWriter =
                    new BufferedWriter(new FileWriter(new File(sctidRefsetOutputDirectory, sctIdFilePrefix + fileName)));

            writerMap.put(refsetId + "UUID", uuidRefsetWriter);
            writerMap.put(refsetId + "SCTID", sctIdRefsetWriter);

            sctIdRefsetWriter.write(refsetType.getRefsetHandler().getRF2HeaderLine());
            uuidRefsetWriter.write(refsetType.getRefsetHandler().getRF2HeaderLine());
            uuidRefsetWriter.newLine();
            sctIdRefsetWriter.newLine();
        }

        // note that we are assuming that the type of refset member will be the
        // same as previous for this file type
        // if not we'll get a class cast exception, as we probably should
        sctIdRefsetWriter.write(refsetType.getRefsetHandler().formatRefsetLineRF2(tf, thinExtByRefPart, memberId,
            refsetId, componentId, true, namespace, project));
        uuidRefsetWriter.write(refsetType.getRefsetHandler().formatRefsetLineRF2(tf, thinExtByRefPart, memberId,
            refsetId, componentId, false, namespace, project));
        uuidRefsetWriter.newLine();
        sctIdRefsetWriter.newLine();
    }

    /**
     * Does the concept match the <code>exportSpecifications</code>
     * 
     * @param concept
     *            I_GetConceptData
     * @return true if a matching concept.
     * @throws Exception
     *             DB error
     */
    boolean testSpecification(I_GetConceptData concept) throws Exception {
        for (RefsetInclusionSpec spec : refsetInclusionSpecs) {
            if (spec.test(concept) && referenceSetExport.getLatestAttributePart(concept) != null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Does the concept match the <code>exportSpecifications</code>
     * 
     * @param id
     *            concept it
     * @return true if a matching concept.
     * @throws TerminologyException
     *             DB error
     * @throws IOException
     *             DB error
     * @throws Exception
     *             DB error
     */
    boolean testSpecification(int id) throws TerminologyException, IOException, Exception {
        return testSpecification(tf.getConcept(id));
    }

    File getExportFile(I_GetConceptData concept) throws Exception {
        for (RefsetInclusionSpec spec : refsetInclusionSpecs) {
            if (spec.test(concept)) {
                return spec.exportFile;
            }
        }
        return null;
    }
}
