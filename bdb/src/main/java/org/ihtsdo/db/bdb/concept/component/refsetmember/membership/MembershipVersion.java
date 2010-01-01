package org.ihtsdo.db.bdb.concept.component.refsetmember.membership;

import java.io.IOException;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMemberMutablePart;

import com.sleepycat.bind.tuple.TupleInput;

public class MembershipVersion extends RefsetMemberMutablePart {

	
	public MembershipVersion(int statusNid, int pathNid, long time) {
		super(statusNid, pathNid, time);
	}

	public MembershipVersion(int statusAtPositionNid) {
		super(statusAtPositionNid);
	}

	public MembershipVersion(TupleInput input) {
		super(input);
	}

	@Override
	public UniversalAceExtByRefPart getUniversalPart()
			throws TerminologyException, IOException {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public RefsetMemberMutablePart makePromotionPart(I_Path promotionPath) {
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
