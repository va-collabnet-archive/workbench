package org.ihtsdo.db.bdb.concept.component.refsetmember;

import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.ihtsdo.db.bdb.concept.component.Version;

public abstract class RefsetMemberVersion  extends Version<RefsetMemberVariablePart, RefsetMember> 
	implements I_ThinExtByRefTuple {

	protected RefsetMemberVersion(RefsetMember component, RefsetMemberVariablePart part) {
		super(component, part);
	}
}
