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
package org.dwfa.ace.api;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.utypes.UniversalAceConceptAttributes;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.PositionSetBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.conattr.ConAttrAnalogBI;

public interface I_ConceptAttributeVersioned extends I_AmTermComponent, ConAttrAnalogBI {

    public boolean addVersion(I_ConceptAttributePart part);

    public List<? extends I_ConceptAttributePart> getMutableParts();

    public int versionCount();

    public int getConId();

    public List<? extends I_ConceptAttributeTuple> getTuples();

    public void convertIds(I_MapNativeToNative jarToDbNativeMap);

    public Set<TimePathId> getTimePathSet();

    /**
     * Retrieves tuples matching the specified allowedStatuses and positions -
     * tuples are returned in the supplied returnTuples List parameter
     * 
     * @param allowedStatus
     *            statuses tuples must match to be returned
     * @param positions
     *            postions a tuple must be on to be returned
     * @param returnTuples
     *            List to be populated with the result of the search
     * @param addUncommitted
     *            if true matching items from the uncommitted list will be
     *            added, if false the uncommitted list is ignored
     * @param returnConflictResolvedLatestState
     *            indicates if all tuples or just the latest state using the
     *            current profile's conflict resolution strategy is required
     * @throws IOException
     * @throws TerminologyException
     */
    public void addTuples(I_IntSet allowedStatus, PositionSetReadOnly positionSet,
            List<I_ConceptAttributeTuple> returnTuples, 
            Precedence precedencePolicy, 
            I_ManageContradiction contradictionManager) throws TerminologyException, IOException;

    public I_ConceptualizeLocally getLocalFixedConcept();

    public UniversalAceConceptAttributes getUniversal() throws IOException, TerminologyException;

    @Deprecated
    public List<? extends I_ConceptAttributeTuple> getTuples(I_IntSet allowedStatus, PositionSetBI viewPositionSet) throws TerminologyException, IOException;

}
