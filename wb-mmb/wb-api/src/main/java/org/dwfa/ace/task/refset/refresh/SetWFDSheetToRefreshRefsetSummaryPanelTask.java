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
package org.dwfa.ace.task.refset.refresh;

import java.awt.GridLayout;
import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.AceTaskUtil;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.wfdetailsSheet.ClearWorkflowDetailsSheet;
import org.dwfa.ace.utypes.UniversalAcePosition;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.Priority;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * This task prepares the Workflow Details Sheet to display the PanelRefreshSummary
 * panel where the user will be asked to review a number of fields before proceeding.   
 * 
 * @author Perry Reid
 * @version 1.0, November 2009 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf", type = BeanType.TASK_BEAN) })
public class SetWFDSheetToRefreshRefsetSummaryPanelTask extends AbstractTask {

    /* -----------------------
     * Properties 
     * -----------------------
     */
	// Serialization Properties 
    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 3;

	// Task Attribute Properties     
	private String profilePropName = ProcessAttachmentKeys.CURRENT_PROFILE.getAttachmentKey();  
	private String ownerUuidPropName = ProcessAttachmentKeys.OWNER_UUID.getAttachmentKey();
    private String commentsPropName = ProcessAttachmentKeys.MESSAGE.getAttachmentKey();
    private String refsetUuidPropName = ProcessAttachmentKeys.WORKING_REFSET.getAttachmentKey();
	private String refsetSpecVersionPropName = ProcessAttachmentKeys.REFSET_VERSION.getAttachmentKey();
    private String editorUuidPropName = ProcessAttachmentKeys.EDITOR_UUID.getAttachmentKey();
    private String editorInboxPropName = ProcessAttachmentKeys.EDITOR_INBOX.getAttachmentKey();
	private String editorCommentsPropName = ProcessAttachmentKeys.EDITOR_COMMENTS.getAttachmentKey();
    private String reviewerUuidPropName = ProcessAttachmentKeys.REVIEWER_UUID.getAttachmentKey();
    private String reviewerInboxPropName = ProcessAttachmentKeys.REVIEWER_INBOX.getAttachmentKey();
	private String snomedVersionPropName = ProcessAttachmentKeys.SNOMED_VERSION.getAttachmentKey();
    private String fileAttachmentsPropName = ProcessAttachmentKeys.FILE_ATTACHMENTS.getAttachmentKey();
	private String reviewCountPropName = ProcessAttachmentKeys.REVIEW_COUNT.getAttachmentKey();
	private String changesListPropName = ProcessAttachmentKeys.CHANGES_LIST.getAttachmentKey();
	         
