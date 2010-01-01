package org.ihtsdo.db.bdb.concept.component.description;

import org.ihtsdo.db.bdb.concept.component.ComponentFactory;

import com.sleepycat.bind.tuple.TupleInput;

public class DescriptionFactory extends ComponentFactory<Description, DescriptionVersion> {

	@Override
	public Description create(int nid, int partCount, boolean editable, TupleInput input) {
		return new Description(nid, partCount, editable);
	}

}
