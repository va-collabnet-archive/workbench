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

import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;

/**
 * Creates an extension. To be used in testing VodbCalculateMemberSet class.
 * 
 * @author Christine Hill
 * 
 */

/**
 * 
 * @goal vodb-create-extension
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */

public class VodbCreateExtension extends AbstractMojo {

    /**
     * @parameter
     * @required
     *           The concept descriptor for the ref set spec.
     */
    private ConceptDescriptor refSetSpecDescriptor;

    /**
     * @parameter
     * @required
     *           The concept descriptor for the member set path.
     */
    private ConceptDescriptor refSetPathDescriptor;

    /**
     * @parameter
     * @required
     *           The ID of the reference set of which we wish to calculate the
     *           member set.
     */
    private ConceptDescriptor refSetTypeDescriptor;

    /**
     * @parameter
     */
    private boolean overrideRefSetTypeWithRandomType = false;

    private I_Path memberSetPath;

    public void execute() throws MojoExecutionException, MojoFailureException {

        I_TermFactory termFactory = LocalVersionedTerminology.get();
        try {

            I_GetConceptData refSetPathConcept = refSetPathDescriptor.getVerifiedConcept();
            memberSetPath = termFactory.getPath(refSetPathConcept.getUids());

            // execute calculate member set plugin
            ExtensionCreator extensionCreator = new ExtensionCreator(refSetSpecDescriptor, refSetTypeDescriptor);

            // iterate over each concept
            termFactory.iterateConcepts(extensionCreator);

            String message = "Number of new extensions: " + extensionCreator.getExtensionCount();
            getLog().info(message);

            System.out.println("Uncommitted end: " + termFactory.getUncommitted().size());
            termFactory.commit();

        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

    private class ExtensionCreator implements I_ProcessConcepts {
        private I_TermFactory termFactory;

        private int extensionCount;

        private int referenceSetId;

        private int typeId;

        public ExtensionCreator(ConceptDescriptor referenceSetDescriptor, ConceptDescriptor typeConceptDescriptor)
                throws Exception {
            termFactory = LocalVersionedTerminology.get();
            extensionCount = 0;
            I_GetConceptData refConcept = referenceSetDescriptor.getVerifiedConcept();
            this.referenceSetId = refConcept.getConceptId();
            I_GetConceptData typeConcept = typeConceptDescriptor.getVerifiedConcept();
            this.typeId = typeConcept.getConceptId();
        }

        public void processConcept(I_GetConceptData concept) throws Exception {

            int conceptId = concept.getConceptId();
            int currentStatusId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids()
                .iterator()
                .next());

            int memberId = termFactory.uuidToNativeWithGeneration(UUID.randomUUID(),
                ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(), termFactory.getPaths(),
                Integer.MAX_VALUE);

            int includeLineageId = termFactory.uuidToNative(RefsetAuxiliary.Concept.INCLUDE_LINEAGE.getUids()
                .iterator()
                .next());
            int includeIndividualId = termFactory.uuidToNative(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.getUids()
                .iterator()
                .next());
            int excludeLineageId = termFactory.uuidToNative(RefsetAuxiliary.Concept.EXCLUDE_LINEAGE.getUids()
                .iterator()
                .next());
            int excludeIndividualId = termFactory.uuidToNative(RefsetAuxiliary.Concept.EXCLUDE_INDIVIDUAL.getUids()
                .iterator()
                .next());
            int conceptTypeId = termFactory.uuidToNative(RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids()
                .iterator()
                .next());

            int i = 1 + (int) (Math.random() * 10);
            boolean skipExtension = false;
            if (overrideRefSetTypeWithRandomType) {
                if (i == 1) {
                    typeId = includeLineageId;
                } else if (i == 2) {
                    typeId = includeIndividualId;
                } else if (i == 3) {
                    typeId = excludeLineageId;
                } else if (i == 4) {
                    typeId = excludeIndividualId;
                } else {
                    skipExtension = true;
                }
            }

            if (!skipExtension) {
                I_ThinExtByRefVersioned extension = termFactory.newExtension(referenceSetId, memberId, conceptId,
                    conceptTypeId);

                I_ThinExtByRefPartConcept conceptExtension = termFactory.newConceptExtensionPart();

                conceptExtension.setPathId(memberSetPath.getConceptId());
                conceptExtension.setStatus(currentStatusId);
                conceptExtension.setVersion(Integer.MAX_VALUE);
                conceptExtension.setConceptId(typeId);

                extension.addVersion(conceptExtension);

                termFactory.addUncommitted(concept);

                extensionCount++;
            }

        }

        /**
         * Gets the number of extensions created.
         * 
         * @return
         */
        public int getExtensionCount() {
            return extensionCount;
        }

        /**
         * Sets the number of extensions.
         * 
         * @param memberSetCount
         */
        public void setExtensionCount(int extensionCount) {
            this.extensionCount = extensionCount;
        }
    }
}
