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
package org.dwfa.ace.query;

import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;

public abstract class Query implements I_ProcessConcepts {

    public enum JOIN {
        AND, OR
    };

    private JOIN queryType;

    private I_ConfigAceFrame profile;

    private List<I_MatchConcept> matchers = new ArrayList<I_MatchConcept>();

    public Query(JOIN queryType, I_ConfigAceFrame profile) {
        super();
        this.queryType = queryType;
        this.profile = profile;
    }

    public void processConcept(I_GetConceptData concept) throws Exception {
        switch (queryType) {
        case AND:
            for (I_MatchConcept m : matchers) {
                if (m.matchConcept(concept, profile) == false) {
                    return;
                }
                processMatch(concept);
            }
            break;
        case OR:
            for (I_MatchConcept m : matchers) {
                if (m.matchConcept(concept, profile)) {
                    processMatch(concept);
                    break;
                }
            }
            break;

        default:
            throw new Exception("Don't know how to handle: " + queryType);
        }

    }

    public abstract void processMatch(I_GetConceptData concept) throws Exception;

    public List<I_MatchConcept> getMatchers() {
        return matchers;
    }

}
