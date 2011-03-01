package org.ihtsdo.tk.api.conattr;

import java.beans.PropertyVetoException;

import org.ihtsdo.tk.api.AnalogBI;

public interface ConAttrAnalogBI<A extends ConAttrAnalogBI>
        extends AnalogBI, ConAttrVersionBI<A> {
	
    public void setDefined(boolean defined) throws PropertyVetoException;

}
