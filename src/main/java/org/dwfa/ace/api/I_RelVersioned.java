package org.dwfa.ace.api;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.utypes.UniversalAceRelationship;
import org.dwfa.tapi.TerminologyException;

public interface I_RelVersioned extends I_AmTermComponent {

	public boolean addVersion(I_RelPart rel);

	public boolean addVersionNoRedundancyCheck(I_RelPart rel);

	public List<I_RelPart> getVersions();
	
	public List<I_RelPart> getVersions(boolean returnConflictResolvedLatestState) throws TerminologyException, IOException;

	public int versionCount();

	public boolean addRetiredRec(int[] releases, int retiredStatusId);

	public boolean removeRedundantRecs();

	/**
	 * 
	 * @return the native id of the source concept (c1) of this relationship
 	 */
	public int getC1Id();
	/**
	 * 
	 * @return the native id of the destination concept (c2) of this relationship
	 */
	public int getC2Id();

	public int getRelId();
	
	/**
	 * @return all the tuples for this relationship
	 */
	public List<I_RelTuple> getTuples();

	/**
	 * @param returnConflictResolvedLatestState
	 * @return tuples for the relationship optionally resolved using the conflict management strategy
	 * @throws TerminologyException
	 * @throws IOException
	 */
	public List<I_RelTuple> getTuples(boolean returnConflictResolvedLatestState) throws TerminologyException, IOException;

	public I_RelTuple getFirstTuple();

	public I_RelTuple getLastTuple();

	/**
	 * Retrieves tuples matching the specified allowedStatuses, allowedTypes and positions -
	 * tuples are returned in the supplied returnTuples List parameter - 
	 * <strong>NOTE: this does not use the conflict management strategy</strong>.
	 * It is strongly recommended that you use a method that does use a conflict
	 * management strategy.
	 * 
	 * @see #addTuples(I_IntSet, I_IntSet, Set, List, boolean, boolean)
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
	@Deprecated
	public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
			Set<I_Position> positions, List<I_RelTuple> returnRels,
			boolean addUncommitted);
			
	/**
	 * Retrieves tuples matching the specified allowedStatuses, allowedTypes and positions -
	 * tuples are returned in the supplied returnTuples List parameter
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
	 * @param returnConflictResolvedLatestState
	 *            indicates if all tuples or just the latest state using the
	 *            current profile's conflict resolution strategy is required
	 * @throws IOException
	 * @throws TerminologyException
	 */
	public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
			Set<I_Position> positions, List<I_RelTuple> returnRels,
			boolean addUncommitted, boolean returnConflictResolvedLatestState) throws TerminologyException, IOException;
	
	/**
	 * Retrieves tuples matching the specified allowedStatuses, allowedTypes and positions -
	 * tuples are returned in the supplied returnTuples List parameter
	 * 
	 * @param allowedTypes
	 *            types tuples must match to be returned
	 * @param returnRels
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
	public void addTuples(I_IntSet allowedTypes, List<I_RelTuple> returnRels,
			boolean addUncommitted, boolean returnConflictResolvedLatestState) throws TerminologyException, IOException;

	public void convertIds(I_MapNativeToNative jarToDbNativeMap);

	public boolean merge(I_RelVersioned jarRel);

	public Set<TimePathId> getTimePathSet();

	public void setC2Id(int destId);

	public UniversalAceRelationship getUniversal() throws IOException,
			TerminologyException;

}