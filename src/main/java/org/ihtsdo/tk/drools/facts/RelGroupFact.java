package org.ihtsdo.tk.drools.facts;

import org.ihtsdo.tk.api.relationship.group.RelGroupVersionBI;

public class RelGroupFact extends ComponentFact<RelGroupVersionBI> {

	public RelGroupFact(Context context, RelGroupVersionBI component) {
		super(context, component);
	}
	
	public RelGroupVersionBI getRelGroup() {
		return component;
	}

}
