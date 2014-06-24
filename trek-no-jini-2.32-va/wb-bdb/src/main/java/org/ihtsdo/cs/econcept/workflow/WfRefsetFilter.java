package org.ihtsdo.cs.econcept.workflow;

import java.util.HashSet;

import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

// Only import Workflow Refsets
public class WfRefsetFilter extends AbstractWfChangeSetFilter {
	public static int nonWfRxCounter = 0;
	
	WfRefsetFilter(String filePath) {
		super(filePath);
	}
	
	@Override
	public boolean scrubMembers(HashSet<TkRefexAbstractMember<?>> wfMembersToCommit) {
		wfMembersToProcess = new HashSet<TkRefexAbstractMember<?>>();
		
		for (TkRefexAbstractMember<?> member : wfMembersToCommit) {
			if (WorkflowHelper.getRefsetUidList().contains(member.refsetUuid)) {
				wfMembersToProcess.add(member);
			} 
		}

		return true;
	}

	@Override
	public HashSet<TkRefexAbstractMember<?>> getApprovedMembers() {
		return wfMembersToProcess;
	}
}