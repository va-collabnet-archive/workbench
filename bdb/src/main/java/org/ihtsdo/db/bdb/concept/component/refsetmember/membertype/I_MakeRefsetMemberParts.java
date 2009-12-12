package org.ihtsdo.db.bdb.concept.component.refsetmember.membertype;

import org.ihtsdo.db.bdb.concept.component.refsetmember.RefsetMemberPart;

import com.sleepycat.bind.tuple.TupleInput;

public interface I_MakeRefsetMemberParts<P extends RefsetMemberPart> {
	public P makePart(TupleInput input);
}
