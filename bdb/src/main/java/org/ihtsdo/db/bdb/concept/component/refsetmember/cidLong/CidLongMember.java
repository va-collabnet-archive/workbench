package org.ihtsdo.db.bdb.concept.component.refsetmember.cidLong;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.etypes.ERefsetCidLongMember;

import com.sleepycat.bind.tuple.TupleInput;

public class CidLongMember 
				extends RefsetMember<CidLongVersion, CidLongMember> {

	private int c1Nid;
	private long longValue;

	public CidLongMember(int nid, int partCount, boolean editable, int refsetNid) {
		super(nid, partCount, editable, refsetNid);
	}

	public CidLongMember(ERefsetCidLongMember refsetMember) {
		super(refsetMember);
		c1Nid = Bdb.uuidToNid(refsetMember.getC1Uuid());
		longValue = refsetMember.getLongValue();
	}

	@Override
	protected final void readMemberParts(TupleInput input) {
		c1Nid = input.readInt();
		longValue = input.readLong();
		if (additionalVersions != null) {
			for (int i = 0; i < additionalVersions.size(); i++) {
				additionalVersions.add(new CidLongVersion(input));
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

	public long getLongValue() {
		return longValue;
	}

	public void setLongValue(long longValue) {
		this.longValue = longValue;
	}

}
