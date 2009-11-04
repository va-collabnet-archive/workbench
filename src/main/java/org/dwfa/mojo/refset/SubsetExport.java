package org.dwfa.mojo.refset;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.mojo.refset.spec.RefsetInclusionSpec;
import org.dwfa.mojo.refset.spec.RefsetPurposeToSubsetTypeMap;
import org.dwfa.mojo.refset.writers.MemberRefsetHandler;
import org.dwfa.tapi.TerminologyException;

/**
 * 
 * This mojo exports refsets from an ACE database in subset format.
 * 
 * @goal subset-export
 * @author Christine Hill
 */
public class SubsetExport extends AbstractMojo implements I_ProcessConcepts {

    /**
     * Specifies which refsets to include in the export.
     * 
     * @parameter
     * @required
     */
    RefsetInclusionSpec[] refsetInclusionSpecs;

    /**
     * Defines the directory to which the SCTID based subsets are
     * exported.
     * 
     * @parameter
     * @required
     */
    File subsetOutputDirectory;

    /**
     * Directory where the read/write SCTID maps are stored
     * 
     * @parameter
     * @required
     */
    File readWriteMapDirectory;

    /**
     * Release version used to embed in the subset file names - if not specified
     * then the "path version" reference set is used to determine the version
     * 
     * @parameter
     */
    String releaseVersion;

    /**
     * Country code used to embed in the subset file names.
     * 
     * @required
     * @parameter
     */
    String countryCode = "UNKNOWN";

    /**
     * Language code used to embed in the subset files.
     * 
     * @parameter
     */
    String languageCode = "en";

    /**
     * File delimiter.
     * 
     * @parameter
     */
    String FILE_DELIMITER = "\t";

    private I_TermFactory tf = LocalVersionedTerminology.get();

    private I_IntSet allowedStatuses;

    private Set<I_Position> positions;

    private HashMap<String, BufferedWriter> writerMap = new HashMap<String, BufferedWriter>();

    private HashMap<Integer, RefsetType> refsetTypeMap = new HashMap<Integer, RefsetType>();

    private ReferenceSetExport referenceSetExport = new ReferenceSetExport();

    private RefsetInclusionSpec currentSpec;

    public void execute() throws MojoExecutionException, MojoFailureException {

        if (!readWriteMapDirectory.exists() || !readWriteMapDirectory.isDirectory() || !readWriteMapDirectory.canRead()) {
            throw new MojoExecutionException("Cannot proceed, readWriteMapDirectory must exist and be readable");
        }

        try {
            allowedStatuses = null;
            positions = null;

            subsetOutputDirectory.mkdirs();

            MemberRefsetHandler.setFixedMapDirectory(readWriteMapDirectory);
            MemberRefsetHandler.setReadWriteMapDirectory(readWriteMapDirectory);

            for (RefsetInclusionSpec spec : refsetInclusionSpecs) {
                currentSpec = spec;
                processConcept(spec.refsetConcept.getVerifiedConcept());
            }

            for (BufferedWriter writer : writerMap.values()) {
                writer.close();
            }

            MemberRefsetHandler.cleanup();

        } catch (Exception e) {
            throw new MojoExecutionException("Exporting subsets failed for specification ", e);
        }
    }

    public void processConcept(I_GetConceptData concept) throws Exception {
        if (testSpecification(concept)) {
            // export the status refset for this concept
            I_ConceptAttributePart latest = referenceSetExport.getLatestAttributePart(concept);
            if (latest == null) {
                getLog().warn(
                    "Concept " + concept + " is exportable " + " but has no parts valid for statuses "
                        + allowedStatuses + " and positions " + positions);
                return;
            }

            exportRefsets(concept.getConceptId());
        } else {
            getLog().warn("Skipping : " + concept.getInitialText());
        }
    }

