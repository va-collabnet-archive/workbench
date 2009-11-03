package org.dwfa.ace.api;

import org.apache.lucene.util.OpenBitSet;

public class IdentifierSetReadOnly extends IdentifierSet {

	private IdentifierSetReadOnly() {
		super();
	}

	private IdentifierSetReadOnly(int numBits) {
		super(numBits);
	}

	public IdentifierSetReadOnly(IdentifierSet set) {
		this.bitSet =  (OpenBitSet) set.bitSet.clone();
	}

	@Override
	public void and(I_RepresentIdSet other) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void or(I_RepresentIdSet other) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setMember(int nid) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNotMember(int nid) {
		throw new UnsupportedOperationException();
	}

}
