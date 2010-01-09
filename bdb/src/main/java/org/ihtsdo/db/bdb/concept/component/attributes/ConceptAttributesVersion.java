package org.ihtsdo.db.bdb.concept.component.attributes;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.component.Version;
import org.ihtsdo.etypes.I_ConceptualizeExternally;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ConceptAttributesVersion 
	extends Version<ConceptAttributesVersion, ConceptAttributes> 
	implements I_ConceptAttributePart, I_ConceptAttributeTuple {

	private transient ConceptAttributes conceptAttributes;
	
	private boolean defined = false;
	
	public ConceptAttributesVersion(int statusAtPositionNid, 
			ConceptAttributes primoridalMember) {
		super(statusAtPositionNid, primoridalMember);
	}

	public ConceptAttributesVersion(I_ConceptualizeExternally another, 
			ConceptAttributes primoridalMember) {
		super(Bdb.uuidToNid(another.getStatusUuid()), 
				Bdb.uuidToNid(another.getPathUuid()),
				another.getTime(), primoridalMember);
		defined = isDefined();
	}
	public ConceptAttributesVersion(I_ConceptAttributePart another,
			int statusNid, int pathNid, long time, 
			ConceptAttributes primoridalMember) {
		super(statusNid, pathNid, time, primoridalMember);
		this.defined = another.isDefined();
	}

	public ConceptAttributesVersion(I_ConceptAttributePart another, 
			ConceptAttributes primoridalMember) {
		super(another.getStatusId(), another.getPathId(), another.getTime(), 
				primoridalMember);
		this.defined = another.isDefined();
	}

	public ConceptAttributesVersion(int statusNid, int pathNid, long time, 
			ConceptAttributes primoridalMember) {
		super(statusNid, pathNid, time, 
				primoridalMember);
	}

	public ConceptAttributesVersion(TupleInput input, 
			ConceptAttributes primoridalMember) {
		super(input.readInt(), primoridalMember);
		defined = input.readBoolean();
	}


	@Override
	public ConceptAttributesVersion makeAnalog(int statusNid, int pathNid, long time) {
		return new ConceptAttributesVersion(this, statusNid, pathNid, time, this.primordialComponent);
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
		return new ConceptAttributesVersion(this, this.primordialComponent);
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
	public String toString() {
		return " defined: " + defined + " " + super.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (ConceptAttributesVersion.class.isAssignableFrom(obj.getClass())) {
			ConceptAttributesVersion another = (ConceptAttributesVersion) obj;
			if (this.defined == another.defined) {
				return super.equals(obj);
			}
		}
		return false;
	}

}
