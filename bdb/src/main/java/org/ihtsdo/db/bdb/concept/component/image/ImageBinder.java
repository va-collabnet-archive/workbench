package org.ihtsdo.db.bdb.concept.component.image;

import java.util.concurrent.atomic.AtomicInteger;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class ImageBinder extends ConceptComponentBinder<ImageVersion, Image> {

	public static AtomicInteger encountered = new AtomicInteger();
	public static AtomicInteger written = new AtomicInteger();

	public ImageBinder() {
		super(new ImageFactory(), encountered, written);
	}
}
