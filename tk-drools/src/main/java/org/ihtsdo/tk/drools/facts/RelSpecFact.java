package org.ihtsdo.tk.drools.facts;

import org.ihtsdo.tk.spec.RelationshipSpec;

public class RelSpecFact  extends SpecFact<RelationshipSpec>  {

	public RelSpecFact(Context context, RelationshipSpec component) {
		super(context, component);
	}

	public RelationshipSpec getRelSpec() {
		return component;
	}

}