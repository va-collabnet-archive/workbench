package org.ihtsdo.db.bdb.concept.component.attributes;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.ihtsdo.db.bdb.concept.component.Part;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ConceptAttributesPart extends Part<ConceptAttributesPart> 
	implements I_ConceptAttributePart<ConceptAttributesPart> {

	private boolean defined = false;
	
	public ConceptAttributesPart(int statusAtPositionNid) {
		super(statusAtPositionNid);
	}

	public ConceptAttributesPart(ConceptAttributesPart another,
			int statusNid, int pathNid, long time) {
		super(statusNid, pathNid, time);
		this.defined = another.defined;
	}
	public ConceptAttributesPart(ConceptAttributesPart another) {
		super(another.statusAtPositionNid, another.getPathId(), another.getTime());
		this.defined = another.defined;
	}

	public ConceptAttributesPart(TupleInput input) {
		super(input.readInt());
		defined = input.readBoolean();
	}

	@Override
	public ConceptAttributesPart makeAnalog(int statusNid, int pathNid, long time) {
		return new ConceptAttributesPart(this, statusNid, pathNid, time);
	}

	@Override
	public boolean hasNewData(ConceptAttributesPart another) {
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
	public ArrayIntList getPartComponentNids() {
		return super.getComponentNids();
	}

	@Override
	public ConceptAttributesPart duplicate() {
		return new ConceptAttributesPart(this);
	}

	@Override
	protected void writeFieldsToBdb(TupleOutput output) {
		output.writeBoolean(defined);
	}

	
}
