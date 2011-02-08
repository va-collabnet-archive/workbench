package org.ihtsdo.tk.api.refex;

import org.ihtsdo.tk.api.ComponentChroncileBI;

public interface RefexChronicleBI<A extends RefexAnalogBI<A>> extends 
        ComponentChroncileBI<RefexVersionBI<A>> {
	
	int getCollectionNid();
        int getReferencedComponentNid();
}
