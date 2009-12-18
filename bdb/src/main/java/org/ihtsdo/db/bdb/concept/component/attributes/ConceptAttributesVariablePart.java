package org.ihtsdo.db.bdb.concept.component.attributes;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.ihtsdo.db.bdb.concept.component.Version;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ConceptAttributesVariablePart extends Version<ConceptAttributesVariablePart> 
	implements I_ConceptAttributePart {

	private boolean defined = false;
	
	public ConceptAttributesVariablePart(int statusAtPositionNid) {
		super(statusAtPositionNid);
	}

	public ConceptAttributesVariablePart(ConceptAttributesVariablePart another,
			int statusNid, int pathNid, long time) {
		super(statusNid, pathNid, time);
		this.defined = another.defined;
	}
	public ConceptAttributesVariablePart(ConceptAttributesVariablePart another) {
		super(another.statusAtPositionNid, another.getPathId(), another.getTime());
		this.defined = another.defined;
	}

	public ConceptAttributesVariablePart(TupleInput input) {
		super(input.readInt());
		defined = input.readBoolean();
	}

	@Override
	public ConceptAttributesVariablePart makeAnalog(int statusNid, int pathNid, long time) {
		return new ConceptAttributesVariablePart(this, statusNid, pathNid, time);
	}

	@Override
	public boolean hasNewData(I_ConceptAttributePart another) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isDefined() {
		return defined;
	}

	@Override
	public void setDefined(boolean defined) {
		this.defined = defined;
	}

	@Override
	public ArrayIntList getVariableVersionNids() {
		return new ArrayIntList(2);
	}

	@Override
	public ConceptAttributesVariablePart duplicate() {
		return new ConceptAttributesVariablePart(this);
	}

	@Override
	protected void writeFieldsToBdb(TupleOutput output) {
		output.writeBoolean(defined);
	}

	
}
