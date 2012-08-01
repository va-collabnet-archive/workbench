package org.ihtsdo.tk.api;

//~--- non-JDK imports --------------------------------------------------------

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.contradiction.ContradictionIdentifierBI;
import org.ihtsdo.tk.db.DbDependency;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

public interface TerminologyStoreDI extends TerminologyDI {
   /**
    * 
    * @param termChangeListener
    * @deprecated not in TK3
    */ 
   @Deprecated
   void addTermChangeListener(TermChangeListener termChangeListener);
   
   /**
    * 
    * @deprecated not in TK3
    */
   @Deprecated
   void suspendChangeNotifications();
   
   /**
    * 
    * @deprecated not in TK3
    */
   @Deprecated
   void resumeChangeNotifications();
   

   void iterateConceptDataInParallel(ProcessUnfetchedConceptDataBI processor) throws Exception;
   
   
   void iterateConceptDataInSequence(ProcessUnfetchedConceptDataBI processor) throws Exception;

   void iterateSapDataInSequence(ProcessStampDataBI processor) throws Exception;
   
   void removeTermChangeListener(TermChangeListener termChangeListener);
   
   /**
    * 
    * @param path
    * @param time
    * @return
    * @throws IOException
    * @deprecated not in TK3
    */
   @Deprecated
   PositionBI newPosition(PathBI path, long time) throws IOException;
   
   /**
    * 
    * @param dependencies
    * @return
    * @deprecated not in TK3
    */
   @Deprecated
   boolean satisfiesDependencies(Collection<DbDependency> dependencies);
   
   /**
    * 
    * @return
    * @throws IOException
    * @deprecated not in TK3
    */
   @Deprecated
   boolean usesRf2Metadata() throws IOException;

   //~--- get methods ---------------------------------------------------------
   /**
    * 
    * @return
    * @throws IOException
    * @deprecated not in TK3
    */
   @Deprecated
   NidBitSetBI getAllConceptNids() throws IOException;

   ComponentChronicleBI<?> getComponent(Collection<UUID> uuids) throws IOException;

   ComponentChronicleBI<?> getComponent(ComponentContainerBI componentContainer) throws IOException;

   ComponentChronicleBI<?> getComponent(int nid) throws IOException;

   ComponentChronicleBI<?> getComponent(UUID... uuids) throws IOException;

   ComponentVersionBI getComponentVersion(ViewCoordinate viewCoordinate, Collection<UUID> uuids)
           throws IOException, ContradictionException;

   ComponentVersionBI getComponentVersion(ViewCoordinate viewCoordinate, int nid)
           throws IOException, ContradictionException;

   ComponentVersionBI getComponentVersion(ViewCoordinate viewCoordinate, UUID... uuids)
           throws IOException, ContradictionException;

   ConceptChronicleBI getConcept(Collection<UUID> uuids) throws IOException;

   ConceptChronicleBI getConcept(ConceptContainerBI conceptContainer) throws IOException;

   ConceptChronicleBI getConcept(int conceptNid) throws IOException;

   ConceptChronicleBI getConcept(UUID... uuids) throws IOException;

   ConceptChronicleBI getConceptForNid(int nid) throws IOException;

   int getConceptNidForNid(int nid) throws IOException;

   ConceptVersionBI getConceptVersion(ViewCoordinate viewCoordinate, Collection<UUID> uuids) throws IOException;

   ConceptVersionBI getConceptVersion(ViewCoordinate viewCoordinate, int conceptNid) throws IOException;

   ConceptVersionBI getConceptVersion(ViewCoordinate viewCoordinate, UUID... uuids) throws IOException;

   Map<Integer, ConceptVersionBI> getConceptVersions(ViewCoordinate viewCoordinate, NidBitSetBI conceptNids) throws IOException;

   Map<Integer, ConceptChronicleBI> getConcepts(NidBitSetBI conceptNids) throws IOException;

   ContradictionIdentifierBI getConflictIdentifier(ViewCoordinate viewCoordinate, boolean useCase);
  
   NidBitSetBI getEmptyNidSet() throws IOException;

   Collection<DbDependency> getLatestChangeSetDependencies() throws IOException;

   /**
    * 
    * @return
    * @throws IOException
    * @deprecated not in TK3
    */
   @Deprecated
   ViewCoordinate getMetadataViewCoordinate() throws IOException;
   
   /**
    * 
    * @return
    * @throws IOException
    * @deprecated not in TK3
    */
   @Deprecated
   EditCoordinate getMetadataEditCoordinate() throws IOException;

   int getNidForUuids(Collection<UUID> uuids) throws IOException;

   int getNidForUuids(UUID... uuids) throws IOException;

   /**
    * 
    * @return
    * @deprecated not in TK3
    */
   @Deprecated
   int getReadOnlyMaxSap();

   List<? extends PathBI> getPathChildren(int pathNid);

   int[] getPossibleChildren(int conceptNid, ViewCoordinate viewCoordinate) throws IOException;

