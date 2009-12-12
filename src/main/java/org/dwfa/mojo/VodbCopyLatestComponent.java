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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.cement.ArchitectonicAuxiliary;

/**
 * Goal which copies the latest changes on a component to another branch.
 * 
 * @goal vodb-copy-latest
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class VodbCopyLatestComponent extends AbstractMojo {

    /**
     * Branch to which the latest component of specified branch will be
     * copied to.
     * 
     * @parameter
     */
    private ConceptDescriptor branchToCopyTo;

    /**
     * Branch which will be copied to new branch.
     * 
     * @parameter
     */
    private ConceptDescriptor branchToCopy;

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

    private class CopyLatestComponent implements I_ProcessConcepts {
        private I_TermFactory termFactory;
        private I_GetConceptData branchToCopyConcept;
        private I_GetConceptData branchToCopyToConcept;
        private int numberCopiedComponents;

        public CopyLatestComponent() throws Exception {
            termFactory = LocalVersionedTerminology.get();
            branchToCopyConcept = branchToCopy.getVerifiedConcept();
            branchToCopyToConcept = branchToCopyTo.getVerifiedConcept();
            numberCopiedComponents = 0;
        }

        public void processConcept(I_GetConceptData concept) throws Exception {

            // get origins
            I_Path originPath = termFactory.getPath(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids());

            I_Position originPosition = termFactory.newPosition(originPath, Integer.MAX_VALUE);

            Set<I_Position> origins = new HashSet<I_Position>();
            origins.add(originPosition);

            // get the copy-to concept/path
            I_Path copyToPath = termFactory.getPath(branchToCopyToConcept.getUids());

            // get concept/path/position of the branch being copied
            I_Path oldPath = termFactory.getPath(branchToCopyConcept.getUids());

            I_Position oldPosition = termFactory.newPosition(oldPath, Integer.MAX_VALUE);
            Set<I_Position> positions = new HashSet<I_Position>();
            positions.add(oldPosition);

            // get latest concept attributes
            List<? extends I_ConceptAttributeTuple> conceptAttributeTuples = concept.getConceptAttributeTuples(null, new PositionSetReadOnly(positions));
            // copy latest attributes to new path/version
            for (I_ConceptAttributeTuple tuple : conceptAttributeTuples) {
                I_ConceptAttributePart newPart = tuple.duplicatePart();
                newPart.setVersion(Integer.MAX_VALUE);
                newPart.setPathId(copyToPath.getConceptId());
                concept.getConceptAttributes().addVersion(newPart);
            }

            // get latest descriptions
            List<? extends I_DescriptionTuple> descriptionTuples = concept.getDescriptionTuples(null, null, new PositionSetReadOnly(positions));
            // copy latest descriptions to new path/version
            for (I_DescriptionTuple tuple : descriptionTuples) {
                I_DescriptionPart newPart = tuple.duplicatePart();
                newPart.setVersion(Integer.MAX_VALUE);
                newPart.setPathId(copyToPath.getConceptId());
                tuple.getDescVersioned().addVersion(newPart);
            }

            // get latest relationships
            List<? extends I_RelTuple> relationshipTuples = concept.getSourceRelTuples(null, null, new PositionSetReadOnly(positions), false);
            // copy latest relationships to new path/version
            for (I_RelTuple tuple : relationshipTuples) {
                I_RelPart newPart = tuple.duplicatePart();
                newPart.setVersion(Integer.MAX_VALUE);
                newPart.setPathId(copyToPath.getConceptId());
                tuple.getRelVersioned().addVersion(newPart);
            }

            numberCopiedComponents++;
            termFactory.addUncommitted(concept);
        }

        public I_GetConceptData getBranchToCopyConcept() {
            return branchToCopyConcept;
        }

        public void setBranchToCopyConcept(I_GetConceptData branchToCopyConcept) {
            this.branchToCopyConcept = branchToCopyConcept;
        }

        public I_GetConceptData getBranchToCopyToConcept() {
            return branchToCopyToConcept;
        }

        public void setBranchToCopyToConcept(I_GetConceptData branchToCopyToConcept) {
            this.branchToCopyToConcept = branchToCopyToConcept;
        }

        public int getNumberCopiedComponents() {
            return numberCopiedComponents;
        }

        public void setNumberCopiedComponents(int numberCopiedComponents) {
            this.numberCopiedComponents = numberCopiedComponents;
        }
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        I_TermFactory termFactory = LocalVersionedTerminology.get();
        try {
            CopyLatestComponent copy = new CopyLatestComponent();
            termFactory.iterateConcepts(copy);
            String message = "Successfully copied " + copy.getNumberCopiedComponents() + " latest components on '"
                + branchToCopy.getDescription() + "' branch to '" + branchToCopyTo.getDescription() + "' branch.";
            getLog().info(message);

            outputHtmlDirectory.mkdirs();
            BufferedWriter htmlWriter = new BufferedWriter(new BufferedWriter(new FileWriter(outputHtmlDirectory
                + File.separator + outputHtmlFileName)));

            htmlWriter.append(message);
            htmlWriter.close();
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

}
