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
import java.util.Set;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.tapi.TerminologyException;

public interface I_ThinExtByRefVersioned extends I_AmTermComponent {

    /**
     * @return <code><b>int</b></code> -- native identifier (nid) of this reference set member.<br>
     *         This is the surrogate key <code>I_ThinExtByRefVersioned</code> object itself.<br>
     *         The <code>I_ThinExtByRefVersioned</code> object associates a
     *         concept, description or relationship component as member of a given reference set
     */
    public int getMemberId();

    /**
     * @return <code><b>int</b></code> -- native identifier (nid) of the component which this reference set member extends.<br>
     *         The component extended by this <code>I_ThinExtByRefVersioned</code> may be a concept, description or relationship.<br>
     */
    public int getComponentId();

    /**
     * @return <code><b>int</b></code> -- native identifier (nid) for the concept which represent this reference set member's extension type.<br>
     *         Example types which this <code>I_ThinExtByRefVersioned</code> reference set member type are boolean, String, Concept and so on...<br>
     */
    public int getTypeId();

    public List<? extends I_ThinExtByRefPart> getVersions();

    public I_ThinExtByRefPart getLatestVersion();

    /**
     * @return <code><b>int</b></code> -- native identifier (nid) for the reference set to which this <code>I_ThinExtByRefVersioned</code>
     *         extension is a member.
     */
    public int getRefsetId();

    public void addVersion(I_ThinExtByRefPart part);

    public void setRefsetId(int refsetId);

    public void setTypeId(int typeId);

    /**
     * Retrieves tuples matching the specified allowedStatuses and positions -
     * tuples are returned in the supplied returnTuples List parameter -
     * <strong>NOTE: this does not use the conflict management
     * strategy</strong>.
     * It is strongly recommended that you use a method that does use a conflict
     * management strategy.
     *
     * @see #addTuples(I_IntSet, Set, List, boolean, boolean)
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
     * @throws IOException
     * @throws TerminologyException
     */
    @Deprecated
    public void addTuples(I_IntSet allowedStatus, Set<I_Position> positions, List<I_ThinExtByRefTuple> returnTuples,
            boolean addUncommitted);

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
    public void addTuples(I_IntSet allowedStatus, Set<I_Position> positions, List<I_ThinExtByRefTuple> returnTuples,
            boolean addUncommitted, boolean returnConflictResolvedLatestState) throws TerminologyException, IOException;

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
    public void addTuples(List<I_ThinExtByRefTuple> returnTuples, boolean addUncommitted,
            boolean returnConflictResolvedLatestState) throws TerminologyException, IOException;

    /**
     * Retrieves tuples matching the specified allowedStatuses and positions
     * configured in the current profile - <strong>NOTE: this does not use the
     * conflict management strategy</strong>. It is strongly recommended that
     * you use a method that does use a conflict management strategy.
     *
     * @see #getTuples(I_IntSet, Set, boolean, boolean)
     *
     * @param allowedStatus
     *            statuses tuples must match to be returned
     * @param positions
     *            positions a tuple must be on to be returned
     * @param addUncommitted
     *            if true matching items from the uncommitted list will be
     *            added, if false the uncommitted list is ignored
     * @return matching tuples
     */
    @Deprecated
    public List<I_ThinExtByRefTuple> getTuples(I_IntSet allowedStatus, Set<I_Position> positions, boolean addUncommitted);

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
    public List<I_ThinExtByRefTuple> getTuples(I_IntSet allowedStatus, Set<I_Position> positions,
            boolean addUncommitted, boolean returnConflictResolvedLatestState) throws TerminologyException, IOException;

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
    public List<I_ThinExtByRefTuple> getTuples(boolean addUncommitted, boolean returnConflictResolvedLatestState)
            throws TerminologyException, IOException;
}
