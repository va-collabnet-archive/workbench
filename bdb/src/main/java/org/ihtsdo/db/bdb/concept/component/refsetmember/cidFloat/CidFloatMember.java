package org.ihtsdo.db.bdb.concept.component.refsetmember.cidFloat;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.etypes.ERefsetCidFloatMember;

import com.sleepycat.bind.tuple.TupleInput;

public class CidFloatMember extends RefsetMember<CidFloatVersion, CidFloatMember> {

	private int c1Nid;
	private float floatValue;

	public CidFloatMember(int nid, int partCount, boolean editable, int refsetNid) {
		super(nid, partCount, editable, refsetNid);
	}

	public CidFloatMember(ERefsetCidFloatMember refsetMember) {
		super(refsetMember);
		c1Nid = Bdb.uuidToNid(refsetMember.getC1Uuid());
		floatValue = refsetMember.getFloatValue();
	}

	@Override
	protected final void readMemberParts(TupleInput input) {
		c1Nid = input.readInt();
		floatValue = input.readFloat();
		if (additionalVersions != null) {
			for (int i = 0; i < additionalVersions.size(); i++) {
				additionalVersions.add(new CidFloatVersion(input));
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

	public float getFloatValue() {
		return floatValue;
	}

	public void setFloatValue(float floatValue) {
		this.floatValue = floatValue;
	}

}
