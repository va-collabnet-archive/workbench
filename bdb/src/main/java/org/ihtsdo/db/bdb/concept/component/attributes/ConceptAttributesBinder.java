package org.ihtsdo.db.bdb.concept.component.attributes;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class ConceptAttributesBinder {
	
	private static final ThreadLocal<ConceptComponentBinder<ConceptAttributes, ConceptAttributesVersion>> binders = 
			new ThreadLocal<ConceptComponentBinder<ConceptAttributes, ConceptAttributesVersion>>() {
		
		@Override
		protected ConceptComponentBinder<ConceptAttributes, ConceptAttributesVersion> initialValue() {
			ConceptAttributesFactory factory = new ConceptAttributesFactory();
			return new ConceptComponentBinder<ConceptAttributes, ConceptAttributesVersion>(
					factory);
		}
	};

	public static ConceptComponentBinder<ConceptAttributes, ConceptAttributesVersion> getBinder() {
		return binders.get();
	}

}
