package org.ihtsdo.ace.task.workflow.actselect;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefsetWriter;



@BeanList(specs = { @Spec(directory = "tasks/workflow/advancingWF", type = BeanType.TASK_BEAN) })
public class AdvanceWorkflow extends AbstractTask {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }
    
    

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    @SuppressWarnings("unchecked")
	public Condition evaluate(I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
    	try {
            UUID selectedActionUid = (UUID) worker.readAttachement(ProcessAttachmentKeys.SELECTED_WORKFLOW_ACTION.name());
            List<WorkflowHistoryJavaBean> availableActions = (List<WorkflowHistoryJavaBean>) worker.readAttachement(ProcessAttachmentKeys.POSSIBLE_WF_ACTIONS_LIST.name());
            
            
    		WorkflowHistoryRefsetWriter writer = new WorkflowHistoryRefsetWriter(true);
    		
    		for (WorkflowHistoryJavaBean bean : availableActions)
    		{
    			if (bean.getAction().equals(selectedActionUid))
    			{
    				writer.updateWorkflowHistory(bean);
        	        return Condition.CONTINUE;
    			}
    		}

    		throw new Exception("Should have found expected Action in List");
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }
    
    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }

}
