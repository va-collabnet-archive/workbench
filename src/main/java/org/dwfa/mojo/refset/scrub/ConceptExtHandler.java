package org.dwfa.mojo.refset.scrub;

/**
 * Interface for a various implementations to process a candidate concept extension.  
 */
public interface ConceptExtHandler {
	
	void process(ConceptExtFinder conceptExtensions);
	
}
