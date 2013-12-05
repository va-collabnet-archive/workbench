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
package org.ihtsdo.mojo.mojo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.binding.snomed.Snomed;

/**
 * Goal which finds all concepts that have no parents.
 * 
 * @goal vodb-find-orphans
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class VodbFindOrphans extends AbstractMojo {

    /**
     * List of branches which will included in search.
     * If left undefined, all will be included.
     * 
     * @parameter
     */
    private final ConceptDescriptor[] branches = null;

    /**
     * List of branches which will included in search.
     * If left undefined, all will be included.
     * 
     * @parameter
     */
    private final ConceptDescriptor[] statuses = null;

    /**
     * The html output file location.
     * 
     * @parameter expression="${project.build.directory}/classes"
     */
    private File outputHtmlDirectory;

    /**
     * The html output file name.
     * 
     * @parameter
     */
    private final String outputHtmlFileName = "report.html";

    /**
     * The text file output location.
     * 
     * @parameter expression="${project.build.directory}/classes"
     */
    private File outputTextDirectory;

    /**
     * The text file containing uuids file name.
     * 
     * @parameter
     */
    private final String outputTextFileName = "uuids.txt";

    private class FindComponents implements I_ProcessConcepts {

        I_TermFactory tf;
        Set<PositionBI> origins;
        BufferedWriter textWriter;
        BufferedWriter htmlWriter;

        public FindComponents() throws IOException {
            outputHtmlDirectory.mkdirs();
            outputTextDirectory.mkdirs();
            textWriter = new BufferedWriter(new BufferedWriter(new FileWriter(outputTextDirectory + File.separator
                + outputTextFileName)));
            htmlWriter = new BufferedWriter(new BufferedWriter(new FileWriter(outputHtmlDirectory + File.separator
                + outputHtmlFileName)));
            origins = new HashSet<>();
            tf = Terms.get();
        }

        @Override
        public void processConcept(I_GetConceptData concept) throws Exception {
            // get origins
            PathBI architectonicPath = tf.getPath(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids());

            // TODO replace with passed in config...
            I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
            PositionBI latestOnArchitectonicPath = tf.newPosition(architectonicPath, Long.MAX_VALUE);

            origins.add(latestOnArchitectonicPath);

            Set<PositionBI> branchPositions = new HashSet<>();

            // get the concepts/paths/positions for the branches to be compared
            if (branches != null) {
                for (ConceptDescriptor branch : branches) {
                    I_GetConceptData currentConcept = branch.getVerifiedConcept();
                    PathBI currentPath = tf.getPath(currentConcept.getUids());
                    PositionBI currentPosition = tf.newPosition(currentPath, Long.MAX_VALUE);
                    branchPositions.add(currentPosition);
                }
            }

            // get latest IS-A relationships
            I_IntSet isARel = tf.newIntSet();
            isARel.add(tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()).getConceptNid());
            isARel.add(tf.getConcept(Snomed.IS_A.getUuids()).getConceptNid());

            I_IntSet allowStatuses = null;
            if (statuses != null) {
                allowStatuses = tf.newIntSet();
                for (ConceptDescriptor status : statuses) {
                    allowStatuses.add(status.getVerifiedConcept().getNid());
                }
            }

            List<? extends I_RelTuple> results = concept.getSourceRelTuples(allowStatuses, isARel, new PositionSetReadOnly(branchPositions), 
                config.getPrecedence(), config.getConflictResolutionStrategy());
            if (results.isEmpty()) {
                String message = "Found an orphaned concept: " + concept.getPrimUuid().toString() + "  " + concept.toUserString();
                getLog().info(message);
                htmlWriter.append(message);
                htmlWriter.append("<br>");
                textWriter.append(concept.getUids().toString());
                textWriter.newLine();
            }
        }

        public BufferedWriter getHtmlWriter() {
            return htmlWriter;
        }

        public void setHtmlWriter(BufferedWriter htmlWriter) {
            this.htmlWriter = htmlWriter;
        }

        public BufferedWriter getTextWriter() {
            return textWriter;
        }

        public void setTextWriter(BufferedWriter textWriter) {
            this.textWriter = textWriter;
        }
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        I_TermFactory tf = Terms.get();
        try {
            FindComponents find = new FindComponents();
            tf.iterateConcepts(find);
            find.getTextWriter().close();
            find.getHtmlWriter().close();
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

}
