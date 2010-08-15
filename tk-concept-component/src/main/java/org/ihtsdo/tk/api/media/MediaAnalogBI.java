package org.ihtsdo.tk.api.media;

import java.beans.PropertyVetoException;

import org.ihtsdo.tk.api.TypedComponentAnalogBI;

public interface MediaAnalogBI extends TypedComponentAnalogBI, MediaVersionBI {
	
    public void setTextDescription(String desc) throws PropertyVetoException;
    
}
