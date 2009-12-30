package org.ihtsdo.db.bdb.concept.component.description;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class DescriptionBinder {

		private static final ThreadLocal<ConceptComponentBinder<Description, DescriptionVersion>> binders = 
				new ThreadLocal<ConceptComponentBinder<Description, DescriptionVersion>>() {
			
			@Override
			protected ConceptComponentBinder<Description, DescriptionVersion> initialValue() {
				DescriptionFactory factory = new DescriptionFactory();
				return new ConceptComponentBinder<Description, DescriptionVersion>(
						factory);
			}
		};

		public static ConceptComponentBinder<Description, DescriptionVersion> getBinder() {
			return binders.get();
		}

}
