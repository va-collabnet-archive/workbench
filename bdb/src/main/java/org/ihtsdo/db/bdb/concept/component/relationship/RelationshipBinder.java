package org.ihtsdo.db.bdb.concept.component.relationship;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class RelationshipBinder {

	private static final ThreadLocal<ConceptComponentBinder<Relationship, RelationshipMutablePart>> binders = 
			new ThreadLocal<ConceptComponentBinder<Relationship, RelationshipMutablePart>>() {
		
		@Override
		protected ConceptComponentBinder<Relationship, RelationshipMutablePart> initialValue() {
			RelationshipFactory factory = new RelationshipFactory();
			return new ConceptComponentBinder<Relationship, RelationshipMutablePart>(
					factory);
		}
	};

	public static ConceptComponentBinder<Relationship, RelationshipMutablePart> getBinder() {
		return binders.get();
	}

}
