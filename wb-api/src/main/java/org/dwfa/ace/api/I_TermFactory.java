/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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



package org.dwfa.ace.api;

//~--- non-JDK imports --------------------------------------------------------


import org.drools.KnowledgeBase;

import org.ihtsdo.tk.api.cs.ChangeSetPolicy;
import org.ihtsdo.tk.api.cs.ChangeSetWriterThreading;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.api.cs.I_WriteChangeSet;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.tapi.AllowDataCheckSuppression;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.SuppressDataChecks;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;

import org.ihtsdo.lucene.SearchResult;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.changeset.ChangeSetGeneratorBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

public interface I_TermFactory {
   void addChangeSetReader(I_ReadChangeSet reader);

   void addChangeSetWriter(String key, ChangeSetGeneratorBI writer);

   void addToWatchList(I_GetConceptData c);

   void addUncommitted(I_ExtendByRef extension);

   void addUncommitted(I_GetConceptData concept);

   void addUncommittedNoChecks(I_ExtendByRef extension);

   void addUncommittedNoChecks(I_GetConceptData concept);

   /**
    * Delete any uncommitted changes.
    *
    * @throws IOException
    */
   void cancel() throws IOException;

   void cancelTransaction() throws IOException;

   /**
    * Close the database. Called on quit.
    *
    * @throws IOException
    */
   public void close() throws IOException;

   void closeChangeSets() throws IOException;

   public boolean commit() throws Exception;

   public void commit(ChangeSetPolicy changeSetPolicy, ChangeSetWriterThreading changeSetWriterThreading)
           throws Exception;

   void commitTransaction() throws IOException;

   long convertToThickVersion(int version);

   long convertToThickVersion(String dateStr) throws ParseException;

   int convertToThinVersion(long time);

   int convertToThinVersion(String dateStr) throws java.text.ParseException;

   SearchResult doLuceneSearch(String query) throws IOException, ParseException;

   void forget(I_ConceptAttributeVersioned attr) throws IOException;

   void forget(I_DescriptionVersioned desc) throws IOException;

   void forget(I_ExtendByRef ext) throws IOException;

   void forget(I_GetConceptData concept) throws IOException;

   void forget(I_RelVersioned rel) throws IOException;

   void iterateConcepts(I_ProcessConcepts procesor) throws Exception;

   /**
    * @deprecated iterateConcepts instead
    */
   void iterateDescriptions(I_ProcessDescriptions processor) throws Exception;

   /**
    * @deprecated iterateConcepts instead
    */
   void iterateExtByRefs(I_ProcessExtByRef processor) throws Exception;

   void loadFromDirectory(File dataDir, String encoding) throws Exception;

   void loadFromSingleJar(String jarFile, String dataPrefix) throws Exception;

   public void loadIsaCacheFromFile() throws Exception;

   public TransferHandler makeTerminologyTransferHandler(JComponent thisComponent);

   public List<UUID> nativeToUuid(int nid) throws IOException;

   void newAceFrame(I_ConfigAceFrame frameConfig) throws Exception;

   I_ConfigAceFrame newAceFrameConfig() throws TerminologyException, IOException;

   /**
    * @param displayInViewer If true, the activity will be lodged in the
    *            activity viewer window
    */
   I_ShowActivity newActivityPanel(boolean displayInViewer, I_ConfigAceFrame aceFrameConfig,
                                   String firstUpperInfo, boolean showStop);

   I_ReadChangeSet newBinaryChangeSetReader(File changeSetFile) throws IOException;

   I_WriteChangeSet newBinaryChangeSetWriter(File changeSetFile) throws IOException;

   I_GetConceptData newConcept(UUID newConceptId, boolean defined, I_ConfigAceFrame aceFrameConfig)
           throws TerminologyException, IOException;

   I_GetConceptData newConcept(UUID conceptUuid, boolean isDefined, I_ConfigAceFrame aceConfig, int statusNid)
           throws TerminologyException, IOException;

   I_GetConceptData newConcept(UUID conceptUuid, boolean isDefined, I_ConfigAceFrame aceConfig,
                               int statusNid, long time)
           throws TerminologyException, IOException;

   /**
    *
    * @return a new concept attribute part with all content uninitialized.
    */
   I_ConceptAttributePart newConceptAttributePart();

