package org.ihtsdo.tk.drools.facts;

import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

public class RelFact extends ComponentFact<RelationshipVersionBI> {

	public RelFact(Context context, RelationshipVersionBI component) {
		super(context, component);
	}
	
	public RelationshipVersionBI getRel() {
		return component;
	}

}
