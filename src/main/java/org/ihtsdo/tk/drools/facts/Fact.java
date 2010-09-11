package org.ihtsdo.tk.drools.facts;


public class Fact <T extends Object> {
	Context context;
	T component;
	
	protected Fact(Context context, T component) {
		super();
		this.context = context;
		this.component = component;
	}
	
	public Context getContext() {
		return context;
	}

	public T getComponent() {
		return component;
	}

	@Override
	public String toString() {
		return "Fact context: " + context + " object: " + component;
	}

}
