package org.ihtsdo.db.bdb.concept.component.refsetmember.cidCidStr;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.ihtsdo.db.bdb.concept.component.refset.AbstractRefsetMember;

public class CidCidStrMember extends AbstractRefsetMember {

	public CidCidStrMember(int nid, int partCount, boolean editable) {
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