   /**
    *
    * @param newDescriptionId
    * @param concept
    * @param lang
    * @param text
    * @param descType
    * @param aceFrameConfig
    * @return
    * @throws TerminologyException
    * @throws IOException
    * @deprecated use alternate newDescription method that uses I_GetConceptData instead of I_ConceptualizeLocally
    */
   @Deprecated
   I_DescriptionVersioned newDescription(UUID newDescriptionId, I_GetConceptData concept, String lang,
           String text, I_ConceptualizeLocally descType, I_ConfigAceFrame aceFrameConfig)
           throws TerminologyException, IOException;

   I_DescriptionVersioned newDescription(UUID newDescriptionId, I_GetConceptData concept, String lang,
           String text, I_GetConceptData descType, I_ConfigAceFrame aceFrameConfig)
           throws TerminologyException, IOException;

   public I_DescriptionVersioned newDescription(UUID descUuid, I_GetConceptData concept, String string,
           String description, I_GetConceptData descriptionType, I_ConfigAceFrame activeAceFrameConfig,
           int statusNid)
           throws TerminologyException, IOException;

   I_DescriptionVersioned newDescription(UUID newDescriptionId, I_GetConceptData concept, String lang,
           String text, I_GetConceptData descType, I_ConfigAceFrame aceFrameConfig, I_GetConceptData status,
           long effectiveDate)
           throws TerminologyException, IOException;

   /**
    *
    * @return a new description part with all content uninitialized.
    */
   I_DescriptionPart newDescriptionPart();


   I_ExtendByRef newExtension(int refsetId, UUID memberPrimUuid, int componentId,
                              Class<? extends I_ExtendByRefPart> partType)
           throws IOException;

   I_ExtendByRef newExtension(int refsetId, UUID memberPrimUuid, int componentId, int typeId)
           throws IOException;

   /**
    * @throws IOException
    * @deprecated Use {@link #newExtension(int, int, int, int)} using {@link AllowDataCheckSuppression} and
    *             {@link SuppressDataChecks} annotations.
    */
   @Deprecated
   I_ExtendByRef newExtensionNoChecks(int refsetId, int memberId, int componentId, int typeId)
           throws IOException;

   /**
    * Create a new concrete extension part.
    * <p>
    * eg. newExtensionPart(I_ExtendByRefPartCid.class)
    *
    * @param <T> A sub-type of {@link I_ExtendByRefPart}.
    * @param t The interface to be instantiated.
    * @return A new strongly typed extension part which is assignable from T.
    */
   <T extends I_ExtendByRefPart> T newExtensionPart(Class<T> t);

   I_ImageVersioned newImage(UUID imageUuid, int conceptNid, int typeNid, byte[] image,
                             String textDescription, String format, I_ConfigAceFrame aceConfig)
           throws IOException, TerminologyException;

   I_IntList newIntList();

   I_IntSet newIntSet();

   PathBI newPath(Set<PositionBI> origins, I_GetConceptData pathConcept)
           throws TerminologyException, IOException;

   PathBI newPath(Collection<? extends PositionBI> positionSet, I_GetConceptData pathConcept,
                  I_ConfigAceFrame commitConfig)
           throws TerminologyException, IOException;

   PositionBI newPosition(PathBI path, long time) throws TerminologyException, IOException;

   /**
    *
    * @return a new relationship part with all content uninitialized.
    */
   I_RelPart newRelPart();

   /**
    * Uses the configuration to set default values for the relationship, and
    * uses the currently selected concept in the hierarchy viewer as the
    * relationship destination.
    *
    * @param newRelUid
    * @param concept
    * @return
    * @throws TerminologyException
    * @throws IOException
    */
   I_RelVersioned newRelationship(UUID newRelUid, I_GetConceptData concept, I_ConfigAceFrame aceFrameConfig)
           throws TerminologyException, IOException;

