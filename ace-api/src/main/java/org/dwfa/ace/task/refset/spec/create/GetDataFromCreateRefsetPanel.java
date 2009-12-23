/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.ace.task.refset.spec.create;

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
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.Priority;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * This task collects the data entered on the CreateRefsetPanel panel 
 * currently displayed in the Workflow Details Sheet and verifies that 
 * the required data has been filled in.
 * 
 * @author Perry Reid
 * @version 1.0, December 2009 
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf", type = BeanType.TASK_BEAN) })
public class GetDataFromCreateRefsetPanel extends AbstractTask {

    /* -----------------------
     * Properties 
     * -----------------------
     */
	// Serialization Properties 
    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;
    
	// Task Attribute Properties         
	private String profilePropName = ProcessAttachmentKeys.CURRENT_PROFILE.getAttachmentKey();  
    private String refsetNamePropName = ProcessAttachmentKeys.WORKING_REFSET.getAttachmentKey();
    private String refsetParentUuidPropName = ProcessAttachmentKeys.ACTIVE_CONCEPT.getAttachmentKey();
    private TermEntry statusTermEntry = new TermEntry(ArchitectonicAuxiliary.Concept.CURRENT_UNREVIEWED.getUids());
    private String commentsPropName = ProcessAttachmentKeys.MESSAGE.getAttachmentKey();
    private String ownerUuidPropName = ProcessAttachmentKeys.OWNER_UUID.getAttachmentKey();
    private String requestorPropName = ProcessAttachmentKeys.REQUESTOR.getAttachmentKey();
    private String editorUuidPropName = ProcessAttachmentKeys.EDITOR_UUID.getAttachmentKey();
    private String reviewerUuidPropName = ProcessAttachmentKeys.REVIEWER_UUID.getAttachmentKey();
    private String fileAttachmentsPropName = ProcessAttachmentKeys.FILE_ATTACHMENTS.getAttachmentKey();
	
