package org.ihtsdo.tk.drools.facts;

import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

public class ComponentFact <T extends ComponentVersionBI> extends Fact<T>{
	
	private ViewCoordinate vc;
	
	protected ComponentFact(Context context, T component, ViewCoordinate vc) {
		super(context, component);
		this.vc = vc;
	}
	
	public ViewCoordinate getVc() {
		return vc;
	}

	@Override
	public String toString() {
		return "Fact context: " + context + " component: " + component;
	}

}