	// Other Properties 
    private transient Exception ex = null;
    private I_TermFactory termFactory;
    private I_ConfigAceFrame config;

    
    /* -----------------------
     * Serialization Methods
     * -----------------------
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(profilePropName);
        out.writeObject(refsetUuidPropName);
        out.writeObject(ownerUuidPropName);
        out.writeObject(editorUuidPropName);
        out.writeObject(editorInboxPropName);
        out.writeObject(refsetSpecVersionPropName);
        out.writeObject(snomedVersionPropName);
        out.writeObject(commentsPropName);
        out.writeObject(fileAttachmentsPropName);
        out.writeObject(editorCommentsPropName);
        out.writeObject(reviewerUuidPropName);
        out.writeObject(reviewerInboxPropName);
        out.writeObject(reviewCountPropName);
        out.writeObject(changesListPropName);
    }
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        
        if (objDataVersion <= dataVersion) {
            if (objDataVersion >= 1) {
                // Read version 1 data fields...
            	profilePropName = (String) in.readObject();
                refsetUuidPropName = (String) in.readObject();
            	ownerUuidPropName = (String) in.readObject();
            	editorUuidPropName = (String) in.readObject();
            	editorInboxPropName = (String) in.readObject();
                refsetSpecVersionPropName = (String) in.readObject();
                snomedVersionPropName = (String) in.readObject();
                commentsPropName = (String) in.readObject();
            	fileAttachmentsPropName = (String) in.readObject();
            	editorCommentsPropName = (String) in.readObject();
            } 
            if (objDataVersion >= 2) {
                // Read version 2 data fields...
            	reviewerUuidPropName = (String) in.readObject();
            	reviewerInboxPropName = (String) in.readObject();
            } 
            if (objDataVersion >= 3) {
                // Read version 3 data fields...
            	reviewCountPropName = (String) in.readObject();
            	changesListPropName = (String) in.readObject();
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


    private void doRun(final I_EncodeBusinessProcess process, final I_Work worker) {

    	try {
 
	        // ----------------------------------------------------------------------------------
	        //  Set up the environment, and prepare the display panels 
	        // ----------------------------------------------------------------------------------

			// Get current Profile / Configuration from the process property  
    		config = (I_ConfigAceFrame) process.getProperty(getProfilePropName());
            termFactory = Terms.get();

			// Clear the Workflow Details Sheet 
			ClearWorkflowDetailsSheet clear = new ClearWorkflowDetailsSheet();
			clear.setProfilePropName(getProfilePropName());
			clear.evaluate(process, worker);

			// Create a new panel to add it to the Workflow Details Sheet
            JPanel workflowDetailsSheet = config.getWorkflowDetailsSheet();
	        int width = 475;
	        int height = 590;
	        workflowDetailsSheet.setSize(width, height);
	        workflowDetailsSheet.setLayout(new GridLayout(1, 1));
	        PanelRefreshSummary newPanel = new PanelRefreshSummary(); 
	        
	        // ----------------------------------------------------------------------------------
	        //  Initialize the fields on this newPanel with the values from the properties
	        // ----------------------------------------------------------------------------------
	        String refsetSpecName = new String("UNKNOWN"); 
	        String ownerName = new String("UNKNOWN"); 
	        String editorName = new String("UNKNOWN"); 
	        String editorInbox = new String("UNKNOWN"); 
	        String reviewerName = new String("UNKNOWN"); 
	        String reviewerInbox = new String("UNKNOWN"); 
	        String resetSpecVersionName = new String("UNKNOWN"); 
	        String snomedVersionName = new String("UNKNOWN"); 
	        String commentsText = new String("No message available."); 
	        List<String> attachedFileNameList = new ArrayList<String>(); 
	        List<String> changedConceptsList = new ArrayList<String>(); 
	        String processPriorityText = new String("UNKNOWN"); 
	        String processDeadlineText = new String("UNKNOWN"); 
	        Integer reviewCount = 0;

	        
	        // Refset - Field Initialization 	        
			try {
		        I_GetConceptData currentRefsetSpec = null;
	        	UUID refsetSpecUUID = (UUID) process.getProperty(refsetUuidPropName);
	        	currentRefsetSpec = (I_GetConceptData) AceTaskUtil.getConceptFromObject(refsetSpecUUID); 
	        	
		        if (currentRefsetSpec != null ) {
		        	refsetSpecName = currentRefsetSpec.getInitialText(); 
		        }	      
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
	        
	        // Refset Spec Version	        
			try {
				Set<I_Position> currentRefsetSpecVersion = new HashSet<I_Position>();
		        I_TermFactory tf = Terms.get();
				
				// Retrieve the positions as Set<UniversalAcePosition> and convert them back to Set<I_Position>
				Set<UniversalAcePosition> universalPositions = 
					(Set<UniversalAcePosition>) process.getProperty(refsetSpecVersionPropName);
		        for (UniversalAcePosition univPos: universalPositions) {
		           I_Path path = tf.getPath(univPos.getPathId());
		           I_Position thinPos = tf.newPosition(path, tf.convertToThinVersion(univPos.getTime()));
		           currentRefsetSpecVersion.add(thinPos);
		        }

		        if (currentRefsetSpecVersion.size() > 0 ) {
		        	resetSpecVersionName = currentRefsetSpecVersion.toString(); 
		        }	  

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

			
			// SNOMED Version 	        
			try {
				Set<I_Position> currentSnomedVersion = new HashSet<I_Position>();
		        I_TermFactory tf = Terms.get();
				
				// Retrieve the positions as Set<UniversalAcePosition> and convert them back to Set<I_Position>
				Set<UniversalAcePosition> universalPositions = 
					(Set<UniversalAcePosition>) process.getProperty(snomedVersionPropName);
		        for (UniversalAcePosition univPos: universalPositions) {
		           I_Path path = tf.getPath(univPos.getPathId());
		           I_Position thinPos = tf.newPosition(path, tf.convertToThinVersion(univPos.getTime()));
		           currentSnomedVersion.add(thinPos);
		        }

		        if (currentSnomedVersion.size() > 0 ) {
		        	snomedVersionName = currentSnomedVersion.toString(); 
		        }	  

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
			
			
	        // Owner - Field Initialization 	        
			try {
		        I_GetConceptData ownerConcept = null;
	        	// Retrieve the UUID for the owner 
		        // TODO: UUID[] CONVERT
		        // UUID ownerUUID = (UUID) process.getProperty(ownerUuidPropName);
	        	// ownerConcept = (I_GetConceptData) AceTaskUtil.getConceptFromObject(ownerUUID); 
	        	ownerConcept = termFactory.getConcept((UUID[]) process.getProperty(ownerUuidPropName));

	        	if (ownerConcept != null ) {
	        		ownerName = ownerConcept.getInitialText(); 
		        }	      
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

			
	        // Editor - Field Initialization 	        
			try {
		        I_GetConceptData editorConcept = null;
	        	// Retrieve the UUID for the editor 
		        // TODO: UUID[] CONVERT
	        	// UUID editorUUID = (UUID) process.getProperty(editorUuidPropName);
	        	// editorConcept = (I_GetConceptData) AceTaskUtil.getConceptFromObject(editorUUID); 
		        editorConcept = termFactory.getConcept((UUID[]) process.getProperty(editorUuidPropName));
		        
	        	if (editorConcept != null ) {
	        		editorName = editorConcept.getInitialText(); 
		        }	      
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

	        // Editor Inbox - Field Initialization 	        
			editorInbox = (String) process.getProperty(editorInboxPropName);

			
			
	        // Reviewer - Field Initialization 	        
			try {
		        I_GetConceptData reviewerConcept = null;
	        	// Retrieve the UUID for the reviewer 
		        // TODO: UUID[] CONVERT
	        	// UUID reviewerUUID = (UUID) process.getProperty(reviewerUuidPropName);
	        	// reviewerConcept = (I_GetConceptData) AceTaskUtil.getConceptFromObject(reviewerUUID); 
		   
		        reviewerConcept = termFactory.getConcept((UUID[]) process.getProperty(reviewerUuidPropName));

	        	if (reviewerConcept != null ) {
	        		reviewerName = reviewerConcept.getInitialText(); 
		        }	      
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

	        // Reviewer Inbox - Field Initialization 	        
			reviewerInbox = (String) process.getProperty(reviewerInboxPropName);

			
			// Priority - Field Initialization 	        
	        Priority processPriority = process.getPriority();
	        if (processPriority == Priority.HIGHEST) {
	        	processPriorityText = "Highest"; 
	        } else if (processPriority == Priority.HIGH) {
	        	processPriorityText = "High"; 
	        } else if (processPriority == Priority.NORMAL) {
	        	processPriorityText = "Normal"; 
	        } else if (processPriority == Priority.LOW) {
	        	processPriorityText = "Low"; 
	        } else if (processPriority == Priority.LOWEST) {
	        	processPriorityText = "Lowest"; 
	        } else {
	        	// set priority to Normal by default 
	        	processPriorityText = "Normal"; 
	        }

	        
	        // Deadline - Field Initialization 
	        Date processDeadlineDate = null; 
	        processDeadlineDate = (Date) process.getDeadline(); 
	        if (processDeadlineDate != null) {
	        	processDeadlineText = processDeadlineDate.toString();
	        }
	        

	        // Comments - Field Initialization 	  
	        String previousComments = null;
			previousComments = (String) process.getProperty(commentsPropName);
	        if (previousComments != null) {
	        	commentsText = previousComments; 
	        }

	        // Comments - Field Initialization 	  
	        reviewCount = (Integer) process.getProperty(reviewCountPropName);


	        
	        // File Attachments - Field Initialization 	        	        
			HashSet<File> previousFileAttachments = null;
        	previousFileAttachments = (HashSet<File>) process.getProperty(fileAttachmentsPropName);
	        if (previousFileAttachments != null ) {
	        	for (File fileObject: previousFileAttachments ) {
	        		attachedFileNameList.add(fileObject.getCanonicalPath()); 
	        	}
	        } else {
	        	
	        }

	        
	        
	        // ----------------------------------------------------------------------------------
	        //  Format the data retrieved above into an HTML String for display in the
	        //  Summary panel. 
	        // ----------------------------------------------------------------------------------
	    	String summaryMessage = new String("<html>"
	        	+ "<STYLE type=\"text/css\"> "
	        	+ "	body { " 
	        	+ "		margin-top: 2px; " 
	        	+ "		margin-right: 12px; " 
	        	+ "		margin-bottom: 2px; "
	        	+ "		margin-left: 12px; "
	        	+ "	} "
	        	+ "</STYLE> " 
	        	+ "<body bgcolor='rgb(255, 255, 220)'> "
	        	+ "<h2>Refresh Refset Spec Summary</h2>" 
	        	+ "<p>The following parameters have been specified for this "
	        	+ "business process.  Please review before we continue. </p>" 
	        	+ "<ul>" 
	        	+ "<li><strong>Refset Spec To Be Refreshed:</strong>"
	        	+ "<br>" + refsetSpecName
	        	+  "  " + resetSpecVersionName + "</li>"
	        	+ "<br>"
	        	+ "<li><strong>Terminology Source Version:</strong>"
	        	+ "<br>" + snomedVersionName + "</li>"
	        	+ "<br>"
	        	+ "<li><strong>Assigned Roles and Responsibilities:</strong>"
	        	+ "<br>"
	        	+ "<ul>  "
	        	+ "<li><i>Owner: </i>" + ownerName + "</li>"  
	        	+ "<li><i>Author/Editor: </i>" + editorName + "</li>"  
	        	+ "<li><i>Reviewer: </i>" + reviewerName + "</li>"  
	        	+ "</ul>"
	        	+ "</li>"
	        	+ "<li><strong>Deadline:</strong>"
	        	+ "<br>" + processDeadlineText + "</li>"
	        	+ "<br>"
	        	+ "<li><strong>Lastest Message / Instructions:</strong>"
	        	+ "<br>" + commentsText + "</li>"
	        	+ "<br>"
	        	+ "<li><strong>Concepts/Clauses to be Reviewed:</strong>"
	        	+ "<br>Count=" + reviewCount + "</li>"
	        	+ "</ul>");

	    	
	    	summaryMessage = summaryMessage.concat("</body></html>");

	    	System.out.println("DEBUG: Summary Message=" + summaryMessage);
	    	newPanel.setMessageText(summaryMessage);

	    	
	    	// FINALLY... Set the process Name and Subject based on the values above
	    	process.setName(refsetSpecName +  " - " + resetSpecVersionName);
	    	process.setSubject("SNOMED Version " + snomedVersionName + " Refresh");
	        
	        /*----------------------------------------------------------------------------------
	         *  Add the initialized panel to the Workflow Details Sheet
	         * ----------------------------------------------------------------------------------
	         */
	        workflowDetailsSheet.add(newPanel);
	        workflowDetailsSheet.validate();
	        
        } catch (Exception e) {
            ex = e;
        }
    }


    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    

 
	/* ---------------------------------------------------------
	 * Getters and Setters for Property Names
	 * ---------------------------------------------------------
	 */
	public String getProfilePropName() {
		return profilePropName;
	}
	public void setProfilePropName(String profilePropName) {
		this.profilePropName = profilePropName;
	}
	public String getEditorInboxPropName() {
		return editorInboxPropName;
	}
	public void setEditorInboxPropName(String editorInboxPropName) {
		this.editorInboxPropName = editorInboxPropName;
	}
	public String getRefsetUuidPropName() {
		return refsetUuidPropName;
	}
	public void setRefsetUuidPropName(String refsetUuidPropName) {
		this.refsetUuidPropName = refsetUuidPropName;
	}
	public String getCommentsPropName() {
		return commentsPropName;
	}
	public void setCommentsPropName(String commentsPropName) {
		this.commentsPropName = commentsPropName;
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
    public String getRefsetSpecVersionPropName() {
		return refsetSpecVersionPropName;
	}
	public void setRefsetSpecVersionPropName(String refsetSpecVersionPropName) {
		this.refsetSpecVersionPropName = refsetSpecVersionPropName;
	}
	public String getSnomedVersionPropName() {
		return snomedVersionPropName;
	}
	public void setSnomedVersionPropName(String snomedVersionPropName) {
		this.snomedVersionPropName = snomedVersionPropName;
	}
	public String getEditorCommentsPropName() {
		return editorCommentsPropName;
	}
	public void setEditorCommentsPropName(String editorCommentsPropName) {
		this.editorCommentsPropName = editorCommentsPropName;
	}
	public String getReviewerUuidPropName() {
		return reviewerUuidPropName;
	}
	public void setReviewerUuidPropName(String reviewerUuidPropName) {
		this.reviewerUuidPropName = reviewerUuidPropName;
	}
	public String getReviewerInboxPropName() {
		return reviewerInboxPropName;
	}
	public void setReviewerInboxPropName(String reviewerInboxPropName) {
		this.reviewerInboxPropName = reviewerInboxPropName;
	}
	public String getReviewCountPropName() {
		return reviewCountPropName;
	}
	public void setReviewCountPropName(String reviewCountPropName) {
		this.reviewCountPropName = reviewCountPropName;
	}
	public String getChangesListPropName() {
		return changesListPropName;
	}
	public void setChangesListPropName(String changesListPropName) {
		this.changesListPropName = changesListPropName;
	}



}
