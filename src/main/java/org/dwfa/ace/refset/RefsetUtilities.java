package org.dwfa.ace.refset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.spec.ConceptSpec;

public abstract class RefsetUtilities {
	
	protected I_GetConceptData pathConcept;
	
	private I_TermFactory termFactory;
	
	protected int retiredConceptId;
	protected int currentStatusId;
	protected int typeId;

	
	public int getInclusionTypeForRefset(I_ThinExtByRefVersioned part) {
		int typeId = 0;
		
		List<? extends I_ThinExtByRefPart> versions = part.getVersions();
		for (I_ThinExtByRefPart version : versions) {
			I_ThinExtByRefPartConcept temp = (I_ThinExtByRefPartConcept) version;
			typeId = temp.getConceptId();
		}

		return typeId;
	}


	public List<Integer> getChildrenOfConcept(int conceptId) throws IOException, Exception {

		List<Integer> children = new ArrayList<Integer>();
		
		I_GetConceptData concept = termFactory.getConcept(conceptId);
		/*
		 * Find all children
		 **/
		List<I_RelTuple> childrentuples = concept.getDestRelTuples(
				getIntSet(ArchitectonicAuxiliary.Concept.CURRENT, ArchitectonicAuxiliary.Concept.PENDING_MOVE), 
				getIntSet(ConceptConstants.SNOMED_IS_A), null, false);

		/*
		 * Iterate over children
		 **/
		for (I_RelTuple child : childrentuples) {
			
			List<I_ConceptAttributeTuple> atts = termFactory.getConcept(child.getC1Id()).getConceptAttributeTuples(getIntSet(ArchitectonicAuxiliary.Concept.CURRENT, ArchitectonicAuxiliary.Concept.PENDING_MOVE), null);
			if (atts.size()==1) {
				children.add(child.getC1Id());
			} 
		}
		return children;
	}
	
