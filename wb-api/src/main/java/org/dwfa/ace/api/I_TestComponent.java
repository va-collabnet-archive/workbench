package org.dwfa.ace.api;

import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.Precedence;

public interface I_TestComponent {
	
	public boolean result(I_AmTermComponent component, I_Position viewPosition, PathSetReadOnly pomotionPaths, 
            NidSetBI allowedStatus, Precedence precedence);

}
