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

/**
 * Goal which finds concepts with child count > specified value.
 * 
 * @goal vodb-check-child-count
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class VodbCheckChildCount extends AbstractMojo {

    /**
     * List of branches which will included in search.
     * If left unspecified, all will be included.
     * 
     * @parameter
     */
    private ConceptDescriptor[] branches;

    /**
     * Find concepts with child count > this value.
     * 
     * @parameter
     */
    private int count = 20;

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
    private String outputHtmlFileName = "report.html";

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
    private String outputTextFileName = "uuids.txt";

    private class FindComponents implements I_ProcessConcepts {

        I_TermFactory termFactory;
        Set<PositionBI> origins;
        BufferedWriter textWriter;
        BufferedWriter htmlWriter;

        public FindComponents() throws Exception {
            outputHtmlDirectory.mkdirs();
            outputTextDirectory.mkdirs();
            textWriter = new BufferedWriter(new BufferedWriter(new FileWriter(outputTextDirectory + File.separator
                + outputTextFileName)));
            htmlWriter = new BufferedWriter(new BufferedWriter(new FileWriter(outputHtmlDirectory + File.separator
                + outputHtmlFileName)));
            termFactory = Terms.get();
            origins = new HashSet<PositionBI>();
        }

        public void processConcept(I_GetConceptData concept) throws Exception {
            // get origins
            PathBI architectonicPath = termFactory.getPath(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids());

            PositionBI latestOnArchitectonicPath = termFactory.newPosition(architectonicPath, Integer.MAX_VALUE);

            origins.add(latestOnArchitectonicPath);

            Set<PositionBI> branchPositions = new HashSet<PositionBI>();

            // TODO replace with passed in config...
            I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

            // get all the concepts/paths/positions for the specified branches
            if (branches == null) {
                branchPositions = null;
            } else {
                for (ConceptDescriptor branch : branches) {
                    I_GetConceptData currentConcept = branch.getVerifiedConcept();
                    PathBI currentPath = termFactory.getPath(currentConcept.getUids());
                    PositionBI currentPosition = termFactory.newPosition(currentPath, Integer.MAX_VALUE);
                    branchPositions.add(currentPosition);
                }
            }

            // get latest IS-A relationships
            I_IntSet isARel = termFactory.newIntSet();
            isARel.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()).getConceptNid());

            List<? extends I_RelTuple> results = concept.getDestRelTuples(null, isARel, new PositionSetReadOnly(branchPositions), 
                config.getPrecedence(), config.getConflictResolutionStrategy());
            if (results.size() > count) {
                String message = "Concept: " + concept + " has > 20 children.";
                getLog().info(message);
                htmlWriter.append(message);
                htmlWriter.append("<br>");
                textWriter.append(concept.getUids().toString());
                textWriter.newLine();
            }

            termFactory.addUncommitted(concept);
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

    public void execute() throws MojoExecutionException, MojoFailureException {
        I_TermFactory termFactory = Terms.get();
        try {
            FindComponents find = new FindComponents();
            termFactory.iterateConcepts(find);
            find.getTextWriter().close();
            find.getHtmlWriter().close();
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

}
