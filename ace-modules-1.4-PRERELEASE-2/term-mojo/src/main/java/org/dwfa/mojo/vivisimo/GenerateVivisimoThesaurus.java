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
package org.dwfa.mojo.vivisimo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.maven.MojoUtil;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.PositionDescriptor;
import org.dwfa.mojo.refset.ExportSpecification;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.IntSet;

/**
 * * GenerateVivisimoThesaurus <br/>
 * <p>
 * The <code>GenerateVivisimoThesaurus</code> class generates a vivisimo
 * thesaurus file from the provided specifications.
 * </p>
 * <p>
 * </p>
 * 
 * 
 * 
 * @see <code>org.apache.maven.plugin.AbstractMojo</code>
 * @author Keith Campbell
 * @goal generate-vivisimo-thesaurus
 */
public class GenerateVivisimoThesaurus extends AbstractMojo {

    /**
     * Location of the directory to output data files to.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private String outputDirectory;

    /**
     * If true, do not create thesaurus entries for preferred terms with no
     * synonyms.
     * 
     * @parameter
     * @required
     */
    private String suppressEntriesWithNoSynonyms;

    /**
     * @parameter
     * @required
     */
    private String maxWordsForPreferredName;

    /**
     * @parameter
     * @required
     */
    private String minWordsForPreferredName;

    /**
     * File name for description table data output file
     * 
     * @parameter expression="vivisimo-thesaurus.xml"
     */
    private String dataFileName;

    /**
     * File name for description table data output file
     * 
     * @parameter
     * @required
     */
    private String thesaurusName;

    /**
     * File name for description table data output file
     * 
     * @parameter
     * @required
     */
    private String thesaurusDomain;

    /**
     * The set of specifications used to determine if a concept should be
     * exported.
     * 
     * @parameter
     * @required
     */
    private ExportSpecification[] specs;

    /**
     * Positions to export data.
     * 
     * @parameter
     * @required
     */
    private PositionDescriptor[] positionsForExport;

    /**
     * Status values to include in export
     * 
     * @parameter
     * @required
     */
    private ConceptDescriptor[] statusValuesForExport;

    /**
     * @parameter default-value="${project.build.directory}"
     * @required
     * @readonly
     */
    private File buildDirectory;

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    private HashSet<I_Position> positions;

    private I_IntSet statusValues;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {

            if (MojoUtil.alreadyRun(getLog(), outputDirectory + dataFileName, this.getClass(), targetDirectory)) {
                return;
            }

            getLog().info("suppressEntriesWithNoSynonyms: " + suppressEntriesWithNoSynonyms);
            I_TermFactory termFactory = LocalVersionedTerminology.get();

            positions = new HashSet<I_Position>(positionsForExport.length);
            for (PositionDescriptor pd : positionsForExport) {
                positions.add(pd.getPosition());
            }
            statusValues = termFactory.newIntSet();
            List<I_GetConceptData> statusValueList = new ArrayList<I_GetConceptData>();
            for (ConceptDescriptor status : statusValuesForExport) {
                I_GetConceptData statusConcept = status.getVerifiedConcept();
                statusValues.add(statusConcept.getConceptId());
                statusValueList.add(statusConcept);
            }
            getLog().info(" processing concepts for positions: " + positions + " with status: " + statusValueList);

            if (outputDirectory.endsWith("/") == false) {
                outputDirectory = outputDirectory + "/";
            }
            File outputDirFile = new File(outputDirectory);
            outputDirFile.mkdirs();
            File dataFile = new File(outputDirectory + dataFileName);
            outputDirFile.getParentFile().mkdirs();
            Writer dataWriter = new BufferedWriter(new FileWriter(dataFile));

            ExportIterator expItr = new ExportIterator(dataWriter, thesaurusName, thesaurusDomain);
            LocalVersionedTerminology.get().iterateConcepts(expItr);
            dataWriter.write("</thesaurus>\n");

            dataWriter.close();

        } catch (Exception e) {
            throw new MojoExecutionException("Unable to export database due to exception", e);
        }

    }// End method execute

    private class ExportIterator implements I_ProcessConcepts {

        private Writer dataWriter;
        private IntSet preferred = new IntSet();
        private IntSet synonym = new IntSet();
        private int maxWords;
        private int minWords;

        public ExportIterator(Writer dataWriter, String name, String domain) throws IOException, TerminologyException {
            this.dataWriter = dataWriter;
            dataWriter.write("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n");
            dataWriter.write("<thesaurus name=\"" + name + "\" language=\"english\" domain=\"" + domain + "\">\n");
            preferred.add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
            preferred.add(ArchitectonicAuxiliary.Concept.XHTML_PREFERRED_DESC_TYPE.localize().getNid());
            synonym.add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());
            synonym.add(ArchitectonicAuxiliary.Concept.XHTML_SYNONYM_DESC_TYPE.localize().getNid());
            maxWords = Integer.parseInt(maxWordsForPreferredName);
            minWords = Integer.parseInt(minWordsForPreferredName);
        }

        public void processConcept(I_GetConceptData concept) throws Exception {
            if (isExportable(concept)) {
                List<I_DescriptionTuple> descTuples = concept.getDescriptionTuples(statusValues, preferred, positions);
                List<I_DescriptionTuple> synonymTuples = concept.getDescriptionTuples(statusValues, synonym, positions);
                if (Boolean.parseBoolean(suppressEntriesWithNoSynonyms)) {
                    if (synonymTuples.size() > 0) {
                        writeWordEntry(descTuples, synonymTuples);
                    }
                } else {
                    writeWordEntry(descTuples, synonymTuples);
                }
            }
        }

        private void writeWordEntry(List<I_DescriptionTuple> descTuples, List<I_DescriptionTuple> synonymTuples)
                throws IOException {
            for (I_DescriptionTuple preferredDesc : descTuples) {
                int words = preferredDesc.getText().split("\\s+").length;
                if (words >= minWords && words <= maxWords) {
                    dataWriter.write("<word name=\"");
                    dataWriter.write(preferredDesc.getText());
                    dataWriter.write("\">\n");
                    for (I_DescriptionTuple synonymDesc : synonymTuples) {
                        dataWriter.write("  <synonym>");
                        dataWriter.write(synonymDesc.getText());
                        dataWriter.write("</synonym>\n");
                    }
                    dataWriter.write("</word>\n");
                }
            }
        }

        private boolean isExportable(I_GetConceptData concept) throws Exception {
            for (ExportSpecification spec : specs) {
                if (spec.test(concept)) {
                    return true;
                }
            }
            return false;
        }

    }

}// End class ExportDatabase