   /**
    * New relationship that <em>DOES NOT</em> use the default values set by
    * the configuration.
    *
    * @param newRelUid
    * @param concept
    * @param relType
    * @param relDestination
    * @param relCharacteristic
    * @param relRefinability
    * @param relGroup
    * @return
    * @throws TerminologyException
    * @throws IOException
    */
   I_RelVersioned newRelationship(UUID newRelUid, I_GetConceptData concept, I_GetConceptData relType,
                                  I_GetConceptData relDestination, I_GetConceptData relCharacteristic,
                                  I_GetConceptData relRefinability, I_GetConceptData relStatus, int relGroup,
                                  I_ConfigAceFrame aceFrameConfig)
           throws TerminologyException, IOException;

   I_RelVersioned newRelationship(UUID relUuid, I_GetConceptData concept, I_GetConceptData concept2,
                                  I_GetConceptData concept3, I_GetConceptData concept4,
                                  I_GetConceptData concept5, I_GetConceptData concept6, int group,
                                  I_ConfigAceFrame importConfig, long effectiveDate)
           throws IOException, TerminologyException;

   /**
    * <b>newRelationshipNoCheck DOESNOT use the configuration to set default values!</b>
    * newRelationshipNoCheck provides direct access for classifier results write-back.<br>
    *
    * @param newRelUid
    * @param concept
    * @return
    * @throws TerminologyException
    * @throws IOException
    */
   public I_RelVersioned newRelationshipNoCheck(UUID newRelUid, I_GetConceptData concept, int relTypeNid,
           int c2Nid, int relCharacteristicNid, int relRefinabilityNid, int relStatusNid, int group,
           int pathNid, long effectiveDate)
           throws TerminologyException, IOException;

   public I_RelVersioned newRelationshipNoCheck(UUID newRelUid, I_GetConceptData concept, int relTypeNid,
           int c2Nid, int relCharacteristicNid, int relRefinabilityNid, int group, int relStatusNid,
           int authorNid, int pathNid, long effectiveDate)
           throws TerminologyException, IOException;

   I_ReadChangeSet newWfHxLuceneChangeSetReader(File changeSetFile) throws IOException;

   public UUID nidToUuid(int nid) throws IOException;

   public boolean pathExists(int pathConceptId) throws TerminologyException, IOException;

   public boolean pathExistsFast(int pathConceptId) throws TerminologyException, IOException;

   public void persistIsaCache() throws Exception;

   void removeChangeSetReader(I_ReadChangeSet reader);

   void removeChangeSetWriter(String key);

   /**
    * Removes an extesion from the extension cache and rolls back pending
    * transactions. We need to do this to keep
    * the caches and transactions in synch.
    *
    * @param memberId The extension to remove from cache.
    * @throws IOException If an exception occurs.
    */
   void removeFromCacheAndRollbackTransaction(int memberId) throws IOException;

   void removeFromWatchList(I_GetConceptData c);

   public void resetViewPositions();

   /**
    * Resume writing of changes to change sets.
    * Typical usage is to call this methods after completion
    * of importing change sets, so than user changes get
    * properly recorded.
    *
    */
   void resumeChangeSetWriters();

   public void searchConcepts(I_TrackContinuation iTrackContinuation, I_RepresentIdSet matches,
                              CountDownLatch conceptLatch, List<I_TestSearchResults> criterion,
                              I_ConfigAceFrame differenceSearchConfig)
           throws IOException, ParseException;

   void startTransaction() throws IOException;

   /**
    * Turn off the writing of changes to change sets.
    * Typical usage is to call this method before importing
    * change sets so that the changes don't get duplicated.
    *
    */
   void suspendChangeSetWriters();

   int uuidToNative(Collection<UUID> uids) throws TerminologyException, IOException;

   int uuidToNative(UUID... uid) throws TerminologyException, IOException;

   void writeId(I_Identify versioned) throws IOException;

   /**
    * Create or modify a path
    *
    * @param p
    * @throws IOException
    */
   public void writePath(PathBI p, I_ConfigAceFrame config) throws IOException;

   /**
    * Add or update an origin position to a path
    */
   public void writePathOrigin(PathBI path, PositionBI origin, I_ConfigAceFrame config) throws IOException;

   //~--- get methods ---------------------------------------------------------

   I_ConfigAceFrame getActiveAceFrameConfig() throws TerminologyException, IOException;

   List<? extends I_ExtendByRef> getAllExtensionsForComponent(int componentId) throws IOException;

   List<? extends I_ExtendByRef> getAllExtensionsForComponent(int componentId, boolean addUncommitted)
           throws IOException;