	// Other Properties 
    private I_TermFactory termFactory;
    private I_GetConceptData status;
    private transient Exception ex = null;
    private transient Condition returnCondition = Condition.ITEM_COMPLETE;

    
    /* -----------------------
     * Serialization Methods
     * -----------------------
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(profilePropName);
        out.writeObject(refsetNamePropName);
        out.writeObject(refsetParentUuidPropName);
        out.writeObject(statusTermEntry);
        out.writeObject(commentsPropName);
        out.writeObject(ownerUuidPropName);
        out.writeObject(requestorPropName);
        out.writeObject(editorUuidPropName);
        out.writeObject(reviewerUuidPropName);
        out.writeObject(fileAttachmentsPropName);
    }
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        
        if (objDataVersion <= dataVersion) {
            if (objDataVersion >= 1) {
                // Read version 1 data fields...
            	profilePropName = (String) in.readObject();
            	refsetNamePropName = (String) in.readObject();
            	refsetParentUuidPropName = (String) in.readObject();
            	statusTermEntry = (TermEntry) in.readObject();
            	commentsPropName = (String) in.readObject();
            	ownerUuidPropName = (String) in.readObject();
            	requestorPropName = (String) in.readObject();
            	editorUuidPropName = (String) in.readObject();
            	reviewerUuidPropName = (String) in.readObject();
            	fileAttachmentsPropName = (String) in.readObject();
            }
            // Initialize transient properties 
            ex = null;
            returnCondition = Condition.ITEM_COMPLETE;           
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
    public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {

        try {
        	
            termFactory = LocalVersionedTerminology.get();
            I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();

            JPanel workflowDetailsSheet = config.getWorkflowDetailsSheet();
            
            for (Component c : workflowDetailsSheet.getComponents()) {
                if (CreateRefsetPanel.class.isAssignableFrom(c.getClass())) {
                	CreateRefsetPanel panel = (CreateRefsetPanel) c;
                	
                    // ---------------------------------------------
                    // Retrieve values from the panel / environment
                    // ---------------------------------------------
                    String refsetName = panel.getRefsetName();
                    I_GetConceptData refsetParent = panel.getRefsetParent();
                    String comments = panel.getComments();
                    String requestor = panel.getRequestor();
                    I_GetConceptData editor = panel.getEditor();
                    I_GetConceptData reviewer = panel.getReviewer();
                    Calendar deadline = panel.getDeadline();
                    String priority = panel.getPriority();
                    HashSet<File> fileAttachments = panel.getAttachments();
                    I_GetConceptData owner = config.getDbConfig().getUserConcept();   
                    SetWfdSheetToCreateRefsetPanel setCreateTask = new SetWfdSheetToCreateRefsetPanel(); 
                    
                    // -------------------------------------------------------------------------
                    // VERIFY ALL REQUIRED FIELDS AND STORE THE ENTERED DATA INTO PROPERTY KEYS
                    // -------------------------------------------------------------------------
                    
                    
                    // -----------------------------------------
                    // Refset Name Field (REQUIRED)
                    // -----------------------------------------
                    if (refsetName == null || refsetName.isEmpty()) {
                    	// Warn the user that Refset is required. 
                    	JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), 
                    			"You must enter a Refset Name. ",
                    			"", JOptionPane.ERROR_MESSAGE);
                    	return Condition.ITEM_CANCELED;
                    } else {
                    	// Set the Refset Name property 
                    	process.setSubject("Creation Request");
                    	process.setName(refsetName);
                    	process.setProperty(refsetNamePropName, refsetName);

                    }


                    // -----------------------------------------
                    // Refset Parent Field (REQUIRED)
                    // -----------------------------------------
                    if (refsetParent == null) {
                    	// Warn the user that Refset is required. 
                    	JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), 
                    			"You must select a Refset Parent. ",
                    			"", JOptionPane.ERROR_MESSAGE);
                    	return Condition.ITEM_CANCELED;
                    } else {
                    	// Set the Refset Parent property 
                        process.setProperty(refsetParentUuidPropName, refsetParent.getUids().iterator().next());
                   }
                   

                    // -----------------------------------------
                    // Comments (OPTIONAL)
                    // -----------------------------------------
                    if (comments != null) {
                        process.setProperty(commentsPropName, comments);
                    } else {
                        process.setProperty(commentsPropName, "");
                    }

                    
                    // -----------------------------------------
                    // Owner
                    // -----------------------------------------
                    // Set owner as the originator
                    process.setOriginator(config.getUsername()); 
                    // Remember the owner's UUID
                    process.setProperty(ownerUuidPropName, 
                    		new UUID[] { owner.getUids().iterator().next() });

                    
                    // -----------------------------------------
                    // Requestor (OPTIONAL)
                    // -----------------------------------------
                    if (requestor != null) {
                        process.setProperty(requestorPropName, requestor);
                    } else {
                        process.setProperty(requestorPropName, "");
                    }

                    
                    // -----------------------------------------
                    // Editor Field (REQUIRED)
                    // -----------------------------------------
                    if (editor == null) {
                    	// Warn the user that Editor is required. 
                    	JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                    			"You must select an editor. ", "", JOptionPane.ERROR_MESSAGE);
                    	return Condition.ITEM_CANCELED;                         
                    } else {
                        setCreateTask.setTermFactory(termFactory);
                    	String editorInbox = setCreateTask.getInbox(editor);
                    	if (editorInbox == null) {
                    		JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                    				"Creation Request cannot be completed. The selected editor has no assigned inbox.",
                    				"", JOptionPane.ERROR_MESSAGE);
                    		return Condition.ITEM_CANCELED;
                    	} else {
                            process.setDestination(editorInbox);

                    		// Set the Editor property 
                    		process.setProperty(editorUuidPropName, new UUID[] { 
                    				editor.getUids().iterator().next() });
                    	}

                    }


                    // -----------------------------------------
                    // Reviewer Field  (OPTIONAL)
                    // -----------------------------------------
                    if (reviewer != null) {
                    	// Set the Reviewer UUID property 
                        process.setProperty(reviewerUuidPropName, 
                        		new UUID[] { reviewer.getUids().iterator().next() });    
                    }


                    // -----------------------------------------
                    // Deadline Field (REQUIRED)
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
                    // Priority Field (REQUIRED)
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
                    // File attachments (OPTIONAL)
                    // -----------------------------------------
                    process.setProperty(fileAttachmentsPropName, fileAttachments);
                   
                    // Under normal conditions this is where we should return from 
                    return Condition.ITEM_COMPLETE;

                }
            }
            
            // If we got here we could not find the PanelRefsetAndParameters panel 
            // so warn the user and cancel the task. 
            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                    "Could not locate the 'CreateRefsetPanel' panel. \n " + 
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
   
	/* ====================================================================
	 * Getters and Setters
	 * ====================================================================
	 */
    public String getCommentsPropName() {
        return commentsPropName;
    }
    public void setCommentsPropName(String commentsPropName) {
        this.commentsPropName = commentsPropName;
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
	public String getReviewerUuidPropName() {
		return reviewerUuidPropName;
	}
	public void setReviewerUuidPropName(String reviewerUuidPropName) {
		this.reviewerUuidPropName = reviewerUuidPropName;
	}
	public String getRefsetNamePropName() {
		return refsetNamePropName;
	}
	public void setRefsetNamePropName(String refsetNamePropName) {
		this.refsetNamePropName = refsetNamePropName;
	}
	public String getRefsetParentUuidPropName() {
		return refsetParentUuidPropName;
	}
	public void setRefsetParentUuidPropName(String refsetParentPropName) {
		this.refsetParentUuidPropName = refsetParentPropName;
	}
	public TermEntry getStatusTermEntry() {
		return statusTermEntry;
	}
	public void setStatusTermEntry(TermEntry statusTermEntry) {
		this.statusTermEntry = statusTermEntry;
	}
	public String getRequestorPropName() {
		return requestorPropName;
	}
	public void setRequestorPropName(String requestorPropName) {
		this.requestorPropName = requestorPropName;
	}


}