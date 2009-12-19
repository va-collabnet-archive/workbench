package org.ihtsdo.db.bdb.concept.component.relationship;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_RelPart;
import org.ihtsdo.db.bdb.concept.component.VariablePart;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class RelationshipVariablePart extends VariablePart<RelationshipVariablePart> 
	implements I_RelPart {
	
	private int characteristicNid;
	private int group;
	private int refinabilityNid;
	private int typeNid;

	public RelationshipVariablePart(int statusAtPositionNid) {
		super(statusAtPositionNid);
	}

	public RelationshipVariablePart(RelationshipVariablePart another) {
		super(another.statusAtPositionNid);
		this.characteristicNid = another.characteristicNid;
		this.group = another.group;
		this.refinabilityNid = another.refinabilityNid;
		this.typeNid = another.typeNid;
	}

	public RelationshipVariablePart(RelationshipVariablePart another, int statusNid,
			int pathNid, long time) {
		super(statusNid, pathNid, time);
		this.characteristicNid = another.characteristicNid;
		this.group = another.group;
		this.refinabilityNid = another.refinabilityNid;
		this.typeNid = another.typeNid;
	}

	public RelationshipVariablePart(TupleInput input) {
		super(input.readInt());
		this.characteristicNid = input.readInt();
		this.group = input.readInt();
		this.refinabilityNid = input.readInt();
		this.typeNid = input.readInt();
	}
	@Override
	protected void writeFieldsToBdb(TupleOutput output) {
		output.writeInt(characteristicNid);
		output.writeInt(group);
		output.writeInt(refinabilityNid);
		output.writeInt(typeNid);
	}	

	public int getCharacteristicId() {
		return characteristicNid;
	}

	public void setCharacteristicId(int characteristicNid) {
		this.characteristicNid = characteristicNid;
	}

	public int getGroup() {
		return group;
	}

	public void setGroup(int group) {
		this.group = group;
	}

	public int getRefinabilityId() {
		return refinabilityNid;
	}

	public void setRefinabilityId(int refinabilityNid) {
		this.refinabilityNid = refinabilityNid;
	}

	public int getTypeId() {
		return typeNid;
	}

	public void setTypeId(int typeNid) {
		this.typeNid = typeNid;
	}

	@Override
	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		throw new UnsupportedOperationException();
	}

	@Override
	public RelationshipVariablePart duplicate() {
		return new RelationshipVariablePart(this);
	}
	
	@Override
	public RelationshipVariablePart makeAnalog(int statusNid, int pathNid, long time) {
		return new RelationshipVariablePart(this, statusNid, pathNid, time);
	}

	@Override
	protected ArrayIntList getVariableVersionNids() {
		ArrayIntList nids = new ArrayIntList(5);
		nids.add(characteristicNid);
		nids.add(refinabilityNid);
		nids.add(typeNid);
		return nids;
	}

	@Override
	public boolean hasNewData(I_RelPart another) {
		throw new UnsupportedOperationException();
	}
}
