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
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame.LANGUAGE_SORT_PREF;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.tapi.TerminologyException;

public interface I_GetConceptData extends I_AmTermComponent {

    /**
     * @return the versioned concept attribute entity associated with this
     *         concept
     * @throws IOException
     */
    public I_ConceptAttributeVersioned getConceptAttributes() throws IOException;

    public int getConceptId();

    /**
     * Retrieves tuples matching the specified allowedStatuses and positions -
     * <strong>NOTE: this does not use the conflict management
     * strategy</strong>.
     * It is strongly recommended that you use a method that does use a conflict
     * management strategy.
     *
     * @see #getConceptAttributeTuples(I_IntSet, Set, boolean, boolean)
     *
     * @param allowedStatus
     *            statuses tuples must match to be returned
     * @param positions
     *            postions a tuple must be on to be returned
     * @return List of matching tuples
     */
    @Deprecated
    public List<I_ConceptAttributeTuple> getConceptAttributeTuples(I_IntSet allowedStatus, Set<I_Position> positions)
            throws IOException;

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
    public List<I_ConceptAttributeTuple> getConceptAttributeTuples(I_IntSet allowedStatus, Set<I_Position> positions,
            boolean addUncommitted, boolean returnConflictResolvedLatestState) throws IOException, TerminologyException;

    /**
     * Retrieves tuples matching the specified allowedStatuses and positions -
     * <strong>NOTE: this does not use the conflict management
     * strategy</strong>.
     * It is strongly recommended that you use a method that does use a conflict
     * management strategy.
     *
     * @see #getConceptAttributeTuples(I_IntSet, Set, boolean, boolean)
     *
     * @param allowedStatus
     *            statuses tuples must match to be returned
     * @param positions
     *            postions a tuple must be on to be returned
     * @param addUncommitted
     *            indicates if uncommitted content should also be added in
     *            results
     * @return List of matching tuples
     */
    @Deprecated
    public List<I_ConceptAttributeTuple> getConceptAttributeTuples(I_IntSet allowedStatus, Set<I_Position> positions,
            boolean addUncommitted) throws IOException;

    /**
     * Retrieves tuples using the conflict strategy if specified
     *
     * @param returnConflictResolvedLatestState
     *            indicates if all tuples or just the latest state using the
     *            current profile's conflict resolution strategy is required
     * @return List of matching tuples
     * @throws TerminologyException
     */
    public List<I_ConceptAttributeTuple> getConceptAttributeTuples(boolean returnConflictResolvedLatestState)
            throws IOException, TerminologyException;

