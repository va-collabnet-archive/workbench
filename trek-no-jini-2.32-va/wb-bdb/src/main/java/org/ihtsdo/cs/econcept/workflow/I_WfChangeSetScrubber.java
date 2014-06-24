package org.ihtsdo.cs.econcept.workflow;

import java.util.HashSet;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;

public interface I_WfChangeSetScrubber {

	boolean scrubMembers(HashSet<TkRefexAbstractMember<?>> wfMembersToCommit);
	
	HashSet<TkRefexAbstractMember<?>> getApprovedMembers();
}
