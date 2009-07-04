package org.dwfa.ace.api;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Hits;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.api.cs.I_WriteChangeSet;
import org.dwfa.ace.api.ebr.I_GetExtensionData;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptInt;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartLanguage;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartLanguageScoped;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartMeasurement;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;

public interface I_TermFactory {

    public String getProperty(String key) throws IOException;

    public void setProperty(String key, String value) throws IOException;

    /**
     * Return a map of all properties in the database. The returned map is unmodifiable. To
     * set properties, use the <code>setProperty</code> method.
     *
     * @return an unmodifable map of the properties.
     * @throws IOException
     */
    public Map<String, String> getProperties() throws IOException;

    public I_GetConceptData newConcept(UUID newConceptId, boolean defined, I_ConfigAceFrame aceFrameConfig)
            throws TerminologyException, IOException;

    public I_ConfigAceFrame newAceFrameConfig() throws TerminologyException, IOException;

    public void newAceFrame(I_ConfigAceFrame frameConfig) throws Exception;

    public I_ConfigAceFrame getActiveAceFrameConfig() throws TerminologyException, IOException;

    public void setActiveAceFrameConfig(I_ConfigAceFrame activeAceFrameConfig) throws TerminologyException, IOException;

    public I_GetConceptData getConcept(Collection<UUID> ids) throws TerminologyException, IOException;

    public I_GetConceptData getConcept(UUID[] ids) throws TerminologyException, IOException;

    public I_GetConceptData getConcept(int nid) throws TerminologyException, IOException;

    /**
     * Find a concept using a textual identifier from a known identifier scheme
     * (it is known to be a UUID or an SCTID, etc)
     *
     * @param conceptId Any textual id, for instance a SNOMED CT ID or a UUID
     * @param sourceId The native id of the source scheme concept, eg {@link ArchitectonicAuxiliary.Concept.SNOMED_INT_ID}
     * @throws TerminologyException if a suitable concept is not located
     */
    public I_GetConceptData getConcept(String conceptId, int sourceId) throws TerminologyException, ParseException, IOException;

    /**
     * Find concepts with a matching textual identifier where the identifier scheme/type is unknown.
     * This may result in multiple matches.
     *
     * @param conceptId Any textual id, for instance a SNOMED CT id
     * @throws TerminologyException if no suitable concepts are located
     */
    public Set<I_GetConceptData> getConcept(String conceptId) throws TerminologyException, ParseException, IOException;

    public Collection<UUID> getUids(int nid) throws TerminologyException, IOException;

    public I_DescriptionVersioned newDescription(UUID newDescriptionId, I_GetConceptData concept, String lang,
            String text, I_ConceptualizeLocally descType, I_ConfigAceFrame aceFrameConfig) throws TerminologyException,
                IOException;

    public I_DescriptionVersioned newDescription(UUID newDescriptionId, I_GetConceptData concept, String lang,
            String text, I_GetConceptData descType, I_ConfigAceFrame aceFrameConfig) throws TerminologyException,
                IOException;

    /**
     * Gets a description given a description native ID and a concept native ID
     *
     * @param dnid description native ID
     * @param cnid concept native ID
     * @return description matching the description and concept IDs
     * @throws TerminologyException if the description could not be found
     * @throws IOException
     */
    public I_DescriptionVersioned getDescription(int dnid, int cnid) throws TerminologyException, IOException;

   /**
    * Gets a description given a description ID
    *
    * @param descriptionId
    * @return
    * @throws TerminologyException
    * @throws ParseException
    * @throws IOException
    */
    I_DescriptionVersioned getDescription(String descriptionId) throws TerminologyException, ParseException, IOException;

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
    public I_RelVersioned newRelationship(UUID newRelUid, I_GetConceptData concept, I_ConfigAceFrame aceFrameConfig)
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
    public I_RelVersioned newRelationship(UUID newRelUid, I_GetConceptData concept, I_GetConceptData relType,
        I_GetConceptData relDestination, I_GetConceptData relCharacteristic, I_GetConceptData relRefinability,
        I_GetConceptData relStatus, int relGroup, I_ConfigAceFrame aceFrameConfig) throws TerminologyException,
            IOException;

    public LogWithAlerts getEditLog();

    public I_Path getPath(Collection<UUID> uids) throws TerminologyException, IOException;

    public I_Path getPath(UUID[] ids) throws TerminologyException, IOException;

    public List<I_Path> getPaths() throws Exception;

