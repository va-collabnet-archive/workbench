package org.ihtsdo.db.util;

import java.util.List;

import org.dwfa.util.HashFunction;
import org.ihtsdo.db.bdb.Bdb;

public abstract class NidPair implements Comparable<NidPair> {
	protected int nid1;
	protected int nid2;
	private int hash;
	
	protected NidPair(long nids) {
        super();
 	}
	
	public static NidPair getNidPair(long nids) {
	       int nid1 = (int) nids;
	       int nid2 = (int) (nids >>> 32);
	       if (Bdb.getConceptNid(nid2) == nid2) {
	    	   return getTypeNidRelNidPair(nid2, nid1);
	       }
		return getRefsetNidMemberNidPair(nid1, nid2);

	}
	public static NidPairForRel getTypeNidRelNidPair(int typeNid, int rNid) {
		// the type (nid2) is a concept, the rNid is not. 
		return new NidPairForRel(rNid, typeNid);
	}

	public static NidPairForRefset getRefsetNidMemberNidPair(int refsetNid, int memberNid) {
		// the refset (nid1) is a concept, the memberNid is not. 
		return new NidPairForRefset(refsetNid, memberNid);
	}
	
	public abstract boolean isRelPair();
	

	protected NidPair(int nid1, int nid2) {
		super();
		this.nid1 = nid1;
		this.nid2 = nid2;
		this.hash = HashFunction.hashCode(new int[] { nid1, nid2 });
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
            if (obj.getClass().isAssignableFrom(NidPair.class)) {
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

	public boolean isRefsetPair() {
		return !isRelPair();
	}

    @Override
    public int compareTo(NidPair o) {
        long diff = asLong() - o.asLong();
        if (diff > 0) {
            return 1;
        }
        if (diff < 0) {
            return -1;
        }
        return 0;
    }

}
