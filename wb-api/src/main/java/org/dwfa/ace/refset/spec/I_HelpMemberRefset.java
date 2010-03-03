package org.dwfa.ace.refset.spec;

import java.util.Collection;
import java.util.Set;

import org.dwfa.ace.api.I_GetConceptData;

public interface I_HelpMemberRefset extends I_HelpSpecRefset {

	/**
	 * Add a collection of concepts to a refset.
	 * 
	 * @param members
	 *            The collection of concepts to be added to the refset
	 * @param batchDescription
	 *            A textual description of the batch being processed. Used in
	 *            the progress reports given during processing.
	 */
	public void addAllToRefset(Collection<I_GetConceptData> members,
			String batchDescription, boolean useMonitor) throws Exception;

	/**
	 * Add a collection of concepts to a refset.
	 * 
	 * @param members
	 *            The collection of concepts to be added to the refset
	 * @param batchDescription
	 *            A textual description of the batch being processed. Used in
	 *            the progress reports given during processing.
	 */
	public void addAllToRefset(Collection<I_GetConceptData> members,
			String batchDescription) throws Exception;

	public void addMarkedParents(Integer... conceptIds) throws Exception;

	public void addDescriptionMarkedParents(Integer... conceptIds)
			throws Exception;

	public void removeMarkedParents(Integer... conceptIds) throws Exception;

	public void removeDescriptionMarkedParents(Integer... conceptIds)
			throws Exception;

	/**
	 * Remove a collection of concepts from a refset.
	 * 
	 * @param members
	 *            The collection of concepts to be removed from the refset
	 * @param batchDescription
	 *            A textual description of the batch being processed. Used in
	 *            the progress reports given during processing.
	 */
	public void removeAllFromRefset(Collection<I_GetConceptData> members,
			String batchDescription, boolean useMonitor) throws Exception;

	/**
	 * Remove a collection of concepts from a refset.
	 * 
	 * @param members
	 *            The collection of concepts to be removed from the refset
	 * @param batchDescription
	 *            A textual description of the batch being processed. Used in
	 *            the progress reports given during processing.
	 */
	public void removeAllFromRefset(Collection<I_GetConceptData> members,
			String batchDescription) throws Exception;

	/**
	 * Add a concept to a refset
	 * 
	 * @param newMemberId
	 *            The concept to be added
	 */
	public boolean addToRefset(int conceptId) throws Exception;

	/**
	 * Remove a concept from a refset
	 * 
	 * @param newMemberId
	 *            The concept to be removed
	 */
	public boolean removeFromRefset(int conceptId) throws Exception;

	public int getMemberTypeId();

	public void setMemberTypeId(int memberTypeId);

	public int getMemberRefsetId();

	public void setMemberRefsetId(int memberRefsetId);

	public Set<Integer> getExistingMembers() throws Exception;

	/**
	 * Find all the current member refset concepts.
	 * <p>
	 * Member refsets must have the following properties:
	 * <ul>
	 * <li>Is a <i>refset identity</i>
	 * <li>A <i>refset purpose</i> of <i>refset membership</i>
	 */
	public Set<Integer> getMemberRefsets() throws Exception;

}