    private void exportRefsets(int refsetId) throws TerminologyException, Exception {

        List<I_ThinExtByRefVersioned> extensions = tf.getRefsetExtensionMembers(refsetId);
        TreeMap<Long, I_ThinExtByRefTuple> treeMap = new TreeMap<Long, I_ThinExtByRefTuple>();

        for (I_ThinExtByRefVersioned thinExtByRefVersioned : extensions) {
            for (I_ThinExtByRefTuple thinExtByRefTuple : thinExtByRefVersioned.getTuples(allowedStatuses, positions,
                true, true)) {
                try {

                    RefsetType refsetType = refsetTypeMap.get(refsetId);
                    if (refsetType == null) {
                        try {
                            refsetType = RefsetType.findByExtension(thinExtByRefTuple.getPart());
                        } catch (EnumConstantNotPresentException e) {
                            getLog().warn(
                                "No handler for tuple " + thinExtByRefTuple.getPart() + " of type "
                                    + thinExtByRefTuple.getPart().getClass(), e);
                            return;
                        }
                        refsetTypeMap.put(refsetId, refsetType);
                    }

                    Long memberId =
                            Long.parseLong(refsetType.getRefsetHandler().toId(tf, thinExtByRefTuple.getComponentId(),
                                true));
                    if (treeMap.containsKey(memberId)) {
                        getLog().warn(
                            "Refset " + tf.getConcept(refsetId).getInitialText()
                                + " export has multiple entries with same member ID : " + thinExtByRefTuple + " "
                                + memberId);
                    }
                    treeMap.put(memberId, thinExtByRefTuple);
                } catch (NumberFormatException e) {
                    getLog().warn(
                        "Refset " + tf.getConcept(refsetId).getInitialText()
                            + " fails to allocate member ID for tuple : " + thinExtByRefTuple);
                    return;
                }
            }
        }
        Iterator<Long> iterator = treeMap.keySet().iterator();
        while (iterator.hasNext()) {
            Long key = iterator.next();
            I_ThinExtByRefTuple ext = treeMap.get(key);

            export(ext);
        }
    }

    private void export(I_ThinExtByRefTuple thinExtByRefTuple) throws Exception {
        export(thinExtByRefTuple.getPart(), thinExtByRefTuple.getMemberId(), thinExtByRefTuple.getRefsetId(),
            thinExtByRefTuple.getComponentId());
    }

    boolean testSpecification(I_GetConceptData concept) throws Exception {
        for (RefsetInclusionSpec spec : refsetInclusionSpecs) {
            if (spec.test(concept) && referenceSetExport.getLatestAttributePart(concept) != null) {
                return true;
            }
        }

        return false;
    }

    File getExportFile(I_GetConceptData concept) throws Exception {
        for (RefsetInclusionSpec spec : refsetInclusionSpecs) {
            if (spec.test(concept)) {
                return spec.exportFile;
            }
        }
        return null;
    }

    File getExportFile(int id) throws TerminologyException, IOException, Exception {
        return getExportFile(tf.getConcept(id));
    }

    boolean testSpecification(int id) throws TerminologyException, IOException, Exception {
        return testSpecification(tf.getConcept(id));
    }

