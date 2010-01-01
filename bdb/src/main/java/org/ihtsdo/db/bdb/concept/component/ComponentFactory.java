package org.ihtsdo.db.bdb.concept.component;

import com.sleepycat.bind.tuple.TupleInput;

public abstract class ComponentFactory<V extends Version<V, C>, C extends ConceptComponent<V, C>> {
	
	public abstract C create(int nid, int partCount, boolean editable, TupleInput input);

}
