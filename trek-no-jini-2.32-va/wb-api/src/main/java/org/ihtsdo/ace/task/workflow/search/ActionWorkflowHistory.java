package org.ihtsdo.ace.task.workflow.search;

import java.io.IOException;
import java.io.ObjectOutputStream;
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
public class ActionWorkflowHistory extends AbstractWorkflowHistorySearchTest {

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
                try {
                    if (!Terms.get().getActiveAceFrameConfig().getWorkflowActions().isEmpty()) {
            		this.testAction = new WorkflowConceptVersion(Terms.get().getActiveAceFrameConfig().getWorkflowActions().first());
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
    public boolean test(SortedSet<WorkflowHistoryJavaBean> wfHistory) throws TaskFailedException {
        UUID testUUID = getCurrentTestUUID();

        if (testUUID == null) {
            throw new TaskFailedException("Failed to complete test.  UUID null.");
        }

        //If any item in the list passes the filter, return true.
        for (WorkflowHistoryJavaBean wfHistoryItem : wfHistory) {
            if (wfHistoryItem.getAction().equals(testUUID)) {
                return true;
            }
        }

        return false;
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
		return hasAction;
	}

	@Override
	public Object getTestValue() {
		return getTestAction();
	}
}
