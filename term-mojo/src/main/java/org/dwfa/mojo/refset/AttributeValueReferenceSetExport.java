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

import java.util.logging.Logger;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;
import org.dwfa.mojo.ConceptConstants;
import org.dwfa.vodb.types.ThinExtByRefPartConcept;

/**
 * 
 * This mojo exports attribute value reference set from an ACE database
 * 
 * @goal export-attirbute-value-refset
 * @author Ean Dungey, Dion McMurtrie
 */
public class AttributeValueReferenceSetExport extends ReferenceSetExport {
    private Logger logger = Logger.getLogger(this.getClass().getName());

    int activeNId;
    int inActiveNId;
    int currentNId;
    int relationshipRefinability;
    int relationshipRefinabilityExtension;
    int descriptionInactivationIndicator;
    int relationshipInactivationIndicator;
    int conceptInactivationIndicator;

    /**
     * Number of processed concepts.
     */
    private int processedConceptsCount = 0;

    /**
     * @see org.dwfa.mojo.refset.ReferenceSetExport#execute()
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            activeNId = org.dwfa.cement.ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid();
            inActiveNId = org.dwfa.cement.ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid();
            currentNId = org.dwfa.cement.ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid();
            relationshipRefinability = ConceptConstants.RELATIONSHIP_REFINABILITY_EXTENSION.localize().getNid();
            relationshipRefinabilityExtension = ConceptConstants.RELATIONSHIP_REFINABILITY_EXTENSION.localize()
                .getNid();
            descriptionInactivationIndicator = ConceptConstants.DESCRIPTION_INACTIVATION_INDICATOR.localize().getNid();
            relationshipInactivationIndicator = ConceptConstants.RELATIONSHIP_INACTIVATION_INDICATOR.localize()
                .getNid();
            conceptInactivationIndicator = ConceptConstants.CONCEPT_INACTIVATION_INDICATOR.localize().getNid();
        } catch (Exception e) {
            throw new MojoExecutionException("", e);
        }

        super.execute();
    }

    /**
     * Check if the concept meets the <code>exportSpecifications</code>, is on
     * the <code>positions</code> and has the <code>allowedStatuses</code>
     * 
     * @see org.dwfa.ace.api.I_ProcessConcepts#processConcept(org.dwfa.ace.api.I_GetConceptData)
     */
    public void processConcept(I_GetConceptData concept) throws Exception {
        if (first) {
            new ConceptProcessor(concept).run();
        } else {
            workQueue.execute(new ConceptProcessor(concept));
        }
    }

    /**
     * Export the relationships for the concepts latest version
     * 
     * @param versionedRel the concept to export the relationships for.
     * 
     * @throws Exception DB errors.
     */
    private void processRelationship(I_RelVersioned versionedRel) throws Exception {
        if (testSpecification(versionedRel.getC2Id())) {
            boolean exportableVersionFound = false;
            I_RelPart latest = null;
            for (I_RelPart part : versionedRel.getVersions()) {
                if (checkPath(part.getPathId()) && allowedStatuses.contains(part.getStatusId())
                    && testSpecificationWithCache(part.getCharacteristicId())
                    && testSpecificationWithCache(part.getPathId())
                    && testSpecificationWithCache(part.getRefinabilityId())) {

                    exportableVersionFound = true;
                    if (latest == null || latest.getVersion() < part.getVersion()) {
                        latest = part;
                    }
                }
            }
            if (exportableVersionFound) {
                // found a valid version of this relationship for export
                int relId = versionedRel.getRelId();
                extractRelationshipRefinability(latest, relId);
                extractStatus(latest, relId, relationshipInactivationIndicator);
            }
        }
    }

    /**
     * Export the descriptions for the concepts latest version
     * 
     * @param versionedDesc the description to export the inactivation status
     *            for.
     * 
     * @throws Exception DB errors.
     */
    private void processDescription(I_DescriptionVersioned versionedDesc) throws Exception {
        boolean exportableVersionFound = false;
        I_DescriptionPart latest = null;
        for (I_DescriptionPart part : versionedDesc.getVersions()) {
            if (checkPath(part.getPathId()) && allowedStatuses.contains(part.getStatusId())
                && testSpecificationWithCache(part.getTypeId())) {

                exportableVersionFound = true;
                if (latest == null || latest.getVersion() < part.getVersion()) {
                    latest = part;
                }

            }
        }

        if (exportableVersionFound) {
            int descId = versionedDesc.getDescId();
            extractStatus(latest, descId, descriptionInactivationIndicator);
        }
    }

