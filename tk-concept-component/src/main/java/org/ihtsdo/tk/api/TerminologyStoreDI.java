package org.ihtsdo.tk.api;

import java.util.Collection;
import java.util.UUID;

public interface TerminologyStoreDI {

	ComponentChroncileBI<?> getComponent(UUID... ids);
	ComponentChroncileBI<?> getComponent(Collection<UUID> ids);
	
	ComponentVersionBI getComponent(Coordinate c, UUID... ids);
	ComponentVersionBI getComponent(Coordinate c, Collection<UUID> ids);
	
	ConceptVersionBI getConcept(Coordinate c, UUID... ids);
	ConceptVersionBI getConcept(Coordinate c, Collection<UUID> ids);
	
}
