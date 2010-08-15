package org.ihtsdo.tk.api.conattr;

import java.beans.PropertyVetoException;

import org.ihtsdo.tk.api.AnalogBI;

public interface ConAttrAnalogBI extends AnalogBI, ConAttrVersionBI {
	
    public void setDefined(boolean defined) throws PropertyVetoException;

}
