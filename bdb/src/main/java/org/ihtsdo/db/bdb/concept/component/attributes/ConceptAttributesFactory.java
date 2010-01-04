package org.ihtsdo.db.bdb.concept.component.attributes;

import java.util.UUID;

import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ComponentFactory;

import com.sleepycat.bind.tuple.TupleInput;

public class ConceptAttributesFactory extends ComponentFactory<ConceptAttributesVersion, ConceptAttributes> {

	@Override
	public ConceptAttributes create(int nid, int partCount, Concept enclosingConcept, 
			TupleInput input, 
			UUID primordialUuid) {
		return new ConceptAttributes(nid, partCount, enclosingConcept, primordialUuid);
	}

}
