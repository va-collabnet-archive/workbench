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
package org.dwfa.ace.task.refset.spec.compute;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.tapi.TerminologyException;

public abstract class RefsetSpecComponent {
    private int possibleConceptsCount;
    protected int refsetSpecNid;
    protected I_HelpSpecRefset helper;
    protected I_ConfigAceFrame config;

    protected RefsetSpecComponent(int refsetSpecNid, I_ConfigAceFrame config) throws Exception {
        super();
        this.config = config;
        helper = Terms.get().getSpecRefsetHelper(this.config);

        this.refsetSpecNid = refsetSpecNid;
    }

    public int getRefsetSpecNid() {
        return refsetSpecNid;
    }

    public int getPossibleConceptsCount() {
        return possibleConceptsCount;
    }

    protected void setPossibleConceptsCount(int possibleConceptsCount) {
        this.possibleConceptsCount = possibleConceptsCount;
    }

    protected Set<Integer> getCurrentStatusIds() {
        try {
            return helper.getCurrentStatusIds();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public abstract boolean execute(I_AmTermComponent component, Collection<I_ShowActivity> activities)
            throws IOException, TerminologyException;

    public abstract I_RepresentIdSet getPossibleConcepts(I_RepresentIdSet parentPossibleConcepts,
            Collection<I_ShowActivity> activities) throws TerminologyException, IOException;

    public abstract I_RepresentIdSet getPossibleDescriptions(I_RepresentIdSet parentPossibleConcepts,
            Collection<I_ShowActivity> activities) throws TerminologyException, IOException;

    public abstract I_RepresentIdSet getPossibleRelationships(I_RepresentIdSet parentPossibleConcepts,
            Collection<I_ShowActivity> activities) throws TerminologyException, IOException;
}
