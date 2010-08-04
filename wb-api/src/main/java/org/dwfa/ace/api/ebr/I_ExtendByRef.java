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
package org.dwfa.ace.api.ebr;

import java.io.IOException;
import java.util.List;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageContradiction;
import org.dwfa.ace.api.PRECEDENCE;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.tapi.TerminologyException;

public interface I_ExtendByRef extends I_AmTermComponent {

    /**
     * @return <code><b>int</b></code> -- native identifier (nid) of this reference set member.<br>
     *         This is the surrogate key <code>I_ExtendByRef</code> object itself.<br> 
     *         The <code>I_ExtendByRef</code> object associates a
     *         concept, description or relationship component as member of a given reference set
     */
    public int getMemberId();

    /**
     * @return <code><b>int</b></code> -- native identifier (nid) of the component which this reference set member extends.<br> 
     *         The component extended by this <code>I_ExtendByRef</code> may be a concept, description or relationship.<br> 
     */
    public int getComponentId();

    /**
     * @return <code><b>int</b></code> -- native identifier (nid) for the concept which represent this reference set member's extension type.<br> 
     *         Example types which this <code>I_ExtendByRef</code> reference set member type are boolean, String, Concept and so on...<br> 
     */
    public int getTypeId();

    public List<? extends I_ExtendByRefPart> getMutableParts();

    /**
     * @return <code><b>int</b></code> -- native identifier (nid) for the reference set to which this <code>I_ExtendByRef</code>
     *         extension is a member.
     */
    public int getRefsetId();

    public void addVersion(I_ExtendByRefPart part);

    public void setRefsetId(int refsetId) throws IOException;

    public void setTypeId(int typeId);

    /**
     * Retrieves tuples matching the specified allowedStatuses and positions -
     * tuples are returned in the supplied returnTuples List parameter
     * 
     * @param allowedStatus
     *            statuses tuples must match to be returned
     * @param positions
     *            positions a tuple must be on to be returned
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
    public void addTuples(I_IntSet allowedStatus, PositionSetReadOnly positions, List<I_ExtendByRefVersion> returnTuples,
            PRECEDENCE precedence, I_ManageContradiction contradictionMgr) throws TerminologyException, IOException;

    /**
     * Retrieves tuples matching the specified allowedStatuses and positions
     * configured in the current profile - tuples are returned in the supplied
     * returnTuples List parameter
     * 
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
    public void addTuples(List<I_ExtendByRefVersion> returnTuples,
            PRECEDENCE precedence, I_ManageContradiction contradictionMgr) throws TerminologyException, IOException;

    /**
     * Retrieves tuples matching the specified allowedStatuses and positions
     * 
     * @param allowedStatus
     *            statuses tuples must match to be returned
     * @param positions
     *            positions a tuple must be on to be returned
     * @param addUncommitted
     *            if true matching items from the uncommitted list will be
     *            added, if false the uncommitted list is ignored
     * @param returnConflictResolvedLatestState
     *            indicates if all tuples or just the latest state using the
     *            current profile's conflict resolution strategy is required
     * @return List of matching tuples
     * @throws IOException
     * @throws TerminologyException
     */
    public List<? extends I_ExtendByRefVersion> getTuples(I_IntSet allowedStatus, PositionSetReadOnly positions,
        PRECEDENCE precedence, I_ManageContradiction contradictionMgr) throws TerminologyException, IOException;

    /**
     * Retrieves tuples matching the specified allowedStatuses and positions
     * configured in the current profile
     * 
     * @param allowedStatus
     *            statuses tuples must match to be returned
     * @param positions
     *            positions a tuple must be on to be returned
     * @param addUncommitted
     *            if true matching items from the uncommitted list will be
     *            added, if false the uncommitted list is ignored
     * @param returnConflictResolvedLatestState
     *            indicates if all tuples or just the latest state using the
     *            current profile's conflict resolution strategy is required
     * @return List of matching tuples
     * @throws IOException
     * @throws TerminologyException
     */
    public List<? extends I_ExtendByRefVersion> getTuples(I_ManageContradiction contradictionMgr)
            throws TerminologyException, IOException;

	public List<? extends I_ExtendByRefVersion> getTuples();
	
	/**
	 * remove any uncommitted parts. 
	 */
	public void cancel();
	
	public boolean isUncommitted();
}