   /**
    * 
    * @return
    * @deprecated not in TK3
    */
   @Deprecated
   long getSequence();

   TerminologySnapshotDI getSnapshot(ViewCoordinate viewCoordinate);

   TerminologyBuilderBI getTerminologyBuilder(EditCoordinate editCoordinate, ViewCoordinate viewCoordinate);

   Collection<? extends ConceptChronicleBI> getUncommittedConcepts();
   /**
    * @return the primordial UUID if known. The IUnknown UUID (00000000-0000-0000-C000-000000000046) if not known.
    */
 
   UUID getUuidPrimordialForNid(int nid) throws IOException;

   List<UUID> getUuidsForNid(int nid) throws IOException;

   boolean hasPath(int nid) throws IOException;

   boolean hasUncommittedChanges();

   boolean hasUuid(UUID memberUuid);
   
   boolean hasUuid(List<UUID> memberUuids);
   
   /**
    * 
    * @param relationshipVersion
    * @throws IOException
    * @deprecated not in TK3
    */
   @Deprecated
   void forget(RelationshipVersionBI relationshipVersion) throws IOException;
   
   /**
    * 
    * @param descriptionVersion
    * @throws IOException
    * @deprecated not in TK3
    */
   @Deprecated
   void forget(DescriptionVersionBI descriptionVersion) throws IOException;
   
   /**
    * 
    * @param refexChronicle
    * @throws IOException
    * @deprecated not in TK3
    */
   @Deprecated
   void forget(RefexChronicleBI refexChronicle) throws IOException;
   
   /**
    * 
    * @param conceptAttributeVersion
    * @throws IOException
    * @deprecated not in TK3
    */
   @Deprecated
   boolean forget(ConceptAttributeVersionBI conceptAttributeVersion) throws IOException;
   
   /**
    * 
    * @param conceptChronicle
    * @throws IOException
    * @deprecated not in TK3
    */
   @Deprecated
   void forget(ConceptChronicleBI conceptChronicle) throws IOException;
   
   /**
    * 
    * @param stampNid
    * @return
    * @deprecated not in TK3
    */
   @Deprecated
   int getPathNidForStampNid(int stampNid);
   /**
    * 
    * @param stampNid
    * @return
    * @deprecated not in TK3
    */
   @Deprecated
   int getAuthorNidForStampNid(int stampNid);
   /**
    * 
    * @param stampNid
    * @return
    * @deprecated not in TK3
    */
   @Deprecated
   int getStatusNidForStampNid(int stampNid);
   /**
    * 
    * @param stampNid
    * @return
    * @deprecated not in TK3
    */
   @Deprecated
   int getModuleNidForStampNid(int stampNid);
   /**
    * 
    * @param stampNid
    * @return
    * @deprecated not in TK3
    */
   @Deprecated
   long getTimeForStampNid(int stampNid);
   
   /**
    * Only CONCEPT_EVENT.PRE_COMMIT is a vetoable change
    * @param conceptEvent
    * @param vetoableChangeListener
    * @deprecated not in TK3
    */
   @Deprecated
   void addVetoablePropertyChangeListener(CONCEPT_EVENT conceptEvent, VetoableChangeListener vetoableChangeListener);
   
   /**
    * 
    * @param conceptEvent
    * @param propertyChangeListener
    * @deprecated not in TK3
    */
   @Deprecated
   void addPropertyChangeListener(CONCEPT_EVENT conceptEvent, PropertyChangeListener propertyChangeListener);

   public enum CONCEPT_EVENT {
    PRE_COMMIT, POST_COMMIT, ADD_UNCOMMITTED;
   }
   
   /**
    * 
    * @param nid
    * @deprecated not in TK3
    */
   @Deprecated
   void touchComponent(int nid);
   /**
    * 
    * @param nid
    * @deprecated not in TK3
    */
   @Deprecated
   void touchComponentAlert(int nid);
   /**
    * 
    * @param nid
    * @deprecated not in TK3
    */
   @Deprecated
   void touchComponentTemplate(int nid);
   /**
    * 
    * @param conceptNids
    * @deprecated not in TK3
    */
   @Deprecated
   void touchComponents(Collection<Integer> conceptNids);
   /**
    * 
    * @param nid
    * @deprecated not in TK3
    */
   @Deprecated
   void touchRefexRC(int nid);
   /**
    * 
    * @param nid
    * @deprecated not in TK3
    */
   @Deprecated
   void touchRelOrigin(int nid);
   /**
    * 
    * @param nid
    * @deprecated not in TK3
    */
   @Deprecated
   void touchRelTarget(int nid);
   
   int getStampNid(TkRevision version) throws IOException;
   
   boolean isKindOf(int childNid, int parentNid, ViewCoordinate vc)
           throws IOException, ContradictionException;
   
   int[] getIncomingRelationshipsSourceNids(int cNid, NidSetBI relTypes) throws IOException;
}
