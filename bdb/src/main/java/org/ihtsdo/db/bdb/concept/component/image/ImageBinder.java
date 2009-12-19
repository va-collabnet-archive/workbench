package org.ihtsdo.db.bdb.concept.component.image;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class ImageBinder {

	private static final ThreadLocal<ConceptComponentBinder<Image, ImageMutablePart>> binders = new ThreadLocal<ConceptComponentBinder<Image, ImageMutablePart>>() {

		@Override
		protected ConceptComponentBinder<Image, ImageMutablePart> initialValue() {
			ImageFactory factory = new ImageFactory();
			return new ConceptComponentBinder<Image, ImageMutablePart>(factory);
		}
	};

	public static ConceptComponentBinder<Image, ImageMutablePart> getBinder() {
		return binders.get();
	}

}
