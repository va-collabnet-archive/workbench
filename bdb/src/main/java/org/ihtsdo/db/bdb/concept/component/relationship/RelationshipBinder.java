package org.ihtsdo.db.bdb.concept.component.relationship;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class RelationshipBinder {

	private static final ThreadLocal<ConceptComponentBinder<Relationship, RelationshipVersion>> binders = 
			new ThreadLocal<ConceptComponentBinder<Relationship, RelationshipVersion>>() {
		
		@Override
		protected ConceptComponentBinder<Relationship, RelationshipVersion> initialValue() {
			RelationshipFactory factory = new RelationshipFactory();
			return new ConceptComponentBinder<Relationship, RelationshipVersion>(
					factory);
		}
	};

	public static ConceptComponentBinder<Relationship, RelationshipVersion> getBinder() {
		return binders.get();
	}

}
