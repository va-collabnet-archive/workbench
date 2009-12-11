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
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_GetExtensionData;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;

/**
 * Validates a specified reference set.
 * Illegal for a concept to inherit conflicting rules from parents.
 * e.g. if one parent says include children, and other parent has
 * exclude children.
 * @author Christine Hill
 *
 */

/**
 *
 * @goal vodb-validate-ref-set
 *
 * @phase process-resources
 * @requiresDependencyResolution compile
 */

public class VodbValidateRefSet extends AbstractMojo {

    public final int ROOT = -1;
    public final int NO_TYPE_DEFINED = 0;
    public final int CONFLICT = -2;
    public final int VALID = -3;

    /**
     * @parameter
     * @required
     * The concept descriptor for the ref set spec.
     */
    private ConceptDescriptor refSetSpecDescriptor;


    /**
     * Location to write list of conflicts - uuid and fsn.
     * @parameter
     */
    private File conflictsOutputFile = new File("conflicts");

    private BufferedWriter conflictsWriter;

    /**
     * Iterates over each concept and calculates the member set.
     */
    public void execute() throws MojoExecutionException, MojoFailureException {

        I_TermFactory termFactory = LocalVersionedTerminology.get();
        try {

            conflictsOutputFile.getParentFile().mkdirs();
            conflictsWriter = new BufferedWriter(
                    new FileWriter(conflictsOutputFile));

            // execute calculate member set plugin
            MemberSetCalculator calculator = new MemberSetCalculator();

            termFactory.iterateConcepts(calculator);

            String message = "Number of concepts with invalid ref set lineage: "
                            + calculator.getInvalidConceptsCount();
            getLog().info(message);

            conflictsWriter.newLine();
            conflictsWriter.write("While verifying ref set spec: "
                    + refSetSpecDescriptor.getUuid()
                    + " : "
                    + refSetSpecDescriptor.getDescription());
            conflictsWriter.newLine();
            conflictsWriter.write("--------------------------------------");
            conflictsWriter.newLine();
            conflictsWriter.write(message);
            conflictsWriter.flush();
            conflictsWriter.close();

        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

    private class MemberSetCalculator implements I_ProcessConcepts {

        private I_TermFactory termFactory;
        private int invalidConcepts;
        private int referenceSetId;
        private int includeLineageId;
        private int excludeLineageId;
        private int includeIndividualId;
        private int excludeIndividualId;
        private int conceptTypeId;

        /**
         * Calculates a member set given a reference set spec.
         * @param referenceSetId The id of the reference set of which we wish to
         * calculate the member set.
         * @throws Exception
         */
        public MemberSetCalculator() throws Exception {

            termFactory = LocalVersionedTerminology.get();

            // verify concepts
            I_GetConceptData refConcept = refSetSpecDescriptor.getVerifiedConcept();
            referenceSetId = refConcept.getConceptId();

            invalidConcepts = 0;

            includeIndividualId = termFactory.uuidToNative(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.getUids().iterator().next());
            excludeIndividualId = termFactory.uuidToNative(RefsetAuxiliary.Concept.EXCLUDE_INDIVIDUAL.getUids().iterator().next());
            includeLineageId = termFactory.uuidToNative(RefsetAuxiliary.Concept.INCLUDE_LINEAGE.getUids().iterator().next());
            excludeLineageId = termFactory.uuidToNative(RefsetAuxiliary.Concept.EXCLUDE_LINEAGE.getUids().iterator().next());
            conceptTypeId = termFactory.uuidToNative(RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids().iterator().next());
        }

        /**
         * Processes each concept in the database. Validate that any inherited
         * conditions from parents are non-conflicting.
         */
        public void processConcept(I_GetConceptData concept) throws Exception {

            int conceptId = concept.getConceptId();

            if (getLatestRefSetType(concept) == 0) {
                // no ref sets found so need to find the parents of this concept
                // for processing

                List<Integer> parentIds = getParents(concept);

                int result = processParents(parentIds);

                if (result == CONFLICT) {
                    conflictsWriter.newLine();
                    conflictsWriter.write("CONFLICT FOR CONCEPT: "
                            + termFactory.getUids(conceptId) + " : "
                            + getFsnFromConceptId(conceptId));
                    conflictsWriter.newLine();
                    conflictsWriter.write("--------------------------------------");
                    invalidConcepts++;
                }
            }
        }

        /**
         * Returns the FSN associated with the given concept.
         * @param conceptId the ID of the concept who's FSN we want.
         * @return The FSN associated with the specified concept.
         * @throws Exception
         */
        public String getFsnFromConceptId(int conceptId) throws Exception {

            I_GetConceptData concept = LocalVersionedTerminology.get().getConcept(conceptId);

            List<I_DescriptionVersioned> descriptions = concept.getDescriptions();
            int fsnId = LocalVersionedTerminology.get().uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.
                    getUids().iterator().next());
            for (I_DescriptionVersioned description : descriptions) {
                List<I_DescriptionPart> parts = description.getVersions();
                for (I_DescriptionPart part : parts) {
                    if (fsnId == part.getTypeId()) {
                        return part.getText();
                    }
                }
            }

            return "unknown";
        }

        /**
         * Calculates if an agreed inherited ref set type can be calculated given
         * the list of parents.
         * @return CONFLICT if the values conflict, VALID if the values are valid &
         * non-conflicting. ROOT is returned if the concept has no parents to process.
         */
        public int processParents(List<Integer> parentIds) throws Exception {

            HashMap<Integer, Integer> parentSetTypes = new HashMap<Integer, Integer>();

            for (Integer parentId : parentIds) {
                int parentType = getLatestRefSetType(termFactory.getConcept(parentId));

                int finalParentType = NO_TYPE_DEFINED;
                if (parentType == NO_TYPE_DEFINED || parentType == includeIndividualId ||
                        parentType == excludeIndividualId) {
                    // parent has no inheritable type, so need to process its parents

                    List<Integer> grandParentIds = getParents(
                            termFactory.getConcept(parentId));
                    finalParentType = processParents(grandParentIds);
                } else {
                    finalParentType = parentType;
                }
                parentSetTypes.put(finalParentType, parentId);
            }

            if (parentIds.size() == 0) {
                // concept has no parents, so return root value
                return ROOT;
            }

            if (parentSetTypes.keySet().contains(excludeLineageId) &&
                    parentSetTypes.keySet().contains(includeLineageId)) {
                conflictsWriter.newLine();
                conflictsWriter.write("EXCLUDED LINEAGE: "
                        + termFactory.getUids(parentSetTypes.get(excludeLineageId)));
                conflictsWriter.write("\t" + getFsnFromConceptId(parentSetTypes.get(excludeLineageId)));
                conflictsWriter.newLine();
                conflictsWriter.write("INCLUDED LINEAGE: "
                        + termFactory.getUids(parentSetTypes.get(includeLineageId)));
                conflictsWriter.write("\t" + getFsnFromConceptId(parentSetTypes.get(includeLineageId)));
                conflictsWriter.newLine();
                return CONFLICT;
            } else if (parentSetTypes.keySet().contains(CONFLICT)) {
                return CONFLICT;
            } else if (parentSetTypes.keySet().contains(includeLineageId)) {
                return includeLineageId;
            } else if (parentSetTypes.keySet().contains(includeIndividualId)) {
                return includeIndividualId;
            } else if (parentSetTypes.keySet().contains(excludeLineageId)) {
                return excludeLineageId;
            } else if (parentSetTypes.keySet().contains(excludeIndividualId)) {
                return excludeIndividualId;
            } else if (parentSetTypes.keySet().contains(NO_TYPE_DEFINED)) {
                return NO_TYPE_DEFINED;
            } else if (parentSetTypes.keySet().contains(ROOT)) {
                return ROOT;
            } else {
                return VALID;
            }
        }

        /**
         * Gets the parents of a particular concept.
         * @param concept The concept whose parents we want to find.
         * @return List of parent IDs.
         */
        public List<Integer> getParents(I_GetConceptData concept) throws Exception {
            I_IntSet isARel = termFactory.newIntSet();
            isARel.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.
                    IS_A_REL.getUids()).getConceptId());
            List<I_RelTuple> relTuples = concept.getSourceRelTuples(null, isARel, null, false);
            List<Integer> parentIds = new ArrayList<Integer>();
            for (I_RelTuple currentRel : relTuples) {
                parentIds.add(currentRel.getC2Id());
            }
            return parentIds;
        }

