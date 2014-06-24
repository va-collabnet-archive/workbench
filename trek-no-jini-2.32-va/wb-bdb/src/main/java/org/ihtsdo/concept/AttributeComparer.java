package org.ihtsdo.concept;

import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.contradiction.ComponentType;

public abstract class AttributeComparer {
	protected boolean comparerInitialized = false;
	protected ComponentType componentType = null;;
	
	protected ComponentType getComponentType() {
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

