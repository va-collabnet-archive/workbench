package org.ihtsdo.db.bdb.concept.component.refsetmember;

import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.ihtsdo.db.bdb.concept.component.Tuple;

public abstract class RefsetMemberTuple  extends Tuple<RefsetMemberPart, RefsetMember> 
	implements I_ThinExtByRefTuple<RefsetMemberPart, RefsetMemberTuple, RefsetMember> {

	private RefsetMemberTuple() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public RefsetMember getFixedPart() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getFixedPartId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public RefsetMemberPart getPart() {
		// TODO Auto-generated method stub
		return null;
	}

}
