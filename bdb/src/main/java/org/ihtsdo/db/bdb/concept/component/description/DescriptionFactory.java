package org.ihtsdo.db.bdb.concept.component.description;

import java.util.UUID;

import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ComponentFactory;

import com.sleepycat.bind.tuple.TupleInput;

public class DescriptionFactory extends ComponentFactory<DescriptionVersion, Description> {

	@Override
	public Description create(int nid, int partCount, Concept enclosingConcept, 
			TupleInput input, 
			UUID primordialUuid) {
		return new Description(nid, partCount, enclosingConcept, 
				primordialUuid);
	}

}
