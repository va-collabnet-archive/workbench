package org.ihtsdo.db.bdb.concept.component.refsetmember.membership;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;

import com.sleepycat.bind.tuple.TupleInput;

public class MembershipMember extends RefsetMember<MembershipVersion, MembershipMember> {

	public MembershipMember(int nid, int partCount, boolean editable) {
		super(nid, partCount, editable);
	}

	@Override
	protected final void readMemberParts(TupleInput input) {
		if (additionalVersions != null) {
			for (int i = 0; i < additionalVersions.size(); i++) {
				additionalVersions.add(new MembershipVersion(input));
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
}
