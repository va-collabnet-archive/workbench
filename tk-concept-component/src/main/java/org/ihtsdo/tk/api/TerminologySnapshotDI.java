package org.ihtsdo.tk.api;

import java.util.Collection;
import java.util.UUID;

import org.ihtsdo.tk.api.concept.ConceptVersionBI;

public interface TerminologySnapshotDI extends TerminologyTransactionDI {

	ComponentVersionBI getComponent(int nid);
	ComponentVersionBI getComponent(UUID... ids);
	ComponentVersionBI getComponent(Collection<UUID> ids);
	
	ConceptVersionBI getConcept(int cNid);
	ConceptVersionBI getConcept(UUID... ids);
	ConceptVersionBI getConcept(Collection<UUID> ids);

}
