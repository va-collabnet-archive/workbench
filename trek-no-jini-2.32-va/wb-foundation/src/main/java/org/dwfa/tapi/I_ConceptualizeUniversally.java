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
package org.dwfa.tapi;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface I_ConceptualizeUniversally extends I_Conceptualize, I_ManifestUniversally {

    public UUID getPrimoridalUid() throws IOException, TerminologyException;

    public boolean isPrimitive(I_StoreUniversalFixedTerminology termStore) throws IOException, TerminologyException;

    public I_DescribeConceptUniversally getDescription(List<I_ConceptualizeUniversally> typePriorityList,
            I_StoreUniversalFixedTerminology termStore) throws IOException, TerminologyException;

    public Collection<I_DescribeConceptUniversally> getDescriptions(I_StoreUniversalFixedTerminology termStore)
            throws IOException, TerminologyException;

    public Collection<I_RelateConceptsUniversally> getSourceRels(I_StoreUniversalFixedTerminology termStore)
            throws IOException, TerminologyException;

    public Collection<I_RelateConceptsUniversally> getSourceRels(Collection<I_ConceptualizeUniversally> types,
            I_StoreUniversalFixedTerminology termStore) throws IOException, TerminologyException;

    public Collection<I_RelateConceptsUniversally> getDestRels(I_StoreUniversalFixedTerminology termStore)
            throws IOException, TerminologyException;

    public Collection<I_RelateConceptsUniversally> getDestRels(Collection<I_ConceptualizeUniversally> types,
            I_StoreUniversalFixedTerminology termStore) throws IOException, TerminologyException;

    public Collection<I_ConceptualizeUniversally> getDestRelConcepts(I_StoreUniversalFixedTerminology termStore)
            throws IOException, TerminologyException;

    public Collection<I_ConceptualizeUniversally> getDestRelConcepts(Collection<I_ConceptualizeUniversally> types,
            I_StoreUniversalFixedTerminology termStore) throws IOException, TerminologyException;

    public Collection<I_ConceptualizeUniversally> getSrcRelConcepts(I_StoreUniversalFixedTerminology termStore)
            throws IOException, TerminologyException;

    public Collection<I_ConceptualizeUniversally> getSrcRelConcepts(Collection<I_ConceptualizeUniversally> types,
            I_StoreUniversalFixedTerminology termStore) throws IOException, TerminologyException;

    public I_ConceptualizeLocally localize() throws IOException, TerminologyException;

}
