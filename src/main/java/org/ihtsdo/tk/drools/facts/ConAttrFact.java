package org.ihtsdo.tk.drools.facts;

import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;

public class ConAttrFact extends ComponentFact<ConAttrVersionBI>{

	public ConAttrFact(Context context, ConAttrVersionBI component) {
		super(context, component);
	}

	public ConAttrVersionBI getConAttr() {
		return component;
	}

}
