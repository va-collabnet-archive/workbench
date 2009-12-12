package org.ihtsdo.db.bdb.concept.component.relationship;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class RelationshipBinder {

	private static final ThreadLocal<ConceptComponentBinder<Relationship, RelationshipPart>> binders = 
			new ThreadLocal<ConceptComponentBinder<Relationship, RelationshipPart>>() {
		
		@Override
		protected ConceptComponentBinder<Relationship, RelationshipPart> initialValue() {
			RelationshipFactory factory = new RelationshipFactory();
			return new ConceptComponentBinder<Relationship, RelationshipPart>(
					factory);
		}
	};

	public static ConceptComponentBinder<Relationship, RelationshipPart> getBinder() {
		return binders.get();
	}

}
