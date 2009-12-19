package org.ihtsdo.db.bdb.concept.component.relationship;

import org.ihtsdo.db.bdb.concept.component.ComponentFactory;

public class RelationshipFactory extends ComponentFactory<Relationship, RelationshipMutablePart> {

	@Override
	public Relationship create(int nid, int partCount, boolean editable) {
		return new Relationship(nid, partCount, editable);
	}

}
