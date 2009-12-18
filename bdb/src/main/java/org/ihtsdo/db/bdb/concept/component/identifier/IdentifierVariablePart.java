package org.ihtsdo.db.bdb.concept.component.identifier;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_IdPart;
import org.ihtsdo.db.bdb.concept.component.Version;

import com.sleepycat.bind.tuple.TupleOutput;

public class IdentifierVariablePart extends Version<IdentifierVersion> implements I_IdPart {

	private int sourceSystemNid;
	private Object sourceId;
	
	protected IdentifierVariablePart(int statusNid, int pathNid, long time) {
		super(statusNid, pathNid, time);
	}

	protected IdentifierVariablePart(int statusAtPositionNid) {
		super(statusAtPositionNid);
	}

	@Override
	public IdentifierVersion makeAnalog(int statusNid, int pathNid, long time) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void writeFieldsToBdb(TupleOutput output) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getSource() {
		return sourceSystemNid;
	}

	@Override
	public Object getSourceId() {
		return sourceId;
	}

	@Override
	public void setSource(int sourceNid) {
		this.sourceSystemNid = sourceNid;
	}

	@Override
	public void setSourceId(Object sourceId) {
		this.sourceId = sourceId;
	}

	@Override
	public ArrayIntList getPartComponentNids() {
		ArrayIntList nids = super.get
		return null;
	}

}
