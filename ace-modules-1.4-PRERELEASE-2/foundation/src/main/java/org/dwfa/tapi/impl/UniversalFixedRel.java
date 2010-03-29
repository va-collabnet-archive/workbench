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
package org.dwfa.tapi.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.I_ManifestUniversally;
import org.dwfa.tapi.I_RelateConceptsLocally;
import org.dwfa.tapi.I_RelateConceptsUniversally;
import org.dwfa.tapi.I_StoreLocalFixedTerminology;
import org.dwfa.tapi.I_StoreUniversalFixedTerminology;
import org.dwfa.tapi.TerminologyException;

public class UniversalFixedRel implements I_RelateConceptsUniversally {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private Collection<UUID> uids;

    private Collection<UUID> c1Uids;

    private Collection<UUID> relTypeUids;

    private Collection<UUID> c2Uids;

    private Collection<UUID> characteristicUids;

    private Collection<UUID> refinabilityUids;

    private int relGrp;

    public UniversalFixedRel(Collection<UUID> uids, Collection<UUID> c1Uids, Collection<UUID> relTypeUids,
            Collection<UUID> c2Uids, Collection<UUID> characteristicUids, Collection<UUID> refinabilityUids, int relGrp) {
        super();
        this.uids = uids;
        this.c1Uids = c1Uids;
        this.relTypeUids = relTypeUids;
        this.c2Uids = c2Uids;
        this.characteristicUids = characteristicUids;
        this.refinabilityUids = refinabilityUids;
        this.relGrp = relGrp;
    }

    public I_ConceptualizeUniversally getC1() {
        return UniversalFixedConcept.get(c1Uids);
    }

    public I_ConceptualizeUniversally getC2() {
        return UniversalFixedConcept.get(c2Uids);
    }

    public I_ConceptualizeUniversally getCharacteristic() {
        return UniversalFixedConcept.get(characteristicUids);
    }

    public I_ConceptualizeUniversally getRefinability() {
        return UniversalFixedConcept.get(refinabilityUids);
    }

    public I_ConceptualizeUniversally getRelType() {
        return UniversalFixedConcept.get(relTypeUids);
    }

    public I_RelateConceptsLocally localize() throws IOException, TerminologyException {
        I_StoreLocalFixedTerminology localTarget = LocalFixedTerminology.getStore();
        return new LocalFixedRel(localTarget.getNid(uids), localTarget.getNid(c1Uids), localTarget.getNid(relTypeUids),
            localTarget.getNid(c2Uids), localTarget.getNid(characteristicUids), localTarget.getNid(refinabilityUids),
            relGrp);
    }

    public int getRelGrp() {
        return relGrp;
    }

    public Collection<UUID> getUids() {
        return uids;
    }

    public boolean isUniversal() {
        return true;
    }

    public String toString() {
        return " rel: " + uids;
    }

    public I_ManifestUniversally getExtension(I_ConceptualizeUniversally extensionType,
            I_StoreUniversalFixedTerminology extensionServer) throws IOException, TerminologyException {
        return extensionServer.getUniversalExtension(this, extensionType);
    }

}
