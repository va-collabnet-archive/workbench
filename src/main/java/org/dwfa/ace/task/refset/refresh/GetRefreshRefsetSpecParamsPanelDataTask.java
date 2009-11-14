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
            } else {
            	 // Set version 1 default values
            	profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();  
            	nextUserTermEntryPropName = ProcessAttachmentKeys.NEXT_USER.getAttachmentKey();
            	refsetUuidPropName = ProcessAttachmentKeys.WORKING_REFSET.getAttachmentKey();
            	commentsPropName = ProcessAttachmentKeys.MESSAGE.getAttachmentKey();
            	editorUuidPropName = ProcessAttachmentKeys.EDITOR_UUID.getAttachmentKey();
            	ownerUuidPropName = ProcessAttachmentKeys.OWNER_UUID.getAttachmentKey();
            }
            if (objDataVersion >= 2) {
               // Read version 2 data fields...
            } else {
            	// Set version 2 Default values... 
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
                	
                    // ---------------------------------------------
                    // Retrieve values from the panel / environment
                    // ---------------------------------------------
                    I_GetConceptData refset = panel.getRefset();
                    I_GetConceptData editor = panel.getEditor();
                    String comments = panel.getComments();
                    Calendar deadline = panel.getDeadline();
                    String priority = panel.getPriority();
                    HashSet<File> attachments = panel.getAttachments();
                    I_GetConceptData owner = config.getDbConfig().getUserConcept();

                    // -----------------------------------------
                    // Verify required fields are present and 
                    // use the values retrieved from this panel 
                    // -----------------------------------------
                    
                    // Priority Field is required! 
                    Priority p;
                    if (priority == null) {
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "You must select a priority. ", "", JOptionPane.ERROR_MESSAGE);
                        return Condition.ITEM_CANCELED;
                    }
                    if (priority.equals("Highest")) {
                        p = Priority.HIGHEST;
                    } else if (priority.equals("High")) {
                        p = Priority.HIGH;
                    } else if (priority.equals("Normal")) {
                        p = Priority.NORMAL;
                    } else if (priority.equals("Low")) {
                        p = Priority.LOW;
                    } else if (priority.equals("Lowest")) {
                        p = Priority.LOWEST;
                    } else {
                        p = null;
                    }
                    process.setPriority(p);

                    

                    // Comments
                    if (comments != null) {
                        process.setProperty(commentsPropName, comments);
                    } else {
                        process.setProperty(commentsPropName, "");
                    }

                    
                    // Editor Field is required! 
                    if (editor != null) {
                        RefsetSpecWizardTask wizard = new RefsetSpecWizardTask();
                        String inboxAddress = wizard.getInbox(editor);
                        process.setProperty(nextUserTermEntryPropName, inboxAddress);
                        process.setProperty(ProcessAttachmentKeys.EDITOR_UUID.getAttachmentKey(), new UUID[] { editor
                            .getUids().iterator().next() });
                        process.setProperty(ProcessAttachmentKeys.OWNER_UUID.getAttachmentKey(), new UUID[] { owner
                            .getUids().iterator().next() });
                        if (inboxAddress == null) {
                            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                                "Refresh Refset wizard cannot be completed. The selected editor has no assigned inbox : "
                                    + editor, "", JOptionPane.ERROR_MESSAGE);
                            return Condition.ITEM_CANCELED;
                        }
                        process.setProperty(editorUuidPropName, 
                        		new UUID[] { editor.getUids().iterator().next() });
                    } else {
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "You must select an editor. ", "", JOptionPane.ERROR_MESSAGE);
                        return Condition.ITEM_CANCELED;
                    }


                    // Originator
                    process.setOriginator(config.getUsername());

                    
                    // Owner
                    process.setProperty(ownerUuidPropName, 
                    		new UUID[] { config.getDbConfig().getUserConcept().getUids().iterator().next() });

                    
                    // Refset Field is required 
                    if (refset == null) {
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "You must select a refset. ",
                            "", JOptionPane.ERROR_MESSAGE);
                        return Condition.ITEM_CANCELED;
                    }
                    process.setSubject("Refresh Refset : " + refset.getInitialText());
                    process.setName("Refresh Refset : " + refset.getInitialText());
                    process.setProperty(refsetUuidPropName, refset.getUids().iterator().next());
                    
                    
                    // Deadline Field is required 
                    if (deadline == null) {
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "You must select a deadline. ", "", JOptionPane.ERROR_MESSAGE);
                        return Condition.ITEM_CANCELED;
                    }
                    process.setDeadline(deadline.getTime());

                    
                    // File attachments 
                    for (File file : attachments) {
                        process.writeAttachment(file.getName(), new FileContent(file));
                    }

                    
                    
                    return Condition.ITEM_COMPLETE;

                }
            }
            return Condition.ITEM_COMPLETE;
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


}
