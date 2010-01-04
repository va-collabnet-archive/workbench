package org.ihtsdo.db.bdb.concept.component.relationship;

import java.util.concurrent.atomic.AtomicInteger;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class RelationshipBinder {

	public static AtomicInteger encountered = new AtomicInteger();
	public static AtomicInteger written = new AtomicInteger();

	private static final ThreadLocal<ConceptComponentBinder<RelationshipVersion, Relationship>> binders = 
			new ThreadLocal<ConceptComponentBinder<RelationshipVersion, Relationship>>() {
		
		@Override
		protected ConceptComponentBinder<RelationshipVersion, Relationship> initialValue() {
			RelationshipFactory factory = new RelationshipFactory();
			return new ConceptComponentBinder<RelationshipVersion, Relationship>(
					factory, encountered, written);
		}
	};

	public static ConceptComponentBinder<RelationshipVersion, Relationship> getBinder() {
		return binders.get();
	}

}
