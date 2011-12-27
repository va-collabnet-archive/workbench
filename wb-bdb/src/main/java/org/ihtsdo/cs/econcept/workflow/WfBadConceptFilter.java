package org.ihtsdo.cs.econcept.workflow;

import java.util.HashSet;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;

// If concept doesn't have descriptions, ignore it
public class WfBadConceptFilter extends AbstractWfChangeSetFilter {
	WfBadConceptFilter(String filePath) {
		super(filePath);  
	}

	@Override
	public boolean scrubMembers(HashSet<TkRefsetAbstractMember<?>> wfMembersToCommit) {
		try {
			wfMembersToProcess = new HashSet<TkRefsetAbstractMember<?>>();
			
			for (TkRefsetAbstractMember<?> member : wfMembersToCommit) {
				I_GetConceptData con = Terms.get().getConcept(member.getComponentUuid());
				
				if (con != null && con.getDescriptions() != null && con.getDescriptions().size() > 0) {
					wfMembersToProcess.add(member);
				} else {
					wfMembersToIgnore.add(member);
					printError(member);
				}
			}
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
		}
		return true;
	}

	@Override
	public HashSet<TkRefsetAbstractMember<?>> getApprovedMembers() {
		return wfMembersToProcess;
	}

}
