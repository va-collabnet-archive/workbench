package org.ihtsdo.db.bdb.concept.component.image;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class ImageBinder {

	private static final ThreadLocal<ConceptComponentBinder<ImageVersion, Image>> binders = 
		new ThreadLocal<ConceptComponentBinder<ImageVersion, Image>>() {

		@Override
		protected ConceptComponentBinder<ImageVersion, Image> initialValue() {
			ImageFactory factory = new ImageFactory();
			return new ConceptComponentBinder<ImageVersion, Image>(factory);
		}
	};

	public static ConceptComponentBinder<ImageVersion, Image> getBinder() {
		return binders.get();
	}

}
