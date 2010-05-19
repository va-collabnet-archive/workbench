package org.ihtsdo.db.util;

import java.util.List;

import org.dwfa.util.HashFunction;

public class NidPair {
	private int nid1;
	private int nid2;
	private int hash;
	
	public NidPair(long nids) {
        super();
        this.nid1 = (int) nids;
        this.nid2 = (int) (nids >>> 32);
	}
	
	public NidPair(int nid1, int nid2) {
		super();
		this.nid1 = nid1;
		this.nid2 = nid2;
		HashFunction.hashCode(new int[] { nid1, nid2 });
	}
	
	public long asLong() {
        long returnValue = nid2;
        returnValue = returnValue & 0x00000000FFFFFFFFL;
        long nid1Long = nid1;
        nid1Long = nid1Long & 0x00000000FFFFFFFFL;
        returnValue = returnValue << 32;
        returnValue = returnValue | nid1Long;
	    return returnValue;
	}

	@Override
	public boolean equals(Object obj) {
		NidPair another = (NidPair) obj;
		return this.nid1 == another.nid1 && this.nid2 == another.nid2;
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
