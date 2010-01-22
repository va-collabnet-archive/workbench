package org.ihtsdo.db.bdb.concept.component.refsetmember.membership;

import java.io.IOException;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetRevision;
import org.ihtsdo.etypes.ERefsetVersion;

import com.sleepycat.bind.tuple.TupleInput;

public class MembershipRevision extends RefsetRevision<MembershipRevision, MembershipMember> {

	
	
	public String toString() {
		return " MembershipVersion: " +super.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (MembershipRevision.class.isAssignableFrom(obj.getClass())) {
			return super.equals(obj);
		}
		return false;
	}

	public MembershipRevision(int statusNid, int pathNid, long time, 
			MembershipMember primoridalMember) {
		super(statusNid, pathNid, time, 
				primoridalMember);
	}

	public MembershipRevision(int statusAtPositionNid, 
			MembershipMember primoridalMember) {
		super(statusAtPositionNid, primoridalMember);
	}

	public MembershipRevision(TupleInput input, 
			MembershipMember primoridalMember) {
		super(input, primoridalMember);
	}

	public MembershipRevision(ERefsetVersion eVersion,
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
	public RefsetRevision<MembershipRevision, MembershipMember> makePromotionPart(I_Path promotionPath) {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public int compareTo(I_ThinExtByRefPart o) {
		// TODO
		throw new UnsupportedOperationException();
	}

}
