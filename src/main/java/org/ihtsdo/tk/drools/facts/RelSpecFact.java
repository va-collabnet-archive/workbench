package org.ihtsdo.tk.drools.facts;

import org.ihtsdo.tk.spec.RelSpec;

public class RelSpecFact  extends SpecFact<RelSpec>  {

	public RelSpecFact(Context context, RelSpec component) {
		super(context, component);
	}

	public RelSpec getRelSpec() {
		return component;
	}

}