package org.ihtsdo.db.bdb.concept.component.image;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class ImageBinder {

	private static final ThreadLocal<ConceptComponentBinder<Image, ImageVersion>> binders = new ThreadLocal<ConceptComponentBinder<Image, ImageVersion>>() {

		@Override
		protected ConceptComponentBinder<Image, ImageVersion> initialValue() {
			ImageFactory factory = new ImageFactory();
			return new ConceptComponentBinder<Image, ImageVersion>(factory);
		}
	};

	public static ConceptComponentBinder<Image, ImageVersion> getBinder() {
		return binders.get();
	}

}
