package org.ihtsdo.tk.api;

import java.io.IOException;

public interface TypedComponentVersionBI extends ComponentVersionBI {

	public int getTypeNid();
	public ConceptVersionBI getType() throws IOException;

}
