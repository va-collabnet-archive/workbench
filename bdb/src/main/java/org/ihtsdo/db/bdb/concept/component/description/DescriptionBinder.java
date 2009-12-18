package org.ihtsdo.db.bdb.concept.component.description;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class DescriptionBinder {

		private static final ThreadLocal<ConceptComponentBinder<Description, DescriptionVariablePart>> binders = 
				new ThreadLocal<ConceptComponentBinder<Description, DescriptionVariablePart>>() {
			
			@Override
			protected ConceptComponentBinder<Description, DescriptionVariablePart> initialValue() {
				DescriptionFactory factory = new DescriptionFactory();
				return new ConceptComponentBinder<Description, DescriptionVariablePart>(
						factory);
			}
		};

		public static ConceptComponentBinder<Description, DescriptionVariablePart> getBinder() {
			return binders.get();
		}

}
