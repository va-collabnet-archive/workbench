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
package org.ihtsdo.mojo.mojo.compare.operators;

import java.util.List;

import org.dwfa.ace.api.I_GetConceptData;
import org.ihtsdo.mojo.mojo.ConceptDescriptor;
import org.ihtsdo.mojo.mojo.compare.CompareOperator;
import org.ihtsdo.mojo.mojo.compare.Match;

public class MustContainPaths implements CompareOperator {

    private ConceptDescriptor[] paths;

    public boolean compare(List<Match> matches) {

        int numberOfMatches = 0;
        for (Match m : matches) {
            try {
                boolean matched = false;
                for (int i = 0; i < paths.length && !matched; i++) {
                    ConceptDescriptor cd = paths[i];
                    I_GetConceptData concept = cd.getVerifiedConcept();
                    if (m.getPath1().getPath().getConceptId() == concept.getConceptId()
                        || m.getPath2().getPath().getConceptId() == concept.getConceptId()) {
                        // The match occurred on a required path
                        matched = true;
                        numberOfMatches++;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return numberOfMatches == paths.length;
    }

}