    /**
     * Create/update and export status concept extensions for all concepts that
     * are not active, inactive
     * or current as we want to know why they are active, inactive or current.
     * 
     * @param latest I_AmPart latest version of the concept
     * @param relId concept id
     * @throws Exception cannot create or export the concept.
     */
    private void extractStatus(I_AmPart latest, int relId, int inactivationRefset) throws Exception {
        I_ThinExtByRefTuple tuple = getCurrentExtension(relId, inactivationRefset);
        I_ThinExtByRefPartConcept part = (I_ThinExtByRefPartConcept) tuple;
        if (part == null) {
            // if the status is INACTIVE or ACTIVE there is no need for a
            // reason. For simplicity, CURRENT will be treated this way too,
            if (latest.getStatusId() != activeNId && latest.getStatusId() != inActiveNId
                && latest.getStatusId() != currentNId) {
                // no extension at all
                // part = tf.newExtensionPart(ThinExtByRefPartConcept.class);
                part = new ThinExtByRefPartConcept();// stunt extension part.
                part.setC1id(latest.getStatusId());
                part.setPathId(latest.getPathId());
                part.setStatusId(activeNId);
                part.setVersion(latest.getVersion());
                export(part, null, inactivationRefset, relId, TYPE.RELATIONSHIP);
            }
        } else if (part.getC1id() != latest.getStatusId()) {
            // add a new row with the latest refinability
            part.setC1id(latest.getStatusId());
            export((I_ThinExtByRefTuple) part, TYPE.RELATIONSHIP);
        }
    }

    /**
     * Create/update and export concept extensions (relationships refinability
     * reference) for all concepts.
     * 
     * @param latest I_AmPart latest version of the concept
     * @param relId concept id
     * @throws Exception cannot create or export the concept.
     */
    private void extractRelationshipRefinability(I_RelPart latest, int relId) throws Exception {
        I_ThinExtByRefTuple tuple = getCurrentExtension(relId, relationshipRefinabilityExtension);
        I_ThinExtByRefPartConcept part = (I_ThinExtByRefPartConcept) tuple;
        if (part == null) {
            // no extension at all
            part = new ThinExtByRefPartConcept();// stunt extension part.
            part.setC1id(latest.getRefinabilityId());
            part.setPathId(latest.getPathId());
            part.setStatusId(activeNId);
            part.setVersion(latest.getVersion());
            export(part, null, relationshipRefinability, relId, TYPE.RELATIONSHIP);
        } else if (part.getC1id() != latest.getRefinabilityId()) {
            // add a new row with the latest refinability
            part.setC1id(latest.getRefinabilityId());
            export((I_ThinExtByRefTuple) tuple, TYPE.RELATIONSHIP);
        }
    }

    private class ConceptProcessor implements Runnable {
        private I_GetConceptData conceptToProcess;

        ConceptProcessor(I_GetConceptData conceptToProcess) {
            this.conceptToProcess = conceptToProcess;
        }

        @Override
        public void run() {
            try {
                processConcept(conceptToProcess);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        void processConcept(I_GetConceptData concept) throws Exception {
            processedConceptsCount++;

            if (testSpecification(concept)) {
                // export the status refset for this concept
                I_ConceptAttributePart latest = getLatestAttributePart(concept);
                if (latest == null) {
                    getLog().warn(
                        "Concept " + concept + " is exportable for specification " + exportSpecifications
                            + " but has no parts valid for statuses " + allowedStatuses + " and positions " + positions);
                    return;
                }

                for (I_DescriptionVersioned desc : concept.getDescriptions()) {
                    processDescription(desc);
                }

                for (I_RelVersioned rel : concept.getSourceRels()) {
                    processRelationship(rel);
                }

                extractStatus(latest, concept.getConceptId(), conceptInactivationIndicator);
            }

            if (processedConceptsCount % 1000 == 0) {
                logger.info("Processed " + processedConceptsCount + " Concepts");
            }
        }
    }
}
