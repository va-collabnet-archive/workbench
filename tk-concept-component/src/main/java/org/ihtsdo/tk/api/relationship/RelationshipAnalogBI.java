package org.ihtsdo.tk.api.relationship;

import java.beans.PropertyVetoException;

import org.ihtsdo.tk.api.TypedComponentAnalogBI;

public interface RelationshipAnalogBI<A extends RelationshipAnalogBI>
        extends TypedComponentAnalogBI, RelationshipVersionBI<A> {


	public void setDestinationNid(int nid) throws PropertyVetoException;
	public void setRefinabilityNid(int nid) throws PropertyVetoException;
	public void setCharacteristicNid(int nid) throws PropertyVetoException;
	public void setGroup(int group) throws PropertyVetoException;

}
