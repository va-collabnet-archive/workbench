package org.ihtsdo.concept;

import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;
import org.ihtsdo.tk.contradiction.ComponentType;

public class ConceptAttributeComparer extends AttributeComparer {

	private boolean lcaDefinedValue = false;
	private int lcaStatusNid = 0;

	public ConceptAttributeComparer() {
		super();
		componentType = ComponentType.ATTRIBUTE;
	}

	@Override
	boolean hasSameAttributes(ComponentVersionBI v) {
		ConceptAttributeVersionBI conAttrVersion = (ConceptAttributeVersionBI)v;

		if ((conAttrVersion.isDefined() != lcaDefinedValue) ||
			(conAttrVersion.getStatusNid() != lcaStatusNid))
			return false;
		
		return true;
	}

	@Override
	public void initializeAttributes(ComponentVersionBI v) {
		ConceptAttributeVersionBI conAttrVersion = (ConceptAttributeVersionBI)v;
		comparerInitialized = true;
		
		lcaDefinedValue = conAttrVersion.isDefined();
		lcaStatusNid = conAttrVersion.getStatusNid();
	}

}
