package org.ihtsdo.db.bdb.concept.component;

import org.ihtsdo.db.bdb.concept.Concept;

import com.sleepycat.bind.tuple.TupleInput;

public abstract class ComponentFactory<V extends Revision<V, C>, C extends ConceptComponent<V, C>> {
	
	public abstract C create(Concept enclosingConcept, TupleInput input);

}
