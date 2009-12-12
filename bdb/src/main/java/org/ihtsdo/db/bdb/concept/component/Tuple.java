package org.ihtsdo.db.bdb.concept.component;

import org.dwfa.ace.api.I_AmTuple;

public abstract class Tuple<P extends Part<P>, F extends ConceptComponent<P>> 
	implements I_AmTuple<P, F> {

}
