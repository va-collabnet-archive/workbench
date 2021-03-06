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
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame.LANGUAGE_SORT_PREF;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.NidListBI;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionSetBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

public interface I_GetConceptData extends I_AmTermComponent, ConceptChronicleBI {

    /**
     * @return the versioned concept attribute entity associated with this
     *         concept
     * @throws IOException
     */
    public I_ConceptAttributeVersioned getConAttrs() throws IOException;

    public int getConceptNid();

    public boolean everHadSrcRelOfType(int typeNid) throws IOException;

    /**
     * Retrieves tuples matching the specified allowedStatuses and positions
     * 
     * @param allowedStatus
     *            statuses tuples must match to be returned
     * @param positions
     *            postions a tuple must be on to be returned
     * @param addUncommitted
     *            indicates if uncommitted content should also be added in
     *            results
     * @param returnConflictResolvedLatestState
     *            indicates if all tuples or just the latest state using the
     *            current profile's conflict resolution strategy is required
     * @return List of matching tuples
     * @throws TerminologyException
     */
    public List<? extends I_ConceptAttributeTuple> getConceptAttributeTuples(NidSetBI allowedStatus,
            PositionSetBI positions, Precedence precedencePolicy, ContradictionManagerBI contradictionManager)
            throws IOException, TerminologyException;
    
    /**
     * Retrieves tuples matching the specified allowedStatuses and positions
     * 
     * @param allowedStatus
     *            statuses tuples must match to be returned
     * @param positions
     *            positions a tuple must be on to be returned
     * @param returnConflictResolvedLatestState
     *            indicates if all tuples or just the latest state using the
     *            current profile's conflict resolution strategy is required
     * @param cuttoffTime
     *          cutoff time to match tuples, tuples with a time greater than
     *          cutoff will no be returned
     * @return List of matching tuples
     * @throws TerminologyException
     */
    public List<? extends I_ConceptAttributeTuple> getConceptAttributeTuples(NidSetBI allowedStatus,
            PositionSetBI positions, Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager, long cuttoffTime)
            throws IOException, TerminologyException;

    /**
     * Retrieves tuples using the conflict strategy if specified
     * 
     * @param returnConflictResolvedLatestState
     *            indicates if all tuples or just the latest state using the
     *            current profile's conflict resolution strategy is required
     * @return List of matching tuples
     * @throws TerminologyException
     */
    public List<? extends I_ConceptAttributeTuple> getConceptAttributeTuples(Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager) throws IOException, TerminologyException;

    /**
     * Retrieves tuples matching the specified allowedStatuses, allowedTypes and
     * positions
     * 
     * @param allowedStatus
     *            statuses tuples must match to be returned
     * @param allowedTypes
     *            types tuples must match to be returned
     * @param positions
     *            positions a tuple must be on to be returned
     * @param returnConflictResolvedLatestState
     *            indicates if all tuples or just the latest state using the
     *            current profile's conflict resolution strategy is required
     * @return List of matching tuples
     */
    public List<? extends I_DescriptionTuple> getDescriptionTuples(NidSetBI allowedStatus, NidSetBI allowedTypes,
            PositionSetBI positionSet, Precedence precedencePolicy, ContradictionManagerBI contradictionManager)
            throws IOException;
    /**
     * Retrieves tuples matching the specified allowedStatuses, allowedTypes and
     * positions
     * 
     * @param allowedStatus
     *            statuses tuples must match to be returned
     * @param allowedTypes
     *            types tuples must match to be returned
     * @param positions
     *            postions a tuple must be on to be returned
     * @param returnConflictResolvedLatestState
     *            indicates if all tuples or just the latest state using the
     *            current profile's conflict resolution strategy is required
     * @param cuttoffTime
     *          cutoff time to match tuples, tuples with a time greater than
     *          cutoff will no be returned
     * @return List of matching tuples
     */
    public List<? extends I_DescriptionTuple> getDescriptionTuples(NidSetBI allowedStatus, NidSetBI allowedTypes,
            PositionSetBI positionSet, Precedence precedencePolicy, ContradictionManagerBI contradictionManager,
            long cuttoffTime)
            throws IOException;

    /**
     * Retrieves tuples matching the specified allowedStatuses, allowedTypes and
     * positions configured in the current profile
     * 
     * @param returnConflictResolvedLatestState
     *            indicates if all tuples or just the latest state using the
     *            current profile's conflict resolution strategy is required
     * @return List of matching tuples
     * @throws TerminologyException
     */
    @Deprecated
    public List<? extends I_DescriptionTuple> getDescriptionTuples() throws IOException, TerminologyException;

