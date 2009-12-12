package org.ihtsdo.db.bdb.concept.component.description;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class DescriptionBinder {

		private static final ThreadLocal<ConceptComponentBinder<Description, DescriptionPart>> binders = 
				new ThreadLocal<ConceptComponentBinder<Description, DescriptionPart>>() {
			
			@Override
			protected ConceptComponentBinder<Description, DescriptionPart> initialValue() {
				DescriptionFactory factory = new DescriptionFactory();
				return new ConceptComponentBinder<Description, DescriptionPart>(
						factory);
			}
		};

		public static ConceptComponentBinder<Description, DescriptionPart> getBinder() {
			return binders.get();
		}

}