        /**
         * Gets the latest ref set type for a concept (e.g. include individual).
         * @param concept The concept.
         * @return int representing the internal id of the ref set type.
         */
        public int getLatestRefSetType(I_GetConceptData concept) throws Exception {

            int conceptId = concept.getConceptId();

            List<I_GetExtensionData> extensions =
                termFactory.getExtensionsForComponent(conceptId);

            for (I_GetExtensionData extensionData: extensions) {
                I_ThinExtByRefVersioned part = extensionData.getExtension();
                List<? extends I_ThinExtByRefPart> extensionVersions = part.getVersions();

                if (part.getTypeId() == conceptTypeId &&
                            part.getRefsetId() == referenceSetId) {

                    int latest = Integer.MIN_VALUE;
                    for (I_ThinExtByRefPart currentVersion : extensionVersions) {
                        if (currentVersion.getVersion() > latest) {
                            latest = currentVersion.getVersion();
                        }
                    }
                    int typeId = 0;

                    for (I_ThinExtByRefPart currentVersion : extensionVersions) {
                        if (currentVersion.getVersion() == latest) {
                            I_ThinExtByRefPartConcept temp = (I_ThinExtByRefPartConcept) currentVersion;
                            typeId = temp.getConceptId();
                            return typeId;
                        }
                    }
                }
            }
            return NO_TYPE_DEFINED;
        }

        /**
         * Gets the number of members in the specified reference set.
         * @return
         */
        public int getInvalidConceptsCount() {

            return invalidConcepts;
        }

        /**
         * Sets the number of members in the specified reference set.
         * @param memberSetCount
         */
        public void setInvalidConceptsCount(int invalidConcepts) {

            this.invalidConcepts = invalidConcepts;
        }
    }

    public ConceptDescriptor getRefSetSpecDescriptor() {
        return refSetSpecDescriptor;
    }

    public void setRefSetSpecDescriptor(ConceptDescriptor refSetSpecDescriptor) {
        this.refSetSpecDescriptor = refSetSpecDescriptor;
    }

}
