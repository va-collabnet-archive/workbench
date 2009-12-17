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
package org.dwfa.ace.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.IntSet;
import org.dwfa.vodb.types.ThinDescVersioned;

public class ActiveConceptAndDescTest implements I_TestSearchResults {

    public boolean test(I_AmTermComponent component, I_ConfigAceFrame frameConfig) throws TaskFailedException {
        ThinDescVersioned descV = (ThinDescVersioned) component;
        ConceptBean concept = ConceptBean.get(descV.getConceptId());
        try {
            List<I_ConceptAttributeTuple> attributes = concept.getConceptAttributeTuples(
                frameConfig.getAllowedStatus(), frameConfig.getViewPositionSet(), true, false);
            if (attributes == null || attributes.size() == 0) {
                return false;
            }
            List<I_DescriptionTuple> matchingTuples = new ArrayList<I_DescriptionTuple>();
            I_IntSet allowedTypes = null;
            if (frameConfig.searchWithDescTypeFilter()) {
                allowedTypes = frameConfig.getDescTypes();
            }

            descV.addTuples(allowedTypes, matchingTuples, true, false);
            if (matchingTuples.size() == 0) {
                return false;
            }

        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return true;
    }
}
