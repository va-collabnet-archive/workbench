package org.ihtsdo.tk.api;

import java.io.IOException;

import org.ihtsdo.tk.api.concept.ConceptVersionBI;

public interface TypedComponentVersionBI extends ComponentVersionBI {

	public int getTypeNid();
	public ConceptVersionBI getType() throws IOException;

}
