package org.ihtsdo.tk.drools.facts;

import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

public class ConAttrFact extends ComponentFact<ConceptAttributeVersionBI>{

	public ConAttrFact(Context context, ConceptAttributeVersionBI component, ViewCoordinate vc) {
		super(context, component, vc);
	}

	public ConceptAttributeVersionBI getConAttr() {
		return component;
	}

}
