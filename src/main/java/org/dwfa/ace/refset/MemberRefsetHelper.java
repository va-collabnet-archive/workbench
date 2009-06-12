package org.dwfa.ace.refset;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.batch.Batch;

/**
 * Utility class providing refset membership operations. 
 */
public class MemberRefsetHelper extends RefsetHelper {

	private Logger logger = Logger.getLogger(MemberRefsetHelper.class.getName());
	
	private int memberTypeId;
	private int memberRefsetId;
	
	public MemberRefsetHelper(int memberRefsetId, int memberTypeId) throws Exception {
		super();
		setMemberRefsetId(memberRefsetId);
		setMemberTypeId(memberTypeId);
	}
	
	/**
	 * Add a collection of concepts to a refset.
	 * 
	 * @param members The collection of concepts to be added to the refset
	 * @param batchDescription A textual description of the batch being processed. 
	 *                         Used in the progress reports given during processing.
	 */
	public void addAllToRefset(Set<I_GetConceptData> members, String batchDescription) 
			throws Exception {
		
		Batch<I_GetConceptData> batch = new Batch<I_GetConceptData>(members, batchDescription) {

			Set<Integer> newMembers = new HashSet<Integer>();
			
			@Override
			public void processItem(I_GetConceptData item) throws Exception {
				if (newRefsetExtension(getMemberRefsetId(), item.getConceptId(), getMemberTypeId())) {
					newMembers.add(item.getConceptId());
				}
			}
	
			@Override
			public void onComplete() throws Exception {
				monitor.setText("Adding marked parent members...");
				monitor.setIndeterminate(true);
				addMarkedParents(newMembers.toArray(new Integer[]{}));
			}			
			
			@Override
			public void onCancel() throws Exception {
				logger.info("Batch operation '" + description + "' cancelled by user.");
				termFactory.cancel();
			}

		};
		
		batch.run();
	}
	
	protected void addMarkedParents(Integer ... conceptIds) throws Exception {
		new MarkedParentRefsetHelper(memberRefsetId, memberTypeId).addParentMembers(conceptIds);
	}

	protected void removeMarkedParents(Integer ... conceptIds) throws Exception {
		new MarkedParentRefsetHelper(memberRefsetId, memberTypeId).removeParentMembers(conceptIds);
	}
	
	/**
	 * Remove a collection of concepts from a refset.
	 * 
	 * @param members The collection of concepts to be removed from the refset
	 * @param batchDescription A textual description of the batch being processed. 
	 *                         Used in the progress reports given during processing.
	 */
	public void removeAllFromRefset(Set<I_GetConceptData> members, String batchDescription) 
			throws Exception {
		
		Batch<I_GetConceptData> batch = new Batch<I_GetConceptData>(members, batchDescription) {

			Set<Integer> removedMembers = new HashSet<Integer>();
			
			@Override
			public void processItem(I_GetConceptData item) throws Exception {
				 if (retireRefsetExtension(getMemberRefsetId(), item.getConceptId(), getMemberTypeId())) {
					 removedMembers.add(item.getConceptId());
				 }
			}

			@Override
			public void onComplete() throws Exception {
				monitor.setText("Removing marked parent members...");
				monitor.setIndeterminate(true);
				removeMarkedParents(removedMembers.toArray(new Integer[]{}));
			}
			
			@Override
			public void onCancel() throws Exception {
				logger.info("Batch operation '" + description + "' cancelled by user.");
				termFactory.cancel();
			}

		};
		
		batch.run();
	}	
	
	/**
	 * Add a concept to a refset
	 * 
	 * @param newMemberId The concept to be added
	 */
	public boolean addToRefset(int conceptId) throws Exception {
		if (newRefsetExtension(getMemberRefsetId(), conceptId, getMemberTypeId())) {
			addMarkedParents(conceptId);
			return true;
		} else return false;
	}

	/**
	 * Remove a concept from a refset
	 * 
	 * @param newMemberId The concept to be removed
	 */
	public boolean removeFromRefset(int conceptId) throws Exception {
		 if (retireRefsetExtension(getMemberRefsetId(), conceptId, getMemberTypeId())) {
			 removeMarkedParents(conceptId);
			 return true;
		 } else return false;
	}


	public int getMemberTypeId() {
		return memberTypeId;
	}


	public void setMemberTypeId(int memberTypeId) {
		this.memberTypeId = memberTypeId;
	}

	public int getMemberRefsetId() {
		return memberRefsetId;
	}

	public void setMemberRefsetId(int memberRefsetId) {
		this.memberRefsetId = memberRefsetId;
	}

}
