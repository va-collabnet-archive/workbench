package org.ihtsdo.cs.econcept.workflow;

import java.util.HashSet;

import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

// Only import Workflow Refsets
public class WfRefsetFilter extends AbstractWfChangeSetFilter {
	public static int nonWfRxCounter = 0;
	
	WfRefsetFilter(String filePath) {
		super(filePath);
	}
	
	@Override
	public boolean scrubMembers(HashSet<TkRefsetAbstractMember<?>> wfMembersToCommit) {
		wfMembersToProcess = new HashSet<TkRefsetAbstractMember<?>>();
		
		for (TkRefsetAbstractMember<?> member : wfMembersToCommit) {
			if (WorkflowHelper.getRefsetUidList().contains(member.refsetUuid)) {
				wfMembersToProcess.add(member);
			} 
		}

		return true;
	}

	@Override
	public HashSet<TkRefsetAbstractMember<?>> getApprovedMembers() {
		return wfMembersToProcess;
	}
}