    public I_Path newPath(Set<I_Position> origins, I_GetConceptData pathConcept) throws TerminologyException,
            IOException;

    public I_Position newPosition(I_Path path, int version) throws TerminologyException, IOException;

    public I_IntSet newIntSet();

    public void forget(I_GetConceptData concept);

    public void forget(I_DescriptionVersioned desc);

    public void forget(I_RelVersioned rel);

    public void addUncommitted(I_GetConceptData concept);

    public void addUncommitted(I_ThinExtByRefVersioned extension);

    public void addUncommittedNoChecks(I_GetConceptData concept);

    public void addUncommittedNoChecks(I_ThinExtByRefVersioned extension);

    /**
     *
     * @return An unmodifiable set of uncommitted items.
     */
    public Set<I_Transact> getUncommitted();

    public void commit() throws Exception;

    public void addChangeSetWriter(I_WriteChangeSet writer);

    public void removeChangeSetWriter(I_WriteChangeSet writer);

    public Collection<I_WriteChangeSet> getChangeSetWriters();

    public void addChangeSetReader(I_ReadChangeSet reader);

    public void removeChangeSetReader(I_ReadChangeSet reader);

    public Collection<I_ReadChangeSet> getChangeSetReaders();

    public void closeChangeSets() throws IOException;

    public I_WriteChangeSet newBinaryChangeSetWriter(File changeSetFile) throws IOException;

    public I_ReadChangeSet newBinaryChangeSetReader(File changeSetFile) throws IOException;

    public void loadFromSingleJar(String jarFile, String dataPrefix) throws Exception;

    public void loadFromDirectory(File dataDir, String encoding) throws Exception;

    /**
     *
     * @param args
     * @throws Exception
     * @deprecated use loadFromSingleJar
     */
    public void loadFromMultipleJars(String[] args) throws Exception;

    public int uuidToNative(UUID uid) throws TerminologyException, IOException;

    public int uuidToNative(Collection<UUID> uids) throws TerminologyException, IOException;

    public int uuidToNativeWithGeneration(Collection<UUID> uids, int source, I_Path idPath, int version)
            throws TerminologyException, IOException;

    public int uuidToNativeWithGeneration(UUID uid, int source, Collection<I_Path> idPaths, int version)
            throws TerminologyException, IOException;

    public int uuidToNativeWithGeneration(UUID uid, int source, I_Path idPath, int version)
            throws TerminologyException, IOException;

    /**
     * @deprecated iterateConcepts instead
     */
    public void iterateDescriptions(I_ProcessDescriptions processor) throws Exception;

    /**
     * @deprecated iterateConcepts instead
     */
    public Iterator<I_DescriptionVersioned> getDescriptionIterator() throws IOException;

    /**
     * @deprecated iterateConcepts instead
     */
    public void iterateRelationships(I_ProcessRelationships processor) throws Exception;

    public void iterateConcepts(I_ProcessConcepts procesor) throws Exception;

    public Iterator<I_GetConceptData> getConceptIterator() throws IOException;

    /**
     * @deprecated iterateConcepts instead
     */
    public void iterateConceptAttributes(I_ProcessConceptAttributes processor) throws Exception;

    /**
     * @deprecated iterateConcepts instead
     */
    public void iterateExtByRefs(I_ProcessExtByRef processor) throws Exception;

    /**
     * @deprecated iterateConcepts instead
     */
    public void iterateIds(I_ProcessIds processor) throws Exception;

    /**
     * @deprecated iterateConcepts instead
     */
    public void iterateImages(I_ProcessImages processor) throws Exception;

    /**
     * @deprecated iterateConcepts instead
     */
    public void iteratePaths(I_ProcessPaths processor) throws Exception;

    public Hits doLuceneSearch(String query) throws IOException, ParseException;

    public int convertToThinVersion(long time);

    public int convertToThinVersion(String dateStr) throws java.text.ParseException;

    public long convertToThickVersion(int version);

    public I_IntList newIntList();

    public I_IdVersioned getId(int nid) throws TerminologyException, IOException;
    public I_IdVersioned getId(UUID uid) throws TerminologyException, IOException;
    public I_IdVersioned getId(Collection<UUID> uids) throws TerminologyException, IOException;
    public I_IdVersioned getAuthorityId() throws TerminologyException, IOException;
    public I_IdVersioned getPreviousAuthorityId() throws TerminologyException, IOException;

    /**
     * Delete any uncommitted changes.
     * @throws IOException
     */

