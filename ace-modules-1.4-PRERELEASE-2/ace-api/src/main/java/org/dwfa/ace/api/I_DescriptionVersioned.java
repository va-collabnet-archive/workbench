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

import org.dwfa.ace.utypes.UniversalAceDescription;
import org.dwfa.tapi.I_DescribeConceptLocally;
import org.dwfa.tapi.TerminologyException;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public interface I_DescriptionVersioned extends I_AmVersioned<I_DescriptionPart> {

    public boolean addVersion(I_DescriptionPart newPart);

    public List<I_DescriptionPart> getVersions();

    /**
     * @param returnConflictResolvedLatestState
     * @return the versions of this description, filtered to a conflict managed
     *         state if passed true
     * @throws TerminologyException
     * @throws IOException
     */
    public List<I_DescriptionPart> getVersions(boolean returnConflictResolvedLatestState) throws TerminologyException,
            IOException;

    public int versionCount();

    public boolean matches(Pattern p);

    public int getConceptId();

    public int getDescId();

    public List<I_DescriptionTuple> getTuples();

    /**
     * @param returnConflictResolvedLatestState
     * @return the tuples of this description, filtered to a conflict managed
     *         state if passed true
     * @throws TerminologyException
     * @throws IOException
     */
    public List<I_DescriptionTuple> getTuples(boolean returnConflictResolvedLatestState) throws TerminologyException,
            IOException;

    public I_DescriptionTuple getFirstTuple();

    /**
     * Returns the last description of a tuple.
     *
     * @return The last description of a tuple.
     * @throws DescriptionHasNoVersionsException If the tuple has 0 versions.
     */
    public I_DescriptionTuple getLastTuple() throws DescriptionHasNoVersionsException;

    public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes, Set<I_Position> positionSet,
            List<I_DescriptionTuple> matchingTuples, boolean addUncommitted);

    /**
     * Retrieves tuples matching the specified allowedStatuses, allowedTypes and
     * positions -
     * tuples are returned in the supplied returnTuples List parameter
     *
     * @param allowedStatus
     *            statuses tuples must match to be returned
     * @param allowedTypes
     *            types tuples must match to be returned
     * @param positionSet
     *            postions a tuple must be on to be returned
     * @param matchingTuples
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
    public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes, Set<I_Position> positionSet,
            List<I_DescriptionTuple> matchingTuples, boolean addUncommitted, boolean returnConflictResolvedLatestState)
            throws TerminologyException, IOException;

    public void convertIds(I_MapNativeToNative jarToDbNativeMap);

    public boolean merge(I_DescriptionVersioned jarDesc);

    public Set<TimePathId> getTimePathSet();

    public I_DescribeConceptLocally toLocalFixedDesc();

    public UniversalAceDescription getUniversal() throws IOException, TerminologyException;

}
