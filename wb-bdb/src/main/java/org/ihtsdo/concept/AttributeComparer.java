package org.ihtsdo.concept;

import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.contradiction.ContradictionInvestigationType;

public abstract class AttributeComparer {
	protected boolean comparerInitialized = false;
	protected ContradictionInvestigationType componentType = null;;
	
	protected ContradictionInvestigationType getComponentType() {
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

