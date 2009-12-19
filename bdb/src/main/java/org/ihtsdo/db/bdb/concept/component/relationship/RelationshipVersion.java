package org.ihtsdo.db.bdb.concept.component.relationship;

import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_RelTuple;
import org.ihtsdo.db.bdb.concept.component.Version;

public class RelationshipVersion extends Version<RelationshipVariablePart, Relationship> 
	implements I_RelTuple {

	protected RelationshipVersion(Relationship component, RelationshipVariablePart part) {
		super(component, part);
	}

	@Override
	public int getC1Id() {
		return getFixedPart().getC1Id();
	}

	@Override
	public int getC2Id() {
		return getFixedPart().getC2Id();
	}

	@Override
	public int getCharacteristicId() {
		return getPart().getCharacteristicId();
	}

	@Override
	public int getGroup() {
		return getPart().getGroup();
	}

	@Override
	public int getRefinabilityId() {
		return getPart().getRefinabilityId();
	}

	@Override
	public int getRelId() {
		return getFixedPartId();
	}

	@Override
	public Relationship getRelVersioned() {
		return getFixedPart();
	}

	@Override
	public void setCharacteristicId(Integer characteristicId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setGroup(Integer group) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setRefinabilityId(Integer refinabilityId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setStatusId(Integer statusId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getTypeId() {
		return getPart().getTypeId();
	}

	@Override
	public void setTypeId(int type) {
		throw new UnsupportedOperationException();
	}

}
