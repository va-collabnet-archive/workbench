package org.ihtsdo.tk.drools.facts;

import org.ihtsdo.tk.api.description.DescriptionVersionBI;

public class DescFact extends ComponentFact<DescriptionVersionBI> {

	public DescFact(Context context, DescriptionVersionBI component) {
		super(context, component);
	}

	public DescriptionVersionBI getDesc() {
		return component;
	}

}
