package org.dwfa.ace.task.refset.refresh;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.refset.spec.RefsetSpecWizardTask;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.Priority;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.bpa.tasks.util.FileContent;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * This task collects the Refresh Refset Spec Params data entered on the 
 * PanelRefsetAndParameters panel currently displayed in the Workflow 
 * Details Sheet and verifies that the required data has been filled in.
 * 
 * @author Perry Reid
 * @version 1.0, November 2009 
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf", type = BeanType.TASK_BEAN) })
public class GetRefreshRefsetSpecParamsPanelDataTask extends AbstractTask {

    /* -----------------------
     * Properties 
     * -----------------------
     */
	// Serialization Properties 
    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;
    
	// Task Attribute Properties         
	private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();  
	private String nextUserTermEntryPropName = ProcessAttachmentKeys.NEXT_USER.getAttachmentKey();
    private String refsetUuidPropName = ProcessAttachmentKeys.WORKING_REFSET.getAttachmentKey();
    private String commentsPropName = ProcessAttachmentKeys.MESSAGE.getAttachmentKey();
    private String editorUuidPropName = ProcessAttachmentKeys.EDITOR_UUID.getAttachmentKey();
    private String ownerUuidPropName = ProcessAttachmentKeys.OWNER_UUID.getAttachmentKey();
    private String fileAttachmentsPropName = ProcessAttachmentKeys.FILE_ATTACHMENTS.getAttachmentKey();
    		
