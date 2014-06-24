package org.ihtsdo.tk.drools.facts;

import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

public class RelFact extends ComponentFact<RelationshipVersionBI> {

	public RelFact(Context context, RelationshipVersionBI component, ViewCoordinate vc) {
		super(context, component, vc);
	}
	
	public RelationshipVersionBI getRel() {
		return component;
	}

}
