package org.dwfa.ace.refset;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;

public class RefsetHelper {

	protected I_TermFactory termFactory;
	
	protected int currentStatusId;
	protected int retiredStatusId;
	protected int conceptTypeId;
	protected int unspecifiedUuid;
	
	private Logger logger = Logger.getLogger(RefsetHelper.class.getName());
	
	protected RefsetHelper() throws Exception {
		termFactory = LocalVersionedTerminology.get();
		currentStatusId = ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid();
		retiredStatusId = ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid();
		unspecifiedUuid = ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid();
		conceptTypeId = RefsetAuxiliary.Concept.CONCEPT_EXTENSION.localize().getNid();
	}
	
	
	protected boolean hasCurrentRefsetExtension(int refsetId, int conceptId, int memberTypeId) throws Exception {
		for (I_ThinExtByRefVersioned extension : 
			termFactory.getAllExtensionsForComponent(conceptId)) {
		
			if (extension.getRefsetId() == refsetId) {
				
				// get the latest version
				I_ThinExtByRefPart latestPart = null;
				for(I_ThinExtByRefPart part : extension.getVersions()) {
					if ((latestPart == null) || (part.getVersion() >= latestPart.getVersion())) {
						latestPart = part;
					}
				}
				
				// confirm its the right extension value and its status is current
				if (latestPart.getStatusId() == currentStatusId) {
					if (latestPart instanceof I_ThinExtByRefPartConcept) {
						int partValue = ((I_ThinExtByRefPartConcept)latestPart).getConceptId();
						if (partValue == memberTypeId) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Add a concept to a refset
	 * 
	 * @param refsetId The subject refset
	 * @param conceptId The concept to be added
	 * @param memberTypeId The value of the concept extension to be added to the new member concept.
	 */
	protected boolean newRefsetExtension(int refsetId, int conceptId, int memberTypeId) throws Exception {
		
		// check subject is not already a member
		if (hasCurrentRefsetExtension(refsetId, conceptId, memberTypeId)) {
			if (logger.isLoggable(Level.FINE)) {
				String extValueDesc = termFactory.getConcept(memberTypeId).getInitialText();
				logger.fine("Concept is already a '" + extValueDesc + "' of the refset. Skipping.");
			}
			return false;
		}
		
		// create a new extension (with a part for each path the user is editing)
		
		I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
		Set<I_Path> userEditPaths = config.getEditingPathSet();
		
		int newMemberId = termFactory.uuidToNativeWithGeneration( 
				UUID.randomUUID(), unspecifiedUuid, userEditPaths, Integer.MAX_VALUE);

		I_ThinExtByRefVersioned newExtension =
			termFactory.newExtensionNoChecks(refsetId, newMemberId, conceptId, conceptTypeId);

		for (I_Path editPath : userEditPaths) {
		
			I_ThinExtByRefPartConcept conceptExtension =
				termFactory.newConceptExtensionPart();

			conceptExtension.setPathId(editPath.getConceptId());
			conceptExtension.setStatusId(currentStatusId);
			conceptExtension.setVersion(Integer.MAX_VALUE);
			conceptExtension.setConceptId(memberTypeId);

			newExtension.addVersion(conceptExtension);
		}		
		
		termFactory.addUncommittedNoChecks(newExtension);
		return true;
	}
	
	/**
	 * Remove a concept from a refset
	 * 
	 * @param refsetId The subject refset
	 * @param conceptId The concept to be removed
	 * @param memberTypeId The value of the concept extension to be removed (the membership type).
	 */
	protected boolean retireRefsetExtension(int refsetId, int conceptId, int memberTypeId) throws Exception {
		
		boolean wasRemoved = false;
		
		// check subject is not already a member
		for (I_ThinExtByRefVersioned extension : 
				termFactory.getAllExtensionsForComponent(conceptId)) {
			
			if (extension.getRefsetId() == refsetId) {
				
				// get the latest version
				I_ThinExtByRefPart latestPart = null;
				for(I_ThinExtByRefPart part : extension.getVersions()) {
					if ((latestPart == null) || (part.getVersion() >= latestPart.getVersion())) {
						latestPart = part;
					}
				}
				
				// confirm its the right extension value and its status is current
				if (latestPart.getStatusId() == currentStatusId) {
					if (latestPart instanceof I_ThinExtByRefPartConcept) {
						int partValue = ((I_ThinExtByRefPartConcept)latestPart).getConceptId();
						if (partValue == memberTypeId) {
							// found a member to retire
							
							I_ThinExtByRefPartConcept clone = (I_ThinExtByRefPartConcept) latestPart.duplicate();
							clone.setStatusId(retiredStatusId);
							clone.setVersion(Integer.MAX_VALUE);
							extension.addVersion(clone);
							termFactory.addUncommittedNoChecks(extension);
							wasRemoved = true;
						}
					}
				}
			}
		}	
		return wasRemoved;
	}
	
	
	/**
	 * Get all the descendants (children, children of children, etc) of a particular concept.
	 */
	public Set<I_GetConceptData> getAllDescendants(I_GetConceptData concept, Condition ... conditions) throws Exception {
		
		I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();

		Set<I_Position> userViewPositions = config.getViewPositionSet();
		I_IntSet userViewStatuses = config.getAllowedStatus();
		
        I_IntSet isARel = termFactory.newIntSet();
        isARel.add(ConceptConstants.SNOMED_IS_A.localize().getNid());
		
		// find all the children
		Set<I_GetConceptData> descendants = 
			getAllDescendants(new HashSet<I_GetConceptData>(), concept, userViewStatuses, isARel, userViewPositions, conditions);
	
		logger.info("Found " + descendants.size() + " descendants of concept '" + concept.getInitialText() + "'.");
	
		return descendants;
	}
	
	
	protected Set<I_GetConceptData> getAllDescendants(Set<I_GetConceptData> resultSet, I_GetConceptData parent, 
			I_IntSet allowedStatuses, I_IntSet allowedTypes, Set<I_Position> positions, Condition ... conditions) throws Exception {

		ITERATE_CHILDREN:
		for (I_RelTuple childTuple : parent.getDestRelTuples(allowedStatuses, allowedTypes, positions, false)) {
			I_GetConceptData childConcept = termFactory.getConcept(childTuple.getC1Id());
			if (childConcept.getConceptId() == parent.getConceptId()) {
				continue ITERATE_CHILDREN;
			}
			if (conditions != null) {
				for (Condition condition : conditions) {
					if (!condition.evaluate(childConcept)) {
						continue ITERATE_CHILDREN;
					}
				}
			}
			if (resultSet.add(childConcept)) {
				resultSet.addAll(getAllDescendants(resultSet, childConcept, allowedStatuses, allowedTypes, positions, conditions));
			}
		}
		return resultSet;
	}
	
	/**
	 * Get all the ancestors (parents, parents of parents, etc) of a particular concept.
	 */
	public Set<I_GetConceptData> getAllAncestors(I_GetConceptData concept, Condition ... conditions) throws Exception {
		
		I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();

		Set<I_Position> userViewPositions = config.getViewPositionSet();
		I_IntSet userViewStatuses = config.getAllowedStatus();
		
        I_IntSet isARel = termFactory.newIntSet();
        isARel.add(ConceptConstants.SNOMED_IS_A.localize().getNid());
		
		// find all the parents
		Set<I_GetConceptData> parentConcepts = 
			getAllAncestors(new HashSet<I_GetConceptData>(), concept, userViewStatuses, isARel, userViewPositions, conditions);
	
		logger.info("Found " + parentConcepts.size() + " ancestors of concept '" + concept.getInitialText() + "'.");
	
		return parentConcepts;
	}
	
	
	protected Set<I_GetConceptData> getAllAncestors(Set<I_GetConceptData> resultSet, I_GetConceptData child, 
			I_IntSet allowedStatuses, I_IntSet allowedTypes, Set<I_Position> positions, Condition ... conditions) throws Exception {

		ITERATE_PARENTS:
		for (I_RelTuple childTuple : child.getSourceRelTuples(allowedStatuses, allowedTypes, positions, false)) {
			I_GetConceptData parentConcept = termFactory.getConcept(childTuple.getC2Id());
			if (parentConcept.getConceptId() == child.getConceptId()) {
				continue ITERATE_PARENTS;
			}
			if (conditions != null) {
				for (Condition condition : conditions) {
					if (!condition.evaluate(parentConcept)) {
						continue ITERATE_PARENTS;
					}
				}
			}			
			if (resultSet.add(parentConcept)) {
				resultSet.addAll(getAllAncestors(resultSet, parentConcept, allowedStatuses, allowedTypes, positions, conditions));
			}
		}
		return resultSet;
	}
	
	public interface Condition {
		public boolean evaluate(I_GetConceptData concept) throws Exception;
	}

}
