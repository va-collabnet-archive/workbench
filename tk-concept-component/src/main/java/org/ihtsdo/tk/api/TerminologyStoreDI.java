package org.ihtsdo.tk.api;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.contradiction.ContradictionIdentifierBI;
import org.ihtsdo.tk.db.DbDependency;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface TerminologyStoreDI extends TerminologyTransactionDI {
   void addTermChangeListener(TermChangeListener cl);

   void iterateConceptDataInParallel(ProcessUnfetchedConceptDataBI processor) throws Exception;

   void iterateConceptDataInSequence(ProcessUnfetchedConceptDataBI processor) throws Exception;

   void removeTermChangeListener(TermChangeListener cl);
   
   PositionBI newPosition(PathBI path, long time) throws IOException;

   boolean satisfiesDependencies(Collection<DbDependency> dependencies);

   boolean usesRf2Metadata() throws IOException;

   //~--- get methods ---------------------------------------------------------

   NidBitSetBI getAllConceptNids() throws IOException;

   KindOfCacheBI getCache(ViewCoordinate vc) throws Exception;

   ComponentChroncileBI<?> getComponent(Collection<UUID> uuids) throws IOException;

   ComponentChroncileBI<?> getComponent(ComponentContainerBI cc) throws IOException;

   ComponentChroncileBI<?> getComponent(int nid) throws IOException;

   ComponentChroncileBI<?> getComponent(UUID... uuids) throws IOException;

   ComponentVersionBI getComponentVersion(ViewCoordinate vc, Collection<UUID> uuids)
           throws IOException, ContraditionException;

   ComponentVersionBI getComponentVersion(ViewCoordinate vc, int nid)
           throws IOException, ContraditionException;

   ComponentVersionBI getComponentVersion(ViewCoordinate vc, UUID... uuids)
           throws IOException, ContraditionException;

   ConceptChronicleBI getConcept(Collection<UUID> uuids) throws IOException;

   ConceptChronicleBI getConcept(ConceptContainerBI cc) throws IOException;

   ConceptChronicleBI getConcept(int cNid) throws IOException;

   ConceptChronicleBI getConcept(UUID... uuids) throws IOException;

   ConceptChronicleBI getConceptForNid(int nid) throws IOException;

   int getConceptNidForNid(int nid) throws IOException;

   ConceptVersionBI getConceptVersion(ViewCoordinate vc, Collection<UUID> uuids) throws IOException;

   ConceptVersionBI getConceptVersion(ViewCoordinate vc, int cNid) throws IOException;

   ConceptVersionBI getConceptVersion(ViewCoordinate vc, UUID... uuids) throws IOException;

   Map<Integer, ConceptVersionBI> getConceptVersions(ViewCoordinate vc, NidBitSetBI cNids) throws IOException;

   Map<Integer, ConceptChronicleBI> getConcepts(NidBitSetBI cNids) throws IOException;

   ContradictionIdentifierBI getConflictIdentifier(ViewCoordinate viewCoord, boolean useCase);

   NidBitSetBI getEmptyNidSet() throws IOException;

   Collection<DbDependency> getLatestChangeSetDependencies() throws IOException;

   ViewCoordinate getMetadataVC() throws IOException;

   int getNidForUuids(Collection<UUID> uuids) throws IOException;

   int getNidForUuids(UUID... uuids) throws IOException;

   List<? extends PathBI> getPathChildren(int nid);

   int[] getPossibleChildren(int cNid, ViewCoordinate vc) throws IOException;

   long getSequence();

   TerminologySnapshotDI getSnapshot(ViewCoordinate vc);

   TerminologyConstructorBI getTerminologyConstructor(EditCoordinate ec, ViewCoordinate vc);

   Collection<? extends ConceptChronicleBI> getUncommittedConcepts();
   /**
    * @return the primordial UUID if known. The IUnknown UUID (00000000-0000-0000-C000-000000000046) if not known.
    */
 
   UUID getUuidPrimordialForNid(int nid) throws IOException;

   List<UUID> getUuidsForNid(int nid) throws IOException;

   boolean hasPath(int nid) throws IOException;

   boolean hasUncommittedChanges();

   boolean hasUuid(UUID memberUUID);

   int getPathNidForSapNid(int sapNid);
   int getAuthorNidForSapNid(int sapNid);
   int getStatusNidForSapNid(int sapNid);
   long getTimeForSapNid(int sapNid);
}
