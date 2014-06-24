package org.ihtsdo.tk.drools.facts;

import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.relationship.group.RelationshipGroupVersionBI;

public class RelGroupFact extends ComponentFact<RelationshipGroupVersionBI> {

	public RelGroupFact(Context context, RelationshipGroupVersionBI component, ViewCoordinate vc) {
		super(context, component, vc);
	}
	
	public RelationshipGroupVersionBI getRelGroup() {
		return component;
	}

}
