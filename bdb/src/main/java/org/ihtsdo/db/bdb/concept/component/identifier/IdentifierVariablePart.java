package org.ihtsdo.db.bdb.concept.component.identifier;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_IdPart;
import org.ihtsdo.db.bdb.concept.component.VariablePart;
import org.ihtsdo.db.bdb.concept.component.identifier.Identifier.VARIABLE_PART_TYPES;

import com.sleepycat.bind.tuple.TupleOutput;

public abstract class IdentifierVariablePart 
	extends VariablePart<IdentifierVariablePart> 
		implements I_IdPart {

	private int sourceSystemNid;
	private Object sourceId;
	
	protected IdentifierVariablePart(int statusNid, int pathNid, long time) {
		super(statusNid, pathNid, time);
	}

	protected IdentifierVariablePart(int statusAtPositionNid) {
		super(statusAtPositionNid);
	}
	
	protected abstract VARIABLE_PART_TYPES getType();

	@Override
	public IdentifierVariablePart makeAnalog(int statusNid, int pathNid, long time) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void writeFieldsToBdb(TupleOutput output) {
		output.writeInt(sourceSystemNid);
		writeSourceIdToBdb(output);
	}
	protected abstract void writeSourceIdToBdb(TupleOutput output);

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
	protected ArrayIntList getVariableVersionNids() {
		ArrayIntList nids = new ArrayIntList(3);
		nids.add(sourceSystemNid);
		return nids;
	}

}