    /**
     * Retrieves tuples matching the specified allowedStatuses, allowedTypes and
     * positions -
     * <strong>NOTE: this does not use the conflict management
     * strategy</strong>.
     * It is strongly recommended that you use a method that does use a conflict
     * management strategy.
     *
     * @see #getDescriptionTuples(I_IntSet, I_IntSet, Set, boolean)
     *
     * @param allowedStatus
     *            statuses tuples must match to be returned
     * @param allowedTypes
     *            types tuples must match to be returned
     * @param positions
     *            postions a tuple must be on to be returned
     * @return List of matching tuples
     */
    @Deprecated
    public List<I_DescriptionTuple> getDescriptionTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
            Set<I_Position> positions) throws IOException;

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
     */
    public List<I_DescriptionTuple> getDescriptionTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
            Set<I_Position> positions, boolean returnConflictResolvedLatestState) throws IOException;

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
    public List<I_DescriptionTuple> getDescriptionTuples(boolean returnConflictResolvedLatestState) throws IOException,
            TerminologyException;

    public List<I_ThinExtByRefVersioned> getExtensions() throws IOException, TerminologyException;

    /**
     * Retrieves tuples matching the specified allowedStatuses, allowedTypes and
     * positions -
     * <strong>NOTE: this does not use the conflict management
     * strategy</strong>.
     * It is strongly recommended that you use a method that does use a conflict
     * management strategy.
     *
     * @param allowedStatus
     *            statuses tuples must match to be returned
     * @param allowedTypes
     *            types tuples must match to be returned
     * @param positions
     *            postions a tuple must be on to be returned
     * @return List of matching tuples
     * @throws TerminologyException
     */
    @Deprecated
    public List<I_ImageTuple> getImageTuples(I_IntSet allowedStatus, I_IntSet allowedTypes, Set<I_Position> positions)
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
     * @return List of matching tuples
     * @throws TerminologyException
     */
    public List<I_ImageTuple> getImageTuples(I_IntSet allowedStatus, I_IntSet allowedTypes, Set<I_Position> positions,
            boolean returnConflictResolvedLatestState) throws IOException;

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
    public List<I_ImageTuple> getImageTuples(boolean returnConflictResolvedLatestState) throws IOException,
            TerminologyException;

    /**
     * Gets the relationship tuples matching the supplied criteria that
     * originate from
     * this concept.
     * If you want to get all the is-a relationships for this concept, pass in
     * only is-a relationship types in the allowed types field.
     * <p>
     * <strong>NOTE: this does not use the conflict management
     * strategy</strong>. It is strongly recommended that you use a method that
     * does use a conflict management strategy.
     *
     * @see #getSourceRelTuples(I_IntSet, I_IntSet, Set, boolean, boolean)
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
     * @return a list of rel tuples that meet the provided criterion.
     * @throws IOException
     * @throws TerminologyException
     */
    @Deprecated
    public List<I_RelTuple> getSourceRelTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
            Set<I_Position> positions, boolean addUncommitted) throws IOException;

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
    public List<I_RelTuple> getSourceRelTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
            Set<I_Position> positions, boolean addUncommitted, boolean returnConflictResolvedLatestState)
            throws IOException, TerminologyException;

    /**
     * Retrieves tuples matching the specified allowedStatuses, allowedTypes and
     * positions configured in the current profile
     *
     * @param addUncommitted
     *            include uncommitted relationships in the return values
     * @param returnConflictResolvedLatestState
     *            indicates if all tuples or just the latest state using the
     *            current profile's conflict resolution strategy is required
     * @return List of matching tuples
     * @throws TerminologyException
     */
    public List<I_RelTuple> getSourceRelTuples(I_IntSet allowedTypes, boolean addUncommitted,
            boolean returnConflictResolvedLatestState) throws IOException, TerminologyException;

    /**
     * Retrieves tuples matching the specified allowedStatuses, allowedTypes and
     * positions -
     * <strong>NOTE: this does not use the conflict management
     * strategy</strong>.
     * It is strongly recommended that you use a method that does use a conflict
     * management strategy.
     *
     * @see #getDestRelTuples(I_IntSet, I_IntSet, Set, boolean, boolean)
     *
     * @param allowedStatus
     *            statuses tuples must match to be returned
     * @param allowedTypes
     *            types tuples must match to be returned
     * @param positions
     *            postions a tuple must be on to be returned
     *            * @param addUncommitted
     *            include uncommitted relationships in the return values
     * @return List of matching tuples
     * @throws TerminologyException
     */
    @Deprecated
    public List<I_RelTuple> getDestRelTuples(I_IntSet allowedStatus, I_IntSet allowedTypes, Set<I_Position> positions,
            boolean addUncommitted) throws IOException;

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
    public List<I_RelTuple> getDestRelTuples(I_IntSet allowedStatus, I_IntSet allowedTypes, Set<I_Position> positions,
            boolean addUncommitted, boolean returnConflictResolvedLatestState) throws IOException, TerminologyException;

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
    public List<I_RelTuple> getDestRelTuples(I_IntSet allowedTypes, boolean addUncommitted,
            boolean returnConflictResolvedLatestState) throws IOException, TerminologyException;

    /**
     * If you want to get all the is-a immediate parents for this concept, pass
     * in only is-a relationship types in the allowed types field. -
     * <strong>NOTE: this does not use the conflict management
     * strategy</strong>.
     * It is strongly recommended that you use a method that does use a conflict
     * management strategy.
     *
     * @see #getSourceRelTargets(I_IntSet, I_IntSet, Set, boolean, boolean)
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
     * @return a list of I_GetConceptData that meet the provided criterion.
     * @throws IOException
     * @throws TerminologyException
     */
    @Deprecated
    public Set<I_GetConceptData> getSourceRelTargets(I_IntSet allowedStatus, I_IntSet allowedTypes,
            Set<I_Position> positions, boolean addUncommitted) throws IOException;

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
    public Set<I_GetConceptData> getSourceRelTargets(I_IntSet allowedStatus, I_IntSet allowedTypes,
            Set<I_Position> positions, boolean addUncommitted, boolean returnConflictResolvedLatestState)
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
    public Set<I_GetConceptData> getSourceRelTargets(I_IntSet allowedTypes, boolean addUncommitted,
            boolean returnConflictResolvedLatestState) throws IOException, TerminologyException;

    /**
     * Gets the relationship origins based on the passed statuses, relationship
     * types and positions -
     * <strong>NOTE: this does not use the conflict management
     * strategy</strong>.
     * It is strongly recommended that you use a method that does use a conflict
     * management strategy.
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
     * @return relationship origins based on the passed statuses, relationship
     *         types and positions
     * @throws IOException
     * @throws TerminologyException
     */
    @Deprecated
    public Set<I_GetConceptData> getDestRelOrigins(I_IntSet allowedStatus, I_IntSet allowedTypes,
            Set<I_Position> positions, boolean addUncommitted) throws IOException;

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
    public Set<I_GetConceptData> getDestRelOrigins(I_IntSet allowedStatus, I_IntSet allowedTypes,
            Set<I_Position> positions, boolean addUncommitted, boolean returnConflictResolvedLatestState)
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
    public Set<I_GetConceptData> getDestRelOrigins(I_IntSet allowedTypes, boolean addUncommitted,
            boolean returnConflictResolvedLatestState) throws IOException, TerminologyException;

    public List<I_DescriptionVersioned> getDescriptions() throws IOException;

    public List<I_RelVersioned> getDestRels() throws IOException;

    /**
     *
     * @return all relationships originating at this concept, regardless of type
     *         of status.
     * @throws IOException
     */
    public List<I_RelVersioned> getSourceRels() throws IOException;

    public String getInitialText() throws IOException;

    public boolean isLeaf(I_ConfigAceFrame aceConfig, boolean addUncommitted) throws IOException;

    public List<I_ImageVersioned> getImages() throws IOException;

    public List<UUID> getUids() throws IOException;

    public List<I_DescriptionVersioned> getUncommittedDescriptions();

    public List<I_RelVersioned> getUncommittedSourceRels();

    public I_ConceptAttributeVersioned getUncommittedConceptAttributes();

    public List<I_ImageVersioned> getUncommittedImages();

    public I_IdVersioned getId() throws IOException;

    public I_DescriptionTuple getDescTuple(I_IntList typePrefOrder, I_IntList langPrefOrder, I_IntSet allowedStatus,
            Set<I_Position> positionSet, LANGUAGE_SORT_PREF sortPref) throws IOException;

    public I_DescriptionTuple getDescTuple(I_IntList descTypePreferenceList, I_ConfigAceFrame config)
            throws IOException;

    public I_IntSet getUncommittedIds();

    public UniversalAceBean getUniversalAceBean() throws IOException, TerminologyException;

    public boolean isParentOf(I_GetConceptData child, I_IntSet allowedStatus, I_IntSet allowedTypes,
            Set<I_Position> positions, boolean addUncommitted) throws IOException;

    public boolean isParentOf(I_GetConceptData child, boolean addUncommitted) throws IOException, TerminologyException;

    public boolean isParentOfOrEqualTo(I_GetConceptData child, I_IntSet allowedStatus, I_IntSet allowedTypes,
            Set<I_Position> positions, boolean addUncommitted) throws IOException;

    public boolean isParentOfOrEqualTo(I_GetConceptData child, boolean addUncommitted) throws IOException,
            TerminologyException;

    public Object getId(int identifierScheme) throws IOException, TerminologyException;

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
    public I_RepresentIdSet getPossibleKindOfConcepts(I_ConfigAceFrame config) throws IOException;

    public List<I_IdVersioned> getUncommittedIdVersioned();

}
