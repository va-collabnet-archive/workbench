package org.ihtsdo.db.bdb.concept.component.refsetmember.membership;

import java.util.UUID;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.etypes.ERefset;

import com.sleepycat.bind.tuple.TupleInput;

public class MembershipMember extends RefsetMember<MembershipVersion, MembershipMember> {

	public MembershipMember(int nid, int partCount, Concept enclosingConcept, 
			UUID primordialUuid) {
		super(nid, partCount, enclosingConcept, 
				primordialUuid);
	}

	public MembershipMember(ERefset refsetMember, Concept enclosingConcept) {
		super(refsetMember, enclosingConcept);
	}

	@Override
	protected final void readMemberParts(TupleInput input) {
		if (additionalVersions != null) {
			for (int i = 0; i < additionalVersions.size(); i++) {
				additionalVersions.add(new MembershipVersion(input, this));
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
