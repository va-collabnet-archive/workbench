package org.ihtsdo.db.bdb.concept.component.image;

import java.util.concurrent.atomic.AtomicInteger;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class ImageBinder {

	public static AtomicInteger encountered = new AtomicInteger();
	public static AtomicInteger written = new AtomicInteger();

	private static final ThreadLocal<ConceptComponentBinder<ImageVersion, Image>> binders = 
		new ThreadLocal<ConceptComponentBinder<ImageVersion, Image>>() {


		@Override
		protected ConceptComponentBinder<ImageVersion, Image> initialValue() {
			ImageFactory factory = new ImageFactory();
			return new ConceptComponentBinder<ImageVersion, Image>(factory, encountered, written);
		}
	};

	public static ConceptComponentBinder<ImageVersion, Image> getBinder() {
		return binders.get();
	}

}
