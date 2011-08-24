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
package org.ihtsdo.mojo.mojo.refset.spec;

import java.io.File;

import org.dwfa.ace.api.I_GetConceptData;
import org.ihtsdo.mojo.mojo.ConceptDescriptor;

public class RefsetInclusionSpec {

    public File exportFile;
    //TODO create a status member = e.g. x for draft use enum?
    public ConceptDescriptor refsetConcept;
    public boolean specContainsSnomedId = false;
    public int subsetVersion = 1;
    public String realmId = "UNKNOWN";
    public String contextId = "UNKNOWN";

    public boolean test(I_GetConceptData testConcept) throws Exception {
        if (refsetConcept.getVerifiedConcept().equals(testConcept)) {
            return true;
        } else {
            return false;
        }
    }
}
