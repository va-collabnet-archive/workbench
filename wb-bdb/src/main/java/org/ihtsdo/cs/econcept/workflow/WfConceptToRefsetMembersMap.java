package org.ihtsdo.cs.econcept.workflow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refset.str.TkRefsetStrMember;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

public class WfConceptToRefsetMembersMap {
	private static HashMap<UUID, HashSet<TkRefsetAbstractMember<?>>> membersAnnotationMap = new HashMap<UUID, HashSet<TkRefsetAbstractMember<?>>>();

	public int getConceptSize() {
		return membersAnnotationMap.size();
	}

	public int getMembersSize() {
		int count = 0;
		
		for (UUID con : membersAnnotationMap.keySet()) {
			count = count + membersAnnotationMap.get(con).size();
		}
		
		return count;
	}

	public Set<UUID> getKeySet() {
		return membersAnnotationMap.keySet();
	}

	public Set<TkRefsetAbstractMember<?>> getMembers(UUID conUid) {
		return membersAnnotationMap.get(conUid);
	}

	public Set<TkRefsetAbstractMember<?>> getAllMembers() {
		Set<TkRefsetAbstractMember<?>> retSet = new HashSet<TkRefsetAbstractMember<?>>();
		
		for (UUID id : getKeySet()) {
			Set<TkRefsetAbstractMember<?>> conceptMembers = membersAnnotationMap.get(id);
			retSet.addAll(conceptMembers);
		}
		
		return retSet;
	}

	public void clear() {
		membersAnnotationMap.clear();
	}

	public boolean alreadyProcessed(TkRefsetAbstractMember<?> member) {
		// Have already processed?
		if (membersAnnotationMap.containsKey(member.getComponentUuid())) {
			Set<TkRefsetAbstractMember<?>> members = membersAnnotationMap.get(member.getComponentUuid());

			if (members.contains(member)) {
				return true;
			}
		}
		
		return false;
	}

	public boolean containsKey(UUID concept) {
		return membersAnnotationMap.containsKey(concept);
	}

	public void addNewMember(TkRefsetAbstractMember<?> member) {
		if (!membersAnnotationMap.containsKey(member.getComponentUuid())) {
			HashSet<TkRefsetAbstractMember<?>> newListOfMembers = new HashSet<TkRefsetAbstractMember<?>>();
			newListOfMembers.add(member);
			membersAnnotationMap.put(member.getComponentUuid(), newListOfMembers);
		} else {
			membersAnnotationMap.get(member.getComponentUuid()).add(member);	
		}
	}

	public Set<TkRefsetAbstractMember<?>> getDuplicateMembers(TkRefsetAbstractMember<?> member) {
		Set<TkRefsetAbstractMember<?>> currentMembers = membersAnnotationMap.get(member.getComponentUuid());
		Set<TkRefsetAbstractMember<?>> duplicates = new HashSet<TkRefsetAbstractMember<?>>();
		
		for (TkRefsetAbstractMember<?> m : currentMembers) {
			if (member.getStatusUuid().equals(m.getStatusUuid()) && 
				member.getAuthorUuid().equals(m.getAuthorUuid()) && 
				member.getPathUuid().equals(m.getPathUuid())&& 
				member.getTime() == m.getTime()) 
			{
				String newMemberVal = ((TkRefsetStrMember)member).getStrValue();
				String existingMemberVal = ((TkRefsetStrMember)m).getStrValue();
				
				if (newMemberVal.equals(existingMemberVal)) {
					duplicates.add(m);
				}
			} 
		}
		
		return duplicates;
	}

	public Set<UUID> getWfHxConcepts() {
		Set<UUID> retSet = new HashSet<UUID>();
		
		for (UUID id : getKeySet()) {
			boolean conceptInWfHxRefset = false;
			Set<TkRefsetAbstractMember<?>> conceptMembers = membersAnnotationMap.get(id);

			for (TkRefsetAbstractMember<?> m : conceptMembers) {
				if (m.getRefsetUuid().equals(WorkflowHelper.getWorkflowRefsetUid())) {
					conceptInWfHxRefset = true;
					break;
				}
			}
			
			if (conceptInWfHxRefset) {
				retSet.add(id);
			}
		}
		
		return retSet;
	}

	public Set<TkRefsetAbstractMember<?>> getAllWfHxMembers() {
		Set<TkRefsetAbstractMember<?>> retSet = new HashSet<TkRefsetAbstractMember<?>>();
		
		for (UUID id : getKeySet()) {
			Set<TkRefsetAbstractMember<?>> conceptMembers = membersAnnotationMap.get(id);

			for (TkRefsetAbstractMember<?> m : conceptMembers) {
				if (m.getRefsetUuid().equals(WorkflowHelper.getWorkflowRefsetUid())) {
					retSet.add(m);
				}
			}
		}
		
		return retSet;
	}
}
