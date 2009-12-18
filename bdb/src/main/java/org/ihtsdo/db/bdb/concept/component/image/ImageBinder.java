package org.ihtsdo.db.bdb.concept.component.image;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class ImageBinder {

	private static final ThreadLocal<ConceptComponentBinder<Image, ImageVariablePart>> binders = new ThreadLocal<ConceptComponentBinder<Image, ImageVariablePart>>() {

		@Override
		protected ConceptComponentBinder<Image, ImageVariablePart> initialValue() {
			ImageFactory factory = new ImageFactory();
			return new ConceptComponentBinder<Image, ImageVariablePart>(factory);
		}
	};

	public static ConceptComponentBinder<Image, ImageVariablePart> getBinder() {
		return binders.get();
	}

}
