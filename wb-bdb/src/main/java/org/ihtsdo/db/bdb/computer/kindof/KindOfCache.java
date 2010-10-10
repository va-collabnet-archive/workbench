package org.ihtsdo.db.bdb.computer.kindof;

import java.io.IOException;

import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.Terms;

public class KindOfCache {

	/**
	 * Only need an approximate query count, so no need to incur
	 * AtomicInt overhead.
	 */
	private int queryCount = 0;
	
	/**
	 * The set of cNids for which kindOf has been tested.
	 */
	private I_RepresentIdSet tested;
	
	private long lastRequestTime = System.currentTimeMillis();
	
	/**
	 * The set of tested cNids that are 
	 * determined to be a kind-of. 
	 */
	private I_RepresentIdSet kindOf;
	private I_RepresentIdSet possiblyKindOf;

	public KindOfCache(I_RepresentIdSet possiblyKindOf) throws IOException {
		super();
		tested = Terms.get().getEmptyIdSet();
		kindOf = Terms.get().getEmptyIdSet();
		this.possiblyKindOf = possiblyKindOf;
	}

	public boolean tested(int cNid) {
		if (possiblyKindOf.isMember(cNid) == false) {
			return true;
		}
		return tested.isMember(cNid);
	}
	
	public boolean isKindOf(int cNid) {
		if (possiblyKindOf.isMember(cNid) == false) {
			return false;
		}
		if (tested.isMember(cNid)) {
			queryCount++;
			lastRequestTime = System.currentTimeMillis();
			return kindOf.isMember(cNid);
		}
		throw new RuntimeException(
				"You must setKindOf before calling isKindOf."
						+ " Use tested(int cNid) to determine if setKindOf is set.");
	}
	
	public void setKindOf(int cNid, boolean isKindOf) {
		if (!tested.isMember(cNid)) {
			if (isKindOf) {
				kindOf.setMember(cNid);
			}
			tested.setMember(cNid);
		}
	}

	public int getSize() {
		return tested.cardinality();
	}

	public long getLastRequestTime() {
		return lastRequestTime;
	}

	public int getQueryCount() {
		return queryCount;
	}
	
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("KindOfCache: tested count: " );
		buff.append(tested.cardinality());
		buff.append("\n   tested: ");
		buff.append(tested.toString());
		buff.append("\n kindOf count: " );
		buff.append(kindOf.cardinality());
		buff.append("\n   kindOf: ");
		buff.append(kindOf);
		return buff.toString();
	}

	public I_RepresentIdSet getPossibleKindOfConcepts() {
		return possiblyKindOf;
	}
}
