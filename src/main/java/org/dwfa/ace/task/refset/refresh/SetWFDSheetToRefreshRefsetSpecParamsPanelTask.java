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
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.AceTaskUtil;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.commit.TestForCreateNewRefsetPermission;
import org.dwfa.ace.task.wfdetailsSheet.ClearWorkflowDetailsSheet;
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
 * This task prepares the Workflow Details Sheet to display the PanelRefsetAndParameters
 * panel where the user will be asked to enter a number of fields required to start the 
 * Refresh Refset process.   
 * 
 * @author Perry Reid
 * @version 1.0, November 2009 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf", type = BeanType.TASK_BEAN) })
public class SetWFDSheetToRefreshRefsetSpecParamsPanelTask extends AbstractTask {

    /* -----------------------
     * Properties 
     * -----------------------
     */
	// Serialization Properties 
    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

	// Task Attribute Properties     
	private String profilePropName = ProcessAttachmentKeys.CURRENT_PROFILE.getAttachmentKey();  
    private String editorInboxPropName = ProcessAttachmentKeys.EDITOR_INBOX.getAttachmentKey();
    private String refsetUuidPropName = ProcessAttachmentKeys.WORKING_REFSET.getAttachmentKey();
    private String commentsPropName = ProcessAttachmentKeys.MESSAGE.getAttachmentKey();
    private String editorUuidPropName = ProcessAttachmentKeys.EDITOR_UUID.getAttachmentKey();
    private String ownerUuidPropName = ProcessAttachmentKeys.OWNER_UUID.getAttachmentKey();
    private String fileAttachmentsPropName = ProcessAttachmentKeys.FILE_ATTACHMENTS.getAttachmentKey();
        
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
        out.writeObject(editorInboxPropName);
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
            	editorInboxPropName = (String) in.readObject();
                commentsPropName = (String) in.readObject();
                refsetUuidPropName = (String) in.readObject();
            	editorUuidPropName = (String) in.readObject();
            	ownerUuidPropName = (String) in.readObject();
            	fileAttachmentsPropName = (String) in.readObject();
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
 
            termFactory = LocalVersionedTerminology.get();

			// Get current Profile / Configuration from the process property  
    		config = (I_ConfigAceFrame) process.getProperty(getProfilePropName());

    		// Get a list of valid Refset Specs 
            Set<I_GetConceptData> refsetSpecs = getValidRefsetSpecs();
            
			// Clear the Workflow Details Sheet 
			ClearWorkflowDetailsSheet clear = new ClearWorkflowDetailsSheet();
			clear.setProfilePropName(getProfilePropName());
			clear.evaluate(process, worker);

			// Create a new panel to add to the Workflow Details Sheet
            JPanel workflowDetailsSheet = config.getWorkflowDetailsSheet();
	        int width = 475;
	        int height = 590;
	        workflowDetailsSheet.setSize(width, height);
	        workflowDetailsSheet.setLayout(new GridLayout(1, 1));
	        PanelRefsetAndParameters newPanel = new PanelRefsetAndParameters(refsetSpecs); 
	        
	        // ----------------------------------------------------------------------------------
	        //  Initialize the fields on this panel with the previously entered values (if any).
	        // ----------------------------------------------------------------------------------
	        
