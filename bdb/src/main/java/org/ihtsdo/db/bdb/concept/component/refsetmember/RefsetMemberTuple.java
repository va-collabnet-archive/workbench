package org.ihtsdo.db.bdb.concept.component.refsetmember;

import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.ihtsdo.db.bdb.concept.component.Tuple;

public abstract class RefsetMemberTuple  extends Tuple<RefsetMemberVariablePart, RefsetMember> 
	implements I_ThinExtByRefTuple {

	protected RefsetMemberTuple(RefsetMember component, RefsetMemberVariablePart part) {
		super(component, part);
	}
}
