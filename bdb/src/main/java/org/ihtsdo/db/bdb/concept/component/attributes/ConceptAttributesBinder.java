package org.ihtsdo.db.bdb.concept.component.attributes;

import java.util.concurrent.atomic.AtomicInteger;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class ConceptAttributesBinder extends ConceptComponentBinder<ConceptAttributesVersion, ConceptAttributes> {
	

	public static AtomicInteger encountered = new AtomicInteger();
	public static AtomicInteger written = new AtomicInteger();

	public ConceptAttributesBinder() {
		super(new ConceptAttributesFactory(), encountered, written);
	}

}
