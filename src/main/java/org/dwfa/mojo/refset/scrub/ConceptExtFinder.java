package org.dwfa.mojo.refset.scrub;

import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;

/**
 * Interface for various implementations that will identify candidate concept extensions that need
 * to be processed or corrected in some manner. It is intended that these concept extensions be passed to 
 * an appropriate {@link ConceptExtHandler}   
 */
public interface ConceptExtFinder extends Iterable<I_ThinExtByRefVersioned> {
	
}
