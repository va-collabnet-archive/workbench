package org.ihtsdo.db.bdb.concept.component.refsetmember.membership;

import java.io.IOException;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetVersion;
import org.ihtsdo.etypes.ERefsetVersion;

import com.sleepycat.bind.tuple.TupleInput;

public class MembershipVersion extends RefsetVersion<MembershipVersion, MembershipMember> {

	
	public MembershipVersion(int statusNid, int pathNid, long time, 
			MembershipMember primoridalMember) {
		super(statusNid, pathNid, time, 
				primoridalMember);
	}

	public MembershipVersion(int statusAtPositionNid, 
			MembershipMember primoridalMember) {
		super(statusAtPositionNid, primoridalMember);
	}

	public MembershipVersion(TupleInput input, 
			MembershipMember primoridalMember) {
		super(input, primoridalMember);
	}

	public MembershipVersion(ERefsetVersion eVersion,
			MembershipMember member) {
		super(eVersion, member);
	}

	@Override
	public UniversalAceExtByRefPart getUniversalPart()
			throws TerminologyException, IOException {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public RefsetVersion<MembershipVersion, MembershipMember> makePromotionPart(I_Path promotionPath) {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public int compareTo(I_ThinExtByRefPart o) {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public MembershipVersion getMutablePart() {
		return this;
	}

}
