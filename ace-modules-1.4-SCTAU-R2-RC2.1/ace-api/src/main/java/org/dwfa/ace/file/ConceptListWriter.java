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
package org.dwfa.ace.file;

import java.io.IOException;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

/**
 * Writes out a "Concept List" which is a tab delimited list of concepts
 * containing
 * the concepts identifier and a description. The identifier will be the
 * concept's
 * SNOMED CT ID if it has one, or its UUID if it does not.
 * 
 * @author Dion
 * 
 */
public class ConceptListWriter extends GenericFileWriter<I_GetConceptData> {

    private Integer snomedIntId;

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.ace.file.GenericFileWriter#serialize(java.lang.Object)
     */
    @Override
    protected String serialize(I_GetConceptData concept) throws IOException, TerminologyException {

        if (snomedIntId == null) {
            snomedIntId = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid();
        }

        Object conceptId = concept.getId(snomedIntId);

        if (conceptId == null) {
            conceptId = concept.getUids().iterator().next();
        }

        // spit out a SNOMED CT ID if possible, if not a UUID
        // description is arbitrary - just pick one
        return conceptId + "\t" + concept.getInitialText();
    }
}
