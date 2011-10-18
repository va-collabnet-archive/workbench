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

public abstract class Refset {

	protected I_GetConceptData refsetConcept;
	protected String refsetName;
	protected int refsetId;
	protected I_TermFactory termFactory;

	protected static Set<? extends I_GetConceptData> getSourceRelTarget(I_GetConceptData refsetIdentityConcept, 
			I_ConfigAceFrame config,
			int relTypeNid) throws IOException, TerminologyException {
			    I_TermFactory tf = Terms.get();
			    I_IntSet allowedTypes = tf.newIntSet();
			    allowedTypes.add(relTypeNid);
//			    System.out.println("************* relTypeNid: " + relTypeNid);
			    //I_GetConceptData relConcept = tf.getConcept(relTypeNid);
//			    System.out.println("************* relConcept: " + relConcept.toString());
//			    System.out.println("************* allowedTypes: " + allowedTypes);
			    Set<? extends I_GetConceptData> matchingConcepts = refsetIdentityConcept.getSourceRelTargets(config.getAllowedStatus(),
			        allowedTypes, config.getViewPositionSetReadOnly(),config.getPrecedence(), config.getConflictResolutionStrategy());
//			    System.out.println("************* matchingConcepts: " + matchingConcepts.size());
			    return matchingConcepts;
			}

	public Refset() {
		super();
		this.termFactory=Terms.get();
	}

	public I_GetConceptData getRefsetPurposeConcept(I_ConfigAceFrame config) {
		try {
			I_GetConceptData refsetPurposeRel =
				termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_PURPOSE_REL.getUids());
			I_GetConceptData refsetConcept = getRefsetConcept();
			if (refsetConcept == null) {
				return null;
			}
	
			return getLatestSourceRelationshipTarget(refsetConcept, refsetPurposeRel, config);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public I_GetConceptData getRefsetTypeConcept(I_ConfigAceFrame config) {
		try {
			I_GetConceptData refsetTypeRel =
				termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_TYPE_REL.getUids());
			I_GetConceptData refsetConcept = getRefsetConcept();
			if (refsetConcept == null) {
				return null;
			}
			
			return getLatestSourceRelationshipTarget(refsetConcept, refsetTypeRel, config);
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
	public I_GetConceptData getLatestSourceRelationshipTarget(I_GetConceptData concept, 
			I_GetConceptData relationshipType,
			I_ConfigAceFrame config)
			throws Exception {
			
				I_GetConceptData latestTarget = null;
				long latestVersion = Long.MIN_VALUE;
			
				I_IntSet allowedTypes = Terms.get().newIntSet();
				allowedTypes.add(relationshipType.getConceptNid());
			
				List<? extends I_RelTuple> relationships = concept.getSourceRelTuples(
						config.getAllowedStatus(), allowedTypes, 
						config.getViewPositionSetReadOnly(),config.getPrecedence(), config.getConflictResolutionStrategy());
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
			I_GetConceptData relationshipType,
			I_ConfigAceFrame config) throws Exception {
			
				I_GetConceptData latestSource = null;
				long latestVersion = Long.MIN_VALUE;
			
				I_IntSet allowedTypes = Terms.get().newIntSet();
				allowedTypes.add(relationshipType.getConceptNid());
			
				List<? extends I_RelTuple> relationships = concept.getDestRelTuples(
						config.getAllowedStatus(), allowedTypes, 
						config.getViewPositionSetReadOnly(),config.getPrecedence(), config.getConflictResolutionStrategy());
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
	public I_RelTuple getLatestRelationship(I_GetConceptData concept, I_GetConceptData relationshipType,
			I_ConfigAceFrame config)
			throws Exception {
			
				I_RelTuple latestRel = null;
				long latestVersion = Long.MIN_VALUE;
			
				I_IntSet allowedTypes = Terms.get().newIntSet();
				allowedTypes.add(relationshipType.getConceptNid());
			
				List<? extends I_RelTuple> relationships = concept.getSourceRelTuples(
						config.getAllowedStatus(), allowedTypes, 
						config.getViewPositionSetReadOnly(),config.getPrecedence(), config.getConflictResolutionStrategy());
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