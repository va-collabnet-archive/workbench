package org.dwfa.ace.task.refset.refresh;

import java.awt.GridLayout;
import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
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
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.AceTaskUtil;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.commit.TestForCreateNewRefsetPermission;
import org.dwfa.ace.task.wfdetailsSheet.ClearWorkflowDetailsSheet;
import org.dwfa.ace.utypes.UniversalAcePosition;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.Priority;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.TerminologyException;
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
    private static final int dataVersion = 1;

	// Task Attribute Properties     
	private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();  
    private String refsetUuidPropName = ProcessAttachmentKeys.WORKING_REFSET.getAttachmentKey();
	private String ownerUuidPropName = ProcessAttachmentKeys.OWNER_UUID.getAttachmentKey();
    private String editorUuidPropName = ProcessAttachmentKeys.EDITOR_UUID.getAttachmentKey();
    private String nextUserTermEntryPropName = ProcessAttachmentKeys.NEXT_USER.getAttachmentKey();
	private String refsetSpecVersionPropName = ProcessAttachmentKeys.POSITION_SET.getAttachmentKey();
	private String snomedVersionPropName = ProcessAttachmentKeys.POSITION_LIST.getAttachmentKey();
    private String commentsPropName = ProcessAttachmentKeys.MESSAGE.getAttachmentKey();
    private String fileAttachmentsPropName = ProcessAttachmentKeys.FILE_ATTACHMENTS.getAttachmentKey();
	private String changeMapPropName = ProcessAttachmentKeys.CON_CON_MAP.getAttachmentKey();
	         
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
        out.writeObject(nextUserTermEntryPropName);
        out.writeObject(refsetSpecVersionPropName);
        out.writeObject(snomedVersionPropName);
        out.writeObject(commentsPropName);
        out.writeObject(fileAttachmentsPropName);
        out.writeObject(changeMapPropName);
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
                nextUserTermEntryPropName = (String) in.readObject();
                refsetSpecVersionPropName = (String) in.readObject();
                snomedVersionPropName = (String) in.readObject();
                commentsPropName = (String) in.readObject();
            	fileAttachmentsPropName = (String) in.readObject();
            	changeMapPropName = (String) in.readObject();
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
            termFactory = LocalVersionedTerminology.get();

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
	        String nextUserName = new String("UNKNOWN"); 
	        String resetSpecVersionName = new String("UNKNOWN"); 
	        String snomedVersionName = new String("UNKNOWN"); 
	        String commentsText = new String("UNKNOWN"); 
	        List<String> attachedFileNameList = new ArrayList<String>(); 
	        List<String> changedConceptsList = new ArrayList<String>(); 
	        String processPriorityText = new String("UNKNOWN"); 
	        String processDeadlineText = new String("UNKNOWN"); 

	        
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
		        I_TermFactory tf = LocalVersionedTerminology.get();
				
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
		        I_TermFactory tf = LocalVersionedTerminology.get();
				
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
	        	UUID ownerUUID = (UUID) process.getProperty(ownerUuidPropName);
	        	
	        	// Translate the UUID back into an I_GetConceptData object 
	        	ownerConcept = (I_GetConceptData) AceTaskUtil.getConceptFromObject(ownerUUID); 
		        
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
	        	UUID editorUUID = (UUID) process.getProperty(editorUuidPropName);
	        	
	        	// Translate the UUID back into an I_GetConceptData object 
	        	editorConcept = (I_GetConceptData) AceTaskUtil.getConceptFromObject(editorUUID); 
		        
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

	        // Next User - Field Initialization 	        
        	nextUserName = (String) process.getProperty(nextUserTermEntryPropName);

			
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
	        	+ "business: </p>" 
	        	+ "<ul>" 
	           	+ "  <li><strong>Refset Spec: </strong>" + refsetSpecName + "</li>"
	        	+ "  <li><strong>Refset Spec Version: </strong>" + resetSpecVersionName + "</li>"
	        	+ "  <li><strong>SNOMED Version: </strong>" + snomedVersionName + "</li>"
	        	+ "  <li><strong>Owner: </strong>" + ownerName + "</li>"
	        	+ "  <li><strong>Editor: </strong>" + editorName + "</li>"
	        	+ "  <li><strong>Process Priority: </strong>" + processPriorityText + "</li>"
	        	+ "  <li><strong>Process Deadline: </strong>" + processDeadlineText + "</li>"
	        	+ "  <li><strong>Owner's Comments: </strong>" + commentsText + "</li>"
	        	+ "  <li><strong>Attached Files: </strong></li>" 
        		+ "      <ul>"); 
	    	
	    	if (attachedFileNameList.size() > 0) {
	           	for (String fileName: attachedFileNameList ) {
	           		summaryMessage = summaryMessage.concat("<li>" + fileName + "</li>"); 
	        	} 
	    	} else {
	    		summaryMessage = summaryMessage.concat("<li>No attached files</li>"); 
	    	}
	    	summaryMessage = summaryMessage.concat("</ul>" 
	        	+ "</ul>"
				+ "</body></html>");

	    	System.out.println("DEBUG: Summary Message=" + summaryMessage);
	    	newPanel.setMessageText(summaryMessage);
	        
	        
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
	public String getNextUserTermEntryPropName() {
		return nextUserTermEntryPropName;
	}
	public void setNextUserTermEntryPropName(String nextUserTermEntryPropName) {
		this.nextUserTermEntryPropName = nextUserTermEntryPropName;
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
	public String getChangeMapPropName() {
		return changeMapPropName;
	}
	public void setChangeMapPropName(String changeMapPropName) {
		this.changeMapPropName = changeMapPropName;
	}


}
