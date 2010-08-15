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

import org.dwfa.ace.utypes.UniversalAceRelationship;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.relationship.RelationshipAnalogBI;

public interface I_RelVersioned extends I_AmTermComponent, RelationshipAnalogBI {

    public boolean addVersion(I_RelPart rel);

    public boolean addVersionNoRedundancyCheck(I_RelPart rel);

    public List<? extends I_RelPart> getMutableParts();

    public List<? extends I_RelPart> getVersions(I_ManageContradiction contradictionManager) throws TerminologyException,
            IOException;

    public int versionCount();

    public boolean addRetiredRec(int[] releases, int retiredStatusId);

    /**
     * 
     * @return the native id of the source concept (c1) of this relationship
     */
    public int getC1Id();

    /**
     * 
     * @return the native id of the destination concept (c2) of this
     *         relationship
     */
    public int getC2Id();

    public int getRelId();

    /**
     * @return all the tuples for this relationship
     */
    public List<? extends I_RelTuple> getTuples();

    /**
     * @param returnConflictResolvedLatestState
     * @return tuples for the relationship optionally resolved using the
     *         conflict management strategy
     * @throws TerminologyException
     * @throws IOException
     */
    public List<? extends I_RelTuple> getTuples(I_ManageContradiction contradictionManager) throws TerminologyException,
            IOException;

    public I_RelTuple getFirstTuple();

    public I_RelTuple getLastTuple();

    /**
     * Retrieves tuples matching the specified allowedStatuses, allowedTypes and
     * positions -
     * tuples are returned in the supplied returnTuples List parameter -
     * <strong>NOTE: this does not use the conflict management
     * strategy</strong>.
     * It is strongly recommended that you use a method that does use a conflict
     * management strategy.
     * 
     * @see #addTuples(I_IntSet, I_IntSet, PositionSetReadOnly, List, boolean, boolean)
     * 
     * @param allowedStatus
     *            statuses tuples must match to be returned
     * @param allowedTypes
     *            types tuples must match to be returned
     * @param positions
     *            positions a tuple must be on to be returned
     * @param returnRels
     *            List to be populated with the result of the search
     * @param addUncommitted
     *            if true matching items from the uncommitted list will be
     *            added, if false the uncommitted list is ignored
     * @throws IOException
     * @throws TerminologyException
     */
    public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes, PositionSetReadOnly positions,
            List<I_RelTuple> returnRels, Precedence precedencePolicy, I_ManageContradiction contradictionManager) 
                    throws TerminologyException, IOException;

    public List<? extends I_RelTuple> getSpecifiedVersions(I_ConfigAceFrame frameConfig) throws TerminologyException, IOException;

    public List<? extends I_RelTuple> getSpecifiedVersions(I_IntSet allowedStatus, PositionSetReadOnly positions, 
            Precedence precedencePolicy, I_ManageContradiction contradictionManager) throws TerminologyException, 
            IOException;
    
    public void convertIds(I_MapNativeToNative jarToDbNativeMap);

    public Set<TimePathId> getTimePathSet();

    public void setC2Id(int destId);

    public UniversalAceRelationship getUniversal() throws IOException, TerminologyException;

}