	        // Refset - Field Initialization 	        
			try {
		        I_GetConceptData previousRefsetSpec = null;
	        	UUID refsetSpecUUID = (UUID) process.getProperty(refsetUuidPropName);
	        	previousRefsetSpec = (I_GetConceptData) AceTaskUtil.getConceptFromObject(refsetSpecUUID); 

		        if (previousRefsetSpec != null ) {
		        	newPanel.setRefset(previousRefsetSpec); 
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
		        I_GetConceptData previousEditor = null;
//		        if (isKeyDefined(process, editorUuidPropName)) {
		        	// Retrieve the UUID for the editor 
		        	UUID editorUUID = (UUID) process.getProperty(editorUuidPropName);
		        	
		        	// Translate the UUID back into an I_GetConceptData object 
		        	System.out.println("Conveting UUID[] to I_GetConceptData");
		        	previousEditor = (I_GetConceptData) AceTaskUtil.getConceptFromObject(editorUUID); 
			        if (previousEditor != null ) {
			        	System.out.println("previousEditor=" + previousEditor.getInitialText());

			        	// set the ComboBox to point to the selected editor  
			        	newPanel.setEditor(previousEditor); 
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

			
			// Priority - Field Initialization 	        
	        Priority previousPriority = process.getPriority();
	        if (previousPriority == Priority.HIGHEST) {
        	    newPanel.setPriority("Highest"); 
	        } else if (previousPriority == Priority.HIGH) {
	        	newPanel.setPriority("High"); 
	        } else if (previousPriority == Priority.NORMAL) {
	        	newPanel.setPriority("Normal"); 
	        } else if (previousPriority == Priority.LOW) {
	        	newPanel.setPriority("Low"); 
	        } else if (previousPriority == Priority.LOWEST) {
	        	newPanel.setPriority("Lowest"); 
	        } else {
	        	// set priority to Normal by default 
	        	newPanel.setPriority("Normal"); 
	        }

	        
	        // Deadline - Field Initialization 
	        Date previousDeadlineDate = null; 
	        previousDeadlineDate = (Date) process.getDeadline(); 
	        if (previousDeadlineDate != null) {
		        Calendar previousDeadline = Calendar.getInstance();
		        previousDeadline.setTime(previousDeadlineDate);
	 	        newPanel.setDeadline(previousDeadline);
	        }
	        

	        // Comments - Field Initialization 	  
	        String previousComments = null;
//	        if (isKeyDefined(process, commentsPropName)) {
				previousComments = (String) process.getProperty(commentsPropName);
		        if (previousComments != null) {
	        	   newPanel.setComments(previousComments); 
		        }
//	        }

	        
	        // File Attachments - Field Initialization 	        	        
			HashSet<File> previousFileAttachments = null;
//	        if (isKeyDefined(process, fileAttachmentsPropName)) { 
	        	previousFileAttachments = (HashSet<File>) process.getProperty(fileAttachmentsPropName);
		        if (previousFileAttachments != null ) {
		        	newPanel.setAttachments(previousFileAttachments); 
		        }	      
//	        }
	        
			
	        /*----------------------------------------------------------------------------------
	         *  Add the initialized panel to the Workflow Details Sheet
	         * ----------------------------------------------------------------------------------
	         */
	        workflowDetailsSheet.add(newPanel);
        } catch (Exception e) {
            ex = e;
        }
    }

    private Set<I_GetConceptData> getValidRefsetSpecs() throws Exception {
        Set<I_GetConceptData> refsetSpecs = new HashSet<I_GetConceptData>();

        I_GetConceptData owner = config.getDbConfig().getUserConcept();
        //TODO Replace this test with a TestForRefreshRefsetPermission 
        TestForCreateNewRefsetPermission permissionTest = new TestForCreateNewRefsetPermission();
        Set<I_GetConceptData> permissibleRefsetParents = new HashSet<I_GetConceptData>();
        permissibleRefsetParents.addAll(permissionTest.getValidRefsetsFromIndividualUserPermissions(owner));
        permissibleRefsetParents.addAll(permissionTest.getValidRefsetsFromRolePermissions(owner));

        I_IntSet allowedTypes = termFactory.newIntSet();
        allowedTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());

        for (I_GetConceptData parent : permissibleRefsetParents) {
            Set<I_GetConceptData> children = parent.getDestRelOrigins(null, allowedTypes, null, true, true);
            for (I_GetConceptData child : children) {
                if (isRefset(child)) {
                	refsetSpecs.add(child);
                }
            }
        }

        return refsetSpecs;
    }

    private boolean isRefset(I_GetConceptData child) throws TerminologyException, IOException {
        I_IntSet allowedTypes = termFactory.newIntSet();
        allowedTypes.add(RefsetAuxiliary.Concept.SPECIFIES_REFSET.localize().getNid());

        List<I_RelTuple> relationships = child.getDestRelTuples(null, allowedTypes, null, true, true);
        if (relationships.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    

    
    
//    /**
//     * Determines if the key provided is already defined as a property in the current process 
//     * 
//     * @param process
//     * @param keyName
//     * @return boolean
//     */
//    public boolean isKeyDefined(I_EncodeBusinessProcess process, String keyName) {
//		String propertyName = new String(); 
//		if (keyName.startsWith("A: ")) {
//			propertyName = keyName.substring(3);
//		} else {
//			propertyName = keyName;
//		}		
//		Collection<String> listOfKeys = process.getAttachmentKeys(); 
//		return listOfKeys.contains(propertyName);
//	}

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


}
