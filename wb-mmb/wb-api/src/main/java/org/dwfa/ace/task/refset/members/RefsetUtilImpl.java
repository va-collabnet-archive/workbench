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
package org.dwfa.ace.task.refset.members;

import static org.dwfa.ace.task.refset.members.export.StatusUUIDs.CURRENT_STATUS_UUIDS;
import static org.dwfa.ace.task.refset.members.export.StatusUUIDs.FULLY_SPECIFIED_UUIDS;
import static org.dwfa.ace.task.refset.members.export.StatusUUIDs.PREFERED_TERM_UUIDS;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

// TODO: Test this.
public final class RefsetUtilImpl implements RefsetUtil {

    public I_ConceptAttributePart getLastestAttributePart(final I_GetConceptData refsetConcept) throws IOException {
        List<? extends I_ConceptAttributePart> refsetAttibuteParts = refsetConcept.getConceptAttributes().getMutableParts();
        I_ConceptAttributePart latestAttributePart = null;
        for (I_ConceptAttributePart attributePart : refsetAttibuteParts) {
            if (latestAttributePart == null || attributePart.getVersion() >= latestAttributePart.getVersion()) {
                latestAttributePart = attributePart;
            }
        }
        return latestAttributePart;
    }

    public I_IntSet createIntSet(final I_TermFactory termFactory, final Collection<UUID> uuid) throws Exception {
        I_IntSet status = termFactory.newIntSet();
        status.add(termFactory.getConcept(uuid).getConceptId());
        status.add(ArchitectonicAuxiliary.getSnomedDescriptionStatusId(uuid));
        return status;
    }

    public I_ExtendByRefPart getLatestVersion(final I_ExtendByRef ext, final I_TermFactory termFactory)
            throws TerminologyException, IOException {
        I_ExtendByRefPart latest = null;
        List<? extends I_ExtendByRefPart> versions = ext.getMutableParts();
        for (I_ExtendByRefPart version : versions) {

            if (latest == null) {
                latest = version;
            } else {
                if (latest.getVersion() < version.getVersion()) {
                    latest = version;
                }
            }
        }

        return latest;
    }

    public I_ExtendByRefPart getLatestVersionIfCurrent(final I_ExtendByRef ext,
            final I_TermFactory termFactory) throws Exception {
        I_ExtendByRefPart latest = getLatestVersion(ext, termFactory);

        if (latest != null && !(latest.getStatusId() == termFactory.getConcept(CURRENT_STATUS_UUIDS).getConceptId())) {
            latest = null;
        }

        return latest;
    }

    public String getSnomedId(final int nid, final I_TermFactory termFactory) throws Exception {

        if (nid == 0) {
            return "no identifier";
        }

        I_Identify idVersioned = termFactory.getId(nid);
        for (I_IdPart idPart : idVersioned.getMutableIdParts()) {
            if (idPart.getAuthorityNid() == termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids())) {
                return idPart.getDenotation().toString();
            }
        }

        return "no SCTID found";
    }

    public <T> T assertExactlyOne(final Collection<T> collection) {
        assert collection.size() == 1 : "Collection " + collection + " was expected to only have one element";
        return collection.iterator().next();
    }

    public int getLocalizedParentMarkerNid() {
        return ConceptConstants.PARENT_MARKER.localize().getNid();
    }

    public int getLocalizedConceptExtensionNid() throws Exception {
        return RefsetAuxiliary.Concept.CONCEPT_EXTENSION.localize().getNid();
    }

    public int getLocalizedCurrentConceptNid() throws Exception {
        return ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid();
    }

    public List<? extends I_DescriptionTuple> getDescriptionTuples(final I_GetConceptData concept,
            final I_IntSet allowedStatuses, final I_IntSet allowedTypes) throws Exception {
        // TODO replace with passed in config...
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
        return concept.getDescriptionTuples(allowedStatuses, allowedTypes, null, 
            config.getPrecedence(), config.getConflictResolutionStrategy());

    }

    public I_IntSet createCurrentStatus(final I_TermFactory termFactory) throws Exception {
        return createIntSet(termFactory, CURRENT_STATUS_UUIDS);
    }

    public I_IntSet createFullySpecifiedName(final I_TermFactory termFactory) throws Exception {
        return createIntSet(termFactory, FULLY_SPECIFIED_UUIDS);
    }

    public I_IntSet createPreferredTerm(final I_TermFactory termFactory) throws Exception {
        return createIntSet(termFactory, PREFERED_TERM_UUIDS);
    }

    public List<? extends I_DescriptionTuple> getFSNDescriptionsForConceptHavingCurrentStatus(final I_TermFactory termFactory,
            final int conceptId) throws Exception {
        I_GetConceptData refsetConcept = termFactory.getConcept(conceptId);
        return getDescriptionTuples(refsetConcept, createCurrentStatus(termFactory),
            createFullySpecifiedName(termFactory));
    }

    public List<? extends I_DescriptionTuple> getPTDescriptionsForConceptHavingCurrentStatus(final I_TermFactory termFactory,
            final int conceptId) throws Exception {
        I_GetConceptData refsetConcept = termFactory.getConcept(conceptId);
        return getDescriptionTuples(refsetConcept, createCurrentStatus(termFactory), createPreferredTerm(termFactory));
    }
}
