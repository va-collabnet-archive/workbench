package org.ihtsdo.db.bdb.concept.component.refsetmember.template;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.ihtsdo.db.bdb.concept.component.refsetmember.RefsetMember;

public class TemplateMember extends RefsetMember {

	public TemplateMember(int nid, int partCount, boolean editable) {
		super(nid, partCount, editable);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected ArrayIntList getVariableVersionNids() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
		// TODO Auto-generated method stub
		return null;
	}

}
