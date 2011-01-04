package org.ihtsdo.tk.api.media;

import java.beans.PropertyVetoException;

import org.ihtsdo.tk.api.TypedComponentAnalogBI;

public interface MediaAnalogBI<A extends MediaAnalogBI>
        extends TypedComponentAnalogBI, MediaVersionBI<A> {
	
    public void setTextDescription(String desc) throws PropertyVetoException;
    
}
