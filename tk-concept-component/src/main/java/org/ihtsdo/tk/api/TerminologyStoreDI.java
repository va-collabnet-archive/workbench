/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.tk.api;

//~--- non-JDK imports --------------------------------------------------------

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

// TODO: Auto-generated Javadoc
/**
 * The Interface TerminologyStoreDI.
 */
public interface TerminologyStoreDI extends TerminologyDI {
   
   /**
    * Adds the term change listener.
    *
    * @param termChangeListener the term change listener
    * @deprecated not in TK3
    */ 
   @Deprecated
   void addTermChangeListener(TermChangeListener termChangeListener);
   
   /**
    * Suspend change notifications.
    *
    * @deprecated not in TK3
    */
   @Deprecated
   void suspendChangeNotifications();
   
   /**
    * Resume change notifications.
    *
    * @deprecated not in TK3
    */
   @Deprecated
   void resumeChangeNotifications();
   

   /**
    * Iterate concept data in parallel.
    *
    * @param processor the processor
    * @throws Exception the exception
    */
   void iterateConceptDataInParallel(ProcessUnfetchedConceptDataBI processor) throws Exception;
   
   
   /**
    * Iterate concept data in sequence.
    *
    * @param processor the processor
    * @throws Exception the exception
    */
   void iterateConceptDataInSequence(ProcessUnfetchedConceptDataBI processor) throws Exception;

   /**
    * Iterate sap data in sequence.
    *
    * @param processor the processor
    * @throws Exception the exception
    */
   void iterateSapDataInSequence(ProcessStampDataBI processor) throws Exception;
   
   /**
    * Removes the term change listener.
    *
    * @param termChangeListener the term change listener
    */
   void removeTermChangeListener(TermChangeListener termChangeListener);
   
   /**
    * New position.
    *
    * @param path the path
    * @param time the time
    * @return the position bi
    * @throws IOException Signals that an I/O exception has occurred.
    * @deprecated not in TK3
    */
   @Deprecated
   PositionBI newPosition(PathBI path, long time) throws IOException;
   
   /**
    * Satisfies dependencies.
    *
    * @param dependencies the dependencies
    * @return true, if successful
    * @deprecated not in TK3
    */
   @Deprecated
   boolean satisfiesDependencies(Collection<DbDependency> dependencies);
   
   /**
    * Uses rf2 metadata.
    *
    * @return true, if successful
    * @throws IOException Signals that an I/O exception has occurred.
    * @deprecated not in TK3
    */
   @Deprecated
   boolean usesRf2Metadata() throws IOException;

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the all concept nids.
    *
    * @return the all concept nids
    * @throws IOException Signals that an I/O exception has occurred.
    * @deprecated not in TK3
    */
   @Deprecated
   NidBitSetBI getAllConceptNids() throws IOException;

   /**
    * Gets the component.
    *
    * @param uuids the uuids
    * @return the component
    * @throws IOException Signals that an I/O exception has occurred.
    */
   ComponentChronicleBI<?> getComponent(Collection<UUID> uuids) throws IOException;

   /**
    * Gets the component.
    *
    * @param componentContainer the component container
    * @return the component
    * @throws IOException Signals that an I/O exception has occurred.
    */
   ComponentChronicleBI<?> getComponent(ComponentContainerBI componentContainer) throws IOException;

   /**
    * Gets the component.
    *
    * @param nid the nid
    * @return the component
    * @throws IOException Signals that an I/O exception has occurred.
    */
   ComponentChronicleBI<?> getComponent(int nid) throws IOException;

   /**
    * Gets the component.
    *
    * @param uuids the uuids
    * @return the component
    * @throws IOException Signals that an I/O exception has occurred.
    */
   ComponentChronicleBI<?> getComponent(UUID... uuids) throws IOException;

