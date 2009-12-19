package org.ihtsdo.db.bdb.concept.component.attributes;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class ConceptAttributesBinder {
	
	private static final ThreadLocal<ConceptComponentBinder<ConceptAttributes, ConceptAttributesMutablePart>> binders = 
			new ThreadLocal<ConceptComponentBinder<ConceptAttributes, ConceptAttributesMutablePart>>() {
		
		@Override
		protected ConceptComponentBinder<ConceptAttributes, ConceptAttributesMutablePart> initialValue() {
			ConceptAttributesFactory factory = new ConceptAttributesFactory();
			return new ConceptComponentBinder<ConceptAttributes, ConceptAttributesMutablePart>(
					factory);
		}
	};

	public static ConceptComponentBinder<ConceptAttributes, ConceptAttributesMutablePart> getBinder() {
		return binders.get();
	}

}
