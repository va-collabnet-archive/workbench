package org.ihtsdo.tk.drools.facts;

import org.ihtsdo.tk.spec.SpecBI;

public class SpecFact <T extends SpecBI> extends Fact<T>{

	protected SpecFact(Context context, T component) {
		super(context, component);
	}

}
