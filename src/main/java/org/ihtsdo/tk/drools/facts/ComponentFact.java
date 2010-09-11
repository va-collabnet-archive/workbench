package org.ihtsdo.tk.drools.facts;

import org.ihtsdo.tk.api.ComponentVersionBI;

public class ComponentFact <T extends ComponentVersionBI> extends Fact<T>{
	
	protected ComponentFact(Context context, T component) {
		super(context, component);
	}
	
	@Override
	public String toString() {
		return "Fact context: " + context + " component: " + component;
	}

}
