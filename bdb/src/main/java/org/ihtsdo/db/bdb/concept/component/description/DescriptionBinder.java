package org.ihtsdo.db.bdb.concept.component.description;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class DescriptionBinder {

		private static final ThreadLocal<ConceptComponentBinder<Description, DescriptionMutablePart>> binders = 
				new ThreadLocal<ConceptComponentBinder<Description, DescriptionMutablePart>>() {
			
			@Override
			protected ConceptComponentBinder<Description, DescriptionMutablePart> initialValue() {
				DescriptionFactory factory = new DescriptionFactory();
				return new ConceptComponentBinder<Description, DescriptionMutablePart>(
						factory);
			}
		};

		public static ConceptComponentBinder<Description, DescriptionMutablePart> getBinder() {
			return binders.get();
		}

}
