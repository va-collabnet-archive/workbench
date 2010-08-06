package org.ihtsdo.tk.api;

public interface DescriptionVersionBI extends TypedComponentVersionBI, DescriptionChronicleBI {

	public String getText();
	public int getConceptNid();
	public int getStatusNid();
	public int getTimeNid();
	public int getPathNid();
	
	public ConceptVersionBI getConcept(Coordinate c);

}
