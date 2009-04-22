package org.dwfa.ace.task.refset.members;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
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
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;

/**
 * Utility class providing refset membership operations. 
 */
public class MemberRefsetHelper {

	private Logger logger = Logger.getLogger(MemberRefsetHelper.class.getName());
	
	protected I_TermFactory termFactory;
	
	protected int currentStatusId;
	protected int retiredStatusId;
	protected int conceptTypeId;
	
	public MemberRefsetHelper() throws Exception {
		termFactory = LocalVersionedTerminology.get();
		currentStatusId = 
			termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next());
		retiredStatusId = 
			termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids().iterator().next());
		conceptTypeId = 
			termFactory.uuidToNative(RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids().iterator().next());
	}
	
	/**
	 * All a collection of concepts to a refset.
	 * 
	 * @param refsetId The subject refset.
	 * @param newMembers The collection of concepts to be added to the refset
	 * @param valueId The value of the concept extension being added to each new member.
	 * @param batchDescription A textual description of the batch being processed. 
	 *                         Used in the progress reports given during processing.
	 */
	public void addAllToRefset(int refsetId, Set<I_GetConceptData> newMembers, int valueId, String batchDescription) 
			throws Exception {
		BatchMonitor batch = new BatchMonitor(batchDescription, newMembers.size(), 100, 20000);			
		batch.start();
		
		for (I_GetConceptData member : newMembers) {
			addToRefset(refsetId, member.getConceptId(), valueId);
			batch.mark();
		}
		
		batch.complete();		
	}
	
	/**
	 * Add a concept to a refset
	 * 
	 * @param refsetId The subject refset
	 * @param newMemberId The concept to be added
	 * @param valueId The value of the concept extension to be added to the new member concept.
	 */
	public void addToRefset(int refsetId, int newMemberId, int valueId) throws Exception {
		
		// check subject is not already a member
		for (I_ThinExtByRefVersioned extension : 
				termFactory.getAllExtensionsForComponent(newMemberId)) {
			
			if (extension.getRefsetId() == refsetId) {
				
				// get the latest version
				I_ThinExtByRefPart latestPart = null;
				for(I_ThinExtByRefPart part : extension.getVersions()) {
					if ((latestPart == null) || (part.getVersion() >= latestPart.getVersion())) {
						latestPart = part;
					}
				}
				
				// confirm its the right extension value and its status is current
				if (latestPart.getStatus() == currentStatusId) {
					if (latestPart instanceof I_ThinExtByRefPartConcept) {
						int partValue = ((I_ThinExtByRefPartConcept)latestPart).getConceptId();
						if (partValue == valueId) {
							// its already a member so skip
							logger.info("Concept is already a member of the refset. Skipping.");
							return;
						}
					}
				}
				
			}
		}

		// create a new extension (with a part for each path the user is editing)
		
		I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
		Set<I_Path> userEditPaths = config.getEditingPathSet();
		
		int memberId = termFactory.uuidToNativeWithGeneration(UUID.randomUUID(),
				ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(),
				termFactory.getPaths(), Integer.MAX_VALUE);

		I_ThinExtByRefVersioned newExtension =
			termFactory.newExtension(refsetId, memberId, newMemberId, conceptTypeId);

		for (I_Path editPath : userEditPaths) {
		
			I_ThinExtByRefPartConcept conceptExtension =
				termFactory.newConceptExtensionPart();

			conceptExtension.setPathId(editPath.getConceptId());
			conceptExtension.setStatus(currentStatusId);
			conceptExtension.setVersion(Integer.MAX_VALUE);
			conceptExtension.setConceptId(valueId);

			newExtension.addVersion(conceptExtension);
		}
		
		termFactory.addUncommitted(newExtension); 
	}

	/**
	 * Remove a concept from a refset
	 * 
	 * @param refsetId The subject refset
	 * @param newMemberId The concept to be removed
	 * @param valueId The value of the concept extension to be removed (the membership type).
	 */
	public void removeFromRefset(int refsetId, int memberId, int valueId) throws Exception {
		
		// check subject is not already a member
		for (I_ThinExtByRefVersioned extension : 
				termFactory.getAllExtensionsForComponent(memberId)) {
			
			if (extension.getRefsetId() == refsetId) {
				
				// get the latest version
				I_ThinExtByRefPart latestPart = null;
				for(I_ThinExtByRefPart part : extension.getVersions()) {
					if ((latestPart == null) || (part.getVersion() >= latestPart.getVersion())) {
						latestPart = part;
					}
				}
				
				// confirm its the right extension value and its status is current
				if (latestPart.getStatus() == currentStatusId) {
					if (latestPart instanceof I_ThinExtByRefPartConcept) {
						int partValue = ((I_ThinExtByRefPartConcept)latestPart).getConceptId();
						if (partValue == valueId) {
							// found a member to retire
							
							I_ThinExtByRefPartConcept clone = (I_ThinExtByRefPartConcept) latestPart.duplicatePart();
							clone.setStatus(retiredStatusId);
							clone.setVersion(Integer.MAX_VALUE);
							extension.addVersion(clone);
							termFactory.addUncommitted(extension);
						}
					}
				}
			}
		}		
	}
	
	/**
	 * Get all the descendants (children, children of children, etc) of a particular concept.
	 */
	public Set<I_GetConceptData> getAllDescendants(I_GetConceptData concept) throws Exception {
		
		I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();

		Set<I_Position> userViewPositions = config.getViewPositionSet();
		I_IntSet userViewStatuses = config.getAllowedStatus();
		
        I_IntSet isARel = termFactory.newIntSet();
        isARel.add(ConceptConstants.SNOMED_IS_A.localize().getNid());
		
		// find all the children
		Set<I_GetConceptData> childConcepts = 
			getAllDescendants(new HashSet<I_GetConceptData>(), concept, userViewStatuses, isARel, userViewPositions);
	
		logger.info("Found " + childConcepts.size() + " descendants of concept '" + concept.getInitialText() + "'.");
	
		return childConcepts;
	}
	
	
	protected Set<I_GetConceptData> getAllDescendants(Set<I_GetConceptData> resultSet, I_GetConceptData parent, 
			I_IntSet allowedStatuses, I_IntSet allowedTypes, Set<I_Position> positions) throws Exception {

		for (I_RelTuple childTuple : parent.getDestRelTuples(allowedStatuses, allowedTypes, positions, false)) {
			I_GetConceptData childConcept = termFactory.getConcept(childTuple.getC1Id());
			if (resultSet.add(childConcept)) {
				resultSet.addAll(getAllDescendants(resultSet, childConcept, allowedStatuses, allowedTypes, positions));
			}
		}
		return resultSet;
	}
	
}
