package org.dwfa.ace.api;

import java.io.IOException;

import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.OpenBitSet;

public class IdentifierSet implements I_RepresentIdSet  {

	protected OpenBitSet bitSet;
	private int offset = 0 - Integer.MIN_VALUE;
	
	public IdentifierSet(OpenBitSet bitSet) {
		super();
		this.bitSet = bitSet;
	}

	public IdentifierSet() {
		bitSet = new OpenBitSet();
	}

	public IdentifierSet(int numBits) {
		bitSet = new OpenBitSet(numBits);
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_RepresentIdSet#isMember(int)
	 */
	public boolean isMember(int nid) {
		return bitSet.fastGet(nid + offset);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_RepresentIdSet#setMember(int)
	 */
	public void setMember(int nid) {
		bitSet.fastSet(nid + offset);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_RepresentIdSet#setNotMember(int)
	 */
	public void setNotMember(int nid) {
		bitSet.fastClear(nid + offset);
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_RepresentIdSet#and(org.dwfa.ace.api.IdentifierSet)
	 */
	public void and(I_RepresentIdSet other) {
		bitSet.and(((IdentifierSet)other).bitSet);
	}
	
	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_RepresentIdSet#or(org.dwfa.ace.api.IdentifierSet)
	 */
	public void or(I_RepresentIdSet other) {
		bitSet.or(((IdentifierSet)other).bitSet);
	}
	
	public I_RepresentIdSet duplicate() {
		return new IdentifierSet((OpenBitSet) bitSet.clone());
	}

	public I_IterateIds iterator() {
		return new NidIterator(bitSet.iterator());
	}

	private static class NidIterator implements I_IterateIds {
		private DocIdSetIterator docIterator;
		
		private NidIterator(DocIdSetIterator docIterator) {
			super();
			this.docIterator = docIterator;
		}

		public boolean next() throws IOException {
			return docIterator.next();
		}

		public int nid() {
			return docIterator.doc() + Integer.MIN_VALUE;
		}

		public boolean skipTo(int target) throws IOException {
			return docIterator.skipTo(target - Integer.MIN_VALUE);
		}
	}
}
