package org.dwfa.ace.task.refset.refresh;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import javax.swing.JOptionPane;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * The RefreshRefsetSpecCompareTask uses the information 
 * collected in the RefreshRefsetSpecWizardTask task to create a list of differences 
 * between the selected Refset and the selected version of SNOMED. 
 * 
 * @author Perry Reid
 * @version 1, November 2009
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec", type = BeanType.TASK_BEAN) })
public class RefreshRefsetSpecCompareTask extends AbstractTask {


    /*
     * -----------------------
     * Properties
     * -----------------------
     */
	private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    private Condition condition;
    private I_TermFactory termFactory;

    /*
     * -----------------------
     * Serialization Methods
     * -----------------------
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            if (objDataVersion >= 1) {
                // Read version 1 data fields...
            } else {
                // Set version 1 default values...
            }
            // Initialize transient properties...
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }


    /**
     * Handles actions required by the task after normal task completion (such
     * as moving a process to another user's input queue).
     * 
     * @return void
     * @param process The currently executing Workflow process
     * @param worker The worker currently executing this task
     * @exception TaskFailedException Thrown if a task fails for any reason.
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }



    /**
     * Performs the primary action of the task, which in this case is Identify all the 
     * differences between the selected Refset Spec and the selected version of SNOMED
     * 
     * @return The exit condition of the task
     * @param process The currently executing Workflow process
     * @param worker The worker currently executing this task
     * @exception TaskFailedException Thrown if a task fails for any reason.
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(final I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

    	// Generate a list of differences...
    	
        try {
			RefreshRefsetSpecCompareTask.this.setCondition(Condition.ITEM_CANCELED);
			RefreshRefsetSpecCompareTask.this.setCondition(Condition.ITEM_SKIPPED);
			RefreshRefsetSpecCompareTask.this.setCondition(Condition.ITEM_COMPLETE);
               
                  
        	JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
        			"RefreshRefsetSpecCompareTask Message:  Made it this far! Condition=" 
        			+ getCondition().toString(), "DEBUG Message",
        			JOptionPane.INFORMATION_MESSAGE);

        	return getCondition();
        } catch (Exception ex) {
        	ex.printStackTrace();
        	throw new TaskFailedException(ex);
        }
    	
    }

    public void setCondition(Condition c) {
        condition = c;
    }

    public Condition getCondition() {
        return condition;
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.ITEM_CANCELED_OR_COMPLETE;
    }

}
