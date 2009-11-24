package org.dwfa.ace.task.refset.refresh;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Insets;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.wfdetailsSheet.ClearWorkflowDetailsSheet;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * This task prepares the Workflow Details Sheet to display the PanelSnomedVersion
 * panel where the user will be asked to select the version of SNOMED to use in the 
 * Refresh Refset process.   
 * 
 * @author Perry Reid
 * @version 1.0, November 2009 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf", type = BeanType.TASK_BEAN) })
public class SetWFDSheetToSnomedVersionPanelTask extends AbstractTask {

    /* -----------------------
     * Properties 
     * -----------------------
     */
	// Serialization Properties 
    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 2;

	// Task Attribute Properties     
	private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
	private String positionSetPropName = ProcessAttachmentKeys.POSITION_LIST.getAttachmentKey();

	// Other Properties 
    private transient Exception ex = null;

    /* -----------------------
     * Serialization Methods
     * -----------------------
     */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(profilePropName);
		out.writeObject(positionSetPropName);
	}

	 private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
	     int objDataVersion = in.readInt();
	     if (objDataVersion <= dataVersion) {
	    	 if (objDataVersion >= 1) {
	    		 // Read version 1 data fields
	    		 profilePropName = (String) in.readObject();
	    		 positionSetPropName = (String) in.readObject();
	    	 } else {
	    		 // Set version 1 default values
	        	 profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
	        	 positionSetPropName = ProcessAttachmentKeys.POSITION_LIST.getAttachmentKey();
	    	 }
	         // Initialize transient properties...
	         ex = null;
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
	 * @see 		org.dwfa.bpa.process.I_DefineTask#evaluate(
	 * 				org.dwfa.bpa.process.I_EncodeBusinessProcess,
	 *      		org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(final I_EncodeBusinessProcess process,
			final I_Work worker) throws TaskFailedException {
		try {
			ex = null;
			if (SwingUtilities.isEventDispatchThread()) {
				doRun(process, worker);
			} else {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						doRun(process, worker); 
					}
				});
			}
		} catch (InterruptedException e) {
			throw new TaskFailedException(e);
		} catch (InvocationTargetException e) {
			throw new TaskFailedException(e);
		} catch (IllegalArgumentException e) {
			throw new TaskFailedException(e);
		} 
		if (ex != null) {
			throw new TaskFailedException(ex);
		}
		return Condition.CONTINUE;
	}


	private void doRun(final I_EncodeBusinessProcess process,
			final I_Work worker) {
		I_ConfigAceFrame config;
		try {
			config = (I_ConfigAceFrame) process.getProperty(getProfilePropName());
			
			// Clear the Workflow Details Sheet 
			ClearWorkflowDetailsSheet clear = new ClearWorkflowDetailsSheet();
			clear.setProfilePropName(getProfilePropName());
			clear.evaluate(process, worker);
			
			// Create a new panel to add to the Workflow Details Sheet
			JPanel workflowDetailsSheet = config.getWorkflowDetailsSheet();
	        int width = 475;
	        int height = 590;
	        workflowDetailsSheet.setSize(width, height);
	        workflowDetailsSheet.setLayout(new BorderLayout());
	        PanelSnomedVersion newPanel = new PanelSnomedVersion(config); 
	        
 
	        /*----------------------------------------------------------------------------------
	         *  Initialize the fields on this panel with the previously entered values (if any).
	         * ----------------------------------------------------------------------------------
	         */
	        // Position Set Field Initialization 	        
			try {
				Set<I_Position> previousPositions = null;
//		        if (isKeyDefined(process, positionSetPropName.substring(3))) {
		        	previousPositions = (Set<I_Position>) process.getProperty(positionSetPropName);
		        	if (previousPositions != null ) {
		        		newPanel.setPositionSet(previousPositions); 
		        	}	  
//		        }
			} catch (NullPointerException e) {
				//TODO  Just ignore the NPE for now - remove this when you add the 
				//      isPropertyDefined class back in.  
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IntrospectionException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}

	        
	        /*----------------------------------------------------------------------------------
	         *  Add the initialized panel to the Workflow Details Sheet
	         * ----------------------------------------------------------------------------------
	         */
	        workflowDetailsSheet.add(newPanel, BorderLayout.NORTH);
	        workflowDetailsSheet.repaint();
	        
		} catch (Exception e) {
			ex = e;
		}
	}

	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
	 */
	public Collection<Condition> getConditions() {
		return AbstractTask.CONTINUE_CONDITION;
	}

	/**
	 *  This method returns the name of the Working Profile property name  
	 * @return
	 */
	public String getProfilePropName() {
		return profilePropName;
	}

	/**
	 *  This method sets the name of the Working Profile property name  
	 * @param profilePropName
	 */
	public void setProfilePropName(String profilePropName) {
		this.profilePropName = profilePropName;
	}

	public String getPositionSetPropName() {
		return positionSetPropName;
	}
	public void setPositionSetPropName(String positionSetPropName) {
		this.positionSetPropName = positionSetPropName;
	}

//	public boolean isKeyDefined(I_EncodeBusinessProcess process, String keyName) {	
//		String propertyName = new String(); 
//		if (keyName.startsWith("A: ")) {
//			propertyName = keyName.substring(3);
//		} else {
//			propertyName = keyName;
//		}		
//		Collection<String> listOfKeys = process.getAttachmentKeys(); 
//		return listOfKeys.contains(propertyName);
//	}


}
