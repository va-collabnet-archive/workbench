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

public interface I_ConceptualizeLocally extends I_Conceptualize, I_ManifestLocally {

    public boolean isPrimitive() throws IOException, TerminologyException;

    public I_DescribeConceptLocally getDescription(List<I_ConceptualizeLocally> typePriorityList) throws IOException,
            TerminologyException;

    public Collection<I_DescribeConceptLocally> getDescriptions() throws IOException, TerminologyException;

    public Collection<I_RelateConceptsLocally> getSourceRels() throws IOException, TerminologyException;

    public Collection<I_RelateConceptsLocally> getSourceRels(Collection<I_ConceptualizeLocally> types)
            throws IOException, TerminologyException;

    public Collection<I_RelateConceptsLocally> getDestRels() throws IOException, TerminologyException;

    public Collection<I_RelateConceptsLocally> getDestRels(Collection<I_ConceptualizeLocally> types)
            throws IOException, TerminologyException;

    public Collection<I_ConceptualizeLocally> getDestRelConcepts() throws IOException, TerminologyException;

    public Collection<I_ConceptualizeLocally> getDestRelConcepts(Collection<I_ConceptualizeLocally> types)
            throws IOException, TerminologyException;

    public Collection<I_ConceptualizeLocally> getSrcRelConcepts() throws IOException, TerminologyException;

    public Collection<I_ConceptualizeLocally> getSrcRelConcepts(Collection<I_ConceptualizeLocally> types)
            throws IOException, TerminologyException;

    public I_ConceptualizeUniversally universalize() throws IOException, TerminologyException;

}
