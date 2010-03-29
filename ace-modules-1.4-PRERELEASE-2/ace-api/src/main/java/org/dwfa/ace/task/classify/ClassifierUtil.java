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
package org.dwfa.ace.task.classify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.dwfa.tapi.TerminologyException;

class ClassifierUtil {

    final private Logger logger;
    final private I_SnorocketFactory rocket;

    final private int rootNid;
    final private int isaId;
    final private I_IntSet allowedPaths;
    final private I_IntSet activeStatus;
    final private I_IntSet statedForms;

    // :EDIT:MEC:
    int countAddConcept;
    int countAddRel;
    int countStatusNotCurrent;

    // :EDIT:MEC:

    ClassifierUtil(final Logger logger, final I_SnorocketFactory rocket) throws TerminologyException, IOException {
        this.logger = logger;
        this.rocket = rocket;
        // :EDIT:MEC:
        countAddConcept = 0;
        countAddRel = 0;
        countStatusNotCurrent = 0;
        // :EDIT:MEC:

        final I_TermFactory termFactory = LocalVersionedTerminology.get();

        rootNid = termFactory.uuidToNative(SNOMED.Concept.ROOT.getUids());
        isaId = termFactory.uuidToNative(SNOMED.Concept.IS_A.getUids());

        final I_ConfigAceFrame frameConfig = termFactory.getActiveAceFrameConfig();

        allowedPaths = termFactory.newIntSet();
        for (I_Position p : frameConfig.getViewPositionSet()) {
            addPathIds(allowedPaths, p);
        }

        activeStatus = frameConfig.getAllowedStatus();

        statedForms = termFactory.newIntSet();
        addConceptToSet(statedForms, termFactory, ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP);
        addConceptToSet(statedForms, termFactory, ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC);
        addConceptToSet(statedForms, termFactory, ArchitectonicAuxiliary.Concept.STATED_AND_INFERRED_RELATIONSHIP);
        addConceptToSet(statedForms, termFactory, ArchitectonicAuxiliary.Concept.STATED_AND_SUBSUMED_RELATIONSHIP);
    }

    private void addConceptToSet(I_IntSet set, I_TermFactory tf, Concept relationship) throws TerminologyException,
            IOException {
        set.add(tf.uuidToNative(relationship.getUids()));
    }

    private void addPathIds(final I_IntSet paths, final I_Position p) {
        paths.add(p.getPath().getConceptId());
        for (I_Position origin : p.getPath().getOrigins()) {
            addPathIds(paths, origin);
        }
    }

    /**
     * If the concept is a SNOMED concept, pass it and all active stated
     * relationships to classifier.
     * 
     * A concept is considered to be a SNOMED concept if it's the root concept
     * or it has an active SNOMED ISA relationship.
     * 
     * @param concept
     * @param includeUncommitted
     * @throws IOException
     */
    void processConcept(final I_GetConceptData concept, final boolean includeUncommitted) throws IOException {
        if (concept.getConceptId() == rootNid) {
            rocket.addConcept(concept.getConceptId(), false);
            // :EDIT:MEC: !!!
            logger.info("!!! ROOT Concept # " + rootNid);
            // :EDIT:MEC:
        } else {
            final I_ConceptAttributePart latestAttributePart = getLatestAttribute(concept, includeUncommitted);

            if (latestAttributePart != null && activeStatus.contains(latestAttributePart.getStatusId())) {
                final List<Relationship> statedRels = collectStatedRelationships(concept, includeUncommitted);

                if (null != statedRels) {
                    rocket.addConcept(concept.getConceptId(), latestAttributePart.isDefined());
                    // :EDIT:MEC: !!!
                    if (++countAddConcept % 100000 == 0) {
                        logger.info("!!! processed concept " + countAddConcept);
                    }
                    // :EDIT:MEC:

                    for (Relationship rel : statedRels) {
                        rocket.addRelationship(rel.cId1, rel.relId, rel.cId2, rel.group);
                        // :EDIT:MEC: !!!
                        if (++countAddRel % 100000 == 0) {
                            logger.info("!!! processed RELATIONSHIP " + countAddRel);
                        }
                        // :EDIT:MEC:
                    }
                }
            }
        }
    }

    private List<Relationship> collectStatedRelationships(final I_GetConceptData concept,
            final boolean includeUncommitted) throws IOException {
        final List<I_RelVersioned> sourceRels = new ArrayList<I_RelVersioned>();
        final List<Relationship> statedRels = new ArrayList<Relationship>();

        boolean isSnomedConcept = false;

        if (includeUncommitted) {
            sourceRels.addAll(concept.getUncommittedSourceRels());
        }
        sourceRels.addAll(concept.getSourceRels());

        for (I_RelVersioned rel : sourceRels) {
            final I_RelPart latestRel = getLatestRel(concept, rel);

            if (null != latestRel && activeStatus.contains(latestRel.getStatusId())) {
                // check the relationship to see if there is a proper SNOMED
                // is-a.
                if (latestRel.getTypeId() == isaId) {
                    isSnomedConcept = true;
                }

                if (statedForms.contains(latestRel.getCharacteristicId())) {
                    final Relationship relationship = new Relationship(rel.getC1Id(), latestRel.getTypeId(),
                        rel.getC2Id(), latestRel.getGroup());
                    statedRels.add(relationship);
                }
            }
        }

        return isSnomedConcept ? statedRels : null;
    }

    private I_ConceptAttributePart getLatestAttribute(final I_GetConceptData concept, final boolean includeUncommitted)
            throws IOException {
        final List<I_ConceptAttributePart> parts = new ArrayList<I_ConceptAttributePart>();

        I_ConceptAttributePart latestAttributePart = null;

        if (includeUncommitted) {
            final I_ConceptAttributeVersioned uncommittedConceptAttributes = concept.getUncommittedConceptAttributes();
            if (null != uncommittedConceptAttributes) {
                parts.addAll(uncommittedConceptAttributes.getVersions());
            }
        }

        parts.addAll(concept.getConceptAttributes().getVersions());

        for (I_ConceptAttributePart attributePart : parts) {
            if (allowedPaths.contains(attributePart.getPathId())) {
                if (latestAttributePart == null || latestAttributePart.getVersion() < attributePart.getVersion()) {
                    latestAttributePart = attributePart;
                } else if (latestAttributePart.getVersion() == attributePart.getVersion()) {
                    if (getLogger().isLoggable(Level.FINE)) {
                        getLogger().log(Level.FINE,
                            "has multiple entries with same version: " + attributePart + " for " + concept);
                    }
                }
            }
        }
        return latestAttributePart;
    }

    private I_RelPart getLatestRel(final I_GetConceptData concept, final I_RelVersioned rel) {
        I_RelPart latestPart = null;
        for (final I_RelPart part : rel.getVersions()) {
            if (allowedPaths.contains(part.getPathId())) {
                if (latestPart == null || latestPart.getVersion() < part.getVersion()) {
                    latestPart = part;
                } else if (latestPart.getVersion() == part.getVersion()) {
                    if (getLogger().isLoggable(Level.FINE)) {
                        getLogger().log(Level.FINE,
                            "has multiple entries with same version: " + rel + " for " + concept);
                    }
                }
            }
        }
        return latestPart;
    }

    private Logger getLogger() {
        return logger;
    }

    static private class Relationship {
        final int cId1;
        final int relId;
        final int cId2;
        final int group;

        public Relationship(final int cId1, final int relId, final int cId2, final int group) {
            this.cId1 = cId1;
            this.relId = relId;
            this.cId2 = cId2;
            this.group = group;
        }
    }
}