   /**
    * Gets the component version.
    *
    * @param viewCoordinate the view coordinate
    * @param uuids the uuids
    * @return the component version
    * @throws IOException Signals that an I/O exception has occurred.
    * @throws ContradictionException the contradiction exception
    */
   ComponentVersionBI getComponentVersion(ViewCoordinate viewCoordinate, Collection<UUID> uuids)
           throws IOException, ContradictionException;

   /**
    * Gets the component version.
    *
    * @param viewCoordinate the view coordinate
    * @param nid the nid
    * @return the component version
    * @throws IOException Signals that an I/O exception has occurred.
    * @throws ContradictionException the contradiction exception
    */
   ComponentVersionBI getComponentVersion(ViewCoordinate viewCoordinate, int nid)
           throws IOException, ContradictionException;

   /**
    * Gets the component version.
    *
    * @param viewCoordinate the view coordinate
    * @param uuids the uuids
    * @return the component version
    * @throws IOException Signals that an I/O exception has occurred.
    * @throws ContradictionException the contradiction exception
    */
   ComponentVersionBI getComponentVersion(ViewCoordinate viewCoordinate, UUID... uuids)
           throws IOException, ContradictionException;

   /**
    * Gets the concept.
    *
    * @param uuids the uuids
    * @return the concept
    * @throws IOException Signals that an I/O exception has occurred.
    */
   ConceptChronicleBI getConcept(Collection<UUID> uuids) throws IOException;

   /**
    * Gets the concept.
    *
    * @param conceptContainer the concept container
    * @return the concept
    * @throws IOException Signals that an I/O exception has occurred.
    */
   ConceptChronicleBI getConcept(ConceptContainerBI conceptContainer) throws IOException;

   /**
    * Gets the concept.
    *
    * @param conceptNid the concept nid
    * @return the concept
    * @throws IOException Signals that an I/O exception has occurred.
    */
   ConceptChronicleBI getConcept(int conceptNid) throws IOException;

   /**
    * Gets the concept.
    *
    * @param uuids the uuids
    * @return the concept
    * @throws IOException Signals that an I/O exception has occurred.
    */
   ConceptChronicleBI getConcept(UUID... uuids) throws IOException;

   /**
    * Gets the concept for nid.
    *
    * @param nid the nid
    * @return the concept for nid
    * @throws IOException Signals that an I/O exception has occurred.
    */
   ConceptChronicleBI getConceptForNid(int nid) throws IOException;

   /**
    * Gets the concept nid for nid.
    *
    * @param nid the nid
    * @return the concept nid for nid
    * @throws IOException Signals that an I/O exception has occurred.
    */
   int getConceptNidForNid(int nid) throws IOException;

   /**
    * Gets the concept version.
    *
    * @param viewCoordinate the view coordinate
    * @param uuids the uuids
    * @return the concept version
    * @throws IOException Signals that an I/O exception has occurred.
    */
   ConceptVersionBI getConceptVersion(ViewCoordinate viewCoordinate, Collection<UUID> uuids) throws IOException;

   /**
    * Gets the concept version.
    *
    * @param viewCoordinate the view coordinate
    * @param conceptNid the concept nid
    * @return the concept version
    * @throws IOException Signals that an I/O exception has occurred.
    */
   ConceptVersionBI getConceptVersion(ViewCoordinate viewCoordinate, int conceptNid) throws IOException;

   /**
    * Gets the concept version.
    *
    * @param viewCoordinate the view coordinate
    * @param uuids the uuids
    * @return the concept version
    * @throws IOException Signals that an I/O exception has occurred.
    */
   ConceptVersionBI getConceptVersion(ViewCoordinate viewCoordinate, UUID... uuids) throws IOException;

   /**
    * Gets the concept versions.
    *
    * @param viewCoordinate the view coordinate
    * @param conceptNids the concept nids
    * @return the concept versions
    * @throws IOException Signals that an I/O exception has occurred.
    */
   Map<Integer, ConceptVersionBI> getConceptVersions(ViewCoordinate viewCoordinate, NidBitSetBI conceptNids) throws IOException;

