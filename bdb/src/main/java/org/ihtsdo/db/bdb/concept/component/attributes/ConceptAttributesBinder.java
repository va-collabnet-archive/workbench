package org.ihtsdo.db.bdb.concept.component.attributes;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class ConceptAttributesBinder {
	
	private static final ThreadLocal<ConceptComponentBinder<ConceptAttributes, ConceptAttributesVariablePart>> binders = 
			new ThreadLocal<ConceptComponentBinder<ConceptAttributes, ConceptAttributesVariablePart>>() {
		
		@Override
		protected ConceptComponentBinder<ConceptAttributes, ConceptAttributesVariablePart> initialValue() {
			ConceptAttributesFactory factory = new ConceptAttributesFactory();
			return new ConceptComponentBinder<ConceptAttributes, ConceptAttributesVariablePart>(
					factory);
		}
	};

	public static ConceptComponentBinder<ConceptAttributes, ConceptAttributesVariablePart> getBinder() {
		return binders.get();
	}

}
