package org.ihtsdo.concept.component.attributes;

import java.util.concurrent.atomic.AtomicInteger;

import org.ihtsdo.concept.component.ConceptComponentBinder;

public class ConceptAttributesBinder extends ConceptComponentBinder<ConceptAttributesRevision, ConceptAttributes> {
	

	public static AtomicInteger encountered = new AtomicInteger();
	public static AtomicInteger written = new AtomicInteger();

	public ConceptAttributesBinder() {
		super(new ConceptAttributesFactory(), encountered, written);
	}

}
