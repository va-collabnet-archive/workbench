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
import java.util.Set;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.dwfa.mojo.ConceptConstants;
import org.dwfa.tapi.TerminologyException;

/**
 * 
 * This mojo exports attribute value reference set from an ACE database
 * 
 * @goal export-cvt3-snomed-id-refset
 * @author Ean Dungey, Dion McMurtrie
 */
public class Ctv3SnomedIdMapReferenceSetExport extends ReferenceSetExport {
    /**
     * Database access object.
     */
    private I_TermFactory tf = LocalVersionedTerminology.get();

    /**
     * List of specified statuses in the <code>exportSpecifications</code>.
     */
    private I_IntSet allowedStatuses;

    /**
     * List of specified positions in the <code>exportSpecifications</code>.
     */
    private Set<I_Position> positions;

    /**
     * Check if the concept meets the <code>exportSpecifications</code>, is on
     * the <code>positions</code> and has the <code>allowedStatuses</code>
     * 
     * @see org.dwfa.ace.api.I_ProcessConcepts#processConcept(org.dwfa.ace.api.I_GetConceptData)
     */
    public void processConcept(I_GetConceptData concept) throws Exception {
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
        I_ThinExtByRefTuple tuple = getCurrentExtension(conceptId, ConceptConstants.CTV3_ID_MAP_EXTENSION);
        I_ThinExtByRefPartString part = (I_ThinExtByRefPartString) tuple;
        I_IdPart ctv3IdPart = getLatestVersion(tf.getConcept(conceptId).getIdentifier().getMutableParts(),
            ArchitectonicAuxiliary.Concept.SNOMED_T3_UUID);
        if (part == null && ctv3IdPart != null) {
            part = tf.newStringExtensionPart();
            part.setStringValue(ctv3IdPart.getDenotation().toString());
            part.setPathId(latest.getPathId());
            part.setStatusId(org.dwfa.cement.ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
            part.setVersion(latest.getVersion());
            export(part, null, ConceptConstants.CTV3_ID_MAP_EXTENSION.localize().getNid(), conceptId);
        } else if (part.getStringValue().equals(latest.getPartComponentNids()) && ctv3IdPart != null) {
            part.setStringValue(ctv3IdPart.getDenotation().toString());
            export((I_ThinExtByRefTuple) part);
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
        I_ThinExtByRefTuple tuple = getCurrentExtension(conceptId, ConceptConstants.SNOMED_ID_MAP_EXTENSION);
        I_ThinExtByRefPartString part = (I_ThinExtByRefPartString) tuple;
        I_IdPart snomedIdPart = getLatestVersion(tf.getConcept(conceptId).getIdentifier().getMutableParts(),
            ArchitectonicAuxiliary.Concept.SNOMED_INT_ID);
        if (part == null && snomedIdPart != null) {
            part = tf.newStringExtensionPart();
            part.setStringValue(snomedIdPart.getDenotation().toString());
            part.setPathId(latest.getPathId());
            part.setStatusId(org.dwfa.cement.ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
            part.setVersion(latest.getVersion());
            export(part, null, ConceptConstants.SNOMED_ID_MAP_EXTENSION.localize().getNid(), conceptId);
        } else if (part.getStringValue().equals(latest.getPartComponentNids()) && snomedIdPart != null) {
            part.setStringValue(snomedIdPart.getDenotation().toString());
            export((I_ThinExtByRefTuple) part);
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
    private I_IdPart getLatestVersion(List<? extends I_IdPart> idParts, Concept sourceConcept) throws TerminologyException,
            IOException {
        I_IdPart latestVersion = null;

        for (I_IdPart iIdPart : idParts) {
            if (iIdPart.getAuthorityNid() == tf.uuidToNative(sourceConcept.getUids())
                && (latestVersion == null || iIdPart.getVersion() > latestVersion.getVersion())) {
                latestVersion = iIdPart;
            }
        }

        return latestVersion;
    }
}