    public Collection<? extends I_ExtendByRef> getExtensions() throws IOException, TerminologyException;

    /**
     * Retrieves tuples matching the specified allowedStatuses, allowedTypes and
     * positions
     * 
     * @param allowedStatus
     *            statuses tuples must match to be returned
     * @param allowedTypes
     *            types tuples must match to be returned
     * @param positions
     *            postions a tuple must be on to be returned
     * @param returnConflictResolvedLatestState
     *            indicates if all tuples or just the latest state using the
     *            current profile's conflict resolution strategy is required
     * @return List of matching tuples
     * @throws TerminologyException
     */
    public List<? extends I_ImageTuple> getImageTuples(NidSetBI allowedStatus, NidSetBI allowedTypes,
            PositionSetBI positions, Precedence precedencePolicy, ContradictionManagerBI contradictionManager)
            throws IOException, TerminologyException;

    /**
     * Retrieves tuples matching the specified allowedStatuses, allowedTypes and
     * positions configured in the current profile
     * 
     * @param returnConflictResolvedLatestState
     *            indicates if all tuples or just the latest state using the
     *            current profile's conflict resolution strategy is required
     * @return List of matching tuples
     * @throws TerminologyException
     */
    public List<? extends I_ImageTuple> getImageTuples() throws IOException, TerminologyException;

    /**
     * If you want to get all the is-a relationships for this concept, pass in
     * only is-a relationship types in the allowed types field.
     * 
     * @param allowedStatus
     *            set of allowed status values that the I_RelTuple must conform
     *            with
     * @param allowedTypes
     *            set of allowed relationship types that the I_RelTuple must
     *            conform with
     * @param positions
     *            set of positions used to determine the state of status, and
     *            types for query
     * @param addUncommitted
     *            include uncommitted relationships in the return values
     * @param returnConflictResolvedLatestState
     *            indicates if all tuples or just the latest state using the
     *            current profile's conflict resolution strategy is required
     * @return a list of rel tuples that meet the provided criterion.
     * @throws IOException
     * @throws TerminologyException
     */
    public List<? extends I_RelTuple> getSourceRelTuples(NidSetBI allowedStatus, NidSetBI allowedTypes,
            PositionSetBI positions, Precedence precedencePolicy, ContradictionManagerBI contradictionManager)
            throws IOException, TerminologyException;
    /**
     * Retrieves tuples matching the specified allowedStatuses and positions
     * 
     * @param allowedStatus
     *            statuses tuples must match to be returned
     * @param positions
     *            positions a tuple must be on to be returned
     * @param returnConflictResolvedLatestState
     *            indicates if all tuples or just the latest state using the
     *            current profile's conflict resolution strategy is required
     * @param cuttoffTime
     *          cutoff time to match tuples, tuples with a time greater than
     *          cutoff will no be returned
     * @return List of matching tuples
     * @throws TerminologyException
     */
    
     public List<? extends I_RelTuple> getSourceRelTuples(NidSetBI allowedStatus, NidSetBI allowedTypes,
           PositionSetBI positions, Precedence precedencePolicy, 
           ContradictionManagerBI contradictionManager, Long cutoffTime)
           throws IOException, TerminologyException;

    public List<? extends I_RelTuple> getSourceRelTuples(NidSetBI allowedStatus, NidSetBI allowedTypes,
            PositionSetBI positions,
            Precedence precedencePolicy, ContradictionManagerBI contradictionManager,
            int classifierNid, RelAssertionType relAssertionType)
            throws IOException, TerminologyException;
    
    /**
     * Retrieves tuples matching the specified allowedStatuses and positions
     * 
     * @param allowedStatus
     *            statuses tuples must match to be returned
     * @param positions
     *            positions a tuple must be on to be returned
     * @param returnConflictResolvedLatestState
     *            indicates if all tuples or just the latest state using the
     *            current profile's conflict resolution strategy is required
     * @param cuttoffTime
     *          cutoff time to match tuples, tuples with a time greater than
     *          cutoff will no be returned
     * @return List of matching tuples
     * @throws TerminologyException
     */
    
     public List<? extends I_RelTuple> getSourceRelTuples(NidSetBI allowedStatus, NidSetBI allowedTypes,
           PositionSetBI positions, Precedence precedencePolicy, ContradictionManagerBI contradictionManager,
           int classifierNid, RelAssertionType relAssertionType, Long cutoffTime)
           throws IOException, TerminologyException;

