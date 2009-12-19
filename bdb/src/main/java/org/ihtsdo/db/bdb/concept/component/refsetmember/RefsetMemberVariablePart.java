package org.ihtsdo.db.bdb.concept.component.refsetmember;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.ihtsdo.db.bdb.concept.component.VariablePart;

import com.sleepycat.bind.tuple.TupleOutput;

public abstract class RefsetMemberVariablePart extends VariablePart<RefsetMemberVariablePart> 
	implements I_ThinExtByRefPart {

	private RefsetMemberVariablePart(int statusNid, int pathNid, long time) {
		super(statusNid, pathNid, time);
		// TODO Auto-generated constructor stub
	}

	private RefsetMemberVariablePart(int statusAtPositionNid) {
		super(statusAtPositionNid);
		// TODO Auto-generated constructor stub
	}

	@Override
	public RefsetMemberVariablePart makeAnalog(int statusNid, int pathNid, long time) {
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
