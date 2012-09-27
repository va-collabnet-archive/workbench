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
import java.util.regex.Pattern;

import org.dwfa.ace.utypes.UniversalAceDescription;
import org.dwfa.tapi.I_DescribeConceptLocally;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionSetBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.description.DescriptionAnalogBI;

public interface I_DescriptionVersioned<A extends DescriptionAnalogBI>
        extends I_AmTermComponent, DescriptionAnalogBI<A> {

    public boolean addVersion(I_DescriptionPart<A> newPart);

    public List<? extends I_DescriptionPart<A>> getMutableParts();

    public int versionCount();

    public boolean matches(Pattern p);

    public int getConceptNid();

    public int getDescId();

    public List<? extends I_DescriptionTuple<A>> getTuples();

    /**
     * @param returnConflictResolvedLatestState
     * @return the tuples of this description, filtered to a conflict managed
     *         state if passed true
     * @throws TerminologyException
     * @throws IOException
     */
    public List<? extends I_DescriptionTuple<A>> getTuples(ContradictionManagerBI contradictionManager) throws TerminologyException,
            IOException;

    /**
     * @param returnConflictResolvedLatestState
     * @return the versions of this description, filtered to a conflict managed
     *         state if passed true
     * @throws TerminologyException
     * @throws IOException
     */
    public List<? extends I_DescriptionPart<A>> getVersions(ContradictionManagerBI contradictionManager) throws TerminologyException,
            IOException;

    public I_DescriptionTuple<A> getFirstTuple();

    public I_DescriptionTuple<A> getLastTuple();

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
    public void addTuples(NidSetBI allowedStatus, NidSetBI allowedTypes, PositionSetBI positionSet,
            List<I_DescriptionTuple<A>> matchingTuples, Precedence precedence, ContradictionManagerBI contradictionMgr)
            throws TerminologyException, IOException;

    public void convertIds(I_MapNativeToNative jarToDbNativeMap);

    public Set<TimePathId> getTimePathSet();

    public I_DescribeConceptLocally toLocalFixedDesc();

    public UniversalAceDescription getUniversal() throws IOException, TerminologyException;

}