   /**
    * Gets the concepts.
    *
    * @param conceptNids the concept nids
    * @return the concepts
    * @throws IOException Signals that an I/O exception has occurred.
    */
   Map<Integer, ConceptChronicleBI> getConcepts(NidBitSetBI conceptNids) throws IOException;

   /**
    * Gets the conflict identifier.
    *
    * @param viewCoordinate the view coordinate
    * @param useCase the use case
    * @return the conflict identifier
    */
   ContradictionIdentifierBI getConflictIdentifier(ViewCoordinate viewCoordinate, boolean useCase);
  
   /**
    * Gets the empty nid set.
    *
    * @return the empty nid set
    * @throws IOException Signals that an I/O exception has occurred.
    */
   NidBitSetBI getEmptyNidSet() throws IOException;

   /**
    * Gets the latest change set dependencies.
    *
    * @return the latest change set dependencies
    * @throws IOException Signals that an I/O exception has occurred.
    */
   Collection<DbDependency> getLatestChangeSetDependencies() throws IOException;

   /**
    * Gets the metadata view coordinate.
    *
    * @return the metadata view coordinate
    * @throws IOException Signals that an I/O exception has occurred.
    * @deprecated not in TK3
    */
   @Deprecated
   ViewCoordinate getMetadataViewCoordinate() throws IOException;
   
   /**
    * Gets the metadata edit coordinate.
    *
    * @return the metadata edit coordinate
    * @throws IOException Signals that an I/O exception has occurred.
    * @deprecated not in TK3
    */
   @Deprecated
   EditCoordinate getMetadataEditCoordinate() throws IOException;

   /**
    * Gets the native id for uuids.
    *
    * @param uuids the uuids
    * @return the native id for uuids
    * @throws IOException Signals that an I/O exception has occurred.
    */
   int getNidForUuids(Collection<UUID> uuids) throws IOException;

   /**
    * Gets the native id for uuids.
    *
    * @param uuids the uuids
    * @return the native id for uuids
    * @throws IOException Signals that an I/O exception has occurred.
    */
   int getNidForUuids(UUID... uuids) throws IOException;

   /**
    * Gets the read only max sap.
    *
    * @return the read only max sap
    * @deprecated not in TK3
    */
   @Deprecated
   int getReadOnlyMaxSap();

   /**
    * Gets the path children.
    *
    * @param pathNid the path nid
    * @return the path children
    */
   List<? extends PathBI> getPathChildren(int pathNid);

   /**
    * Gets the possible children.
    *
    * @param conceptNid the concept nid
    * @param viewCoordinate the view coordinate
    * @return the possible children
    * @throws IOException Signals that an I/O exception has occurred.
    * @throws ContradictionException the contradiction exception
    */
   int[] getPossibleChildren(int conceptNid, ViewCoordinate viewCoordinate) throws IOException, ContradictionException;

   /**
    * Gets the sequence.
    *
    * @return the sequence
    * @deprecated not in TK3
    */
   @Deprecated
   long getSequence();

   /**
    * Gets the snapshot.
    *
    * @param viewCoordinate the view coordinate
    * @return the snapshot
    */
   TerminologySnapshotDI getSnapshot(ViewCoordinate viewCoordinate);

   /**
    * Gets the terminology builder.
    *
    * @param editCoordinate the edit coordinate
    * @param viewCoordinate the view coordinate
    * @return the terminology builder
    */
   TerminologyBuilderBI getTerminologyBuilder(EditCoordinate editCoordinate, ViewCoordinate viewCoordinate);

   /**
    * Gets the uncommitted concepts.
    *
    * @return the uncommitted concepts
    */
   Collection<? extends ConceptChronicleBI> getUncommittedConcepts();
   
   /**
    * Gets the uuid primordial for nid.
    *
    * @param nid the nid
    * @return the primordial UUID if known. The IUnknown UUID (00000000-0000-0000-C000-000000000046) if not known.
    * @throws IOException Signals that an I/O exception has occurred.
    */
 
