package org.ihtsdo.db.bdb.concept.component.description;

import java.util.concurrent.atomic.AtomicInteger;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class DescriptionBinder extends ConceptComponentBinder<DescriptionRevision, Description> {
	
		public static AtomicInteger encountered = new AtomicInteger();
		public static AtomicInteger written = new AtomicInteger();

		public DescriptionBinder() {
			super(new DescriptionFactory(), encountered, written);
		}

}
