package org.dwfa.ace.api;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.utypes.UniversalAceConceptAttributes;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.TerminologyException;

public interface I_ConceptAttributeVersioned extends I_AmTermComponent {

	public boolean addVersion(I_ConceptAttributePart part);

	public List<I_ConceptAttributePart> getVersions();

	public int versionCount();

	public int getConId();

	public List<I_ConceptAttributeTuple> getTuples();

	public void convertIds(I_MapNativeToNative jarToDbNativeMap);

	public boolean merge(I_ConceptAttributeVersioned jarCon);

	public Set<TimePathId> getTimePathSet();

	/**
	 * Retrieves tuples matching the specified allowedStatuses and positions -
	 * tuples are returned in the supplied returnTuples List parameter -
	 * <strong>NOTE: this does not use the conflict management strategy</strong>.
	 * It is strongly recommended that you use a method that does use a conflict
	 * management strategy.
	 * 
	 * @see #addTuples(I_IntSet, Set, List, boolean, boolean)
	 * 
	 * @param allowedStatus
	 *            statuses tuples must match to be returned
	 * @param positions
	 *            postions a tuple must be on to be returned
	 * @param returnTuples
	 *            List to be populated with the result of the search
	 * @throws IOException
	 * @throws TerminologyException
	 */
	@Deprecated
    public void addTuples(I_IntSet allowedStatus,
         Set<I_Position> positionSet, List<I_ConceptAttributeTuple> returnTuples);

	/**
	 * Retrieves tuples matching the specified allowedStatuses and positions -
	 * tuples are returned in the supplied returnTuples List parameter -
	 * <strong>NOTE: this does not use the conflict management strategy</strong>.
	 * It is strongly recommended that you use a method that does use a conflict
	 * management strategy.
	 * 
	 * @see #addTuples(I_IntSet, Set, List, boolean, boolean)
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
	 * @throws IOException
	 * @throws TerminologyException
	 */
	@Deprecated
    public void addTuples(I_IntSet allowedStatus,
         Set<I_Position> positionSet, List<I_ConceptAttributeTuple> returnTuples, boolean addUncommitted);
   

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
    public void addTuples(I_IntSet allowedStatus,
         Set<I_Position> positionSet, List<I_ConceptAttributeTuple> returnTuples, boolean addUncommitted, 
         boolean returnConflictResolvedLatestState) throws TerminologyException, IOException;

	public I_ConceptualizeLocally getLocalFixedConcept();
	
	public UniversalAceConceptAttributes getUniversal() throws IOException, TerminologyException;

	public List<I_ConceptAttributeTuple> getTuples(I_IntSet allowedStatus,
			Set<I_Position> viewPositionSet);

}