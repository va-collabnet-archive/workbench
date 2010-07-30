package org.ihtsdo.db.util;

public class NidPairForRefset extends NidPair {

	protected NidPairForRefset(int rNid, int typeNid) {
		super(rNid, typeNid);
	}

	protected NidPairForRefset(long nids) {
		super(nids);
	}

	public boolean isRelPair() {
		return false;
	}

	public int getRefsetNid() {
		return nid1;
	}

	public int getMemberNid() {
		return nid2;
	}
	
	@Override
	public String toString() {
		return "refsetNid: " + nid1 + " memberNid:" + nid2;
	}

}
