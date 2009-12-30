package org.ihtsdo.db.bdb.concept.component.attributes;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.ihtsdo.db.bdb.concept.component.Version;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ConceptAttributesVersion 
	extends Version<ConceptAttributesVersion, ConceptAttributes> 
	implements I_ConceptAttributePart, I_ConceptAttributeTuple {

	private transient ConceptAttributes conceptAttributes;
	
	private boolean defined = false;
	
	public ConceptAttributesVersion(int statusAtPositionNid) {
		super(statusAtPositionNid);
	}

	public ConceptAttributesVersion(I_ConceptAttributePart another,
			int statusNid, int pathNid, long time) {
		super(statusNid, pathNid, time);
		this.defined = another.isDefined();
	}

	public ConceptAttributesVersion(I_ConceptAttributePart another) {
		super(another.getStatusId(), another.getPathId(), another.getTime());
		this.defined = another.isDefined();
	}

	public ConceptAttributesVersion(int statusNid, int pathNid, long time) {
		super(statusNid, pathNid, time);
	}

	public ConceptAttributesVersion(TupleInput input) {
		super(input.readInt());
		defined = input.readBoolean();
	}


	@Override
	public ConceptAttributesVersion makeAnalog(int statusNid, int pathNid, long time) {
		return new ConceptAttributesVersion(this, statusNid, pathNid, time);
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
	public ConceptAttributesVersion duplicate() {
		return new ConceptAttributesVersion(this);
	}

	@Override
	protected void writeFieldsToBdb(TupleOutput output) {
		output.writeBoolean(defined);
	}

	@Override
	public int getConId() {
		return conceptAttributes.nid;
	}

	@Override
	public ConceptAttributes getConVersioned() {
		return conceptAttributes;
	}

	@Override
	public int getConceptStatus() {
		return getStatusId();
	}

	@Override
	public ConceptAttributesVersion getMutablePart() {
		return this;
	}

}
