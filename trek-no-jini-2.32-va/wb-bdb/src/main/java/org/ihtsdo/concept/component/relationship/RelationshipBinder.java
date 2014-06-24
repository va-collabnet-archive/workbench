package org.ihtsdo.concept.component.relationship;

import java.util.concurrent.atomic.AtomicInteger;

import org.ihtsdo.concept.component.ConceptComponentBinder;

public class RelationshipBinder extends ConceptComponentBinder<RelationshipRevision, Relationship> {

	public static AtomicInteger encountered = new AtomicInteger();
	public static AtomicInteger written = new AtomicInteger();

	public RelationshipBinder() {
		super(new RelationshipFactory(), encountered, written);
	}

}
