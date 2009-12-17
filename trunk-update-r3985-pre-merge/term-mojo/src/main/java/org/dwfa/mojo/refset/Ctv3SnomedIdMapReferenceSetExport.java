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

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;
import org.dwfa.mojo.ConceptConstants;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.ThinExtByRefPartString;

/**
 * 
 * This mojo exports attribute value reference set from an ACE database
 * 
 * @goal export-cvt3-snomed-id-refset
 * @author Ean Dungey, Dion McMurtrie
 */
public class Ctv3SnomedIdMapReferenceSetExport extends ReferenceSetExport {
    private Logger logger = Logger.getLogger(this.getClass().getName());
    /**
     * Number of processed concepts.
     */
    private int processedConceptsCount = 0;

    int ctv3IdMapExtension;
    int snomedIdMapExtension;

    /**
     * 
     * @see org.dwfa.mojo.refset.ReferenceSetExport#execute()
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            ctv3IdMapExtension = ConceptConstants.CTV3_ID_MAP_EXTENSION.localize().getNid();
            snomedIdMapExtension = ConceptConstants.SNOMED_ID_MAP_EXTENSION.localize().getNid();
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
     * Create/update and export string extensions for all concepts that have
     * SCT3 ids.
     * 
     * @param latest I_AmPart latest version of the concept
     * @param conceptId concept id
     * @throws Exception cannot create or export the concept.
     */
    private void exportCtv3IdMap(I_AmPart latest, int conceptId) throws Exception {
        I_ThinExtByRefTuple tuple = getCurrentExtension(conceptId, ctv3IdMapExtension);
        I_ThinExtByRefPartString part = (I_ThinExtByRefPartString) tuple;
        I_IdPart ctv3IdPart = getLatestVersion(tf.getConcept(conceptId).getId().getVersions(),
            ArchitectonicAuxiliary.Concept.CTV3_ID);
        if (ctv3IdPart != null) {
            if (part == null) {
                part = new ThinExtByRefPartString();// stunt extension part.
                part.setStringValue(ctv3IdPart.getSourceId().toString());
                part.setPathId(latest.getPathId());
                part.setStatusId(org.dwfa.cement.ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
                part.setVersion(latest.getVersion());
                export(part, null, ConceptConstants.CTV3_ID_MAP_EXTENSION.localize().getNid(), conceptId, TYPE.CONCEPT);
            } else if (part.getStringValue().equals(latest.getPartComponentNids())) {
                part.setStringValue(ctv3IdPart.getSourceId().toString());
                export((I_ThinExtByRefTuple) part, TYPE.CONCEPT);
            }
        }
    }

    /**
     * Create/update and export string extensions for all concepts that have
     * snomed ids.
     * 
     * @param latest I_AmPart latest version of the concept
     * @param conceptId concept id
     * @throws Exception cannot create or export the concept.
     */
    private void exportSnomedIdMap(I_AmPart latest, int conceptId) throws Exception {
        I_ThinExtByRefTuple tuple = getCurrentExtension(conceptId, snomedIdMapExtension);
        I_ThinExtByRefPartString part = (I_ThinExtByRefPartString) tuple;
        I_IdPart snomedIdPart = getLatestVersion(tf.getConcept(conceptId).getId().getVersions(),
            ArchitectonicAuxiliary.Concept.SNOMED_INT_ID);
        if (snomedIdPart != null) {
            if (part == null) {
                part = new ThinExtByRefPartString();// stunt extension part.
                part.setStringValue(snomedIdPart.getSourceId().toString());
                part.setPathId(latest.getPathId());
                part.setStatusId(org.dwfa.cement.ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
                part.setVersion(latest.getVersion());
                export(part, null, ConceptConstants.SNOMED_ID_MAP_EXTENSION.localize().getNid(), conceptId,
                    TYPE.CONCEPT);
            } else if (part.getStringValue().equals(latest.getPartComponentNids())) {
                part.setStringValue(snomedIdPart.getSourceId().toString());
                export((I_ThinExtByRefTuple) part, TYPE.CONCEPT);
            }
        }
    }

    /**
     * Get the latest version for the list of id parts with the source
     * <code>sourceConcept</code>
     * 
     * @param sourceConcept Concept eg SNOMED_T3_UUID, SNOMED_INT_ID etc
     * @return I_IdPart latest Id version for the sourceConcept.
     * @throws IOException DB errors
     * @throws TerminologyException DB errors
     */
    private I_IdPart getLatestVersion(List<I_IdPart> idParts, Concept sourceConcept) throws TerminologyException,
            IOException {
        I_IdPart latestVersion = null;

        for (I_IdPart iIdPart : idParts) {
            if (iIdPart.getSource() == tf.uuidToNative(sourceConcept.getUids())
                && (latestVersion == null || iIdPart.getVersion() > latestVersion.getVersion())) {
                latestVersion = iIdPart;
            }
        }

        return latestVersion;
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

                exportCtv3IdMap(latest, concept.getConceptId());
                exportSnomedIdMap(latest, concept.getConceptId());
            }

            if (processedConceptsCount % 1000 == 0) {
                logger.info("Processed " + processedConceptsCount + " Concepts");
            }
        }
    }
}