    /**
     * Retrieves tuples matching the specified allowedStatuses, allowedTypes and
     * positions
     * 
     * @param allowedStatus
     *            statuses tuples must match to be returned
     * @param allowedTypes
     *            types tuples must match to be returned
     * @param positions
     *            postions a tuple must be on to be returned
     *            * @param addUncommitted
     *            include uncommitted relationships in the return values
     * @param returnConflictResolvedLatestState
     *            indicates if all tuples or just the latest state using the
     *            current profile's conflict resolution strategy is required
     * @return List of matching tuples
     * @throws TerminologyException
     */
    public List<? extends I_RelTuple> getDestRelTuples(NidSetBI allowedStatus, NidSetBI allowedTypes,
            PositionSetBI positions, Precedence precedencePolicy, ContradictionManagerBI contradictionManager)
            throws IOException, TerminologyException;

    public List<? extends I_RelTuple> getDestRelTuples(NidSetBI allowedStatus, NidSetBI allowedTypes, PositionSetBI positions,
            Precedence precedencePolicy, ContradictionManagerBI contradictionManager,
            int classifierNid, RelAssertionType relAssertionType) throws IOException, TerminologyException;

    /**
     * Retrieves tuples matching the specified allowedStatuses, allowedTypes and
     * positions configured in the current profile
     * 
     * @param allowedTypes
     *            types tuples must match to be returned
     * @param addUncommitted
     *            include uncommitted relationships in the return values
     * @param returnConflictResolvedLatestState
     *            indicates if all tuples or just the latest state using the
     *            current profile's conflict resolution strategy is required
     * @return List of matching tuples
     * @throws TerminologyException
     */
    public List<? extends I_RelTuple> getDestRelTuples(NidSetBI allowedTypes, Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager) throws IOException, TerminologyException;

    /**
     * If you want to get all the is-a immediate parents for this concept, pass
     * in only is-a relationship types in the allowed types field.
     * 
     * @param allowedStatus
     *            set of allowed status values that the I_RelTuple must conform
     *            with
     * @param allowedTypes
     *            set of allowed relationship types that the I_RelTuple must
     *            conform with
     * @param positions
     *            set of positions used to determine the state of status, and
     *            types for query
     * @param addUncommitted
     *            include uncommitted relationships in the return values
     * @param returnConflictResolvedLatestState
     *            indicates if all tuples or just the latest state using the
     *            current profile's conflict resolution strategy is required
     * @return a list of I_GetConceptData that meet the provided criterion.
     * @throws IOException
     * @throws TerminologyException
     */
    public Set<? extends I_GetConceptData> getSourceRelTargets(NidSetBI allowedStatus, NidSetBI allowedTypes,
            PositionSetBI positions, Precedence precedencePolicy, ContradictionManagerBI contradictionManager)
            throws IOException, TerminologyException;

    /**
     * returns source rel targets based on the current profile settings
     * 
     * @param allowedTypes
     *            types of relationships for which the targets will be returned
     * @param addUncommitted
     *            include uncommitted relationships in the return values
     * @param returnConflictResolvedLatestState
     *            indicates if all tuples or just the latest state using the
     *            current profile's conflict resolution strategy is required
     * @return a list of I_GetConceptData that meet the provided criterion.
     * @return a list of I_GetConceptData that meet the provided criterion.
     * @throws IOException
     * @throws TerminologyException
     */
    public Set<? extends I_GetConceptData> getSourceRelTargets(NidSetBI allowedTypes, Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager) throws IOException, TerminologyException;

    /**
     * Gets the relationship origins based on the passed statuses, relationship
     * types and positions
     * 
     * @param allowedStatus
     *            allowed statuses when matching relationships
     * @param allowedTypes
     *            allowed relationship types
     * @param positions
     *            positions relationships must match
     * @param addUncommitted
     *            indicates whether the uncommitted list should be included in
     *            the search
     * @param returnConflictResolvedLatestState
     *            indicates whether to return all matches or just conflict
     *            resolved matches using the conflict resolution strategy in the
     *            current profile
     * @return relationship origins based on the passed statuses, relationship
     *         types and positions
     * @throws IOException
     * @throws TerminologyException
     */
    public Set<? extends I_GetConceptData> getDestRelOrigins(NidSetBI allowedStatus, NidSetBI allowedTypes,
            PositionSetBI positions, Precedence precedencePolicy, ContradictionManagerBI contradictionManager)
            throws IOException, TerminologyException;

