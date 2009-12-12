package org.ihtsdo.db.bdb.concept.component.image;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class ImageBinder {

	private static final ThreadLocal<ConceptComponentBinder<Image, ImagePart>> binders = new ThreadLocal<ConceptComponentBinder<Image, ImagePart>>() {

		@Override
		protected ConceptComponentBinder<Image, ImagePart> initialValue() {
			ImageFactory factory = new ImageFactory();
			return new ConceptComponentBinder<Image, ImagePart>(factory);
		}
	};

	public static ConceptComponentBinder<Image, ImagePart> getBinder() {
		return binders.get();
	}

}
