package org.ihtsdo.ace.task.search;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;

@BeanList(specs = {
    @Spec(directory = "tasks/ide/search", type = BeanType.TASK_BEAN),
    @Spec(directory = "search/workflow", type = BeanType.TASK_BEAN)})
public class ActionLastWorkflowHistory extends AbstractWorkflowHistorySearchTest {

    private static final long serialVersionUID = 1;
    private static final int dataVersion = 1;
    /**
     * Property name for the Action being searched.
     */
    private I_GetConceptData testAction = null;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.testAction);

    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        
        if (objDataVersion == 1) 
        {
        	Object obj = in.readObject();
        	
            if (obj instanceof I_GetConceptData) {
            	this.testAction = (I_GetConceptData) obj;
            } else {
            	this.testAction = null;
            }
        	
        	if (this.testAction == null) 
        	{
                try 
                {
                    Iterator<? extends I_GetConceptData> wfitr = Terms.get().getActiveAceFrameConfig().getWorkflowActions().iterator();
                    
                    if (wfitr.hasNext()) {
                        this.testAction = Terms.get().getActiveAceFrameConfig().getWorkflowActions().iterator().next();
                    }
                } catch (Exception e) {
                    AceLog.getAppLog().alertAndLogException(e);
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

    @Override
    public boolean test(Set<WorkflowHistoryJavaBean> wfHistory) throws TaskFailedException {
        UUID testUUID = getCurrentTestUUID();

        if (testUUID == null) {
            throw new TaskFailedException("Failed to complete test.  UUID null.");
        }
        WorkflowHistoryJavaBean bean = getCurrent(wfHistory);
        if (bean.getAction().equals(testUUID)) {
            return true;
        }

        return false;
    }

    public WorkflowHistoryJavaBean getCurrent(Set<WorkflowHistoryJavaBean> wfHistory) {

        WorkflowHistoryJavaBean current = null;

        for (WorkflowHistoryJavaBean wfHistoryItem : wfHistory) {
            if (current == null || wfHistoryItem.getEffectiveTime().longValue() > current.getEffectiveTime().longValue()) {
                current = wfHistoryItem;
            }
        }

        return current;
    }

    private UUID getCurrentTestUUID() throws TaskFailedException {
        return testAction.getPrimUuid();
    }

    public I_GetConceptData getTestAction() {
        return testAction;
    }

    public void setTestAction(I_GetConceptData testAction) {
        this.testAction = testAction;
    }

	@Override
	public int getTestType() {
		return currentAction;
	}

	@Override
	public Object getTestValue() {
		return getTestAction();
	}
}
