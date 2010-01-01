package org.ihtsdo.db.bdb.concept.component.relationship;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class RelationshipBinder {

	private static final ThreadLocal<ConceptComponentBinder<RelationshipVersion, Relationship>> binders = 
			new ThreadLocal<ConceptComponentBinder<RelationshipVersion, Relationship>>() {
		
		@Override
		protected ConceptComponentBinder<RelationshipVersion, Relationship> initialValue() {
			RelationshipFactory factory = new RelationshipFactory();
			return new ConceptComponentBinder<RelationshipVersion, Relationship>(
					factory);
		}
	};

	public static ConceptComponentBinder<RelationshipVersion, Relationship> getBinder() {
		return binders.get();
	}

}
