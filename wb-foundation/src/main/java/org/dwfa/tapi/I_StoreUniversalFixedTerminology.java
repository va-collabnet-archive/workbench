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

public interface I_StoreUniversalFixedTerminology {

    public I_ExtendUniversally getUniversalExtension(I_ManifestUniversally component,
            I_ConceptualizeUniversally extensionType) throws IOException, TerminologyException;

    public Collection<I_ConceptualizeUniversally> getUniversalRoots() throws IOException, TerminologyException;

    public I_ConceptualizeUniversally getConcept(Collection<UUID> uids) throws IOException, TerminologyException;

    public I_DescribeConceptUniversally getDescription(Collection<UUID> uids) throws IOException, TerminologyException;

    public Collection<I_DescribeConceptUniversally> getDescriptionsForConcept(I_ConceptualizeUniversally concept)
            throws IOException, TerminologyException;

    public I_RelateConceptsUniversally getRel(Collection<UUID> uids) throws IOException, TerminologyException;

    public Collection<I_RelateConceptsUniversally> getSourceRels(I_ConceptualizeUniversally concept)
            throws IOException, TerminologyException;

    public Collection<I_RelateConceptsUniversally> getDestRels(I_ConceptualizeUniversally concept) throws IOException,
            TerminologyException;

    public Collection<I_DescribeConceptUniversally> doUniversalDescriptionSearch(String[] words) throws IOException,
            TerminologyException;

    public Collection<I_DescribeConceptUniversally> doUniversalDescriptionSearch(List<String> words)
            throws IOException, TerminologyException;

    public Collection<I_ConceptualizeUniversally> doUniversalConceptSearch(String[] words) throws IOException,
            TerminologyException;

    public Collection<I_ConceptualizeUniversally> doUniversalConceptSearch(List<String> words) throws IOException,
            TerminologyException;

    public Collection<I_ConceptualizeUniversally> getExtensionTypes() throws IOException, TerminologyException;

}
