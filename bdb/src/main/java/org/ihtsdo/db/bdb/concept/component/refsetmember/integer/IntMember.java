package org.ihtsdo.db.bdb.concept.component.refsetmember.integer;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;

import com.sleepycat.bind.tuple.TupleInput;

public class IntMember extends RefsetMember<IntVersion, IntMember> {
	private int intValue;

	public IntMember(int nid, int partCount, boolean editable) {
		super(nid, partCount, editable);
		throw new UnsupportedOperationException();
	}

	@Override
	protected final void readMemberParts(TupleInput input) {
		intValue = input.readInt();
		if (additionalVersions != null) {
			for (int i = 0; i < additionalVersions.size(); i++) {
				additionalVersions.add(new IntVersion(input));
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

	public int getIntValue() {
		return intValue;
	}

	public void setIntValue(int intValue) {
		this.intValue = intValue;
	}

}
