package org.ihtsdo.concept.component.image;

import java.util.concurrent.atomic.AtomicInteger;

import org.ihtsdo.concept.component.ConceptComponentBinder;

public class ImageBinder extends ConceptComponentBinder<ImageRevision, Image> {

	public static AtomicInteger encountered = new AtomicInteger();
	public static AtomicInteger written = new AtomicInteger();

	public ImageBinder() {
		super(new ImageFactory(), encountered, written);
	}
}
