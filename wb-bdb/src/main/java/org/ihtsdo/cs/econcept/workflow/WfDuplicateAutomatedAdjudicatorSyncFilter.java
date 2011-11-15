package org.ihtsdo.cs.econcept.workflow;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
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
public class WfDuplicateAutomatedAdjudicatorSyncFilter implements
		I_WfChangeSetScrubber {

	private HashSet<TkRefsetAbstractMember<?>>  wfMembersToRetire = new HashSet<TkRefsetAbstractMember<?>>();
	private HashSet<TkRefsetAbstractMember<?>> origListOfMembersToCommit = new HashSet<TkRefsetAbstractMember<?>>();
	
	@Override
	public boolean identifyMembers(HashSet<TkRefsetAbstractMember<?>> wfMembersToCommit) {
		try {
			origListOfMembersToCommit.addAll(wfMembersToCommit);
			wfMembersToRetire = filterMultipleAdjudications(wfMembersToCommit);
		} catch (Exception e) {
			AceLog.getEditLog().log(Level.WARNING, "Error in identifyMembers() in " + this.getClass().getCanonicalName() + " with Exception: " + e.getMessage());
			return false;
		}
		
		return true;
	}

	@Override
	public HashSet<TkRefsetAbstractMember<?>> processScrubbedMembers() {
    	try {
    		retireInDB(wfMembersToRetire);

    		origListOfMembersToCommit.addAll(wfMembersToRetire);
    	} catch (Exception e) {
			AceLog.getEditLog().log(Level.WARNING, "Error in processScrubbedMembers() in " + this.getClass().getCanonicalName() + " with Exception: " + e.getMessage());
    	}
    	
    	return origListOfMembersToCommit;
	}

	private void retireInDB(HashSet<TkRefsetAbstractMember<?>> wfMembersToRetire) throws NumberFormatException, TerminologyException, IOException, Exception {
		// Already be committed in DB.  Therefore retire directly (NoCheck)
		for (TkRefsetAbstractMember<?> member : wfMembersToRetire) {
			WorkflowHelper.retireRow(WorkflowHelper.populateWorkflowHistoryJavaBean(member));
		}
	}

	private HashSet<TkRefsetAbstractMember<?>> filterMultipleAdjudications(Set<TkRefsetAbstractMember<?>> origMembers) throws Exception {
		HashSet<TkRefsetAbstractMember<?>> activeMembers = new HashSet<TkRefsetAbstractMember<?>>();
		HashSet<TkRefsetAbstractMember<?>>  retiredMembers = new HashSet<TkRefsetAbstractMember<?>>();

		// Filter Current Members only
		for (TkRefsetAbstractMember<?> mem : origMembers) {
			if (mem.getStatusUuid().equals(ArchitectonicAuxiliary.Concept.CURRENT.getPrimoridalUid()) ||
				mem.getStatusUuid().equals(ArchitectonicAuxiliary.Concept.ACTIVE.getPrimoridalUid())) {
				
				activeMembers.add(mem);
			}
		}
		
		// Get Adj Members
		UUID adjPath = identifyAdjudicationPath(activeMembers);

		// RefsetId / Set<adjPathVersions>
		Map<UUID, Set<TkRefsetAbstractMember<?>>> adjudicatedMembers = initializeAdjMembers(origMembers, adjPath);

		for (UUID refsetId : adjudicatedMembers.keySet()) {
    		Set<TkRefsetAbstractMember<?>> membersToAdjudicate = adjudicatedMembers.get(refsetId);
			
    		HashSet<HashSet<TkRefsetAbstractMember<?>>> nonLatestDups = identifyAdjudToRetire(refsetId, membersToAdjudicate);

    		for (HashSet<TkRefsetAbstractMember<?>> set : nonLatestDups) {
				retiredMembers.addAll(set);
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

	private UUID identifyAdjudicationPath(Set<TkRefsetAbstractMember<?>> origMembers) {
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
					
					if (!checkPathFound) {
						return null;
					}
				}
			}
		} else {
			return null;
		}
		
		return matchingPath;
	}

	private HashSet<HashSet<TkRefsetAbstractMember<?>>> identifyAdjudToRetire(UUID refsetId, Set<TkRefsetAbstractMember<?>> membersToCommit) throws IOException, TerminologyException {
		HashSet<HashSet<TkRefsetAbstractMember<?>>> dupsSet = new HashSet<HashSet<TkRefsetAbstractMember<?>>>();
		HashSet<HashSet<TkRefsetAbstractMember<?>>> retSet = new HashSet<HashSet<TkRefsetAbstractMember<?>>>();
		
		WorkflowRefsetReader reader = null;
		
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
		
		HashSet<TkRefsetAbstractMember<?>> nonDups = new HashSet<TkRefsetAbstractMember<?>>();
		
		for (TkRefsetAbstractMember<?> member : membersToCommit) {
			// First see if in dups Map
			boolean dupFound = false;
			for (HashSet<TkRefsetAbstractMember<?>> set : dupsSet) {
				TkRefsetAbstractMember<?> dup = set.iterator().next();
				
				if (reader.isIdenticalAutomatedAdjudication(dup, member)) {
					set.add(member);
					dupFound = true;
					break;
				} 
			}
			
			// If not in Dups map, see if in nonDups
			if (!dupFound) {
				TkRefsetAbstractMember<?> toRemoveFromNonDups = null;
				for (TkRefsetAbstractMember<?> nonDup: nonDups) {
					if (reader.isIdenticalAutomatedAdjudication(member, nonDup)) {
						// Remove from nonDup and add both to new dup set 
						toRemoveFromNonDups = nonDup;
						dupFound = true;
						HashSet<TkRefsetAbstractMember<?>> dups = new HashSet<TkRefsetAbstractMember<?>>();
						dups.add(member);
						dups.add(nonDup);
						dupsSet.add(dups);
						break;
					} 
				}

				if (toRemoveFromNonDups != null) {
					nonDups.remove(toRemoveFromNonDups);
				}
			}
			
			if (!dupFound) {
				nonDups.add(member);
			} 
		} 	// End Member

		// For each Dups Set, retire all but latest 
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
}
