package org.dwfa.ace.api;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Hits;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.api.cs.I_WriteChangeSet;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;

public interface I_TermFactory {

	public I_GetConceptData newConcept(UUID newConceptId, boolean defined, 
			I_ConfigAceFrame aceFrameConfig) throws TerminologyException, IOException;
	
	public I_ConfigAceFrame newAceFrameConfig() throws TerminologyException, IOException;
	public I_ConfigAceFrame getActiveAceFrameConfig() throws TerminologyException, IOException;
	public void setActiveAceFrameConfig(I_ConfigAceFrame activeAceFrameConfig) throws TerminologyException, IOException;
	
	
	public I_GetConceptData getConcept(Collection<UUID> ids) throws TerminologyException, IOException;
	public I_GetConceptData getConcept(UUID[] ids) throws TerminologyException, IOException;
	public I_GetConceptData getConcept(int nid) throws TerminologyException, IOException;
	
	public Collection<UUID> getUids(int nid) throws TerminologyException, IOException;

	public I_DescriptionVersioned newDescription(UUID newDescriptionId, I_GetConceptData concept,
			String lang, String text, I_ConceptualizeLocally descType, 
			I_ConfigAceFrame aceFrameConfig) throws TerminologyException, IOException;
	public I_DescriptionVersioned getDescription(int dnid) throws TerminologyException, IOException;
	
	/**
	 * Uses the configuration to set default values for the relationship, and uses the
	 * currently selected concept in the hierarchy viewer as the relationship destination.
	 * 
	 * @param newRelUid
	 * @param concept
	 * @return
	 * @throws TerminologyException
	 * @throws IOException
	 */
	public I_RelVersioned newRelationship(UUID newRelUid, I_GetConceptData concept, 
			I_ConfigAceFrame aceFrameConfig) throws TerminologyException, IOException;

	/**
	 * New relationship that <em>DOES NOT</em> use the default values 
	 * set by the configuration.
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
	public I_RelVersioned newRelationship(UUID newRelUid,
			I_GetConceptData concept, 
			I_GetConceptData relType,
			I_GetConceptData relDestination,
			I_GetConceptData relCharacteristic,
			I_GetConceptData relRefinability, 
			I_GetConceptData relStatus, int relGroup, 
			I_ConfigAceFrame aceFrameConfig)
			throws TerminologyException, IOException;
	
	public LogWithAlerts getEditLog();
	
	public I_Path getPath(Collection<UUID> uids) throws TerminologyException, IOException;
	
	public I_Path getPath(UUID[] ids) throws TerminologyException, IOException;
	
	public List<I_Path> getPaths() throws Exception;
	
	public I_Path newPath(Set<I_Position> origins, I_GetConceptData pathConcept) throws TerminologyException, IOException;
	
	public I_Position newPosition(I_Path path, int version) throws TerminologyException, IOException;
	
	public I_IntSet newIntSet();
	
	public void forget(I_GetConceptData concept);
	public void forget(I_DescriptionVersioned desc);
	public void forget(I_RelVersioned rel);
	
	public void addUncommitted(I_GetConceptData concept);
	public void commit() throws Exception;
	public void addChangeSetWriter(I_WriteChangeSet writer);
	public void removeChangeSetWriter(I_WriteChangeSet writer);
	public void closeChangeSets() throws IOException;
	public I_WriteChangeSet newBinaryChangeSetWriter(File changeSetFile) throws IOException;
	public I_ReadChangeSet newBinaryChangeSetReader(File changeSetFile) throws IOException;
	
	public void loadFromSingleJar(String jarFile, String dataPrefix) throws Exception;
	/**
	 * 
	 * @param args
	 * @throws Exception
	 * @deprecated use loadFromSingleJar
	 */
	public void loadFromMultipleJars(String[] args) throws Exception;
	
	public int uuidToNative(UUID uid) throws TerminologyException, IOException;

	public int uuidToNative(Collection<UUID> uids) throws TerminologyException,
			IOException;
	
	public int uuidToNativeWithGeneration(Collection<UUID> uids, int source,
			I_Path idPath, int version) throws TerminologyException,
			IOException;
	
	public int uuidToNativeWithGeneration(UUID uid, int source,
			Collection<I_Path> idPaths, int version)
			throws TerminologyException, IOException;
	
	public int uuidToNativeWithGeneration(UUID uid, int source, I_Path idPath,
			int version) throws TerminologyException, IOException;
	
	public void iterateDescriptions(I_ProcessDescriptions processor) throws Exception;
	public void iterateRelationships(I_ProcessRelationships processor) throws Exception;
	public void iterateConcepts(I_ProcessConcepts procesor) throws Exception;
	public void iterateConceptAttributes(I_ProcessConceptAttributes processor) throws Exception;
	public void iterateIds(I_ProcessIds processor) throws Exception;
	public void iterateImages(I_ProcessImages processor) throws Exception;
	public void iteratePaths(I_ProcessPaths processor) throws Exception;
	
	public Hits searchLicitWords(String query) throws IOException, ParseException;

	public Hits doLicitSearch(String query) throws IOException, ParseException;
	
	public Hits doLuceneSearch(String query) throws IOException, ParseException;
    
    public int convertToThinVersion(long time);
    public int convertToThinVersion(String dateStr) throws java.text.ParseException;
    public long convertToThickVersion(int version);

	public I_IntList newIntList();

   public I_IdVersioned getId(int nid) throws IOException;

   public void cancel();


}
