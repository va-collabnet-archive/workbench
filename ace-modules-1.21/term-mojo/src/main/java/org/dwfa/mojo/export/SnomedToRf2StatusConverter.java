/*
 *  Copyright 2010 International Health Terminology Standards Development Organisation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.dwfa.mojo.export;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.mojo.ConceptConstants;

/**
 * Utility Class to Convert a SNOMED CT component status to RF2 meta-data status.
 * @author Matthew Edwards
 */
class SnomedToRf2StatusConverter {

    /** Unmodifiable Pair Map, ensures that the map will never be modified and therefore ensure data accuracy.*/
    private final Map<Integer, Integer> snomedToRf2Map;

    /**
     * Constructs an instance of {@code SnomedToRf2StatusConverter}. Loads the information for SNOMED CT Statuses and
     * RF2 meta-data statuses and places them in a map.
     * @throws Exception if there is an error accessing the Terminology Database.
     */
    public SnomedToRf2StatusConverter() throws Exception {
        int duplicateStatusNId = ConceptConstants.DUPLICATE_STATUS.localize().getNid();
        int ambiguousStatusNId = ConceptConstants.AMBIGUOUS_STATUS.localize().getNid();
        int erroneousStatusNId = ConceptConstants.ERRONEOUS_STATUS.localize().getNid();
        int outdatedStatusNId = ConceptConstants.OUTDATED_STATUS.localize().getNid();
        int inappropriateStatusNId = ConceptConstants.INAPPROPRIATE_STATUS.localize().getNid();
        int movedElsewhereStatusNId = ConceptConstants.MOVED_ELSEWHERE_STATUS.localize().getNid();
        int limitedStatusNId = ConceptConstants.LIMITED.localize().getNid();

        int aceDuplicateStatusNId = ArchitectonicAuxiliary.Concept.DUPLICATE.localize().getNid();
        int aceAmbiguousStatusNId = ArchitectonicAuxiliary.Concept.AMBIGUOUS.localize().getNid();
        int aceErroneousStatusNId = ArchitectonicAuxiliary.Concept.ERRONEOUS.localize().getNid();
        int aceOutdatedStatusNId = ArchitectonicAuxiliary.Concept.OUTDATED.localize().getNid();
        int aceInappropriateStatusNId = ArchitectonicAuxiliary.Concept.INAPPROPRIATE.localize().getNid();
        int aceMovedElsewhereStatusNId = ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE.localize().getNid();
        int aceLimitedStatusNId = ArchitectonicAuxiliary.Concept.LIMITED.localize().getNid();

        Map<Integer, Integer> constructorMap = new HashMap<Integer, Integer>();

        constructorMap.put(aceDuplicateStatusNId, duplicateStatusNId);
        constructorMap.put(aceAmbiguousStatusNId, ambiguousStatusNId);
        constructorMap.put(aceErroneousStatusNId, erroneousStatusNId);
        constructorMap.put(aceOutdatedStatusNId, outdatedStatusNId);
        constructorMap.put(aceInappropriateStatusNId, inappropriateStatusNId);
        constructorMap.put(aceMovedElsewhereStatusNId, movedElsewhereStatusNId);
        constructorMap.put(aceLimitedStatusNId, limitedStatusNId);

        this.snomedToRf2Map = Collections.unmodifiableMap(constructorMap);
    }

    /**
     * Convert the SNOMED CT component status to RF2 meta-data status.
     *
     * @param snomedStatusNid the SNOMED CT Component Status.
     * @return the RF2 Status if it exists or {@code -1} if no RF2 Status for the the given SNOMED CT component status
     * exists.
     */
    int toRf2Status(final int snomedStatusNid) {
        if (snomedToRf2Map.containsKey(snomedStatusNid)) {
            return snomedToRf2Map.get(snomedStatusNid);
        } else {
            return -1;
        }
    }
}
