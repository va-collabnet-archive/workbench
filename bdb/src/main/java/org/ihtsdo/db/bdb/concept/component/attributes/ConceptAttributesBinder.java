package org.ihtsdo.db.bdb.concept.component.attributes;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class ConceptAttributesBinder {
	
	private static final ThreadLocal<ConceptComponentBinder<ConceptAttributes, ConceptAttributesPart>> binders = 
			new ThreadLocal<ConceptComponentBinder<ConceptAttributes, ConceptAttributesPart>>() {
		
		@Override
		protected ConceptComponentBinder<ConceptAttributes, ConceptAttributesPart> initialValue() {
			ConceptAttributesFactory factory = new ConceptAttributesFactory();
			return new ConceptComponentBinder<ConceptAttributes, ConceptAttributesPart>(
					factory);
		}
	};

	public static ConceptComponentBinder<ConceptAttributes, ConceptAttributesPart> getBinder() {
		return binders.get();
	}

}
