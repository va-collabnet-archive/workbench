package org.ihtsdo.db.bdb.concept.component.attributes;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.ihtsdo.db.bdb.concept.component.MutablePart;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ConceptAttributesMutablePart extends MutablePart<ConceptAttributesMutablePart> 
	implements I_ConceptAttributePart {

	private boolean defined = false;
	
	public ConceptAttributesMutablePart(int statusAtPositionNid) {
		super(statusAtPositionNid);
	}

	public ConceptAttributesMutablePart(ConceptAttributesMutablePart another,
			int statusNid, int pathNid, long time) {
		super(statusNid, pathNid, time);
		this.defined = another.defined;
	}

	public ConceptAttributesMutablePart(I_ConceptAttributePart another) {
		super(another.getStatusId(), another.getPathId(), another.getTime());
		this.defined = another.isDefined();
	}

	public ConceptAttributesMutablePart(int statusNid, int pathNid, long time) {
		super(statusNid, pathNid, time);
	}

	public ConceptAttributesMutablePart(TupleInput input) {
		super(input.readInt());
		defined = input.readBoolean();
	}


	@Override
	public ConceptAttributesMutablePart makeAnalog(int statusNid, int pathNid, long time) {
		return new ConceptAttributesMutablePart(this, statusNid, pathNid, time);
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
	protected ArrayIntList getVariableVersionNids() {
		return new ArrayIntList(2);
	}

	@Override
	public ConceptAttributesMutablePart duplicate() {
		return new ConceptAttributesMutablePart(this);
	}

	@Override
	protected void writeFieldsToBdb(TupleOutput output) {
		output.writeBoolean(defined);
	}

}
