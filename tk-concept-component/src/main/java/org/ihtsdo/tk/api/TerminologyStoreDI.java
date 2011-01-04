package org.ihtsdo.tk.api;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.ihtsdo.tk.api.amend.TerminologyAmendmentBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.db.DbDependency;

public interface TerminologyStoreDI extends TerminologyTransactionDI {

    TerminologySnapshotDI getSnapshot(ViewCoordinate vc);

    ComponentChroncileBI<?> getComponent(int nid) throws IOException;

    ComponentChroncileBI<?> getComponent(UUID... uuids) throws IOException;

    ComponentChroncileBI<?> getComponent(Collection<UUID> uuids) throws IOException;

    ConceptChronicleBI getConcept(int cNid) throws IOException;

    ConceptChronicleBI getConcept(UUID... uuids) throws IOException;

    ConceptChronicleBI getConcept(Collection<UUID> uuids) throws IOException;

    Map<Integer, ConceptChronicleBI> getConcepts(NidBitSetBI cNids) throws IOException;

    ComponentVersionBI getComponentVersion(ViewCoordinate vc, int nid) throws IOException, ContraditionException;

    ComponentVersionBI getComponentVersion(ViewCoordinate vc, UUID... uuids) throws IOException, ContraditionException;

    ComponentVersionBI getComponentVersion(ViewCoordinate vc, Collection<UUID> uuids) throws IOException, ContraditionException;

    ConceptVersionBI getConceptVersion(ViewCoordinate vc, int cNid) throws IOException;

    ConceptVersionBI getConceptVersion(ViewCoordinate vc, UUID... uuids) throws IOException;

    ConceptVersionBI getConceptVersion(ViewCoordinate vc, Collection<UUID> uuids) throws IOException;

    Map<Integer, ConceptVersionBI> getConceptVersions(ViewCoordinate vc, NidBitSetBI cNids) throws IOException;

    int getConceptNidForNid(int nid) throws IOException;

    List<UUID> getUuidsForNid(int nid);

    int getNidForUuids(UUID... uuids) throws IOException;

    int getNidForUuids(Collection<UUID> uuids) throws IOException;

    KindOfCacheBI getCache(ViewCoordinate vc) throws Exception;

    Collection<DbDependency> getLatestChangeSetDependencies() throws IOException;

    boolean satisfiesDependencies(Collection<DbDependency> dependencies);

    TerminologyAmendmentBI getAmender(EditCoordinate ec, ViewCoordinate vc);

    public boolean hasUuid(UUID memberUUID);
}
