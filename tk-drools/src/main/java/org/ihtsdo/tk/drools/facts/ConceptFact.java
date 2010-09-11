package org.ihtsdo.tk.drools.facts;

import org.ihtsdo.tk.api.concept.ConceptVersionBI;

public class ConceptFact extends ComponentFact<ConceptVersionBI> {

	public ConceptFact(Context context, ConceptVersionBI component) {
		super(context, component);
	}

	public ConceptVersionBI getConcept() {
		return component;
	}
	
}
