package org.ihtsdo.db.bdb.concept.component.refsetmember;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.ihtsdo.db.bdb.concept.component.MutablePart;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public abstract class RefsetMemberMutablePart extends MutablePart<RefsetMemberMutablePart> 
	implements I_ThinExtByRefPart {


	public RefsetMemberMutablePart(int statusNid, int pathNid, long time) {
		super(statusNid, pathNid, time);
	}

	public RefsetMemberMutablePart(int statusAtPositionNid) {
		super(statusAtPositionNid);
	}

	public RefsetMemberMutablePart(TupleInput input) {
		super(input);
	}

	@Override
	public final int getStatus() {
		return getStatusId();
	}

	@Override
	public final void setStatus(int idStatus) {
		throw new UnsupportedOperationException();
	}


	@Override
	public RefsetMemberMutablePart makeAnalog(int statusNid, int pathNid, long time) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void writeFieldsToBdb(TupleOutput output) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected ArrayIntList getVariableVersionNids() {
		throw new UnsupportedOperationException();
	}

}
