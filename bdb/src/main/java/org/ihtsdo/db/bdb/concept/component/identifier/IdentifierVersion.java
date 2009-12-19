package org.ihtsdo.db.bdb.concept.component.identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdTuple;
import org.dwfa.ace.api.I_IdVersioned;
import org.ihtsdo.db.bdb.concept.component.Version;
import org.ihtsdo.db.bdb.concept.component.identifier.Identifier.VARIABLE_PART_TYPES;

public class IdentifierVersion 
	extends Version<IdentifierVariablePart, Identifier> 
	implements I_IdTuple {

	protected IdentifierVersion(Identifier component,
			IdentifierVariablePart version) {
		super(component, version);
	}

	@Override
	public I_IdVersioned<IdentifierVariablePart, IdentifierVersion> getIdVersioned() {
		return component;
	}

	@Override
	public int getNativeId() {
		return component.nid;
	}

	@Override
	public int getSource() {
		return version.getSource();
	}

	@Override
	public Object getSourceId() {
		return version.getSourceId();
	}

	@Override
	public List<UUID> getUIDs() {
		List<UUID> returnValues = new ArrayList<UUID>();
		for (IdentifierVariablePart p: component.variableParts) {
			if (p.getType() == VARIABLE_PART_TYPES.UUID) {
				returnValues.add((UUID) p.getSourceId());
			}
		}
		return returnValues;
	}

	@Override
	public boolean hasVersion(I_IdPart newPart) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setNativeId(int nativeId) {
		// TODO Auto-generated method stub
		
	}

}
