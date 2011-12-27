package org.ihtsdo.cs.econcept.workflow;

import java.util.HashSet;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;

public interface I_WfChangeSetScrubber {

	boolean scrubMembers(HashSet<TkRefsetAbstractMember<?>> wfMembersToCommit);
	
	HashSet<TkRefsetAbstractMember<?>> getApprovedMembers();
}
