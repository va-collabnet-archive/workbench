package org.ihtsdo.db.bdb.concept.component.relationship;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.utypes.UniversalAceRelationshipPart;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.component.Version;
import org.ihtsdo.etypes.ERelationshipVersion;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class RelationshipVersion 
	extends Version<RelationshipVersion, Relationship> 
	implements I_RelPart, I_RelTuple {
	
	private transient Relationship relationship;
	
	private int characteristicNid;
	private int group;
	private int refinabilityNid;
	private int typeNid;

	public RelationshipVersion(int statusAtPositionNid) {
		super(statusAtPositionNid);
	}

	public RelationshipVersion(RelationshipVersion another) {
		super(another.statusAtPositionNid);
		this.characteristicNid = another.characteristicNid;
		this.group = another.group;
		this.refinabilityNid = another.refinabilityNid;
		this.typeNid = another.typeNid;
	}

	public RelationshipVersion(I_RelPart another, int statusNid,
			int pathNid, long time) {
		super(statusNid, pathNid, time);
		this.characteristicNid = another.getCharacteristicId();
		this.group = another.getGroup();
		this.refinabilityNid = another.getRefinabilityId();
		this.typeNid = another.getTypeId();
	}

	public RelationshipVersion(TupleInput input) {
		super(input.readInt());
		this.characteristicNid = input.readInt();
		this.group = input.readInt();
		this.refinabilityNid = input.readInt();
		this.typeNid = input.readInt();
	}

	public RelationshipVersion(ERelationshipVersion erv) {
		super(Bdb.uuidToNid(erv.getStatusUuid()), 
				Bdb.uuidToNid(erv.getPathUuid()), erv.getTime());
		this.characteristicNid = Bdb.uuidToNid(erv.getCharacteristicUuid());
		this.group = erv.getGroup();
		this.refinabilityNid = Bdb.uuidToNid(erv.getRefinabilityUuid());
		this.typeNid = Bdb.uuidToNid(erv.getTypeUuid());
		this.statusAtPositionNid = Bdb.getStatusAtPositionNid(erv);
	}

	@Override
	public void writeFieldsToBdb(TupleOutput output) {
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
	public RelationshipVersion duplicate() {
		return new RelationshipVersion(this);
	}
	
	@Override
	public RelationshipVersion makeAnalog(int statusNid, int pathNid, long time) {
		return new RelationshipVersion(this, statusNid, pathNid, time);
	}

	@Override
	public ArrayIntList getVariableVersionNids() {
		ArrayIntList nids = new ArrayIntList(5);
		nids.add(characteristicNid);
		nids.add(refinabilityNid);
		nids.add(typeNid);
		return nids;
	}

	@Override
	public int getC1Id() {
		return relationship.getC1Id();
	}

	@Override
	public int getC2Id() {
		return relationship.getC2Id();
	}

	@Override
	public I_RelPart getMutablePart() {
		return this;
	}

	@Override
	public int getRelId() {
		return relationship.nid;
	}

	@Override
	public Relationship getRelVersioned() {
		return relationship;
	}
	
	
}
