package org.ihtsdo.cs.econcept.workflow;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.workflow.refset.WorkflowRefset;
import org.ihtsdo.workflow.refset.edcat.EditorCategoryRefsetReader;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefsetReader;
import org.ihtsdo.workflow.refset.semHier.SemanticHierarchyRefsetReader;
import org.ihtsdo.workflow.refset.semTag.SemanticTagsRefsetReader;
import org.ihtsdo.workflow.refset.stateTrans.StateTransitionRefsetReader;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetReader;

// In case where multiple automated adjudications occurred prior to everyone syncing, will be the same number of current adjudication rows.  
// Therefore, retired all others but last one.
// REQUIRED to handle automated adjudication of WF refsets done via contadiction detector 
public class WfDuplicateAutomatedAdjudicatorSyncFilter extends AbstractWfChangeSetFilter {

	WfDuplicateAutomatedAdjudicatorSyncFilter(String filePath) {
		super(filePath);
	}

	@Override
	public boolean scrubMembers(HashSet<TkRefexAbstractMember<?>> wfMembersToCommit) {
		try {
			wfMembersToProcess.addAll(wfMembersToCommit);
			wfMembersToIgnore = filterMultipleAdjudications(wfMembersToCommit);
		} catch (Exception e) {
			AceLog.getEditLog().log(Level.WARNING, "Error in identifyMembers() in " + this.getClass().getCanonicalName() + " with Exception: " + e.getMessage());
			return false;
		}
		
		return true;
	}

	@Override
	public HashSet<TkRefexAbstractMember<?>> getApprovedMembers() {
    	try {
    		wfMembersToProcess.removeAll(wfMembersToIgnore);

    		retireInDB(wfMembersToIgnore);
    	} catch (Exception e) {
			AceLog.getEditLog().log(Level.WARNING, "Error in processScrubbedMembers() in " + this.getClass().getCanonicalName() + " with Exception: " + e.getMessage());
    	}
    	
    	return wfMembersToProcess;
	}


	private HashSet<TkRefexAbstractMember<?>> filterMultipleAdjudications(Set<TkRefexAbstractMember<?>> origMembers) throws Exception {
		HashSet<TkRefexAbstractMember<?>>  retiredMembers = new HashSet<TkRefexAbstractMember<?>>();
		
		// Get Adj Members 
		UUID adjPath = identifyCommonAdjudicationPath(origMembers);

		if (adjPath != null) {
			// RefsetId / Set<adjPathVersions>
			Map<UUID, Set<TkRefexAbstractMember<?>>> adjudicatedMembers = initializeAdjMembers(origMembers, adjPath);

			// Per Refset
			for (UUID refsetId : adjudicatedMembers.keySet()) {
	    		Set<TkRefexAbstractMember<?>> membersToAdjudicate = adjudicatedMembers.get(refsetId);
				
	    		HashSet<HashSet<TkRefexAbstractMember<?>>> nonLatestDups = identifyAdjudToRetire(refsetId, membersToAdjudicate);
	
	    		// Per duplicate set of adjudication (may be multiple per refset)
	    		for (HashSet<TkRefexAbstractMember<?>> set : nonLatestDups) {
					retiredMembers.addAll(set);
	    		}
			}
		}
		
		return retiredMembers;
	}
	

