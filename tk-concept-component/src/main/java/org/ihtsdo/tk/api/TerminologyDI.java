package org.ihtsdo.tk.api;

import java.util.Collection;
import java.util.UUID;

import org.ihtsdo.tk.api.concept.ConceptVersionBI;

public interface TerminologyDI extends TerminologyTransactionDI {

	ComponentChroncileBI<?> getComponent(int nid);
	ComponentChroncileBI<?> getComponent(UUID... ids);
	ComponentChroncileBI<?> getComponent(Collection<UUID> ids);
	
	ComponentVersionBI getComponent(Coordinate c, int nid);
	ComponentVersionBI getComponent(Coordinate c, UUID... ids);
	ComponentVersionBI getComponent(Coordinate c, Collection<UUID> ids);
	
	ConceptVersionBI getConcept(Coordinate c, int cNid);
	ConceptVersionBI getConcept(Coordinate c, UUID... ids);
	ConceptVersionBI getConcept(Coordinate c, Collection<UUID> ids);
	
	TerminologySnapshotDI getSnapshot(Coordinate c);
		
}
