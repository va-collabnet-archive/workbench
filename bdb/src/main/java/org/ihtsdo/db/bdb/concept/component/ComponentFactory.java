package org.ihtsdo.db.bdb.concept.component;

import java.util.UUID;

import org.ihtsdo.db.bdb.concept.Concept;

import com.sleepycat.bind.tuple.TupleInput;

public abstract class ComponentFactory<V extends Version<V, C>, C extends ConceptComponent<V, C>> {
	
	public abstract C create(int nid, int partCount, 
			Concept enclosingConcept, TupleInput input, 
			UUID primordialUuid);

}
