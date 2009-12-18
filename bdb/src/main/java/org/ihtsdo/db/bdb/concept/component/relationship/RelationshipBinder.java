package org.ihtsdo.db.bdb.concept.component.relationship;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class RelationshipBinder {

	private static final ThreadLocal<ConceptComponentBinder<Relationship, RelationshipVariablePart>> binders = 
			new ThreadLocal<ConceptComponentBinder<Relationship, RelationshipVariablePart>>() {
		
		@Override
		protected ConceptComponentBinder<Relationship, RelationshipVariablePart> initialValue() {
			RelationshipFactory factory = new RelationshipFactory();
			return new ConceptComponentBinder<Relationship, RelationshipVariablePart>(
					factory);
		}
	};

	public static ConceptComponentBinder<Relationship, RelationshipVariablePart> getBinder() {
		return binders.get();
	}

}
