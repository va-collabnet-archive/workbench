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

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.dwfa.mojo.ConceptConstants;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.PositionDescriptor;
import org.dwfa.mojo.refset.writers.MemberRefsetHandler;
import org.dwfa.tapi.TerminologyException;

/**
 * 
 * This mojo exports attribute value reference set from an ACE database
 * 
 * @goal export-attirbute-value-refset
 * @author Ean Dungey, Dion McMurtrie
 */
public class AttributeValueReferenceSetExport extends ReferenceSetExport {
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
     * File writer maps.
     */
    private HashMap<String, BufferedWriter> writerMap = new HashMap<String, BufferedWriter>();

    /**
     * Setup files <code>positions</code> <code>exportSpecifications</code>
     * <code>allowedStatuses</code> <code>sctidRefsetOutputDirectory</code>
     * <code>uuidRefsetOutputDirectory</code> and iterate over concepts in
     * the DB.
     * 
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException, MojoFailureException {

        if (!fixedMapDirectory.exists() || !fixedMapDirectory.isDirectory() || !fixedMapDirectory.canRead()) {
            throw new MojoExecutionException("Cannot proceed, fixedMapDirectory must exist and be readable");
        }

        if (!readWriteMapDirectory.exists() || !readWriteMapDirectory.isDirectory() || !readWriteMapDirectory.canRead()) {
            throw new MojoExecutionException("Cannot proceed, readWriteMapDirectory must exist and be readable");
        }

        try {
            allowedStatuses = tf.newIntSet();
            positions = new HashSet<I_Position>();
            for (ExportSpecification spec : exportSpecifications) {
                for (PositionDescriptor pd : spec.getPositionsForExport()) {
                    positions.add(pd.getPosition());
                }
                for (ConceptDescriptor status : spec.getStatusValuesForExport()) {
                    allowedStatuses.add(status.getVerifiedConcept().getConceptId());
                }
            }

            sctidRefsetOutputDirectory.mkdirs();
            uuidRefsetOutputDirectory.mkdirs();

            MemberRefsetHandler.setFixedMapDirectory(fixedMapDirectory);
            MemberRefsetHandler.setReadWriteMapDirectory(readWriteMapDirectory);
            if (rf2Descriptor != null && rf2Descriptor.getModule() != null) {
                MemberRefsetHandler.setModule(rf2Descriptor.getModule());
            }

            tf.iterateConcepts(this);

            for (BufferedWriter writer : writerMap.values()) {
                writer.close();
            }

            MemberRefsetHandler.cleanup();

        } catch (Exception e) {
            throw new MojoExecutionException("exporting reference sets failed for specification "
                + exportSpecifications, e);
        }
    }

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

            for (I_RelVersioned rel : concept.getSourceRels()) {
                processRelationship(rel);
            }

            extractStatus(latest, concept.getConceptId());
            extractDefinitionType(latest, concept.getConceptId());
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
            for (I_RelPart part : versionedRel.getMutableParts()) {
                if (testSpecification(part.getCharacteristicId()) && testSpecification(part.getPathId())
                    && testSpecification(part.getRefinabilityId()) && allowedStatuses.contains(part.getStatusId())
                    && checkPath(part.getPathId())) {

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
                extractStatus(latest, relId);
            }
        }
    }

    /**
     * Create/update and export concept extensions (Inactivation refset) for all
     * concepts that are not active, inactive
     * or current.
     * 
     * @param latest I_AmPart latest version of the concept
     * @param relId concept id
     * @throws Exception cannot create or export the concept.
     */
    private void extractStatus(I_AmPart latest, int relId) throws Exception {
        I_ThinExtByRefTuple tuple = getCurrentExtension(relId, ConceptConstants.STATUS_REASON_EXTENSION);
        I_ThinExtByRefPartConcept part = (I_ThinExtByRefPartConcept) tuple;
        if (part == null) {
            // if the status is INACTIVE or ACTIVE there is no need for a
            // reason. For simplicity, CURRENT will be treated this way too,
            if (latest.getStatusId() != getNid(org.dwfa.cement.ArchitectonicAuxiliary.Concept.ACTIVE)
                && latest.getStatusId() != getNid(org.dwfa.cement.ArchitectonicAuxiliary.Concept.INACTIVE)
                && latest.getStatusId() != getNid(org.dwfa.cement.ArchitectonicAuxiliary.Concept.CURRENT)) {
                // no extension at all
                part = tf.newConceptExtensionPart();
                part.setC1id(latest.getStatusId());
                part.setPathId(latest.getPathId());
                part.setStatusId(org.dwfa.cement.ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
                part.setVersion(latest.getVersion());
                export(part, null, ConceptConstants.STATUS_REASON_EXTENSION.localize().getNid(), relId);
            }
        } else if (part.getC1id() != latest.getStatusId()) {
            // add a new row with the latest refinability
            part.setC1id(latest.getStatusId());
            export((I_ThinExtByRefTuple) part);
        }
    }

    /**
     * Create/update and export concept extensions (Reason for inactivation
     * refset) for all concepts.
     * 
     * @param latest I_AmPart latest version of the concept
     * @param relId concept id
     * @throws Exception cannot create or export the concept.
     */
    private void extractDefinitionType(I_ConceptAttributePart latest, int conceptId) throws Exception {
        I_ThinExtByRefTuple tuple = getCurrentExtension(conceptId, ConceptConstants.DEFINITION_TYPE_EXTENSION);
        I_ThinExtByRefPartConcept part = (I_ThinExtByRefPartConcept) tuple;
        if (part == null) {
            // no extension at all
            part = tf.newConceptExtensionPart();
            part.setC1id(latest.isDefined() ? getNid(Concept.DEFINED_DEFINITION) : getNid(Concept.PRIMITIVE_DEFINITION));
            part.setPathId(latest.getPathId());
            part.setStatusId(org.dwfa.cement.ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
            part.setVersion(latest.getVersion());
            export(part, null, ConceptConstants.DEFINITION_TYPE_EXTENSION.localize().getNid(), conceptId);
        } else if (part.getC1id() != latest.getStatusId()) {
            // add a new row with the latest refinability
            part.setC1id(latest.isDefined() ? getNid(Concept.DEFINED_DEFINITION) : getNid(Concept.PRIMITIVE_DEFINITION));
            export((I_ThinExtByRefTuple) part);
        }
    }

    /**
     * Get the native id for the concept in the DB.
     * 
     * @param concept to get the native id for
     * @return int native id
     * @throws TerminologyException DB errors
     * @throws IOException DB errors
     */
    private int getNid(org.dwfa.cement.ArchitectonicAuxiliary.Concept concept) throws TerminologyException, IOException {
        return tf.uuidToNative(concept.getUids());
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
        I_ThinExtByRefTuple tuple = getCurrentExtension(relId, ConceptConstants.RELATIONSHIP_REFINABILITY_EXTENSION);
        I_ThinExtByRefPartConcept part = (I_ThinExtByRefPartConcept) tuple;
        if (part == null) {
            // no extension at all
            part = tf.newConceptExtensionPart();
            part.setC1id(latest.getRefinabilityId());
            part.setPathId(latest.getPathId());
            part.setStatusId(org.dwfa.cement.ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
            part.setVersion(latest.getVersion());
            export(part, null, ConceptConstants.RELATIONSHIP_REFINABILITY_EXTENSION.localize().getNid(), relId);
        } else if (part.getC1id() != latest.getRefinabilityId()) {
            // add a new row with the latest refinability
            part.setC1id(latest.getRefinabilityId());
            export((I_ThinExtByRefTuple) tuple);
        }
    }
}
