package org.ihtsdo.tk.drools.facts;

import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

public class ConAttrFact extends ComponentFact<ConAttrVersionBI>{

	public ConAttrFact(Context context, ConAttrVersionBI component, ViewCoordinate vc) {
		super(context, component, vc);
	}

	public ConAttrVersionBI getConAttr() {
		return component;
	}

}
