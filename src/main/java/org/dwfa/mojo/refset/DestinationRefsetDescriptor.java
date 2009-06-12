package org.dwfa.mojo.refset;

import org.dwfa.ace.api.ConceptDescriptor;

/**
 * Describes a destination refset to be modified, including the path 
 * it should be modified on (at present this is singular).  
 */
public class DestinationRefsetDescriptor {

	private ConceptDescriptor refset;
	
	private ConceptDescriptor path;

	public ConceptDescriptor getRefset() {
		return refset;
	}

	public void setRefset(ConceptDescriptor refset) {
		this.refset = refset;
	}

	public ConceptDescriptor getPath() {
		return path;
	}

	public void setPath(ConceptDescriptor path) {
		this.path = path;
	}
	
}
