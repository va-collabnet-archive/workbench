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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.I_DescribeConceptUniversally;
import org.dwfa.tapi.I_ManifestUniversally;
import org.dwfa.tapi.I_RelateConceptsUniversally;
import org.dwfa.tapi.I_StoreUniversalFixedTerminology;
import org.dwfa.tapi.TerminologyException;

public class UniversalFixedConcept implements I_ConceptualizeUniversally {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private ArrayList<UUID> uids;
    private Boolean primitive;
    private transient Collection<I_DescribeConceptUniversally> descriptions;
    private transient Collection<I_RelateConceptsUniversally> sourceRels;
    private transient Collection<I_RelateConceptsUniversally> destRels;

    private UniversalFixedConcept(Collection<UUID> uids) {
        super();
        this.uids = new ArrayList<UUID>(uids);
    }

    public static UniversalFixedConcept get(Collection<UUID> uids) {
        return new UniversalFixedConcept(uids);
    }

    public Collection<UUID> getUids() {
        return uids;
    }

    public boolean isUniversal() {
        return true;
    }

    public boolean isPrimitive(I_StoreUniversalFixedTerminology termStore) throws IOException, TerminologyException {
        if (primitive == null) {
            I_ConceptualizeUniversally serverConcept = termStore.getConcept(uids);
            primitive = serverConcept.isPrimitive(termStore);
        }
        return primitive;
    }

    public I_ConceptualizeLocally localize() throws IOException, TerminologyException {
        return LocalFixedConcept.get(uids);
    }

    public String toString() {
        return " concept: " + uids;
    }

    public I_ManifestUniversally getExtension(I_ConceptualizeUniversally extensionType,
            I_StoreUniversalFixedTerminology extensionServer) throws IOException, TerminologyException {
        return extensionServer.getUniversalExtension(this, extensionType);
    }

    public Collection<I_DescribeConceptUniversally> getDescriptions(I_StoreUniversalFixedTerminology termStore)
            throws IOException, TerminologyException {
        if (descriptions == null) {
            descriptions = termStore.getDescriptionsForConcept(this);
        }
        return descriptions;
    }

    public Collection<I_ConceptualizeUniversally> getDestRelConcepts(I_StoreUniversalFixedTerminology termStore)
            throws IOException, TerminologyException {
        Collection<I_ConceptualizeUniversally> results = new ArrayList<I_ConceptualizeUniversally>();
        for (I_RelateConceptsUniversally r : termStore.getSourceRels(this)) {
            results.add(r.getC2());
        }
        return results;
    }

    public Collection<I_ConceptualizeUniversally> getDestRelConcepts(Collection<I_ConceptualizeUniversally> types,
            I_StoreUniversalFixedTerminology termStore) throws IOException, TerminologyException {
        Collection<I_ConceptualizeUniversally> results = new ArrayList<I_ConceptualizeUniversally>();
        for (I_RelateConceptsUniversally r : getSourceRels(termStore)) {
            if (types.contains(r.getRelType())) {
                results.add(r.getC2());
            }
        }
        return results;
    }

    public Collection<I_RelateConceptsUniversally> getDestRels(I_StoreUniversalFixedTerminology termStore)
            throws IOException, TerminologyException {
        if (destRels == null) {
            destRels = termStore.getDestRels(this);
        }
        return destRels;
    }

    public Collection<I_RelateConceptsUniversally> getSourceRels(I_StoreUniversalFixedTerminology termStore)
            throws IOException, TerminologyException {
        if (sourceRels == null) {
            sourceRels = termStore.getSourceRels(this);
        }
        return sourceRels;
    }

    public Collection<I_RelateConceptsUniversally> getDestRels(Collection<I_ConceptualizeUniversally> types,
            I_StoreUniversalFixedTerminology termStore) throws IOException, TerminologyException {
        Collection<I_RelateConceptsUniversally> destRelsOfType = new ArrayList<I_RelateConceptsUniversally>();
        for (I_RelateConceptsUniversally rel : getDestRels(termStore)) {
            if (types.contains(rel.getRelType())) {
                destRelsOfType.add(rel);
            }
        }
        return destRelsOfType;
    }

    public Collection<I_RelateConceptsUniversally> getSourceRels(Collection<I_ConceptualizeUniversally> types,
            I_StoreUniversalFixedTerminology termStore) throws IOException, TerminologyException {
        Collection<I_RelateConceptsUniversally> srcRelsOfType = new ArrayList<I_RelateConceptsUniversally>();
        for (I_RelateConceptsUniversally rel : getSourceRels(termStore)) {
            if (types.contains(rel.getRelType())) {
                srcRelsOfType.add(rel);
            }
        }
        return srcRelsOfType;
    }

    public Collection<I_ConceptualizeUniversally> getSrcRelConcepts(I_StoreUniversalFixedTerminology termStore)
            throws IOException, TerminologyException {
        Collection<I_ConceptualizeUniversally> results = new ArrayList<I_ConceptualizeUniversally>();
        for (I_RelateConceptsUniversally r : termStore.getDestRels(this)) {
            results.add(r.getC1());
        }
        return results;
    }

    public Collection<I_ConceptualizeUniversally> getSrcRelConcepts(Collection<I_ConceptualizeUniversally> types,
            I_StoreUniversalFixedTerminology termStore) throws IOException, TerminologyException {
        Collection<I_ConceptualizeUniversally> results = new ArrayList<I_ConceptualizeUniversally>();
        for (I_RelateConceptsUniversally r : termStore.getDestRels(this)) {
            if (types.contains(r.getRelType())) {
                results.add(r.getC1());
            }
        }
        return results;
    }

    public I_DescribeConceptUniversally getDescription(List<I_ConceptualizeUniversally> typePriorityList,
            I_StoreUniversalFixedTerminology termStore) throws IOException, TerminologyException {
        for (I_ConceptualizeUniversally descType : typePriorityList) {
            for (I_DescribeConceptUniversally desc : getDescriptions(termStore)) {
                if (desc.getDescType().equals(descType)) {
                    return desc;
                }
            }
        }
        return null;
    }

	@Override
	public UUID getPrimoridalUid() throws IOException, TerminologyException {
		return uids.get(0);
	}

}
