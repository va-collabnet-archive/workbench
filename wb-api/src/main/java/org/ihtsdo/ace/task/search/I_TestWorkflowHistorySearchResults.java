package org.ihtsdo.ace.task.search;

import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.bpa.process.TaskFailedException;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;

public interface I_TestWorkflowHistorySearchResults extends
		I_TestSearchResults {
    public boolean test(WorkflowHistoryJavaBean bean, I_ConfigAceFrame frameConfig) throws TaskFailedException;
    
    public abstract boolean test(Set<WorkflowHistoryJavaBean> wfHistory) throws TaskFailedException;

}
