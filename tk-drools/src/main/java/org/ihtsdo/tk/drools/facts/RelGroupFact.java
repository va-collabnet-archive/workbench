package org.ihtsdo.tk.drools.facts;

import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.relationship.group.RelGroupVersionBI;

public class RelGroupFact extends ComponentFact<RelGroupVersionBI> {

	public RelGroupFact(Context context, RelGroupVersionBI component, ViewCoordinate vc) {
		super(context, component, vc);
	}
	
	public RelGroupVersionBI getRelGroup() {
		return component;
	}

}
