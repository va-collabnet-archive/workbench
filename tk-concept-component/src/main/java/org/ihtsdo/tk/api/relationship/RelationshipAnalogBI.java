package org.ihtsdo.tk.api.relationship;

import java.beans.PropertyVetoException;

import org.ihtsdo.tk.api.TypedComponentAnalogBI;

public interface RelationshipAnalogBI<A extends RelationshipAnalogBI>
        extends TypedComponentAnalogBI, RelationshipVersionBI<A> {


	public void setTargetNid(int targetNid) throws PropertyVetoException;
	public void setRefinabilityNid(int refinabilityNid) throws PropertyVetoException;
	public void setCharacteristicNid(int characteristicNid) throws PropertyVetoException;
	public void setGroup(int group) throws PropertyVetoException;

}
