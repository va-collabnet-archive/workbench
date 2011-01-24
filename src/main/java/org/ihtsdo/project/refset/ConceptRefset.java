package org.ihtsdo.project.refset;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class ConceptRefset {

	protected I_GetConceptData refsetConcept;
	protected String refsetName;
	protected int refsetId;
	protected I_TermFactory termFactory;

	protected static Set<? extends I_GetConceptData> getSourceRelTarget(I_GetConceptData refsetIdentityConcept, I_ConfigAceFrame config,
			int refsetIdentityNid) throws IOException, TerminologyException {
		I_TermFactory tf = Terms.get();
		I_IntSet allowedTypes = tf.newIntSet();
		allowedTypes.add(refsetIdentityNid);
		Set<? extends I_GetConceptData> matchingConcepts = refsetIdentityConcept.getSourceRelTargets(
				config.getAllowedStatus(),
				allowedTypes, config.getViewPositionSetReadOnly(), 
				config.getPrecedence(), config.getConflictResolutionStrategy());
		return matchingConcepts;
	}

	public ConceptRefset() {
		super();
	}

	public I_GetConceptData getRefsetPurposeConcept() {
		try {
			I_GetConceptData refsetPurposeRel =
				termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_PURPOSE_REL.getUids());
			I_GetConceptData refsetConcept = getRefsetConcept();
			if (refsetConcept == null) {
				return null;
			}

			return getLatestSourceRelationshipTarget(refsetConcept, refsetPurposeRel);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public I_GetConceptData getRefsetTypeConcept() {
		try {
			I_GetConceptData refsetTypeRel =
				termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_TYPE_REL.getUids());
			I_GetConceptData refsetConcept = getRefsetConcept();
			if (refsetConcept == null) {
				return null;
			}

			return getLatestSourceRelationshipTarget(refsetConcept, refsetTypeRel);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Gets the latest specified relationship's target.
	 * 
	 * @param relationshipType
	 * @return
	 * @throws Exception
	 */
	public I_GetConceptData getLatestSourceRelationshipTarget(I_GetConceptData concept, I_GetConceptData relationshipType)
	throws Exception {
		//TODO: move config to parameter
		I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
		I_GetConceptData latestTarget = null;
		long latestVersion = Integer.MIN_VALUE;

		I_IntSet allowedTypes = Terms.get().newIntSet();
		allowedTypes.add(relationshipType.getConceptNid());

		List<? extends I_RelTuple> relationships = concept.getSourceRelTuples(null, allowedTypes, null, 
		config.getPrecedence(), config.getConflictResolutionStrategy());
		for (I_RelTuple rel : relationships) {
			if (rel.getTime() > latestVersion) {
				latestVersion = rel.getTime();
				latestTarget = Terms.get().getConcept(rel.getC2Id());
			}
		}

		return latestTarget;
	}

	/**
	 * Gets the latest specified relationship's target.
	 * 
	 * @param relationshipType
	 * @return
	 * @throws Exception
	 */
	public I_GetConceptData getLatestDestinationRelationshipSource(I_GetConceptData concept,
			I_GetConceptData relationshipType) throws Exception {
		//TODO: move config to parameter
		I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
		I_GetConceptData latestSource = null;
		long latestVersion = Integer.MIN_VALUE;

		I_IntSet allowedTypes = Terms.get().newIntSet();
		allowedTypes.add(relationshipType.getConceptNid());

		List<? extends I_RelTuple> relationships = concept.getDestRelTuples(null, allowedTypes, null, 
				config.getPrecedence(), config.getConflictResolutionStrategy());
		for (I_RelTuple rel : relationships) {
			if (rel.getTime() > latestVersion) {
				latestVersion = rel.getTime();
				latestSource = Terms.get().getConcept(rel.getC1Id());
			}
		}

		return latestSource;
	}

	/**
	 * Gets the latest specified relationship's target.
	 * 
	 * @param relationshipType
	 * @return
	 * @throws Exception
	 */
	public I_RelTuple getLatestRelationship(I_GetConceptData concept, I_GetConceptData relationshipType)
	throws Exception {
		//TODO: move config to parameter
		I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
		I_RelTuple latestRel = null;
		long latestVersion = Integer.MIN_VALUE;

		I_IntSet allowedTypes = Terms.get().newIntSet();
		allowedTypes.add(relationshipType.getConceptNid());

		List<? extends I_RelTuple> relationships = concept.getSourceRelTuples(null, allowedTypes, null, 
				config.getPrecedence(), config.getConflictResolutionStrategy());
		for (I_RelTuple rel : relationships) {
			if (rel.getTime() > latestVersion) {
				latestVersion = rel.getTime();
				latestRel = rel;
			}
		}

		return latestRel;
	}

	public I_GetConceptData getRefsetConcept() {
		return refsetConcept;
	}

	public void setRefsetConcept(I_GetConceptData refsetConcept) {
		this.refsetConcept = refsetConcept;
	}

	public int getRefsetId() {
		return refsetId;
	}

	public void setRefsetId(int refsetId) {
		this.refsetId = refsetId;
	}

	public String getRefsetName() {
		return refsetName;
	}

	public void setRefsetName(String refsetName) {
		this.refsetName = refsetName;
	}
}