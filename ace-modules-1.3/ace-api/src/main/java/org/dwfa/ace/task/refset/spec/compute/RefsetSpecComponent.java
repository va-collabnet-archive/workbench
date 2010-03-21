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
import java.util.HashSet;
import java.util.Set;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

public abstract class RefsetSpecComponent {
    private int possibleConceptsCount;

    public int getPossibleConceptsCount() {
        return possibleConceptsCount;
    }

    protected void setPossibleConceptsCount(int possibleConceptsCount) {
        this.possibleConceptsCount = possibleConceptsCount;
    }

    protected Set<Integer> getCurrentStatusIds() {
        Set<Integer> statuses = new HashSet<Integer>();

        try {
            statuses.add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.CURRENT_UNREVIEWED.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.READY_TO_PROMOTE.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.PROMOTED.localize().getNid());
            statuses.add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statuses;
    }

    public abstract boolean execute(I_AmTermComponent component) throws IOException, TerminologyException;

    public abstract I_RepresentIdSet getPossibleConcepts(I_ConfigAceFrame config,
            I_RepresentIdSet parentPossibleConcepts) throws TerminologyException, IOException;

    public abstract I_RepresentIdSet getPossibleDescriptions(I_ConfigAceFrame config,
            I_RepresentIdSet parentPossibleConcepts) throws TerminologyException, IOException;

    public abstract I_RepresentIdSet getPossibleRelationships(I_ConfigAceFrame config,
            I_RepresentIdSet parentPossibleConcepts) throws TerminologyException, IOException;
}
