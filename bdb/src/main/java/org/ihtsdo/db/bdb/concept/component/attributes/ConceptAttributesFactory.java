package org.ihtsdo.db.bdb.concept.component.attributes;

import org.ihtsdo.db.bdb.concept.component.ComponentFactory;

public class ConceptAttributesFactory extends ComponentFactory<ConceptAttributes, ConceptAttributesVariablePart> {

	@Override
	public ConceptAttributes create(int nid, int partCount, boolean editable) {
		return new ConceptAttributes(nid, partCount, editable);
	}

}
