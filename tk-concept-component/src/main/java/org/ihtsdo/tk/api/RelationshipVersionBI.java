package org.ihtsdo.tk.api;

import java.io.IOException;

public interface RelationshipVersionBI extends TypedComponentVersionBI {

	public int getOriginNid();
	public int getDestinationNid();

	public ConceptVersionBI getOrigin(Coordinate c) throws IOException;
	public ConceptVersionBI getDestination(Coordinate c) throws IOException;

}
