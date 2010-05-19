package org.ihtsdo.concept.component.attributes;

import java.io.IOException;

import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ComponentFactory;

import com.sleepycat.bind.tuple.TupleInput;

public class ConceptAttributesFactory extends ComponentFactory<ConceptAttributesRevision, ConceptAttributes> {

	@Override
	public ConceptAttributes create(Concept enclosingConcept, 
			TupleInput input) throws IOException {
		return new ConceptAttributes(enclosingConcept, input);
	}

}
