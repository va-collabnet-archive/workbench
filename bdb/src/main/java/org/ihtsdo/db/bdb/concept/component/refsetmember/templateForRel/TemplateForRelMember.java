package org.ihtsdo.db.bdb.concept.component.refsetmember.templateForRel;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.ihtsdo.db.bdb.concept.component.refset.AbstractRefsetMember;

public class TemplateForRelMember extends AbstractRefsetMember {

	public TemplateForRelMember(int nid, int partCount, boolean editable) {
		super(nid, partCount, editable);
		throw new UnsupportedOperationException();
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
