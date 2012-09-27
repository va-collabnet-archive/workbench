package org.ihtsdo.tk.drools.facts;

import org.ihtsdo.tk.spec.DescriptionSpec;

public class DescSpecFact extends SpecFact<DescriptionSpec>  {

	public DescSpecFact(Context context, DescriptionSpec component) {
		super(context, component);
	}

	public DescriptionSpec getDescSpec() {
		return component;
	}

}