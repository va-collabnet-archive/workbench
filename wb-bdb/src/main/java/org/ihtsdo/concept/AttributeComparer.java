package org.ihtsdo.concept;

import org.ihtsdo.concept.ContradictionIdentifier.CONTRADICTION_INVESTIGATION_TYPE;
import org.ihtsdo.tk.api.ComponentVersionBI;

public abstract class AttributeComparer {
	protected boolean comparerInitialized = false;
	protected CONTRADICTION_INVESTIGATION_TYPE componentType = null;;
	
	protected CONTRADICTION_INVESTIGATION_TYPE getComponentType() {
		return componentType;
	}
	
	public boolean isInitialized() {
		return comparerInitialized;
	}
	
	public void clear() { 
		comparerInitialized = false; 
	}

	abstract public void initializeAttributes(ComponentVersionBI v);
	abstract boolean hasSameAttributes(ComponentVersionBI v);
}

