package org.ihtsdo.tk.api;

import java.util.Set;



public interface PositionSetBI extends Set<PositionBI> {
    
    public NidSetBI getViewPathNidSet();
    public PositionBI[] getPositionArray();
	
}
