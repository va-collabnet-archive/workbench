package org.ihtsdo.db.bdb.concept.component.refsetmember.cidCid;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.etypes.ERefsetCidCidMember;

import com.sleepycat.bind.tuple.TupleInput;

public class CidCidMember extends RefsetMember<CidCidVersion, CidCidMember> {

	private int c1Nid;
	private int c2Nid;

	public CidCidMember(int nid, int partCount, boolean editable, int refsetNid) {
		super(nid, partCount, editable, refsetNid);
	}

	public CidCidMember(ERefsetCidCidMember refsetMember) {
		super(refsetMember);
		c1Nid = Bdb.uuidToNid(refsetMember.getC1Uuid());
		c2Nid = Bdb.uuidToNid(refsetMember.getC2Uuid());
	}

	@Override
	protected final void readMemberParts(TupleInput input) {
		c1Nid = input.readInt();
		c2Nid = input.readInt();
		if (additionalVersions != null) {
			for (int i = 0; i < additionalVersions.size(); i++) {
				additionalVersions.add(new CidCidVersion(input));
			}
		}
	}

	@Override
	protected ArrayIntList getVariableVersionNids() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getC1Nid() {
		return c1Nid;
	}

	public void setC1Nid(int c1Nid) {
		this.c1Nid = c1Nid;
	}

	public int getC2Nid() {
		return c2Nid;
	}

	public void setC2Nid(int c2Nid) {
		this.c2Nid = c2Nid;
	}

}
