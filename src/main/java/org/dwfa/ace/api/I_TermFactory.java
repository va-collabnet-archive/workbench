package org.dwfa.ace.api;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;

public interface I_TermFactory {

	public I_GetConceptData newConcept(UUID newConceptId, boolean defined, 
			I_ConfigAceFrame aceFrameConfig) throws TerminologyException, IOException;
	
	public I_GetConceptData getConcept(Collection<UUID> ids) throws TerminologyException, IOException;
	public I_GetConceptData getConcept(UUID[] ids) throws TerminologyException, IOException;

	public I_DescriptionVersioned newDescription(UUID newDescriptionId, I_GetConceptData concept,
			String lang, String text, I_ConceptualizeLocally descType, 
			I_ConfigAceFrame aceFrameConfig) throws TerminologyException, IOException;
	
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
	
	public I_Position newPosition(I_Path path, int version);
	
	public I_IntSet newIntSet();
	
	public void forget(I_GetConceptData concept);
	public void forget(I_DescriptionVersioned desc);
	public void forget(I_RelVersioned rel);
	
	public void addUncommitted(I_GetConceptData concept);
}