    /**
     * Gets the relationship origins based on the passed relationship
     * types
     * 
     * @param allowedTypes
     *            allowed relationship types
     * @param addUncommitted
     *            indicates whether the uncommitted list should be included in
     *            the search
     * @param returnConflictResolvedLatestState
     *            indicates whether to return all matches or just conflict
     *            resolved matches using the conflict resolution strategy in the
     *            current profile
     * @return relationship origins based on the passed relationship
     *         types
     * @throws IOException
     * @throws TerminologyException
     */
    @Deprecated
    public Set<? extends I_GetConceptData> getDestRelOrigins(NidSetBI allowedTypes) throws IOException,
            TerminologyException;

    public Collection<? extends I_DescriptionVersioned> getDescs() throws IOException;

    public Collection<? extends I_RelVersioned> getDestRels() throws IOException;

    public I_RelVersioned getDestRel(int relNid) throws IOException;

    public I_RelVersioned getSourceRel(int relNid) throws IOException;

    /**
     * 
     * @return all relationships originating at this concept, regardless of type
     *         of status.
     * @throws IOException
     */
    public Collection<? extends I_RelVersioned> getSourceRels() throws IOException;

    public String getInitialText() throws IOException;

    public boolean isLeaf(I_ConfigAceFrame aceConfig, boolean addUncommitted) throws IOException;

    public Collection<? extends I_ImageVersioned> getImages() throws IOException;

    public List<UUID> getUids() throws IOException;

    public I_Identify getIdentifier() throws IOException;

    public I_DescriptionTuple getDescTuple(NidListBI typePrefOrder, NidListBI langPrefOrder, NidSetBI allowedStatus,
            PositionSetBI positionSet, LANGUAGE_SORT_PREF sortPref, Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager) throws IOException;

    public I_DescriptionTuple getDescTuple(NidListBI descTypePreferenceList, I_ConfigAceFrame config)
            throws IOException;

    public NidSetBI getUncommittedIds();

    public UniversalAceBean getUniversalAceBean() throws IOException, TerminologyException;

    public boolean isParentOf(I_GetConceptData child, NidSetBI allowedStatus, NidSetBI allowedTypes,
            PositionSetBI positions, Precedence precedencePolicy, ContradictionManagerBI contradictionManager)
            throws IOException, TerminologyException, ContradictionException;

    @Deprecated
    public boolean isParentOf(I_GetConceptData child) throws IOException, TerminologyException, ContradictionException;

    public boolean isParentOfOrEqualTo(I_GetConceptData child, NidSetBI allowedStatus, NidSetBI allowedTypes,
            PositionSetBI positions, Precedence precedencePolicy, ContradictionManagerBI contradictionManager)
            throws IOException, TerminologyException, ContradictionException;

    @Deprecated
    public boolean isParentOfOrEqualTo(I_GetConceptData child) throws IOException, TerminologyException, ContradictionException;

    /**
     * Denotation: the act of pointing out by name. Used as an 
     * alternative to the repeated use of identifier with different
     * contextual meanings. 
     * 
     * @return
     */
    public Object getDenotation(int authorityNid) throws IOException, TerminologyException;

    /**
     * This method efficiently determines "possible" concepts that are a
     * "kind of" a concept,
     * and bypasses more comprehensive checks (such as version checks). It is
     * useful
     * for pre-processing queries to limit number of concepts that the
     * full query spec must be tested for...
     * 
     * @param config
     * @return A collection of the native identifiers of all possible children
     *         of this concept
     *         according to the relationships specified in the config.
     * @throws IOException
     */
    public NidBitSetBI getPossibleKindOfConcepts(I_ConfigAceFrame config) throws IOException, ContradictionException;

    /**
     * Child of = only immediate children, not grand-children, etc. 
     * 
     * @param configFrame
     * @return
     * @throws IOException
     */
    public I_RepresentIdSet getPossibleChildOfConcepts(I_ConfigAceFrame configFrame) throws IOException, ContradictionException;

    public List<I_Identify> getUncommittedIdVersioned();

    public String toLongString();

    public boolean isCanceled() throws IOException;

    public Set<? extends I_ConceptAttributeTuple> getCommonConceptAttributeTuples(I_ConfigAceFrame config)
            throws IOException, TerminologyException;

    public Set<? extends I_DescriptionTuple> getCommonDescTuples(I_ConfigAceFrame config) throws IOException;

    public Set<? extends I_RelTuple> getCommonRelTuples(I_ConfigAceFrame config) throws IOException,
            TerminologyException;
}
