package org.dwfa.ace.api;

public interface I_TestComponent {
	
	public boolean result(I_AmTermComponent component, I_Position viewPosition, PathSetReadOnly pomotionPaths, 
            I_IntSet allowedStatus, PRECEDENCE precedence);

}
