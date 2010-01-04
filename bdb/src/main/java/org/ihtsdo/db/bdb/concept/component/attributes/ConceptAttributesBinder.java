package org.ihtsdo.db.bdb.concept.component.attributes;

import java.util.concurrent.atomic.AtomicInteger;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class ConceptAttributesBinder {
	
	public static AtomicInteger encountered = new AtomicInteger();
	public static AtomicInteger written = new AtomicInteger();

	private static final ThreadLocal<ConceptComponentBinder<ConceptAttributesVersion, ConceptAttributes>> binders = 
			new ThreadLocal<ConceptComponentBinder<ConceptAttributesVersion, ConceptAttributes>>() {
		
		@Override
		protected ConceptComponentBinder<ConceptAttributesVersion, ConceptAttributes> initialValue() {
			ConceptAttributesFactory factory = new ConceptAttributesFactory();
			return new ConceptComponentBinder<ConceptAttributesVersion, ConceptAttributes>(
					factory, encountered, written);
		}
	};

	public static ConceptComponentBinder<ConceptAttributesVersion, ConceptAttributes> getBinder() {
		return binders.get();
	}

}
