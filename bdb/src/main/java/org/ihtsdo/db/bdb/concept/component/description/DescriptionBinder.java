package org.ihtsdo.db.bdb.concept.component.description;

import java.util.concurrent.atomic.AtomicInteger;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class DescriptionBinder {
	
		public static AtomicInteger encountered = new AtomicInteger();
		public static AtomicInteger written = new AtomicInteger();

		private static final ThreadLocal<ConceptComponentBinder<DescriptionVersion, Description>> binders = 
				new ThreadLocal<ConceptComponentBinder<DescriptionVersion, Description>>() {
			
			@Override
			protected ConceptComponentBinder<DescriptionVersion, Description> initialValue() {
				DescriptionFactory factory = new DescriptionFactory();
				return new ConceptComponentBinder<DescriptionVersion, Description>(
						factory, encountered, written);
			}
		};

		public static ConceptComponentBinder<DescriptionVersion, Description> getBinder() {
			return binders.get();
		}

}
