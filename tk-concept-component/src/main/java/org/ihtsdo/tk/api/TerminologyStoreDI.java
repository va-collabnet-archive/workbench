package org.ihtsdo.tk.api;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;

public interface TerminologyStoreDI extends TerminologyTransactionDI {
	
	TerminologySnapshotDI getSnapshot(Coordinate c);

	ComponentChroncileBI<?> getComponent(int nid) throws IOException;
	ComponentChroncileBI<?> getComponent(UUID... uuids) throws IOException;
	ComponentChroncileBI<?> getComponent(Collection<UUID> uuids) throws IOException;
	
	ConceptChronicleBI getConcept(int cNid) throws IOException;
	ConceptChronicleBI getConcept(UUID... uuids) throws IOException;
	ConceptChronicleBI getConcept(Collection<UUID> uuids) throws IOException;
	
	ComponentVersionBI getComponentVersion(Coordinate coordinate, int nid) throws IOException, ContraditionException;
	ComponentVersionBI getComponentVersion(Coordinate coordinate, UUID... uuids) throws IOException, ContraditionException;
	ComponentVersionBI getComponentVersion(Coordinate coordinate, Collection<UUID> uuids) throws IOException, ContraditionException;
	
	ConceptVersionBI getConceptVersion(Coordinate coordinate, int cNid) throws IOException;
	ConceptVersionBI getConceptVersion(Coordinate coordinate, UUID... uuids) throws IOException;
	ConceptVersionBI getConceptVersion(Coordinate coordinate, Collection<UUID> uuids) throws IOException;

	int uuidsToNid(UUID... uuids) throws IOException;
	int uuidsToNid(Collection<UUID> uuids) throws IOException;
		
}
