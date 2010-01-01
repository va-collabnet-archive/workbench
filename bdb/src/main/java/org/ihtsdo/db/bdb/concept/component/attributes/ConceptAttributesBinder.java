package org.ihtsdo.db.bdb.concept.component.attributes;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class ConceptAttributesBinder {
	
	private static final ThreadLocal<ConceptComponentBinder<ConceptAttributesVersion, ConceptAttributes>> binders = 
			new ThreadLocal<ConceptComponentBinder<ConceptAttributesVersion, ConceptAttributes>>() {
		
		@Override
		protected ConceptComponentBinder<ConceptAttributesVersion, ConceptAttributes> initialValue() {
			ConceptAttributesFactory factory = new ConceptAttributesFactory();
			return new ConceptComponentBinder<ConceptAttributesVersion, ConceptAttributes>(
					factory);
		}
	};

	public static ConceptComponentBinder<ConceptAttributesVersion, ConceptAttributes> getBinder() {
		return binders.get();
	}

}
