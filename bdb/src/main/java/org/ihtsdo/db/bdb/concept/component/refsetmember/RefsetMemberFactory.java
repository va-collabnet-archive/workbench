package org.ihtsdo.db.bdb.concept.component.refsetmember;

import org.ihtsdo.db.bdb.concept.component.ComponentFactory;

public class RefsetMemberFactory extends ComponentFactory<RefsetMember, RefsetMemberVariablePart> {

	@Override
	public RefsetMember create(int nid, int partCount, boolean editable) {
		return new RefsetMember(nid, partCount, editable);
	}

}