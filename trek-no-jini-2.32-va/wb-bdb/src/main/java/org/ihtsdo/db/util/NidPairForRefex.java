package org.ihtsdo.db.util;

public class NidPairForRefex extends NidPair {

	protected NidPairForRefex(int rNid, int typeNid) {
		super(rNid, typeNid);
	}

	protected NidPairForRefex(long nids) {
		super(nids);
	}

	public boolean isRelPair() {
		return false;
	}

	public int getRefexNid() {
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
