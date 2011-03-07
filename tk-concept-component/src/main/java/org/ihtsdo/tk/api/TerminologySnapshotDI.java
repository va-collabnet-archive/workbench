package org.ihtsdo.tk.api;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.ihtsdo.tk.api.amend.TerminologyAmendmentBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;

public interface TerminologySnapshotDI extends TerminologyTransactionDI {

    ComponentVersionBI getComponentVersion(int nid) throws IOException, ContraditionException;

    ComponentVersionBI getComponentVersion(UUID... uuids) throws IOException, ContraditionException;

    ComponentVersionBI getComponentVersion(Collection<UUID> uuids) throws IOException, ContraditionException;

    ConceptVersionBI getConceptVersion(int cNid) throws IOException;

    ConceptVersionBI getConceptVersion(UUID... uuids) throws IOException;

    ConceptVersionBI getConceptVersion(Collection<UUID> uuids) throws IOException;

    Map<Integer, ConceptVersionBI> getConceptVersions(NidBitSetBI cNids) throws IOException;

    TerminologyAmendmentBI getAmender(EditCoordinate ec);
}