    public void cancel() throws IOException;

    /**
     * Turn off the writing of changes to change sets.
     * Typical usage is to call this method before importing
     * change sets so that the changes don't get duplicated.
     *
     */
    public void suspendChangeSetWriters();

    /**
     * Resume writing of changes to change sets.
     * Typical usage is to call this methods after completion
     * of importing change sets, so than user changes get
     * properly recorded.
     *
     */
    public void resumeChangeSetWriters();

    /**
     *
     * @return a new description part with all content uninitialized.
     */
    public I_DescriptionPart newDescriptionPart();
    /**
     *
     * @return a new concept attribute part with all content uninitialized.
     */
    public I_ConceptAttributePart newConceptAttributePart();
    /**
     *
     * @return a new relationship part with all content uninitialized.
     */
    public I_RelPart newRelPart();

    // TODO We need a method call that will include the concept id...
    public I_GetExtensionData getExtensionWrapper(int memberId) throws IOException;

    // TODO We need a method call that will include the concept id...
    public I_ThinExtByRefVersioned getExtension(int memberId) throws IOException;

    public List<I_ThinExtByRefVersioned> getRefsetExtensionMembers(int refsetId) throws IOException;

    /**
     * @deprecated use getAllExtensionsForComponent
     */
    public List<I_GetExtensionData> getExtensionsForComponent(int componentId) throws IOException;

    // TODO We need a method call that will include the concept id...
    public List<I_ThinExtByRefVersioned> getAllExtensionsForComponent(int componentId) throws IOException;

    // TODO We need a method call that will include the concept id...
    public List<I_ThinExtByRefVersioned> getAllExtensionsForComponent(int componentId, boolean addUncommitted) throws IOException;

    // TODO getAllExtensionsForConcept() -- return all extensions of the concept, or any desc, rel, or ext of this concept...
    public I_ThinExtByRefVersioned newExtension(int refsetId, int memberId, int componentId, int typeId);
    public I_ThinExtByRefVersioned newExtensionNoChecks(int refsetId, int memberId, int componentId, int typeId);

    public I_ThinExtByRefPartBoolean newBooleanExtensionPart();
    public I_ThinExtByRefPartConcept newConceptExtensionPart();
    public I_ThinExtByRefPartConceptConcept newConceptConceptExtensionPart();
    public I_ThinExtByRefPartConceptConceptConcept newConceptConceptConceptExtensionPart();
    public I_ThinExtByRefPartConceptConceptString newConceptConceptStringExtensionPart();
    public I_ThinExtByRefPartConceptInt newConceptIntExtensionPart();
    public I_ThinExtByRefPartConceptString newConceptStringExtensionPart();
    public I_ThinExtByRefPartInteger newIntegerExtensionPart();
    public I_ThinExtByRefPartLanguage newLanguageExtensionPart();
    public I_ThinExtByRefPartLanguageScoped newLanguageScopedExtensionPart();
    public I_ThinExtByRefPartMeasurement newMeasurementExtensionPart();
    public I_ThinExtByRefPartString newStringExtensionPart();

    public String getStats() throws IOException;

    /**
     * Use of this call is discouraged for routine use. It is provided to allow more efficiency for some types of
     * operations such as writing the results of a large computation such as a classification or a refset generation.
     * It should not be used to bypass the transactional model for manually generated changes.
     *
     * @return
     */
    public I_WriteDirectToDb getDirectInterface();


    public boolean hasId(Collection<UUID> uids) throws IOException;
    public boolean hasId(UUID uid) throws IOException;
    public boolean hasImage(int imageId) throws IOException;
    public boolean hasPath(int nativeId) throws IOException;
    public boolean hasRel(int relId, int conceptId) throws IOException;
    public boolean hasDescription(int descId, int conceptId) throws IOException;
    public boolean hasConcept(int conceptId) throws IOException;
    public boolean hasExtension(int memberId) throws IOException;

    public boolean getTransactional();
    public void startTransaction() throws IOException;
    public void cancelTransaction() throws IOException;
    public void commitTransaction() throws IOException;

    /**
     * @param displayInViewer If true, the activity will be lodged in the activity viewer window
     */
    public I_ShowActivity newActivityPanel(boolean displayInViewer);

    public I_HandleSubversion getSvnHandler();
    
    /**
     * Count of the number of concepts in the database. 
     * The count may not be accurate in the face of concurrent update operations in the database
     * @return
     * @throws IOException
     */
    public int getConceptCount() throws IOException;


}
