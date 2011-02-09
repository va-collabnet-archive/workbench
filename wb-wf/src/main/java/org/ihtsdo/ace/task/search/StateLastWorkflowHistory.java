package org.ihtsdo.ace.task.search;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;

@BeanList(specs = { @Spec(directory = "tasks/ide/search", type = BeanType.TASK_BEAN),
                   @Spec(directory = "search/workflow", type = BeanType.TASK_BEAN) })

public class StateLastWorkflowHistory extends AbstractWorkflowHistorySearchTest {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    /**
     * Property name for the State being searched.
     */
     private I_GetConceptData testState = null;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(testState);

    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            this.testState = (I_GetConceptData) in.readObject();
			if (this.testState == null)
			{
				try {
               Iterator<? extends I_GetConceptData> itr = Terms.get().getActiveAceFrameConfig().getWorkflowStates().iterator();
               if (itr.hasNext()) {
                  this.testState = itr.next();
               }
	            } catch (Exception e) {
	            	AceLog.getAppLog().log(Level.WARNING, "Error in initializing drop-down value", e);
	            }
			}
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    @Override
    public boolean test(WorkflowHistoryJavaBean bean, I_ConfigAceFrame frameConfig) throws TaskFailedException {
    	return false;
    }

    public boolean test(Set<WorkflowHistoryJavaBean> wfHistory) throws TaskFailedException {
    
    	UUID testUUID = getCurrentTestUUID();
    
    	if (testUUID == null)
    		throw new TaskFailedException("Failed to complete test.  UUID null.");
    	
		if (getCurrent(wfHistory).getState().equals(testUUID)) 
			return true;
		
		return false;
    }
    
    public WorkflowHistoryJavaBean getCurrent (Set<WorkflowHistoryJavaBean> wfHistory) {

		WorkflowHistoryJavaBean current = null;
		
		for (WorkflowHistoryJavaBean wfHistoryItem : wfHistory) {
			if (current == null || wfHistoryItem.getEffectiveTime() > current.getEffectiveTime()) 
				current = wfHistoryItem;
		}
		
		return current;
	}

    private UUID getCurrentTestUUID() throws TaskFailedException {
    	return testState.getPrimUuid();
	}
    
    public I_GetConceptData getTestState() {
        return testState;
    }

    public void setTestState(I_GetConceptData testState) {
        this.testState = testState;
    }
}
