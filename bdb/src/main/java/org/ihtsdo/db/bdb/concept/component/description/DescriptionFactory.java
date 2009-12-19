package org.ihtsdo.db.bdb.concept.component.description;

import org.ihtsdo.db.bdb.concept.component.ComponentFactory;

public class DescriptionFactory extends ComponentFactory<Description, DescriptionMutablePart> {

	@Override
	public Description create(int nid, int partCount, boolean editable) {
		return new Description(nid, partCount, editable);
	}

}
