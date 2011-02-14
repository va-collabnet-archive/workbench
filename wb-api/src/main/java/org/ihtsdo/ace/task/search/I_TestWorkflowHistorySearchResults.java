package org.ihtsdo.ace.task.search;

import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.bpa.process.TaskFailedException;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;

public interface I_TestWorkflowHistorySearchResults extends
		I_TestSearchResults {
    public static final int currentAction = 0;							// 0
    public static final int hasAction = currentAction + 1;				// 1
    public static final int currentModeler = hasAction + 1;			// 2
    public static final int hasModeler = currentModeler + 1;			// 3
    public static final int path = hasModeler + 1;						// 4
    public static final int hierarchy = path + 1;						// 5
    public static final int currentState = hierarchy + 1;				// 6
    public static final int hasState = currentState + 1;				// 7
    public static final int timestampBefore = hasState + 1;			// 8
    public static final int timestampAfter = timestampBefore + 1;		// 9

	
    public abstract int getTestType();

    public abstract Object getTestValue();
    
    public abstract boolean test(Set<WorkflowHistoryJavaBean> wfHistory) throws TaskFailedException;

}