   UUID getUuidPrimordialForNid(int nid) throws IOException;

   /**
    * Gets the uuids for nid.
    *
    * @param nid the nid
    * @return the uuids for nid
    * @throws IOException Signals that an I/O exception has occurred.
    */
   List<UUID> getUuidsForNid(int nid) throws IOException;

   /**
    * Checks for path.
    *
    * @param nid the nid
    * @return true, if successful
    * @throws IOException Signals that an I/O exception has occurred.
    */
   boolean hasPath(int nid) throws IOException;

   /**
    * Checks for uncommitted changes.
    *
    * @return true, if successful
    */
   boolean hasUncommittedChanges();

   /**
    * Checks for uuid.
    *
    * @param memberUuid the member uuid
    * @return true, if successful
    */
   boolean hasUuid(UUID memberUuid);
   
   /**
    * Checks for uuid.
    *
    * @param memberUuids the member uuids
    * @return true, if successful
    */
   boolean hasUuid(List<UUID> memberUuids);
   
   /**
    * Forget.
    *
    * @param relationshipVersion the relationship version
    * @throws IOException Signals that an I/O exception has occurred.
    * @deprecated not in TK3
    */
   @Deprecated
   void forget(RelationshipVersionBI relationshipVersion) throws IOException;
   
   /**
    * Forget.
    *
    * @param descriptionVersion the description version
    * @throws IOException Signals that an I/O exception has occurred.
    * @deprecated not in TK3
    */
   @Deprecated
   void forget(DescriptionVersionBI descriptionVersion) throws IOException;
   
   /**
    * Forget.
    *
    * @param refexChronicle the refex chronicle
    * @throws IOException Signals that an I/O exception has occurred.
    * @deprecated not in TK3
    */
   @Deprecated
   void forget(RefexChronicleBI refexChronicle) throws IOException;
   
   /**
    * Forget.
    *
    * @param conceptAttributeVersion the concept attribute version
    * @return true, if successful
    * @throws IOException Signals that an I/O exception has occurred.
    * @deprecated not in TK3
    */
   @Deprecated
   boolean forget(ConceptAttributeVersionBI conceptAttributeVersion) throws IOException;
   
   /**
    * Forget.
    *
    * @param conceptChronicle the concept chronicle
    * @throws IOException Signals that an I/O exception has occurred.
    * @deprecated not in TK3
    */
   @Deprecated
   void forget(ConceptChronicleBI conceptChronicle) throws IOException;
   
   /**
    * Gets the path nid for stamp nid.
    *
    * @param stampNid the stamp nid
    * @return the path nid for stamp nid
    * @deprecated not in TK3
    */
   @Deprecated
   int getPathNidForStampNid(int stampNid);
   
   /**
    * Gets the author nid for stamp nid.
    *
    * @param stampNid the stamp nid
    * @return the author nid for stamp nid
    * @deprecated not in TK3
    */
   @Deprecated
   int getAuthorNidForStampNid(int stampNid);
   
   /**
    * Gets the status nid for stamp nid.
    *
    * @param stampNid the stamp nid
    * @return the status nid for stamp nid
    * @deprecated not in TK3
    */
   @Deprecated
   int getStatusNidForStampNid(int stampNid);
   
   /**
    * Gets the module nid for stamp nid.
    *
    * @param stampNid the stamp nid
    * @return the module nid for stamp nid
    * @deprecated not in TK3
    */
   @Deprecated
   int getModuleNidForStampNid(int stampNid);
   
   /**
    * Gets the time for stamp nid.
    *
    * @param stampNid the stamp nid
    * @return the time for stamp nid
    * @deprecated not in TK3
    */
   @Deprecated
   long getTimeForStampNid(int stampNid);
   
   /**
    * Only CONCEPT_EVENT.PRE_COMMIT is a vetoable change
    *
    * @param conceptEvent the concept event
    * @param vetoableChangeListener the vetoable change listener
    * @deprecated not in TK3
    */
   @Deprecated
   void addVetoablePropertyChangeListener(CONCEPT_EVENT conceptEvent, VetoableChangeListener vetoableChangeListener);
   
