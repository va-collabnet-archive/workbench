package org.ihtsdo.db.bdb.concept.component.image;

import org.ihtsdo.db.bdb.concept.component.ComponentFactory;

public class ImageFactory extends ComponentFactory<Image, ImageVersion> {

	@Override
	public Image create(int nid, int partCount, boolean editable) {
		return new Image(nid, partCount, editable);
	}

}