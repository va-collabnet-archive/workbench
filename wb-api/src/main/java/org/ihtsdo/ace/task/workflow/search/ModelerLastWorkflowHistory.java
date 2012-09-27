package org.ihtsdo.ace.task.workflow.search;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;

import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.ace.task.gui.component.WorkflowConceptVersion;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

@BeanList(specs = { @Spec(directory = "tasks/ide/search", type = BeanType.TASK_BEAN),
                   @Spec(directory = "search/workflow", type = BeanType.TASK_BEAN) })

public class ModelerLastWorkflowHistory extends AbstractWorkflowHistorySearchTest {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    /**
     * Property name for the Modeler being searched.
     */
     private WorkflowConceptVersion testModeler = null;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.testModeler);

    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException, TerminologyException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
         Object obj = in.readObject();
         if (obj instanceof WorkflowConceptVersion) {
          this.testModeler = (WorkflowConceptVersion) obj;
         } else {
            this.testModeler = null;
         }
			if (this.testModeler == null)
			{
				ViewCoordinate vc = Terms.get().getActiveAceFrameConfig().getViewCoordinate();
				
				ConceptVersionBI leadModeler = WorkflowHelper.getLeadModeler(vc);

				if (leadModeler != null)
					this.testModeler = new WorkflowConceptVersion(leadModeler);
				else
					this.testModeler = new WorkflowConceptVersion(WorkflowHelper.lookupModeler(WorkflowHelper.getModelerKeySet().iterator().next()));
			}
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    @Override
	public boolean test(SortedSet<WorkflowHistoryJavaBean> wfHistory)
			throws TaskFailedException {

    	 try {
             UUID modUUID =  testModeler.getPrimUuid();

			 if (modUUID != null && getCurrent(wfHistory).getModeler().equals(modUUID))
				return true;
			else
				return false;

        } catch (Exception e) {

			throw new TaskFailedException("Couldn't read search Modeler!");
		}

    }



	public WorkflowHistoryJavaBean getCurrent (Set<WorkflowHistoryJavaBean> wfHistory) {

		WorkflowHistoryJavaBean current = null;

		for (WorkflowHistoryJavaBean wfHistoryItem : wfHistory) {
			if (current == null || wfHistoryItem.getEffectiveTime().longValue() > current.getEffectiveTime().longValue())
				current = wfHistoryItem;
		}


		return current;
	}



    public WorkflowConceptVersion getTestModeler() {
        return testModeler;
    }

    public void setTestModeler(WorkflowConceptVersion testModeler) {
        this.testModeler = testModeler;
    }


    public UUID getCurrentTestUUID() throws TaskFailedException {
        return testModeler.getPrimUuid();
    }

    @Override
	public int getTestType() {
		return currentModeler;
	}

	@Override
	public Object getTestValue() {
		return getTestModeler();
	}
}
