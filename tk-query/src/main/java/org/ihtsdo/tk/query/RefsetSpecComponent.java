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
package org.ihtsdo.tk.query;

import java.io.IOException;
import java.util.Set;

import org.dwfa.tapi.ComputationCanceled;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.query.RefsetSpecQuery.GROUPING_TYPE;

public abstract class RefsetSpecComponent {
    private int possibleConceptsCount;
    protected ViewCoordinate viewCoordinate;

    protected RefsetSpecComponent(ViewCoordinate viewCoordinate) throws Exception {
        super();
        this.viewCoordinate = viewCoordinate;
    }

    public int getPossibleConceptsCount() {
        return possibleConceptsCount;
    }

    protected void setPossibleConceptsCount(int possibleConceptsCount) {
        this.possibleConceptsCount = possibleConceptsCount;
    }

    protected Set<Integer> getCurrentStatusIds() {
        throw new UnsupportedOperationException("Not supported.");
    }

	public abstract boolean execute(int componentNid, Object component,
			GROUPING_TYPE version, ViewCoordinate v1Is, ViewCoordinate v2Is) throws IOException,
			TerminologyException, ContradictionException;

    public abstract NidBitSetBI getPossibleConcepts(NidBitSetBI parentPossibleConcepts) throws IOException, ComputationCanceled, ContradictionException, TerminologyException;

    public abstract NidBitSetBI getPossibleDescriptions(NidBitSetBI parentPossibleConcepts) throws IOException, ComputationCanceled;

    public abstract NidBitSetBI getPossibleRelationships(NidBitSetBI parentPossibleConcepts) throws IOException, ComputationCanceled;
}
