package org.ihtsdo.ace.task.workflow.search;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.UUID;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.ace.task.gui.component.WorkflowConceptVersion;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
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
    private WorkflowConceptVersion testAction = null;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.testAction);

    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        
        if (objDataVersion == 1) 
        {
        	Object obj = in.readObject();
        	
            if (obj instanceof WorkflowConceptVersion) {
            	this.testAction = (WorkflowConceptVersion) obj;
            } else {
            	this.testAction = null;
            }
        	
        	if (this.testAction == null) 
        	{
                try 
                {
                    for (ConceptVersionBI  action : Terms.get().getActiveAceFrameConfig().getWorkflowActions()) {
                        this.testAction = new WorkflowConceptVersion(action);
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
    public boolean test(SortedSet<WorkflowHistoryJavaBean> workflow) throws TaskFailedException {
        UUID testUUID = getCurrentTestUUID();

        if (testUUID == null) {
            throw new TaskFailedException("Failed to complete test.  UUID null.");
        }
        WorkflowHistoryJavaBean bean = getCurrent(workflow);
        if (bean.getAction().equals(testUUID)) {
            return true;
        }

        return false;
    }

    public WorkflowHistoryJavaBean getCurrent(SortedSet<WorkflowHistoryJavaBean> wfHistory) {

        WorkflowHistoryJavaBean current = null;

        for (WorkflowHistoryJavaBean wfHistoryItem : wfHistory) {
            if (current == null || wfHistoryItem.getEffectiveTime().longValue() > current.getEffectiveTime().longValue()) {
                current = wfHistoryItem;
            }
        }

        return current;
    }

    public UUID getCurrentTestUUID() throws TaskFailedException {
        return testAction.getPrimUuid();
    }

    public WorkflowConceptVersion getTestAction() {
        return testAction;
    }

    public void setTestAction(WorkflowConceptVersion testAction) {
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
