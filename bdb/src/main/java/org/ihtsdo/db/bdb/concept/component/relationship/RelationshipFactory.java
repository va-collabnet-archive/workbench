package org.ihtsdo.db.bdb.concept.component.relationship;

import java.util.UUID;

import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ComponentFactory;

import com.sleepycat.bind.tuple.TupleInput;

public class RelationshipFactory extends ComponentFactory<RelationshipVersion, Relationship> {

	@Override
	public Relationship create(int nid, int partCount, Concept enclosingConcept, 
			TupleInput input, 
			UUID primordialUuid) {
		return new Relationship(nid, partCount, enclosingConcept, 
				primordialUuid);
	}

}
