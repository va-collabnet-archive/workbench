package org.dwfa.ace.api;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.tk.api.PathBI;

public interface I_HelpRefsets extends I_HelpLineage {

	/**
	 * Get the latest, current concept extension part for the FIRST extension
	 * matching a specific refset.
	 * 
	 * @param refsetId int
	 * @param conceptId int
	 * @return I_ExtendByRefPartCid with a status of current.
	 * 
	 * @throws Exception if cannot get all extension for a concept id..
	 */
	public I_ExtendByRefPartCid getFirstCurrentRefsetExtension(
			int refsetId, int conceptId) throws Exception;

	/**
	 * Obtain all current extensions (latest part only) for a particular refset
	 * that exist on a
	 * specific concept.
	 * 
	 * This method is strongly typed. The caller must provide the actual type of
	 * the refset.
	 * 
	 * @param <T> the strong/concrete type of the refset extension
	 * @param refsetId Only returns extensions matching this reference set
	 * @param conceptId Only returns extensions that exists on this concept
	 * @return All matching refset extension (latest version parts only)
	 * @throws Exception if unable to complete (never returns null)
	 * @throws ClassCastException if a matching refset extension is not of type
	 *             T
	 */
	public <T extends I_ExtendByRefPart> List<T> getAllCurrentRefsetExtensions(
			int refsetId, int conceptId) throws Exception;

	/**
	 * @param refsetId Only extensions for this refset will be evaluated
	 * @param conceptId The concept to obtain extensions from
	 * @param extProps The fields (being the name of the bean property) and the
	 *            values to be validated
	 * @return
	 * @throws Exception Unable to complete
	 */
	public boolean hasRefsetExtension(int refsetId, int conceptId,
			final RefsetPropertyMap extProps) throws Exception;

	public I_ExtendByRef getRefsetExtension(int refsetId,
			int conceptId, final RefsetPropertyMap extProps) throws Exception;

	public boolean hasCurrentRefsetExtension(int refsetId, int conceptId,
			final RefsetPropertyMap extProps) throws Exception;

	/**
	 * Add a concept to a refset
	 * 
	 * @param refsetId The subject refset
	 * @param conceptId The concept to be added
	 * @param memberTypeId The value of the concept extension to be added to the
	 *            new member concept.
	 * @param checkNotExists Is true, will only execute if the extension does
	 *            not already exist.
	 */
	public boolean newRefsetExtension(int refsetId, int componentId, REFSET_TYPES type,
			RefsetPropertyMap propMap, I_ConfigAceFrame config) throws Exception;
	
	public <T extends I_ExtendByRefPart> I_ExtendByRef getOrCreateRefsetExtension(
			int refsetId, int componentId, REFSET_TYPES type,
			RefsetPropertyMap extProps, UUID memberUuid) throws Exception;

	public  <T extends I_ExtendByRefPart> I_ExtendByRef makeWfMetadataMemberAndSetup(
			int refsetId, int conceptNid, REFSET_TYPES str, 
			RefsetPropertyMap propMap, UUID randomUUID) throws IOException;


	/**
	 * Remove a concept from a refset
	 * 
	 * @param refsetId The subject refset
	 * @param conceptId The concept to be removed
	 * @param memberTypeId The value of the concept extension to be removed (the
	 *            membership type).
	 */
	public boolean retireRefsetExtension(int refsetId, int conceptId,
			final RefsetPropertyMap extProps) throws Exception;
	public boolean retireRefsetStrExtension(int refsetId, int conceptId,
			final RefsetPropertyMap extProps) throws Exception;
	
	public void setEditPaths(PathBI... editPaths);

	public boolean hasPurpose(int refsetId, int purposeId) throws Exception;

	public boolean hasPurpose(int refsetId,
			I_ConceptualizeUniversally purposeConcept) throws Exception;

	public Set<? extends I_GetConceptData> getCommentsRefsetForRefset(
			I_GetConceptData refsetIdentityConcept, I_ConfigAceFrame config)
			throws IOException, TerminologyException;

	public Set<? extends I_GetConceptData> getMarkedParentRefsetForRefset(
			I_GetConceptData refsetIdentityConcept, I_ConfigAceFrame config)
			throws IOException, TerminologyException;

	public Set<? extends I_GetConceptData> getPromotionRefsetForRefset(
			I_GetConceptData refsetIdentityConcept, I_ConfigAceFrame config)
			throws IOException, TerminologyException;

	public Set<? extends I_GetConceptData> getSpecificationRefsetForRefset(
			I_GetConceptData refsetIdentityConcept, I_ConfigAceFrame config)
			throws IOException, TerminologyException;
	
	public Set<? extends I_GetConceptData> getComputeTimeRefsetForRefset(I_GetConceptData refsetToPromote, I_ConfigAceFrame config) throws IOException, TerminologyException;
	
	public Set<? extends I_GetConceptData> getEditTimeRefsetForRefset(I_GetConceptData refsetToPromote, I_ConfigAceFrame config) throws IOException, TerminologyException;
	
	public Set<Integer> getMemberRefsets() throws Exception;

	public List<Integer> getSpecificationRefsets() throws Exception;

	public I_GetConceptData getMemberSetConcept(int refsetId) throws Exception;

	public int getExcludeMembersRefset(int specRefsetConceptId);

	public List<Integer> getChildrenOfConcept(int conceptId) throws IOException,
			Exception;

	public boolean isAutocommitActive();

	public void setAutocommitActive(boolean autocommitActive);

    

}