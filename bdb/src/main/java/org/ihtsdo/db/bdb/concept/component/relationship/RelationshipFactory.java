package org.ihtsdo.db.bdb.concept.component.relationship;

import org.ihtsdo.db.bdb.concept.component.ComponentFactory;

import com.sleepycat.bind.tuple.TupleInput;

public class RelationshipFactory extends ComponentFactory<Relationship, RelationshipVersion> {

	@Override
	public Relationship create(int nid, int partCount, boolean editable, TupleInput input) {
		return new Relationship(nid, partCount, editable);
	}

}
