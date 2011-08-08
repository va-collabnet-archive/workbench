package org.ihtsdo.ace.task.workflow.search;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;

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
public class StateLastWorkflowHistory extends AbstractWorkflowHistorySearchTest {

    private static final long serialVersionUID = 1;
    private static final int dataVersion = 1;
    /**
     * Property name for the State being searched.
     */
    private WorkflowConceptVersion testState = null;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(testState);

    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            Object obj = in.readObject();

            if (obj instanceof WorkflowConceptVersion) {
                this.testState = (WorkflowConceptVersion) obj;
            } else {
                try {
                    if (!Terms.get().getActiveAceFrameConfig().getWorkflowStates().isEmpty()) {
                        this.testState = new WorkflowConceptVersion(Terms.get().getActiveAceFrameConfig().getWorkflowStates().first());
                    }
                } catch (Exception e) {
                    AceLog.getAppLog().log(Level.WARNING, "Error in initializing drop-down value", e);
                }
            }
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public boolean test(SortedSet<WorkflowHistoryJavaBean> wfHistory) throws TaskFailedException {

        UUID testUUID = getCurrentTestUUID();

        if (testUUID == null) {
            throw new TaskFailedException("Failed to complete test.  UUID null.");
        }

        if (getCurrent(wfHistory).getState().equals(testUUID)) {
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

    public UUID getCurrentTestUUID() throws TaskFailedException {
        return testState.getPrimUuid();
    }

    public WorkflowConceptVersion getTestState() {
        return testState;
    }

    public void setTestState(WorkflowConceptVersion testState) {
        this.testState = testState;
    }

    @Override
    public int getTestType() {
        return currentState;
    }

    @Override
    public Object getTestValue() {
        return getTestState();
    }
}
