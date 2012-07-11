package org.ihtsdo.cs.econcept.workflow;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.logging.Level;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;


// CLEAN UP of previous change set error where all workflow history was added to the cs file, 
// not just those that were modified as part of commit
// Therefore, only process items that are post on or after 08/31/11 as previous records imported via database build
public class WfPostLastReleaseFilter extends AbstractWfChangeSetFilter {
	
	SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd");
	long  lastReleaseDate;
	
	public WfPostLastReleaseFilter(String filePath) {
		super(filePath);

		try {
			lastReleaseDate = formatter.parse("2011.08.01").getTime();
		} catch (ParseException e) {
			AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	@Override
	public boolean scrubMembers(HashSet<TkRefexAbstractMember<?>> wfMembersToCommit) {
		try {
			wfMembersToProcess = new HashSet<TkRefexAbstractMember<?>>();

			// Don't print those that do not meet release date filter
			for (TkRefexAbstractMember<?> member : wfMembersToCommit) {
				if (member.getRefexUuid().equals(WorkflowHelper.getWorkflowRefsetUid())) {
					WorkflowHistoryJavaBean bean = WorkflowHelper.populateWorkflowHistoryJavaBean(member);
					
					if (bean.getWorkflowTime() >= lastReleaseDate) {
						wfMembersToProcess.add(member);
					} 
				} else {
					// Non wfHx refset member, so don't filter
					wfMembersToProcess.add(member);
				}
			}
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}

	@Override
	public HashSet<TkRefexAbstractMember<?>> getApprovedMembers() {
		return wfMembersToProcess;
	}
}