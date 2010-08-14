package org.ihtsdo.tk.api.relationship;

import java.io.IOException;

import org.ihtsdo.tk.api.Coordinate;
import org.ihtsdo.tk.api.TypedComponentVersionBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;

public interface RelationshipVersionBI extends TypedComponentVersionBI {

	public int getOriginNid();
	public int getDestinationNid();

	public ConceptVersionBI getOrigin(Coordinate c) throws IOException;
	public ConceptVersionBI getDestination(Coordinate c) throws IOException;

}
