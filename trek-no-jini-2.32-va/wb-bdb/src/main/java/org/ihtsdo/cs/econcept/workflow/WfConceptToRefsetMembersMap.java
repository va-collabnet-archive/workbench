package org.ihtsdo.cs.econcept.workflow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrMember;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

public class WfConceptToRefsetMembersMap {
	private static HashMap<UUID, HashSet<TkRefexAbstractMember<?>>> membersAnnotationMap = new HashMap<UUID, HashSet<TkRefexAbstractMember<?>>>();

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

	public Set<TkRefexAbstractMember<?>> getMembers(UUID conUid) {
		return membersAnnotationMap.get(conUid);
	}

	public Set<TkRefexAbstractMember<?>> getAllMembers() {
		Set<TkRefexAbstractMember<?>> retSet = new HashSet<TkRefexAbstractMember<?>>();
		
		for (UUID id : getKeySet()) {
			Set<TkRefexAbstractMember<?>> conceptMembers = membersAnnotationMap.get(id);
			retSet.addAll(conceptMembers);
		}
		
		return retSet;
	}

	public void clear() {
		membersAnnotationMap.clear();
	}

	public boolean alreadyProcessed(TkRefexAbstractMember<?> member) {
		// Have already processed?
		if (membersAnnotationMap.containsKey(member.getComponentUuid())) {
			Set<TkRefexAbstractMember<?>> members = membersAnnotationMap.get(member.getComponentUuid());

			if (members.contains(member)) {
				return true;
			}
		}
		
		return false;
	}

	public boolean containsKey(UUID concept) {
		return membersAnnotationMap.containsKey(concept);
	}

	public void addNewMember(TkRefexAbstractMember<?> member) {
		if (!membersAnnotationMap.containsKey(member.getComponentUuid())) {
			HashSet<TkRefexAbstractMember<?>> newListOfMembers = new HashSet<TkRefexAbstractMember<?>>();
			newListOfMembers.add(member);
			membersAnnotationMap.put(member.getComponentUuid(), newListOfMembers);
		} else {
			membersAnnotationMap.get(member.getComponentUuid()).add(member);	
		}
	}

	public Set<TkRefexAbstractMember<?>> getDuplicateMembers(TkRefexAbstractMember<?> member) {
		Set<TkRefexAbstractMember<?>> currentMembers = membersAnnotationMap.get(member.getComponentUuid());
		Set<TkRefexAbstractMember<?>> duplicates = new HashSet<TkRefexAbstractMember<?>>();
		
		for (TkRefexAbstractMember<?> m : currentMembers) {
			if (member.getStatusUuid().equals(m.getStatusUuid()) && 
				member.getAuthorUuid().equals(m.getAuthorUuid()) && 
				member.getPathUuid().equals(m.getPathUuid())&& 
				member.getTime() == m.getTime()) 
			{
				String newMemberVal = ((TkRefsetStrMember)member).getString1();
				String existingMemberVal = ((TkRefsetStrMember)m).getString1();
				
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
			Set<TkRefexAbstractMember<?>> conceptMembers = membersAnnotationMap.get(id);

			for (TkRefexAbstractMember<?> m : conceptMembers) {
				if (m.getRefexUuid().equals(WorkflowHelper.getWorkflowRefsetUid())) {
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

	public Set<TkRefexAbstractMember<?>> getAllWfHxMembers() {
		Set<TkRefexAbstractMember<?>> retSet = new HashSet<TkRefexAbstractMember<?>>();
		
		for (UUID id : getKeySet()) {
			Set<TkRefexAbstractMember<?>> conceptMembers = membersAnnotationMap.get(id);

			for (TkRefexAbstractMember<?> m : conceptMembers) {
				if (m.getRefexUuid().equals(WorkflowHelper.getWorkflowRefsetUid())) {
					retSet.add(m);
				}
			}
		}
		
		return retSet;
	}
}
