package org.ihtsdo.db.bdb.concept.component.description;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class DescriptionBinder {

		private static final ThreadLocal<ConceptComponentBinder<DescriptionVersion, Description>> binders = 
				new ThreadLocal<ConceptComponentBinder<DescriptionVersion, Description>>() {
			
			@Override
			protected ConceptComponentBinder<DescriptionVersion, Description> initialValue() {
				DescriptionFactory factory = new DescriptionFactory();
				return new ConceptComponentBinder<DescriptionVersion, Description>(
						factory);
			}
		};

		public static ConceptComponentBinder<DescriptionVersion, Description> getBinder() {
			return binders.get();
		}

}
