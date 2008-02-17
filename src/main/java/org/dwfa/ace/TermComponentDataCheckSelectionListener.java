package org.dwfa.ace;

import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_GetConceptData;

public class TermComponentDataCheckSelectionListener {

	I_ContainTermComponent linkedComponent;

	public TermComponentDataCheckSelectionListener(
			I_ContainTermComponent linkedComponent) {
		super();
		this.linkedComponent = linkedComponent;
	}

	public void setSelection(I_GetConceptData concept) {
		linkedComponent.setTermComponent(concept);
	}

}
