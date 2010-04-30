package org.ihtsdo.db.util;

import java.util.List;

import org.dwfa.util.HashFunction;

public class NidPair {
	private int nid1;
	private int nid2;
	private int hash;
	
	public NidPair(int nid1, int nid2) {
		super();
		this.nid1 = nid1;
		this.nid2 = nid2;
		HashFunction.hashCode(new int[] { nid1, nid2 });
	}

	@Override
	public boolean equals(Object obj) {
		if (NidPair.class.equals(obj.getClass())) {
			NidPair another = (NidPair) obj;
			return this.nid1 == another.nid1 && this.nid2 == another.nid2;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public String toString() {
		return "nid1: " + nid1 + " nid2:" + nid2;
	}
	
	public void  addToList(List<Integer> list) {
		list.add(nid1);
		list.add(nid2);
	}

	public int getNid1() {
		return nid1;
	}

	public int getNid2() {
		return nid2;
	}
}