	private Map<UUID, Set<TkRefexAbstractMember<?>>> 
			initializeAdjMembers(Set<TkRefexAbstractMember<?>> origMembers, UUID adjPath) 
	{
		Map<UUID, Set<TkRefexAbstractMember<?>>> refsetToAdjVersions = new HashMap<UUID, Set<TkRefexAbstractMember<?>>>();
		 
		for (TkRefexAbstractMember<?> member : origMembers) {
			if (member.getPathUuid().equals(adjPath) &&
				WorkflowHelper.getRefsetUidList().contains(member.getRefexUuid())) 
			{
				if (refsetToAdjVersions.containsKey(member.getRefexUuid())) {
					refsetToAdjVersions.get(member.getRefexUuid()).add(member);
				} else {
					Set<TkRefexAbstractMember<?>> newSet = new HashSet<TkRefexAbstractMember<?>>();
					newSet.add(member);
					 
					refsetToAdjVersions.put(member.getRefexUuid(), newSet);
				}
			}
	 	}
		 
		return refsetToAdjVersions;
	}
	private HashSet<HashSet<TkRefexAbstractMember<?>>> identifyAdjudToRetire(UUID refsetId, Set<TkRefexAbstractMember<?>> membersToCommit) throws IOException, TerminologyException {
		HashSet<HashSet<TkRefexAbstractMember<?>>> dupsSet = new HashSet<HashSet<TkRefexAbstractMember<?>>>();
		HashSet<HashSet<TkRefexAbstractMember<?>>> retSet = new HashSet<HashSet<TkRefexAbstractMember<?>>>();
		HashSet<TkRefexAbstractMember<?>> nonDups = new HashSet<TkRefexAbstractMember<?>>();
		WorkflowRefsetReader reader = null;
		

		// Get Required Refset Reader
		if (refsetId.equals(WorkflowRefset.workflowHistoryConcept.getPrimoridalUid())){
			reader = new WorkflowHistoryRefsetReader();
		} else if (refsetId.equals(WorkflowRefset.editorCategoryConcept.getPrimoridalUid())){
			reader = new EditorCategoryRefsetReader();
		} else if (refsetId.equals(WorkflowRefset.semanticHierarchyConcept.getPrimoridalUid())){
			reader = new SemanticHierarchyRefsetReader();
		} else if (refsetId.equals(WorkflowRefset.semanticTagConcept.getPrimoridalUid())){
			reader = new SemanticTagsRefsetReader();
		} else if (refsetId.equals(WorkflowRefset.stateTransitionConcept.getPrimoridalUid())){
			reader = new StateTransitionRefsetReader();
		} 
		
		for (TkRefexAbstractMember<?> member : membersToCommit) {
			// Examine If already identified as duplicate, 
			boolean dupFound = false;
			for (HashSet<TkRefexAbstractMember<?>> set : dupsSet) {
				TkRefexAbstractMember<?> dup = set.iterator().next();
				
				// If identicial to another dup, add to set and break
				if (reader.isIdenticalAutomatedAdjudication(dup, member)) {
					set.add(member);
					dupFound = true;
					break;
				} 
			}
			
			if (!dupFound) {
				TkRefexAbstractMember<?> toRemoveFromNonDups = null;

				// Examine those not identified as dups (yet?)
				for (TkRefexAbstractMember<?> nonDup: nonDups) {
					if (reader.isIdenticalAutomatedAdjudication(member, nonDup)) {
						// Dup identified Remove from nonDup and add both to new dup set 
						toRemoveFromNonDups = nonDup;
						dupFound = true;

						HashSet<TkRefexAbstractMember<?>> dups = new HashSet<TkRefexAbstractMember<?>>();
						dups.add(member);
						dups.add(nonDup);
						dupsSet.add(dups);
						break;
					} 
				}

				// Remove matched dup member previous thought of as non-Dup
				if (toRemoveFromNonDups != null) {
					nonDups.remove(toRemoveFromNonDups);
				}
			}
			
			if (!dupFound) {
				nonDups.add(member);
			} 
		}


		// For each Dups Set, retire all but latest.  Do here and not later 
		for (HashSet<TkRefexAbstractMember<?>> dups : dupsSet) {
			// Identify latest Auto-Adjudicated Version
			long latestTime = 0;
			TkRefexAbstractMember<?> latestDup = null;
			for (TkRefexAbstractMember<?> dup : dups) {
				if (dup.getTime() > latestTime ) {
					latestTime = dup.getTime();
					latestDup = dup;
				}
			}
			dups.remove(latestDup);
			
			retSet.add(dups);
		}
			
		return retSet;
	}


	private UUID identifyCommonAdjudicationPath(Set<TkRefexAbstractMember<?>> origMembers) {
		HashMap<UUID, Set<UUID>> modPaths = new HashMap<UUID, Set<UUID>>();
		
		for (TkRefexAbstractMember<?>  member : origMembers) {
			UUID authId = member.getAuthorUuid();
			UUID pathId = member.getPathUuid();
			
			if (modPaths.containsKey(authId)) {
				modPaths.get(authId).add(pathId);
			} else {
				HashSet<UUID> set = new HashSet<UUID>();
				set.add(pathId);
				modPaths.put(authId, set);
			}
		}
		
		Set<UUID> multPathMods = new HashSet<UUID>();
		for (UUID authId : modPaths.keySet()) {
			if (modPaths.get(authId).size() > 1) {
				multPathMods.add(authId);
			}
		}
		
		UUID matchingPath = null;
		Set<UUID> testPaths = null;

		if (multPathMods.size() > 1) {
			
			for (UUID authId : multPathMods) {
				if (testPaths == null) {
					testPaths = modPaths.get(authId);
				} else {
					boolean checkPathFound = false;
					Set<UUID> checkPaths = modPaths.get(authId);
					
					for (UUID checkPath : checkPaths) {
						if (testPaths.contains(checkPath)) {
							if (matchingPath == null) {
								matchingPath = checkPath;
							} else if (!matchingPath.equals(checkPath)) {
								// Have multiple adjudication paths
								return null;
							}
							checkPathFound = true;
							break;
						} 
					}
					
					/*
					 * Spackman has on WbAux path & own path (under user: "user")
					 * 
					 * if (!checkPathFound) {
						return null;
					}*/
				}

			}

			return matchingPath;
		} else if (multPathMods.size() == 1) {
			return multPathMods.iterator().next();
		} else {
			return null;
		}
	}

	private void retireInDB(HashSet<TkRefexAbstractMember<?>> wfMembersToRetire) throws NumberFormatException, TerminologyException, IOException, Exception {
		// Already be committed in DB.  Therefore retire directly (NoCheck)
		for (TkRefexAbstractMember<?> member : wfMembersToRetire) {
			WorkflowHelper.retireWfHxRow(WorkflowHelper.populateWorkflowHistoryJavaBean(member));
		}
	}
}