	// Other Properties 
    private I_TermFactory termFactory;

    
    /* -----------------------
     * Serialization Methods
     * -----------------------
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(profilePropName);
        out.writeObject(nextUserTermEntryPropName);
        out.writeObject(commentsPropName);
        out.writeObject(refsetUuidPropName);
        out.writeObject(editorUuidPropName);
        out.writeObject(ownerUuidPropName);
        out.writeObject(fileAttachmentsPropName);
    }
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        
        if (objDataVersion <= dataVersion) {
            if (objDataVersion >= 1) {
                // Read version 1 data fields...
            	profilePropName = (String) in.readObject();
                nextUserTermEntryPropName = (String) in.readObject();
                commentsPropName = (String) in.readObject();
                refsetUuidPropName = (String) in.readObject();
            	editorUuidPropName = (String) in.readObject();
            	ownerUuidPropName = (String) in.readObject();
            	fileAttachmentsPropName = (String) in.readObject();
            }
            // Initialize transient properties 
            
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
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
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
    public Condition evaluate(final I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

        try {
            termFactory = LocalVersionedTerminology.get();

            I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
            JPanel workflowDetailsSheet = config.getWorkflowDetailsSheet();
            
            for (Component c : workflowDetailsSheet.getComponents()) {
                if (PanelRefsetAndParameters.class.isAssignableFrom(c.getClass())) {
                	PanelRefsetAndParameters panel = (PanelRefsetAndParameters) c;
                	
                    // Retrieve values from the panel / environment

                    I_GetConceptData refset = panel.getRefset();
                    I_GetConceptData editor = panel.getEditor();
                    String comments = panel.getComments();
                    Calendar deadline = panel.getDeadline();
                    String priority = panel.getPriority();
                    HashSet<File> fileAttachments = panel.getAttachments();
                    I_GetConceptData owner = config.getDbConfig().getUserConcept();

                    // -----------------------------------------
                    // VERIFY ALL REQUIRED FIELDS AND STORE
                    // THE ENTERED DATA INTO PROPERTY KEYS
                    // -----------------------------------------
                    
                    // -----------------------------------------
                    // Refset Field is required 
                    // -----------------------------------------
                   if (refset == null) {
                   		// Warn the user that Refset is required. 
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "You must select a refset. ",
                            "", JOptionPane.ERROR_MESSAGE);
                        return Condition.ITEM_CANCELED;
                    } else {
                    	// Set the Refset property 
                        process.setSubject("Refresh Refset : " + refset.getInitialText());
                        process.setName("Refresh Refset : " + refset.getInitialText());
                        process.setProperty(refsetUuidPropName, refset.getUids().iterator().next());
                    }
                    
                    
                    // -----------------------------------------
                    // Editor Field is required! 
                    // -----------------------------------------
                    if (editor == null) {
                    	// Warn the user that Editor is required. 
                    	JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                    			"You must select an editor. ", "", JOptionPane.ERROR_MESSAGE);
                    	return Condition.ITEM_CANCELED;                         
                    } else {
                       	// Set the Editor property 
                    	process.setProperty(editorUuidPropName, editor.getUids().iterator().next() );
                        
                        // Set the WF's Next User based on selected Editor 
                        RefsetSpecWizardTask wizard = new RefsetSpecWizardTask();                    
                        String inboxAddress = wizard.getInbox(editor);
                        if (inboxAddress == null) {
                            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                                "Refresh Refset process cannot continue... The selected editor has no assigned inbox : "
                                    + editor, "", JOptionPane.ERROR_MESSAGE);
                            return Condition.ITEM_CANCELED;
                        } else {
                            process.setDestination(inboxAddress);
                            process.setProperty(nextUserTermEntryPropName, inboxAddress);                       
                        }     
                    }


                    // -----------------------------------------
                    // Deadline Field is required 
                    // -----------------------------------------
                    if (deadline == null) {
                    	// Warn the user that Editor is required. 
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "You must select a deadline. ", "", JOptionPane.ERROR_MESSAGE);
                        return Condition.ITEM_CANCELED;
                    } else {
                       	// Set the Deadline property 
                       process.setDeadline(deadline.getTime());                   	
                    }

                    
                    // -----------------------------------------
                    // Priority Field is required! 
                    // -----------------------------------------
                    Priority newPriority;
                    if (priority == null) {
                    	// Warn the user that Priority is required! 
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "You must select a priority. ", "", JOptionPane.ERROR_MESSAGE);
                        return Condition.ITEM_CANCELED;
                    } else {
                    	// Set the priority based on the value selected 
                        if (priority.equals("Highest")) {
                        	newPriority = Priority.HIGHEST;
                        } else if (priority.equals("High")) {
                        	newPriority = Priority.HIGH;
                        } else if (priority.equals("Normal")) {
                        	newPriority = Priority.NORMAL;
                        } else if (priority.equals("Low")) {
                        	newPriority = Priority.LOW;
                        } else if (priority.equals("Lowest")) {
                        	newPriority = Priority.LOWEST;
                        } else {
                        	newPriority = null;
                        }
                        process.setPriority(newPriority);
                    }


                    // -----------------------------------------
                    // Comments
                    // -----------------------------------------
                    if (comments != null) {
                        process.setProperty(commentsPropName, comments);
                    } else {
                        process.setProperty(commentsPropName, "");
                    }

                    
                    // -----------------------------------------
                    // Originator
                    // -----------------------------------------
                    process.setOriginator(config.getUsername());
                    
                   
                    // -----------------------------------------
                    // Owner
                    // -----------------------------------------
                    process.setProperty(ownerUuidPropName, owner.getUids().iterator().next() );

                    
                    // -----------------------------------------
                    // File attachments 
                    // -----------------------------------------
                    process.setProperty(fileAttachmentsPropName, fileAttachments);
                   
                    
//                    if (fileAttachments == null || fileAttachments.isEmpty()) {
//                       	// remove the key from the process. 
//                        process.takeAttachment(fileAttachmentsPropName);
//                    } else {
//                    	// Store the attachments in the Key
//                        process.setProperty(fileAttachmentsPropName, fileAttachments);
//                    }

                    // Under normal conditions this is where we should return from 
                    return Condition.ITEM_COMPLETE;

                }
            }
            
            // If we got here we could not find the PanelRefsetAndParameters panel 
            // so warn the user and cancel the task. 
            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                    "Could not locate the 'PanelRefsetAndParameters' panel. \n " + 
                    "Canceling the task. ", "", JOptionPane.ERROR_MESSAGE);           
            return Condition.ITEM_CANCELED;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new TaskFailedException(e.getMessage());
        }
    }


    /**
     * This method overrides: getDataContainerIds() in AbstractTask
     * @return The data container identifiers used by this task.
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }

    /**
     * This method implements the interface method specified by: getConditions() in I_DefineTask
     * @return The possible evaluation conditions for this task.
	 * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return AbstractTask.ITEM_CANCELED_OR_COMPLETE;
    }
   
    public String getNextUserTermEntryPropName() {
        return nextUserTermEntryPropName;
    }
    public void setNextUserTermEntryPropName(String nextUserTermEntryPropName) {
        this.nextUserTermEntryPropName = nextUserTermEntryPropName;
    }
    public String getCommentsPropName() {
        return commentsPropName;
    }
    public void setCommentsPropName(String commentsPropName) {
        this.commentsPropName = commentsPropName;
    }
    public String getRefsetUuidPropName() {
        return refsetUuidPropName;
    }
    public void setRefsetUuidPropName(String refsetUuidPropName) {
        this.refsetUuidPropName = refsetUuidPropName;
    }
    public String getProfilePropName() {
		return profilePropName;
	}
	public void setProfilePropName(String profilePropName) {
		this.profilePropName = profilePropName;
	}
	public String getEditorUuidPropName() {
		return editorUuidPropName;
	}
	public void setEditorUuidPropName(String editorUuidPropName) {
		this.editorUuidPropName = editorUuidPropName;
	}
	public String getOwnerUuidPropName() {
		return ownerUuidPropName;
	}
	public void setOwnerUuidPropName(String ownerUuidPropName) {
		this.ownerUuidPropName = ownerUuidPropName;
	}
	public String getFileAttachmentsPropName() {
		return fileAttachmentsPropName;
	}
	public void setFileAttachmentsPropName(String fileAttachmentsPropName) {
		this.fileAttachmentsPropName = fileAttachmentsPropName;
	}


}
