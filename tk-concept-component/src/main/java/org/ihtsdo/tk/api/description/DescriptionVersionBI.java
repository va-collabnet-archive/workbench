package org.ihtsdo.tk.api.description;

import org.ihtsdo.tk.api.Coordinate;
import org.ihtsdo.tk.api.TypedComponentVersionBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;

public interface DescriptionVersionBI extends TypedComponentVersionBI, DescriptionChronicleBI {

	public String getText();
	public int getConceptNid();
	public int getStatusNid();
	public int getTimeNid();
	public int getPathNid();
	
	public ConceptVersionBI getConcept(Coordinate c);

}
