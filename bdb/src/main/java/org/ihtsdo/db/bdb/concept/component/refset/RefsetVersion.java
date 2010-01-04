package org.ihtsdo.db.bdb.concept.component.refset;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.ihtsdo.db.bdb.concept.component.Version;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public abstract class RefsetVersion<V extends RefsetVersion<V, C>, 
                                              C extends RefsetMember<V, C>> 
	extends Version<V, C> 
	implements I_ThinExtByRefPart {


	public RefsetVersion(int statusNid, int pathNid, long time, 
			C primordialComponent) {
		super(statusNid, pathNid, time, primordialComponent);
	}

	public RefsetVersion(int statusAtPositionNid, C primordialComponent) {
		super(statusAtPositionNid, primordialComponent);
	}

	public RefsetVersion(TupleInput input, C primordialComponent) {
		super(input, primordialComponent);
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
	public V makeAnalog(int statusNid, int pathNid, long time) {
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

	@Override
	public I_ThinExtByRefPart duplicate() {
		throw new UnsupportedOperationException();
	}

}
