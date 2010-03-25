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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.mojo.ConceptDescriptor;
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
import org.dwfa.cement.ArchitectonicAuxiliary;

/**
 * Goal which monitors two branches for changes. Agreed changes are copied
 * to a new branch. Any encountered conflicts result in a html summary report
 * and text file containing a list of the conflicting concept identifiers.
 * 
 * Optionally can check for flagged concept status (exclude
 * components from being copied if they are flagged).
 * 
 * @goal vodb-monitor-branches
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class VodbMonitorBranches extends AbstractMojo {

    /**
     * Branch to which the compared branches will be copied to, if they
     * agree on the value.
     * 
     * @parameter
     * @required
     */
    private ConceptDescriptor branchToCopyTo;

    /**
     * The updated status of any copied concepts.
     * 
     * @parameter
     * @required
     */
    private ConceptDescriptor updatedStatus;

    /**
     * Branches which will be compared.
     * 
     * @parameter
     * @required
     */
    private ConceptDescriptor[] branchesToCompare;

    /**
     * The flagged status that will be checked.
     * 
     * @parameter
     */
    private ConceptDescriptor flaggedConcept = null;

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
    private String outputHtmlFileName = "conflict_report.html";

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
    private String outputTextFileName = "conflict_uuids.txt";

    private class MonitorComponents implements I_ProcessConcepts {

        BufferedWriter textWriter;
        I_TermFactory termFactory;
        int flaggedStatusId;
        int conflicts;
        int conceptCount;
        int agreedChanges;

        public MonitorComponents() throws Exception {
            termFactory = LocalVersionedTerminology.get();
            conflicts = 0;
            conceptCount = 0;
            agreedChanges = 0;
        }

        public void processConcept(I_GetConceptData concept) throws Exception {

            if (flaggedConcept != null) {
                flaggedStatusId = termFactory.getConcept(flaggedConcept.getVerifiedConcept().getUids()).getConceptId();
            }

            int updatedStatusId = termFactory.getConcept(updatedStatus.getVerifiedConcept().getUids()).getConceptId();

            // get origins
            I_Path architectonicPath = termFactory.getPath(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids());

            I_Position latestOnArchitectonicPath = termFactory.newPosition(architectonicPath, Integer.MAX_VALUE);
            Set<I_Position> origins = new HashSet<I_Position>();
            origins.add(latestOnArchitectonicPath);

            // get the branch to copy to concept/path
            I_GetConceptData copyToConcept = branchToCopyTo.getVerifiedConcept();
            I_Path copyToPath = termFactory.getPath(copyToConcept.getUids());

            // get all the positions for the branches to be compared
            List<I_Position> positions = new LinkedList<I_Position>();
            for (ConceptDescriptor branch : branchesToCompare) {
                I_GetConceptData compareConcept = branch.getVerifiedConcept();
                I_Position comparePosition = termFactory.newPosition(termFactory.getPath(compareConcept.getUids()),
                    Integer.MAX_VALUE);
                positions.add(comparePosition);
            }
            List<I_ConceptAttributeTuple> allConceptAttributeTuples = new LinkedList<I_ConceptAttributeTuple>();
            List<I_ConceptAttributeTuple> conceptAttributeTuples1 = new LinkedList<I_ConceptAttributeTuple>();
            List<I_ConceptAttributeTuple> conceptAttributeTuples2 = new LinkedList<I_ConceptAttributeTuple>();

            List<I_DescriptionTuple> allDescriptionTuples = new LinkedList<I_DescriptionTuple>();
            List<I_DescriptionTuple> descriptionTuples1 = new LinkedList<I_DescriptionTuple>();
            List<I_DescriptionTuple> descriptionTuples2 = new LinkedList<I_DescriptionTuple>();

            List<I_RelTuple> allRelationshipTuples = new LinkedList<I_RelTuple>();
            List<I_RelTuple> relationshipTuples1 = new LinkedList<I_RelTuple>();
            List<I_RelTuple> relationshipTuples2 = new LinkedList<I_RelTuple>();

            // get latest concept attributes/descriptions/relationships
            boolean attributesMatch = true;
            boolean descriptionsMatch = true;
            boolean relationshipsMatch = true;
            for (int i = 0; i > positions.size(); i++) {

                Set<I_Position> firstPosition = new HashSet<I_Position>();
                firstPosition.add(positions.get(0));
                Set<I_Position> secondPosition = new HashSet<I_Position>();
                secondPosition.add(positions.get(i + 1));

                conceptAttributeTuples1 = concept.getConceptAttributeTuples(null, firstPosition);
                conceptAttributeTuples2 = concept.getConceptAttributeTuples(null, secondPosition);
                if (flaggedConcept != null) {
                    if (!CompareComponents.attributeListsEqual(conceptAttributeTuples1, conceptAttributeTuples2,
                        flaggedStatusId)) {
                        attributesMatch = false;
                        break;
                    }
                } else if (!CompareComponents.attributeListsEqual(conceptAttributeTuples1, conceptAttributeTuples2)) {
                    attributesMatch = false;
                    break;
                }

                descriptionTuples1 = concept.getDescriptionTuples(null, null, firstPosition);
                descriptionTuples2 = concept.getDescriptionTuples(null, null, secondPosition);
                if (flaggedConcept != null) {
                    if (!CompareComponents.descriptionListsEqual(descriptionTuples1, descriptionTuples2,
                        flaggedStatusId)) {
                        descriptionsMatch = false;
                        break;
                    }
                } else if (!CompareComponents.descriptionListsEqual(descriptionTuples1, descriptionTuples2)) {
                    descriptionsMatch = false;
                    break;
                }

                relationshipTuples1 = concept.getSourceRelTuples(null, null, firstPosition, false);
                relationshipTuples2 = concept.getSourceRelTuples(null, null, secondPosition, false);
                if (flaggedConcept != null) {
                    if (!CompareComponents.relationshipListsEqual(relationshipTuples1, relationshipTuples2,
                        flaggedStatusId)) {
                        relationshipsMatch = false;
                        break;
                    }
                } else if (!CompareComponents.relationshipListsEqual(relationshipTuples1, relationshipTuples2)) {
                    relationshipsMatch = false;
                    break;
                }

                allConceptAttributeTuples.addAll(conceptAttributeTuples1);
                allConceptAttributeTuples.addAll(conceptAttributeTuples2);

                allDescriptionTuples.addAll(descriptionTuples1);
                allDescriptionTuples.addAll(descriptionTuples2);

                allRelationshipTuples.addAll(relationshipTuples1);
                allRelationshipTuples.addAll(relationshipTuples2);
            }

            // check if the latest tuples are equal (excluding criteria)
            if (descriptionsMatch && relationshipsMatch && attributesMatch) {
                agreedChanges++;
                // copy latest attributes to new path/version
                for (I_ConceptAttributeTuple tuple : allConceptAttributeTuples) {
                    I_ConceptAttributePart newPart = tuple.duplicatePart();
                    newPart.setVersion(Integer.MAX_VALUE);
                    newPart.setPathId(copyToPath.getConceptId());
                    newPart.setConceptStatus(updatedStatusId);
                    tuple.getConVersioned().addVersion(newPart);
                }
                // copy latest descriptions to new path/version
                for (I_DescriptionTuple tuple : allDescriptionTuples) {
                    I_DescriptionPart newPart = tuple.duplicatePart();
                    newPart.setVersion(Integer.MAX_VALUE);
                    newPart.setPathId(copyToPath.getConceptId());
                    newPart.setStatusId(updatedStatusId);
                    tuple.getDescVersioned().addVersion(newPart);
                }
                // copy latest relationships to new path/version
                for (I_RelTuple tuple : allRelationshipTuples) {
                    I_RelPart newPart = tuple.duplicatePart();
                    newPart.setVersion(Integer.MAX_VALUE);
                    newPart.setPathId(copyToPath.getConceptId());
                    newPart.setStatusId(updatedStatusId);
                    tuple.getRelVersioned().addVersion(newPart);
                }
            } else {
                conflicts++;
                getLog().info("CONFLICT: " + concept);
                if (textWriter == null) {
                    outputTextDirectory.mkdirs();
                    textWriter = new BufferedWriter(new BufferedWriter(new FileWriter(outputTextDirectory
                        + File.separator + outputTextFileName)));
                }
                textWriter.append(concept.getUids().toString());
            }

            conceptCount++;
            termFactory.addUncommitted(concept);
        }

        public BufferedWriter getTextWriter() {
            return textWriter;
        }

        public void setTextWriter(BufferedWriter textWriter) {
            this.textWriter = textWriter;
        }

        public int getConflicts() {
            return conflicts;
        }

        public void setConflicts(int conflicts) {
            this.conflicts = conflicts;
        }

        public int getConceptCount() {
            return conceptCount;
        }

        public void setConceptCount(int conceptCount) {
            this.conceptCount = conceptCount;
        }

        public int getAgreedChanges() {
            return agreedChanges;
        }

        public void setAgreedChanges(int agreedChanges) {
            this.agreedChanges = agreedChanges;
        }
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        I_TermFactory termFactory = LocalVersionedTerminology.get();
        try {
            MonitorComponents componentMonitor = new MonitorComponents();
            termFactory.iterateConcepts(componentMonitor);
            if (componentMonitor.getConflicts() > 0) {
                outputHtmlDirectory.mkdirs();
                BufferedWriter htmlWriter = new BufferedWriter(new BufferedWriter(new FileWriter(outputHtmlDirectory
                    + File.separator + outputHtmlFileName)));
                htmlWriter.append("Monitored " + componentMonitor.getConceptCount() + " components.");
                htmlWriter.append("<br>");
                htmlWriter.append("Number of agreed changes: " + componentMonitor.getAgreedChanges());
                htmlWriter.append("<br>");
                htmlWriter.append("Number of conflicts: " + componentMonitor.getConflicts());

                htmlWriter.close();
            }

            if (componentMonitor.getTextWriter() != null) {
                componentMonitor.getTextWriter().close();
            }

        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

}
