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
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
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
	public boolean scrubMembers(HashSet<TkRefsetAbstractMember<?>> wfMembersToCommit) {
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
	public HashSet<TkRefsetAbstractMember<?>> getApprovedMembers() {
    	try {
    		wfMembersToProcess.removeAll(wfMembersToIgnore);

    		retireInDB(wfMembersToIgnore);
    	} catch (Exception e) {
			AceLog.getEditLog().log(Level.WARNING, "Error in processScrubbedMembers() in " + this.getClass().getCanonicalName() + " with Exception: " + e.getMessage());
    	}
    	
    	return wfMembersToProcess;
	}


	private HashSet<TkRefsetAbstractMember<?>> filterMultipleAdjudications(Set<TkRefsetAbstractMember<?>> origMembers) throws Exception {
		HashSet<TkRefsetAbstractMember<?>>  retiredMembers = new HashSet<TkRefsetAbstractMember<?>>();
		
		// Get Adj Members 
		UUID adjPath = identifyCommonAdjudicationPath(origMembers);

		if (adjPath != null) {
			// RefsetId / Set<adjPathVersions>
			Map<UUID, Set<TkRefsetAbstractMember<?>>> adjudicatedMembers = initializeAdjMembers(origMembers, adjPath);

			// Per Refset
			for (UUID refsetId : adjudicatedMembers.keySet()) {
	    		Set<TkRefsetAbstractMember<?>> membersToAdjudicate = adjudicatedMembers.get(refsetId);
				
	    		HashSet<HashSet<TkRefsetAbstractMember<?>>> nonLatestDups = identifyAdjudToRetire(refsetId, membersToAdjudicate);
	
	    		// Per duplicate set of adjudication (may be multiple per refset)
	    		for (HashSet<TkRefsetAbstractMember<?>> set : nonLatestDups) {
					retiredMembers.addAll(set);
	    		}
			}
		}
		
		return retiredMembers;
	}
	

	private Map<UUID, Set<TkRefsetAbstractMember<?>>> 
			initializeAdjMembers(Set<TkRefsetAbstractMember<?>> origMembers, UUID adjPath) 
	{
		Map<UUID, Set<TkRefsetAbstractMember<?>>> refsetToAdjVersions = new HashMap<UUID, Set<TkRefsetAbstractMember<?>>>();
		 
		for (TkRefsetAbstractMember<?> member : origMembers) {
			if (member.getPathUuid().equals(adjPath) &&
				WorkflowHelper.getRefsetUidList().contains(member.getRefsetUuid())) 
			{
				if (refsetToAdjVersions.containsKey(member.getRefsetUuid())) {
					refsetToAdjVersions.get(member.getRefsetUuid()).add(member);
				} else {
					Set<TkRefsetAbstractMember<?>> newSet = new HashSet<TkRefsetAbstractMember<?>>();
					newSet.add(member);
					 
					refsetToAdjVersions.put(member.getRefsetUuid(), newSet);
				}
			}
	 	}
		 
		return refsetToAdjVersions;
	}
	private HashSet<HashSet<TkRefsetAbstractMember<?>>> identifyAdjudToRetire(UUID refsetId, Set<TkRefsetAbstractMember<?>> membersToCommit) throws IOException, TerminologyException {
		HashSet<HashSet<TkRefsetAbstractMember<?>>> dupsSet = new HashSet<HashSet<TkRefsetAbstractMember<?>>>();
		HashSet<HashSet<TkRefsetAbstractMember<?>>> retSet = new HashSet<HashSet<TkRefsetAbstractMember<?>>>();
		HashSet<TkRefsetAbstractMember<?>> nonDups = new HashSet<TkRefsetAbstractMember<?>>();
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
		
		for (TkRefsetAbstractMember<?> member : membersToCommit) {
			// Examine If already identified as duplicate, 
			boolean dupFound = false;
			for (HashSet<TkRefsetAbstractMember<?>> set : dupsSet) {
				TkRefsetAbstractMember<?> dup = set.iterator().next();
				
				// If identicial to another dup, add to set and break
				if (reader.isIdenticalAutomatedAdjudication(dup, member)) {
					set.add(member);
					dupFound = true;
					break;
				} 
			}
			
			if (!dupFound) {
				TkRefsetAbstractMember<?> toRemoveFromNonDups = null;

				// Examine those not identified as dups (yet?)
				for (TkRefsetAbstractMember<?> nonDup: nonDups) {
					if (reader.isIdenticalAutomatedAdjudication(member, nonDup)) {
						// Dup identified Remove from nonDup and add both to new dup set 
						toRemoveFromNonDups = nonDup;
						dupFound = true;

						HashSet<TkRefsetAbstractMember<?>> dups = new HashSet<TkRefsetAbstractMember<?>>();
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
		for (HashSet<TkRefsetAbstractMember<?>> dups : dupsSet) {
			// Identify latest Auto-Adjudicated Version
			long latestTime = 0;
			TkRefsetAbstractMember<?> latestDup = null;
			for (TkRefsetAbstractMember<?> dup : dups) {
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


	private UUID identifyCommonAdjudicationPath(Set<TkRefsetAbstractMember<?>> origMembers) {
		HashMap<UUID, Set<UUID>> modPaths = new HashMap<UUID, Set<UUID>>();
		
		for (TkRefsetAbstractMember<?>  member : origMembers) {
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

	private void retireInDB(HashSet<TkRefsetAbstractMember<?>> wfMembersToRetire) throws NumberFormatException, TerminologyException, IOException, Exception {
		// Already be committed in DB.  Therefore retire directly (NoCheck)
		for (TkRefsetAbstractMember<?> member : wfMembersToRetire) {
			WorkflowHelper.retireWfHxRow(WorkflowHelper.populateWorkflowHistoryJavaBean(member));
		}
	}
}
