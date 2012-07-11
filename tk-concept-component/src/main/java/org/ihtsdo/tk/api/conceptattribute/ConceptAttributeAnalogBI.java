package org.ihtsdo.tk.api.conceptattribute;

import java.beans.PropertyVetoException;

import org.ihtsdo.tk.api.AnalogBI;

public interface ConceptAttributeAnalogBI<A extends ConceptAttributeAnalogBI>
        extends AnalogBI, ConceptAttributeVersionBI<A> {
	
    public void setDefined(boolean defined) throws PropertyVetoException;

}
