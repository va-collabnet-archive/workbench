package org.ihtsdo.db.bdb.concept.component.refsetmember.membertype;

import java.io.IOException;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.component.refsetmember.RefsetMemberPart;

public class MembershipPart extends RefsetMemberPart {

	@Override
	public UniversalAceExtByRefPart getUniversalPart()
			throws TerminologyException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RefsetMemberPart makePromotionPart(I_Path promotionPath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int compareTo(I_ThinExtByRefPart<RefsetMemberPart> o) {
		// TODO Auto-generated method stub
		return 0;
	}

}