   public int getAuthorNid();
   
   I_Identify getAuthorityId() throws IOException;

   public int getCachePercent();

   public long getCacheSize();

   Collection<I_ReadChangeSet> getChangeSetReaders();

   Collection<I_WriteChangeSet> getChangeSetWriters();

   /**
    * Method to call prior to commit that will list all commit failures that
    * will be encountered.
    * Useful for checking for errors prior to commit performed by a workflow
    * process.
    *
    * @return Data Constraint failures that would be encountered if <code>commit()</code> is called.
    */
   List<AlertToDataConstraintFailure> getCommitErrorsAndWarnings();

   public Object getComponent(int nid) throws TerminologyException, IOException;

   I_GetConceptData getConcept(Collection<UUID> ids) throws TerminologyException, IOException;

   I_GetConceptData getConcept(int nid) throws TerminologyException, IOException;

   /**
    * Find concepts with a matching textual identifier where the identifier
    * scheme/type is unknown.
    * This may result in multiple matches.
    *
    * @param conceptId Any textual id, for instance a SNOMED CT id
    * @throws TerminologyException if no suitable concepts are located
    */
   Set<I_GetConceptData> getConcept(String conceptId)
           throws TerminologyException, ParseException, IOException;
   
   /**
    * Find concepts with a matching textual identifier where the identifier
    * scheme/type is unknown.
    * This may result in multiple matches.
    *
    * @param conceptId Any textual id, for instance a SNOMED CT id
    * @throws TerminologyException if no suitable concepts are located
    */
   Set<ConceptChronicleBI> getConceptChronicle(String conceptId)
           throws TerminologyException, java.text.ParseException, IOException;

   I_GetConceptData getConcept(UUID... ids) throws TerminologyException, IOException;

   /**
    * Find a concept using a textual identifier from a known identifier scheme
    * (it is known to be a UUID or an SCTID, etc)
    *
    * @param conceptId Any textual id, for instance a SNOMED CT ID or a UUID
    * @param sourceId The native id of the source scheme concept, eg
    *            {@link ArchitectonicAuxiliary.Concept.SNOMED_INT_ID}
    * @throws TerminologyException if a suitable concept is not located
    */
   I_GetConceptData getConcept(String conceptId, int sourceId)
           throws TerminologyException, ParseException, IOException;

   /**
    * Count of the number of concepts in the database.
    * The count may not be accurate in the face of concurrent update operations
    * in the database
    *
    * @return
    * @throws IOException
    */
   int getConceptCount() throws IOException;

   public I_GetConceptData getConceptForNid(int componentNid) throws IOException;

   public Iterator<I_GetConceptData> getConceptIterator() throws IOException;

   /**
    *
    * @return a mutable bit set, with all concept identifiers set to true.
    * @throws IOException
    */
   public I_RepresentIdSet getConceptNidSet() throws IOException;

   /**
    *
    * @return a set of all the concept native identifiers in the database.
    * @throws IOException
    */
   public I_IntSet getConceptNids() throws IOException;

   /**
    * Gets a description given a description ID
    *
    * @param dNid
    * @return
    * @throws TerminologyException
    * @throws ParseException
    * @throws IOException
    */
   I_DescriptionVersioned getDescription(int dNid) throws TerminologyException, IOException;

   /**
    * Gets a description given a description native ID and a concept native ID
    *
    * @param dnid description native ID
    * @param cnid concept native ID
    * @return description matching the description and concept IDs
    * @throws TerminologyException if the description could not be found
    * @throws IOException
    */
   I_DescriptionVersioned getDescription(int dNid, int cNid) throws TerminologyException, IOException;

   /**
    *
    * @return a mutable bit set, with all description identifiers set to true.
    * @throws IOException
    */
   public I_RepresentIdSet getDescriptionIdSet() throws IOException;

   /**
    * @deprecated iterateConcepts instead
    */
   Iterator<I_DescriptionVersioned> getDescriptionIterator() throws IOException;

   /**
    * Use of this call is discouraged for routine use. It is provided to allow
    * more efficiency for some types of
    * operations such as writing the results of a large computation such as a
    * classification or a refset generation.
    * It should not be used to bypass the transactional model for manually
    * generated changes.
    *
    * @return
    */
   I_WriteDirectToDb getDirectInterface();