	public List<Integer> findAllowedRefsets() throws TerminologyException, IOException {
		
		List<Integer> allowedRefsets = new ArrayList<Integer>();
		
		termFactory = LocalVersionedTerminology.get();
		
		I_IntSet status = termFactory.newIntSet();
		status.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()).getConceptId());

		I_IntSet is_a = termFactory.newIntSet();
		is_a.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()).getConceptId());

		I_GetConceptData refsetRoot = termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_IDENTITY.getUids());

		Set<I_GetConceptData> refsetChildren = refsetRoot.getDestRelOrigins(status,is_a, null, false);
		for (I_GetConceptData refsetConcept : refsetChildren) {
			Set<I_GetConceptData> purposeConcepts = new HashSet<I_GetConceptData>();

			List<I_RelVersioned> rels = refsetConcept.getSourceRels();
			for (I_RelVersioned rel: rels) {
				List<I_RelTuple> tuples = rel.getTuples();
				for (I_RelTuple tuple : tuples) {
					if (tuple.getStatusId()==termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()).getConceptId() && 
							tuple.getRelTypeId()==termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_PURPOSE_REL.getUids()).getConceptId()) {

						purposeConcepts.add(termFactory.getConcept(tuple.getC2Id()));
					}
				}
			}

			if (purposeConcepts.size()==1) {

				if (purposeConcepts.iterator().next().getConceptId()==termFactory.getConcept(RefsetAuxiliary.Concept.INCLUSION_SPECIFICATION.getUids()).getConceptId()) {
					allowedRefsets.add(refsetConcept.getConceptId());
				} 
			} 
		}
		return allowedRefsets;
	}
	protected I_IntSet getIntSet(ArchitectonicAuxiliary.Concept... concepts) throws Exception {
		I_IntSet status = termFactory.newIntSet();

		for (ArchitectonicAuxiliary.Concept concept : concepts) {
			status.add(termFactory.getConcept(concept.getUids()).getConceptId());
		}
		assert status.getSetValues().length > 0: "getIntSet returns an empty set";
		return status;
	}
	
	protected I_IntSet getIntSet(ConceptSpec... concepts) throws Exception {
		I_IntSet status = termFactory.newIntSet();

		for (ConceptSpec concept : concepts) {
			status.add(concept.localize().getNid());
		}

		return status;
	}

	private <T> T assertOneOrNone(
			Collection<T> collection) {
		assert collection.size() <= 1 :
			"Exactly one element expected, encountered " + collection;

		if (collection.size()==1) {		
			return collection.iterator().next();
		} else {
			return null;
		}
	}

	private <T> T assertExactlyOne(
			Collection<T> collection) {
		assert collection.size() == 1 :
			"Exactly one element expected, encountered " + collection;

		return collection.iterator().next();
	}
	
	public void addToNestedSet(Map<Integer,Set<ConceptRefsetInclusionDetails>> nestedList, ConceptRefsetInclusionDetails conceptDetails, Integer refset) {
		Set<ConceptRefsetInclusionDetails> conceptsInRefset = nestedList.get(refset);
		if (conceptsInRefset==null) {
			conceptsInRefset = new HashSet<ConceptRefsetInclusionDetails>();
			nestedList.put(refset, conceptsInRefset);
		}
		conceptsInRefset.add(conceptDetails);
	}

	public I_GetConceptData getMemberSetConcept(int refsetId) throws Exception {
		I_IntSet currentIntSet = getIntSet(ArchitectonicAuxiliary.Concept.CURRENT);
		I_IntSet generatesRelIntSet = getIntSet(ConceptConstants.GENERATES_REL);

		I_GetConceptData memberSetSpecConcept = assertOneOrNone(termFactory.getConcept(refsetId).getSourceRelTargets(
				currentIntSet, 
				generatesRelIntSet, null, false));
		return memberSetSpecConcept;
	}
	
	/**
	 * Retires the latest version of a specified extension.
	 * @param extensionPart The extension to check.
	 * @throws Exception
	 */
	public void retireLatestExtension(I_ThinExtByRefVersioned extensionPart) throws Exception {

		if (extensionPart != null) {

			List<I_ThinExtByRefTuple> extensionParts = new ArrayList<I_ThinExtByRefTuple>();
			extensionPart.addTuples(getIntSet(ArchitectonicAuxiliary.Concept.CURRENT), null, extensionParts, true);

			if (extensionParts.size() > 0) {
				I_ThinExtByRefPart latestVersion = assertExactlyOne(extensionParts);

				I_ThinExtByRefPart clone = latestVersion.duplicatePart();
				clone.setStatus(retiredConceptId);
				clone.setVersion(Integer.MAX_VALUE);
				extensionPart.addVersion(clone);


				termFactory.addUncommitted(extensionPart);
			}
		}

	}

	/**
	 * Adds a particular concept to the member set.
	 * @param conceptId the concept id of the concept we wish to add to the member set.
	 * @param includeTypeConceptId 
	 * @throws Exception
	 */
	public void addToMemberSet(int conceptId, int includeTypeConceptId, int memberSetId) throws Exception {

		int memberId = termFactory.uuidToNativeWithGeneration(UUID.randomUUID(),
				ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(),
				termFactory.getPaths(), Integer.MAX_VALUE);

		I_ThinExtByRefVersioned newExtension =
			termFactory.newExtension(memberSetId, memberId, conceptId,typeId);

		I_ThinExtByRefPartConcept conceptExtension =
			termFactory.newConceptExtensionPart();


		conceptExtension.setPathId(pathConcept.getConceptId());
		conceptExtension.setStatus(currentStatusId);
		conceptExtension.setVersion(Integer.MAX_VALUE);
		conceptExtension.setConceptId(getMembershipType(includeTypeConceptId));

		newExtension.addVersion(conceptExtension);

		termFactory.addUncommitted(newExtension);    			

	}
	
	private int getMembershipType(int includeTypeConceptId) throws Exception {
		I_GetConceptData includeConcept = termFactory.getConcept(includeTypeConceptId);

		Set<I_GetConceptData> membershipTypes = includeConcept.getSourceRelTargets(
				getIntSet(ArchitectonicAuxiliary.Concept.CURRENT), getIntSet(ConceptConstants.CREATES_MEMBERSHIP_TYPE), null, false);

		return assertExactlyOne(membershipTypes).getConceptId();
	}


	public I_GetConceptData getPathConcept() {
		return pathConcept;
	}


	public void setPathConcept(I_GetConceptData pathConcept) {
		this.pathConcept = pathConcept;
	}
}