    private void export(I_ThinExtByRefPart thinExtByRefPart, Integer memberId, int refsetId, int componentId)
            throws Exception {
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

        BufferedWriter subsetIndexFileWriter = writerMap.get("INDEX");
        if (subsetIndexFileWriter == null) {
            subsetIndexFileWriter =
                    new BufferedWriter(new FileWriter(new File(subsetOutputDirectory, "sct_subsets_" + countryCode
                        + "_" + releaseVersion + ".txt")));
            subsetIndexFileWriter.write("SUBSETID" + FILE_DELIMITER + "SUBSETORIGINALID" + FILE_DELIMITER
                + "SUBSETVERSION" + FILE_DELIMITER + "SUBSETNAME" + FILE_DELIMITER + "SUBSETTYPE" + FILE_DELIMITER
                + "LANGUAGECODE" + FILE_DELIMITER + "REALMID" + FILE_DELIMITER + "CONTEXTID");
            subsetIndexFileWriter.newLine();
            writerMap.put("INDEX", subsetIndexFileWriter);
        }

        BufferedWriter subsetMemberFileWriter = writerMap.get(refsetId + "SCTID");
        if (subsetMemberFileWriter == null) {
            // must not have written to this file yet
            I_GetConceptData refsetConcept = tf.getConcept(refsetId);
            String refsetName = referenceSetExport.getPreferredTerm(refsetConcept);

            refsetName = refsetName.replace("/", "-");
            refsetName = refsetName.replace("'", "_");

            if (releaseVersion == null) {
                releaseVersion = referenceSetExport.getReleaseVersion(refsetConcept);
            }

            File subsetFile = getExportFile(refsetId);
            if (subsetFile == null) {
                subsetFile =
                        new File(subsetOutputDirectory, "sct_subsetmembers_" + countryCode + "_" + refsetName + "_"
                            + releaseVersion + ".txt");
            } else {
                subsetFile = new File(subsetOutputDirectory, subsetFile.getName());
            }

            subsetMemberFileWriter = new BufferedWriter(new FileWriter(subsetFile));
            writerMap.put(refsetId + "SCTID", subsetMemberFileWriter);

            subsetMemberFileWriter.write(refsetType.getRefsetHandler().getSubsetFormatHeaderLine());
            subsetMemberFileWriter.newLine();

            // first time this subset member has been found, so add to index
            // table as well
            String subOriginalId = null;
            if (!(currentSpec.specContainsSnomedId)) {
                subOriginalId = refsetType.getRefsetHandler().getSnomedIntegerId(tf, refsetId);
            } else {
                // check if the spec has the original snomed id
                I_IntSet allowedTypes = tf.newIntSet();
                allowedTypes.add(tf.getConcept(RefsetAuxiliary.Concept.SPECIFIES_REFSET.getUids()).getConceptId());

                Set<I_GetConceptData> concepts =
                        refsetConcept.getDestRelOrigins(allowedStatuses, allowedTypes, positions, true, true);
                for (I_GetConceptData concept : concepts) {
                    subOriginalId = refsetType.getRefsetHandler().getSnomedIntegerId(tf, concept.getConceptId());
                    if (subOriginalId != null) {
                        break;
                    }
                }
            }

            if (subOriginalId == null) {
                subOriginalId = "UNKNOWN";
            }
            subsetIndexFileWriter.write(refsetType.getRefsetHandler().getRefsetSctID(tf, refsetId) + FILE_DELIMITER
                + subOriginalId + FILE_DELIMITER + "UNKNOWN" + FILE_DELIMITER
                + referenceSetExport.getPreferredTerm(tf.getConcept(refsetId)) + FILE_DELIMITER
                + getSubsetTypeFromRefsetPurpose(refsetId) + FILE_DELIMITER + languageCode + FILE_DELIMITER + "UNKNOWN"
                + FILE_DELIMITER + "UNKNOWN");
            subsetIndexFileWriter.newLine();
        }

        subsetMemberFileWriter.write(refsetType.getRefsetHandler().formatRefsetAsSubset(tf, thinExtByRefPart, memberId,
            refsetId, componentId, true));
        subsetMemberFileWriter.newLine();
    }

    private String getSubsetTypeFromRefsetPurpose(int refsetId) {
        try {
            I_GetConceptData refsetMemberConcept = tf.getConcept(refsetId);
            RefsetSpec refsetSpec = new RefsetSpec(refsetMemberConcept, true);
            I_GetConceptData refsetPurpose = refsetSpec.getRefsetPurposeConcept();
            int subsetTypeId = RefsetPurposeToSubsetTypeMap.convert(refsetPurpose);

            if (subsetTypeId == -1) {
                return "UNKNOWN";
            } else {
                return "" + subsetTypeId;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "UNKNOWN";
        }
    }
}
