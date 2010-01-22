package org.ihtsdo.db.bdb.concept.component.relationship;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.component.Revision;
import org.ihtsdo.etypes.ERelationshipVersion;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class RelationshipRevision 
	extends Revision<RelationshipRevision, Relationship> 
	implements I_RelPart, I_RelTuple {
	
	private transient Relationship relationship;
	
	private int characteristicNid;
	private int group;
	private int refinabilityNid;
	private int typeNid;

	
	public String toString() {
		return " characteristicNid: " + characteristicNid + " group: " + group + 
		" refinabilityNid: " + refinabilityNid + " typeNid: " + typeNid+ " " +super.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (RelationshipRevision.class.isAssignableFrom(obj.getClass())) {
			RelationshipRevision another = (RelationshipRevision) obj;
			if (this.characteristicNid != another.characteristicNid) {
				return false;
			}
			if (this.group != another.group) {
				return false;
			}
			if (this.refinabilityNid != another.refinabilityNid) {
				return false;
			}
			if (this.typeNid != another.typeNid) {
				return false;
			}
			return super.equals(obj);
		}
		return false;
	}
	public RelationshipRevision(int statusAtPositionNid, 
			Relationship primordialRel) {
		super(statusAtPositionNid, primordialRel);
	}

	public RelationshipRevision(RelationshipRevision another, 
			Relationship primordialRel) {
		super(another.statusAtPositionNid, primordialRel);
		this.characteristicNid = another.characteristicNid;
		this.group = another.group;
		this.refinabilityNid = another.refinabilityNid;
		this.typeNid = another.typeNid;
	}

	public RelationshipRevision(I_RelPart another, int statusNid,
			int pathNid, long time, 
			Relationship primordialRel) {
		super(statusNid, pathNid, time, primordialRel);
		this.characteristicNid = another.getCharacteristicId();
		this.group = another.getGroup();
		this.refinabilityNid = another.getRefinabilityId();
		this.typeNid = another.getTypeId();
	}

	public RelationshipRevision(TupleInput input, 
			Relationship primordialRel) {
		super(input.readInt(), primordialRel);
		this.characteristicNid = input.readInt();
		this.group = input.readInt();
		this.refinabilityNid = input.readInt();
		this.typeNid = input.readInt();
	}

	public RelationshipRevision(ERelationshipVersion erv, 
			Relationship primordialRel) {
		super(Bdb.uuidToNid(erv.getStatusUuid()), 
				Bdb.uuidToNid(erv.getPathUuid()), erv.getTime(), 
				primordialRel);
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
	public RelationshipRevision duplicate() {
		return new RelationshipRevision(this, primordialComponent);
	}
	
	@Override
	public RelationshipRevision makeAnalog(int statusNid, int pathNid, long time) {
		return new RelationshipRevision(this, statusNid, pathNid, time,
				primordialComponent);
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
