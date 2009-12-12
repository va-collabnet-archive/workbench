package org.ihtsdo.db.bdb.concept.component.refsetmember;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.ihtsdo.db.bdb.concept.component.Part;

import com.sleepycat.bind.tuple.TupleOutput;

public abstract class RefsetMemberPart extends Part<RefsetMemberPart> 
	implements I_ThinExtByRefPart<RefsetMemberPart> {

	private RefsetMemberPart(int statusNid, int pathNid, long time) {
		super(statusNid, pathNid, time);
		// TODO Auto-generated constructor stub
	}

	private RefsetMemberPart(int statusAtPositionNid) {
		super(statusAtPositionNid);
		// TODO Auto-generated constructor stub
	}

	@Override
	public RefsetMemberPart makeAnalog(int statusNid, int pathNid, long time) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void writeFieldsToBdb(TupleOutput output) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ArrayIntList getPartComponentNids() {
		// TODO Auto-generated method stub
		return null;
	}

}
