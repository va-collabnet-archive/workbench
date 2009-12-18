package org.ihtsdo.db.bdb.concept.component.refsetmember.membertype;

import org.ihtsdo.db.bdb.concept.component.refsetmember.RefsetMemberVariablePart;

import com.sleepycat.bind.tuple.TupleInput;

public interface I_MakeRefsetMemberParts<P extends RefsetMemberVariablePart> {
	public P makePart(TupleInput input);
}