   LogWithAlerts getEditLog();

   /**
    *
    * @return a bit set, sized to hold all current identifiers, all set to
    *         false.
    * @throws IOException
    */
   public I_RepresentIdSet getEmptyIdSet() throws IOException;

   I_ExtendByRef getExtension(int memberId) throws IOException;

   I_Identify getId(Collection<UUID> uids) throws IOException;

   I_Identify getId(int nid) throws IOException;

   I_Identify getId(UUID uid) throws IOException;

   /**
    *
    * @return a bit set, sized to hold all current identifiers, members of ids
    *         set to true.
    * @throws IOException
    */
   public I_RepresentIdSet getIdSetFromIntCollection(Collection<Integer> ids) throws IOException;

   /**
    *
    * @return a bit set, sized to hold all current identifiers, members of ids
    *         set to true.
    * @throws IOException
    */
   public I_RepresentIdSet getIdSetfromTermCollection(Collection<? extends I_AmTermComponent> components)
           throws IOException;

   public I_ImageVersioned getImage(int parseInt) throws IOException;

   public I_ImageVersioned getImage(UUID fromString) throws IOException;

   public HashMap<Integer, KnowledgeBase> getKnowledgeBaseCache();
   
   PathBI getPath(Collection<UUID> uids) throws TerminologyException, IOException;

   PathBI getPath(int nid) throws TerminologyException, IOException;

   PathBI getPath(UUID... ids) throws TerminologyException, IOException;

   public List<? extends PathBI> getPathChildren(int nid) throws TerminologyException;

   List<PathBI> getPaths() throws Exception;

   I_Identify getPreviousAuthorityId() throws IOException;

   /**
    * Return a map of all properties in the database. The returned map is
    * unmodifiable. To
    * set properties, use the <code>setProperty</code> method.
    *
    * @return an unmodifable map of the properties.
    * @throws IOException
    */
   Map<String, String> getProperties() throws IOException;

   String getProperty(String key) throws IOException;

   /**
    *
    * @return a read-only bit set, with all concept identifiers set to true.
    * @throws IOException
    */
   public I_RepresentIdSet getReadOnlyConceptIdSet() throws IOException;

   Collection<? extends I_ExtendByRef> getRefsetExtensionMembers(int refsetId) throws IOException;

   List<? extends I_ExtendByRef> getRefsetExtensionsForComponent(int refsetNid, int nid) throws IOException;

   I_RelVersioned getRelationship(int rNid) throws IOException;

   /**
    *
    * @return a mutable bit set, with all relationship identifiers set to true.
    * @throws IOException
    */
   public I_RepresentIdSet getRelationshipIdSet() throws IOException;

   String getStats() throws IOException;

   I_HandleSubversion getSvnHandler();

   public List<TimePathId> getTimePathList() throws Exception;

   boolean getTransactional();

   Collection<UUID> getUids(int nid) throws TerminologyException, IOException;

   /**
    *
    * @return An unmodifiable set of uncommitted items.
    */
   Set<? extends I_Transact> getUncommitted();

   boolean hasConcept(int conceptId) throws IOException;

   boolean hasDescription(int descId, int conceptId) throws IOException;

   boolean hasExtension(int memberId) throws IOException;

   boolean hasId(Collection<UUID> uids) throws IOException;

   boolean hasId(UUID uid) throws IOException;

   boolean hasImage(int imageId) throws IOException;

   boolean hasPath(int nativeId) throws IOException;

   boolean hasRel(int relId, int conceptId) throws IOException;

   public boolean isCheckCommitDataEnabled();

   public boolean isCheckCreationDataEnabled();

   //~--- set methods ---------------------------------------------------------

   void setActiveAceFrameConfig(I_ConfigAceFrame activeAceFrameConfig)
           throws TerminologyException, IOException;

   public void setCachePercent(String cachePercent);

   public void setCacheSize(String cacheSize);

   public void setCheckCommitDataEnabled(boolean enabled);

   public void setCheckCreationDataEnabled(boolean enabled);

   public void setKnowledgeBaseCache(HashMap<Integer, KnowledgeBase> kbCache);

   void setProperty(String key, String value) throws IOException;
}
