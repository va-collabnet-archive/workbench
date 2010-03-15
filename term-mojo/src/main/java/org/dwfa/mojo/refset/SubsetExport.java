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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.dwfa.maven.transform.SctIdGenerator.TYPE;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.PositionDescriptor;
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
     * Defines the directory to which the SCTID based subsets are
     * exported.
     * 
     * @parameter
     * @required
     */
    File subsetOutputDirectory;

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

    public void execute() throws MojoExecutionException, MojoFailureException {

        try {
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

            subsetOutputDirectory.mkdirs();

            tf.iterateConcepts(this);

            for (BufferedWriter writer : writerMap.values()) {
                writer.close();
            }

            MemberRefsetHandler.cleanup();

        } catch (Exception e) {
            throw new MojoExecutionException("Exporting subsets failed for specification " + exportSpecifications, e);
        }
    }

    public void processConcept(I_GetConceptData concept) throws Exception {
        if (testSpecification(concept)) {
            // export the status refset for this concept
            I_ConceptAttributePart latest = referenceSetExport.getLatestAttributePart(concept);
            if (latest == null) {
                getLog().warn(
                    "Concept " + concept + " is exportable for specification " + exportSpecifications
                        + " but has no parts valid for statuses " + allowedStatuses + " and positions " + positions);
                return;
            }

            exportRefsets(concept.getConceptId());
        }
    }

    private void exportRefsets(int componentId) throws TerminologyException, Exception {
        List<I_ThinExtByRefVersioned> extensions = tf.getAllExtensionsForComponent(componentId);
        for (I_ThinExtByRefVersioned thinExtByRefVersioned : extensions) {
            if (testSpecification(thinExtByRefVersioned.getRefsetId())) {
                for (I_ThinExtByRefTuple thinExtByRefTuple : thinExtByRefVersioned.getTuples(allowedStatuses,
                    positions, false, true)) {
                    export(thinExtByRefTuple);
                }
            }
        }
    }

    private void export(I_ThinExtByRefTuple thinExtByRefTuple) throws Exception {
        export(thinExtByRefTuple.getPart(), thinExtByRefTuple.getMemberId(), thinExtByRefTuple.getRefsetId(),
            thinExtByRefTuple.getComponentId());
    }

    boolean testSpecification(I_GetConceptData concept) throws Exception {
        for (ExportSpecification spec : exportSpecifications) {
            if (spec.test(concept) && referenceSetExport.getLatestAttributePart(concept) != null) {
                return true;
            }
        }

        return false;
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
                getLog().warn("No handler for tuple " + thinExtByRefPart + " of type " + thinExtByRefPart.getClass(), e);
                return;
            }
            refsetTypeMap.put(refsetId, refsetType);
        }

        BufferedWriter subsetIndexFileWriter = writerMap.get("INDEX");
        if (subsetIndexFileWriter == null) {
            subsetIndexFileWriter = new BufferedWriter(new FileWriter(new File(subsetOutputDirectory, "sct_subsets_"
                + countryCode + "_" + releaseVersion + ".txt")));
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

            subsetMemberFileWriter = new BufferedWriter(new FileWriter(new File(subsetOutputDirectory,
                "sct_subsetmembers_" + countryCode + "_" + refsetName + "_" + releaseVersion + ".txt")));

            writerMap.put(refsetId + "SCTID", subsetMemberFileWriter);

            subsetMemberFileWriter.write(refsetType.getRefsetHandler().getSubsetFormatHeaderLine());
            subsetMemberFileWriter.newLine();

            // first time this subset member has been found, so add to index
            // table as well
            subsetIndexFileWriter.write(refsetType.getRefsetHandler().getRefsetSctID(tf, refsetId, TYPE.SUBSET)
                + FILE_DELIMITER + "UNKNOWN" + FILE_DELIMITER + "UNKNOWN" + FILE_DELIMITER
                + referenceSetExport.getPreferredTerm(tf.getConcept(refsetId)) + FILE_DELIMITER + "UNKNOWN"
                + FILE_DELIMITER + languageCode + FILE_DELIMITER + "UNKNOWN" + FILE_DELIMITER + "UNKNOWN");
            subsetIndexFileWriter.newLine();
        }

        subsetMemberFileWriter.write(refsetType.getRefsetHandler().formatRefsetAsSubset(tf, thinExtByRefPart, memberId,
            refsetId, componentId, true));
        subsetMemberFileWriter.newLine();
    }
}
