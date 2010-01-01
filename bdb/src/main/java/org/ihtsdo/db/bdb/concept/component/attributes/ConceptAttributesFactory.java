package org.ihtsdo.db.bdb.concept.component.attributes;

import org.ihtsdo.db.bdb.concept.component.ComponentFactory;

import com.sleepycat.bind.tuple.TupleInput;

public class ConceptAttributesFactory extends ComponentFactory<ConceptAttributesVersion, ConceptAttributes> {

	@Override
	public ConceptAttributes create(int nid, int partCount, boolean editable, TupleInput input) {
		return new ConceptAttributes(nid, partCount, editable);
	}

}
