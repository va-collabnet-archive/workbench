package org.ihtsdo.tk.api;

import java.beans.PropertyVetoException;

public interface TypedComponentAnalogBI extends AnalogBI {

	public void setTypeNid(int typeNid) throws PropertyVetoException;

}
