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
package org.dwfa.mojo.memrefset.mojo;

import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;
import static org.dwfa.cement.ArchitectonicAuxiliary.Concept;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public final class RefSetNameRetrieverImpl implements RefSetNameRetriever {

    private static final Collection<UUID> CURRENT_STATUS_UUIDS = Concept.CURRENT.getUids();
    private static final Collection<UUID> FULLY_SPECIFIED_UUIDS = Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids();

    public String retrieveName(final UUID uuid) {
        try {
            I_TermFactory termFactory = LocalVersionedTerminology.get();
            I_IntSet status = createCurrentStatus(termFactory);
            I_IntSet fsn = createFullySpecifiedUUIDs(termFactory);
            List<I_DescriptionTuple> descriptions = termFactory.getConcept(new UUID[] { uuid }).getDescriptionTuples(
                status, fsn, null);
            // TODO: may have to check for this.
            return descriptions.get(0).getText();
        } catch (Exception e) {
            return "N/A";
        }
    }

    private I_IntSet createFullySpecifiedUUIDs(final I_TermFactory termFactory) throws Exception {
        I_IntSet fsn = termFactory.newIntSet();
        fsn.add(termFactory.getConcept(FULLY_SPECIFIED_UUIDS).getConceptId());
        fsn.add(ArchitectonicAuxiliary.getSnomedDescriptionTypeId(FULLY_SPECIFIED_UUIDS));
        return fsn;
    }

    private I_IntSet createCurrentStatus(final I_TermFactory termFactory) throws Exception {
        I_IntSet status = termFactory.newIntSet();
        status.add(termFactory.getConcept(CURRENT_STATUS_UUIDS).getConceptId());
        status.add(ArchitectonicAuxiliary.getSnomedDescriptionStatusId(CURRENT_STATUS_UUIDS));
        return status;
    }
}
