package org.ihtsdo.concept;

import org.ihtsdo.concept.ConflictIdentifier.CONTRADICTION_INVESTIGATION_TYPE;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;

public class ConceptAttributeComparer extends AttributeComparer {

	private boolean lcaDefinedValue = false;
	private int lcaStatusNid = 0;

	public ConceptAttributeComparer() {
		super();
		componentType = CONTRADICTION_INVESTIGATION_TYPE.ATTRIBUTE;
	}

	@Override
	boolean hasSameAttributes(ComponentVersionBI v) {
		ConAttrVersionBI conAttrVersion = (ConAttrVersionBI)v;

		if ((conAttrVersion.isDefined() != lcaDefinedValue) ||
			(conAttrVersion.getStatusNid() != lcaStatusNid))
			return false;
		
		return true;
	}

	@Override
	public void initializeAttributes(ComponentVersionBI v) {
		ConAttrVersionBI conAttrVersion = (ConAttrVersionBI)v;
		comparerInitialized = true;
		
		lcaDefinedValue = conAttrVersion.isDefined();
		lcaStatusNid = conAttrVersion.getStatusNid();
	}

}