   /**
    * Adds the property change listener.
    *
    * @param conceptEvent the concept event
    * @param propertyChangeListener the property change listener
    * @deprecated not in TK3
    */
   @Deprecated
   void addPropertyChangeListener(CONCEPT_EVENT conceptEvent, PropertyChangeListener propertyChangeListener);

   /**
    * Checks for extension.
    *
    * @param refsetNid the refset nid
    * @param componentNid the component nid
    * @return true, if successful
    */
   boolean hasExtension(int refsetNid, int componentNid);

   /**
    * The Enum CONCEPT_EVENT.
    */
   public enum CONCEPT_EVENT {
    
    /** The pre commit. */
    PRE_COMMIT, 
 /** The post commit. */
 POST_COMMIT, 
 /** The add uncommitted. */
 ADD_UNCOMMITTED;
   }
   
   /**
    * Touch component.
    *
    * @param nid the nid
    * @deprecated not in TK3
    */
   @Deprecated
   void touchComponent(int nid);
   
   /**
    * Touch component alert.
    *
    * @param nid the nid
    * @deprecated not in TK3
    */
   @Deprecated
   void touchComponentAlert(int nid);
   
   /**
    * Touch component template.
    *
    * @param nid the nid
    * @deprecated not in TK3
    */
   @Deprecated
   void touchComponentTemplate(int nid);
   
   /**
    * Touch components.
    *
    * @param conceptNids the concept nids
    * @deprecated not in TK3
    */
   @Deprecated
   void touchComponents(Collection<Integer> conceptNids);
   
   /**
    * Touch refex rc.
    *
    * @param nid the nid
    * @deprecated not in TK3
    */
   @Deprecated
   void touchRefexRC(int nid);
   
   /**
    * Touch rel origin.
    *
    * @param nid the nid
    * @deprecated not in TK3
    */
   @Deprecated
   void touchRelOrigin(int nid);
   
   /**
    * Touch rel target.
    *
    * @param nid the nid
    * @deprecated not in TK3
    */
   @Deprecated
   void touchRelTarget(int nid);
   
   /**
    * Gets the status-time-author-module-path nid.
    *
    * @param version the version
    * @return the status-time-author-module-path nid
    * @throws IOException Signals that an I/O exception has occurred.
    */
   int getStampNid(TkRevision version) throws IOException;
   
   /**
    * Checks if is kind of.
    *
    * @param childNid the child nid
    * @param parentNid the parent nid
    * @param vc the vc
    * @return true, if is kind of
    * @throws IOException Signals that an I/O exception has occurred.
    * @throws ContradictionException the contradiction exception
    */
   boolean isKindOf(int childNid, int parentNid, ViewCoordinate vc)
           throws IOException, ContradictionException;
   
   /**
    * Checks if is child of.
    *
    * @param childNid the child nid
    * @param parentNid the parent nid
    * @param vc the vc
    * @return true, if is child of
    * @throws IOException Signals that an I/O exception has occurred.
    * @throws ContradictionException the contradiction exception
    */
   boolean isChildOf(int childNid, int parentNid, ViewCoordinate vc)
            throws IOException, ContradictionException;
   
   /**
    * Gets the ancestors.
    *
    * @param childNid the child nid
    * @param vc the vc
    * @return the ancestors
    * @throws IOException Signals that an I/O exception has occurred.
    * @throws ContradictionException the contradiction exception
    */
   Set<Integer> getAncestors(int childNid, ViewCoordinate vc) throws IOException, ContradictionException;
       
   /**
    * Gets the incoming relationships source nids.
    *
    * @param cNid the c nid
    * @param relTypes the rel types
    * @return the incoming relationships source nids
    * @throws IOException Signals that an I/O exception has occurred.
    */
   int[] getIncomingRelationshipsSourceNids(int cNid, NidSetBI relTypes) throws IOException;
}
