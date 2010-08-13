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

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;

public class ConceptQuery implements I_MatchConcept {

    public void addChildOfConstraint(I_AmChildOf parentMatcher) {
        // TODO Auto-generated method stub

    }

    public void addDescConstraint(I_MatchDescription descMatcher) {
        // TODO Auto-generated method stub

    }

    public void addRefsetConstraint(I_MatchRefset refsetMatcher) {
        // TODO Auto-generated method stub

    }

    public void addRelConstraint(I_MatchRelationship relMatcher) {
        // TODO Auto-generated method stub

    }

    public void addStatusConstraint(I_MatchConcept conceptMatcher) {
        // TODO Auto-generated method stub

    }

    public boolean matchConcept(I_GetConceptData concept, I_ConfigAceFrame profile) {
        // TODO Auto-generated method stub
        return false;
    }

}
