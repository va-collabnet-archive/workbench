package org.dwfa.ace.api;

import org.ihtsdo.tk.api.Precedence;

public interface I_TestComponent {
	
	public boolean result(I_AmTermComponent component, I_Position viewPosition, PathSetReadOnly pomotionPaths, 
            I_IntSet allowedStatus, Precedence precedence);

}
