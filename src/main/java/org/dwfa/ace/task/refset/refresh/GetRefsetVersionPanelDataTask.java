package org.dwfa.ace.task.refset.refresh;

import java.awt.Component;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

import org.dwfa.ace.task.refset.refresh.PanelRefsetVersion;

/**
 * This task collects the Refset Spec Version data entered on the 
 * PanelRefsetVersion panel currently displayed in the Workflow Details Sheet 
 * and verifies that the required data has been filled in.
 * 
 * @author Perry Reid
 * @version 1.0, November 2009 
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf", type = BeanType.TASK_BEAN) })
public class GetRefsetVersionPanelDataTask extends AbstractTask {

	
	/* -----------------------
     * Properties 
     * -----------------------
     */
	// Serialization Properties 
	private static final long serialVersionUID = 1;
	private static final int dataVersion = 1;

	// Task Attribute Properties 
	private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
	private String positionSetPropName = ProcessAttachmentKeys.POSITION_SET.getAttachmentKey();
	

    /* -----------------------
     * Serialization Methods
     * -----------------------
     */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(profilePropName);
		out.writeObject(positionSetPropName);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion <= dataVersion) {
            if (objDataVersion >= 1) {
                // Read version 1 data fields
				profilePropName = (String) in.readObject();
				positionSetPropName = (String) in.readObject();
            } 
			// Initialize transient properties...
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	
	/**
	 * Handles actions required by the task after normal task completion (such as moving a 
	 * process to another user's input queue).   
	 * @return  	void
	 * @param   	process	The currently executing Workflow process
	 * @param 		worker	The worker currently executing this task 
	 * @exception  	TaskFailedException Thrown if a task fails for any reason.
	 * @see 		org.dwfa.bpa.process.I_DefineTask#complete(
	 * 				org.dwfa.bpa.process.I_EncodeBusinessProcess,
	 *      		org.dwfa.bpa.process.I_Work)
	 */
	public void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		// Nothing to do
	}

	/**
	 * Performs the primary action of the task, which in this case is to gather and 
	 * validate data that has been entered by the user on the Workflow Details Sheet.
	 * @return  	The exit condition of the task
	 * @param   	process	The currently executing Workflow process
	 * @param 		worker	The worker currently executing this task 
	 * @exception  	TaskFailedException Thrown if a task fails for any reason.
	 * @see 		org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
	 *      		org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(final I_EncodeBusinessProcess process,
			final I_Work worker) throws TaskFailedException {
		try {
			I_ConfigAceFrame config = (I_ConfigAceFrame) process.readProperty(getProfilePropName());
			JPanel workflowDetailsSheet = config.getWorkflowDetailsSheet();
			for (Component c: workflowDetailsSheet.getComponents()) {
				if (PanelRefsetVersion.class.isAssignableFrom(c.getClass())) {
					PanelRefsetVersion panel = (PanelRefsetVersion) c;
					
                    // --------------------------------
                    // Retrieve values from the panel 
                    // --------------------------------
					Set<I_Position> positionSet = null;
					positionSet = panel.getPositionSet();	
					
                    // -----------------------------------------
                    // Verify required fields are present and 
                    // use the values retrieved from this panel 
                    // -----------------------------------------
					if (positionSet == null || positionSet.isEmpty() ) {
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), 
                        		"You must select a version for this Refset Spec. ",
                                "", JOptionPane.ERROR_MESSAGE);
                        return Condition.ITEM_CANCELED;
					} else {
						process.setProperty(positionSetPropName, positionSet);
						return Condition.ITEM_COMPLETE;
					}
				}
			}
		} catch (InvocationTargetException e) {
			throw new TaskFailedException(e);
		} catch (IllegalArgumentException e) {
			throw new TaskFailedException(e);
		} catch (IntrospectionException e) {
			throw new TaskFailedException(e);
		} catch (IllegalAccessException e) {
			throw new TaskFailedException(e);
		} 
		throw new TaskFailedException("Cannot find PanelRefsetVersion.");
	}


    /**
     * This method implements the interface method specified by: getConditions() in I_DefineTask
     * @return The possible evaluation conditions for this task.
	 * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
	public Collection<Condition> getConditions() {
		return AbstractTask.ITEM_CANCELED_OR_COMPLETE;
	}

	public String getPositionSetPropName() {
		return positionSetPropName;
	}

	public void setPositionSetPropName(String positionSetPropName) {
		this.positionSetPropName = positionSetPropName;
	}

	public String getProfilePropName() {
		return profilePropName;
	}

	public void setProfilePropName(String profilePropName) {
		this.profilePropName = profilePropName;
	}



}
