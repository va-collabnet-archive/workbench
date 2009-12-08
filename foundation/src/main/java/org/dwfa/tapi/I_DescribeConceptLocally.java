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

public interface I_DescribeConceptLocally extends I_DescribeConcept, I_ManifestLocally {

    public I_ConceptualizeLocally getConcept() throws IOException, TerminologyException;

    public I_ConceptualizeLocally getDescType() throws IOException, TerminologyException;

    public I_ConceptualizeLocally getStatus() throws IOException, TerminologyException;

    public I_DescribeConceptUniversally universalize() throws IOException, TerminologyException;

}
