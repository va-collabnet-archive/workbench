package org.ihtsdo.workflow.refset.history;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.WorkflowRefsetFields;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetWriter;



/* 
* @author Jesse Efron
* 
*/
public class WorkflowHistoryRefsetWriter extends WorkflowRefsetWriter {

	private static boolean inUse = false;

	public WorkflowHistoryRefsetWriter() throws IOException, TerminologyException {
		refset = new WorkflowHistoryRefset();
		fields = new WorkflowHistoryRSFields();
		
		setRefsetName(refset.getRefsetName());
		setRefsetId(refset.getRefsetId());
	}
	
	// Statics
	public static void lockMutex() {
		inUse = true; 
	}
	
	public static boolean isInUse() {
		return inUse;
	}
	
	public static void unLockMutex() {
		inUse = false;
	}

	// Setters
	public void setReferencedComponentId(UUID uid) {
		((WorkflowHistoryRSFields)fields).setReferencedComponentUid(uid);
}

	public void setReleaseDescriptionUid(UUID uid) {
		setReferencedComponentId(uid);
}

	public void setWorkflowUid(UUID uid) {
			((WorkflowHistoryRSFields)fields).setWorkflowUid(uid);
	}

 	public void setConceptUid(UUID uid) {
			((WorkflowHistoryRSFields)fields).setConceptUid(uid);
	}
	
	public void setPathUid(UUID path) {
		((WorkflowHistoryRSFields)fields).setPathUid(path);
	}

	public void setModelerUid(UUID modeler) {
		((WorkflowHistoryRSFields)fields).setModelerUid(modeler);	
	}

	public void setActionUid(UUID action) {
		((WorkflowHistoryRSFields)fields).setActionUid(action);
	}

	public void setStateUid(UUID state) {
		((WorkflowHistoryRSFields)fields).setStateUid(state);
	}

	public void setFSN(String fsn) {
		((WorkflowHistoryRSFields)fields).setFSN(fsn);
	}

	public void setWorkflowTime(Long timeStamp) {
		((WorkflowHistoryRSFields)fields).setWorkflowTime(timeStamp);
	}

	public void setEffectiveTime(Long timeStamp) {
		((WorkflowHistoryRSFields)fields).setEffectiveTime(timeStamp);
	}

	public void setAutoApproved(boolean b) {
		((WorkflowHistoryRSFields)fields).setAutoApproved(b);
	}	
	
	public void setOverride(boolean b) {
		((WorkflowHistoryRSFields)fields).setOverride(b);
	}	
	
	
	// Getters
	public UUID getReferencedComponentUid() {
		return ((WorkflowHistoryRSFields)fields).getReferencedComponentId();
	}
	
	public UUID getReleaseDescriptionUid() {
		return getReferencedComponentUid();
	}
	
	public UUID getConceptUid() {
		return ((WorkflowHistoryRSFields)fields).getConceptUid();
	}
	
	public UUID getWorkflowUid() {
		return ((WorkflowHistoryRSFields)fields).getWorkflowUid();
	}
	
	public UUID getPathUid() {
		return ((WorkflowHistoryRSFields)fields).getPathUid();
	}
	
	public UUID getModelerUid() {
		return ((WorkflowHistoryRSFields)fields).getModelerUid();	
	}
	
	public UUID getActionUid() {
		return ((WorkflowHistoryRSFields)fields).getActionUid();
	}
	
	public UUID getStateUid() {
		return ((WorkflowHistoryRSFields)fields).getStateUid();
	}
	
	public String getFSN() {
		return ((WorkflowHistoryRSFields)fields).getFSN();
	}
	
	public Long getWorkflowTime() {
		return ((WorkflowHistoryRSFields)fields).getWorkflowTime();
	}

	public Long getEffectiveTime() {
		return ((WorkflowHistoryRSFields)fields).getEffectiveTime();
	}
	
	public boolean getAutoApproved() {
		return ((WorkflowHistoryRSFields)fields).getAutoApproved();
	}
	
	public boolean getOverride() {
		return ((WorkflowHistoryRSFields)fields).getOverride();
	}

	
	
	
	// Actual Fields
	public class WorkflowHistoryRSFields extends WorkflowRefsetFields {
	
		public UUID concept = null;
		public UUID workflowId = null;
		public UUID path = null;
		public UUID modeler = null;
		public UUID action = null;
		public UUID state = null;
		public String fsn = null;
		public Long workflowTime = null;
		public Long effectiveTime = null;
		public boolean autoApproved = false;
		public boolean override = false;
		

		
		
		public void setReferencedComponentUid(UUID uid) {
			try {
				setReferencedComponentId(uid);
			} catch (Exception e) {
		    	AceLog.getAppLog().log(Level.SEVERE, "Unable to set WorkflowHistoryRefset's refCompId");
			}
		}
		
		public void setReleaseDescriptionUid(UUID uid) {
			setReferencedComponentUid(uid);
		}
		
		public void setConceptUid(UUID uid) {
			concept = uid;
		}

		public void setWorkflowUid(UUID uid) {
			workflowId = uid;
		}
		
		public void setModelerUid(UUID mod) {
			modeler = mod;
		}

		public void setActionUid(UUID act) {
			action = act;
		}

		public void setPathUid(UUID uid) {
			path = uid;
		}
		
		public void setStateUid(UUID s) {
			state = s;
		}

		public void setFSN(String desc) {
			fsn = desc;
		}

		public void setWorkflowTime(Long ts) {
			workflowTime = ts;
		}

		public void setEffectiveTime(Long ts) {
			effectiveTime = ts;
		}
		
		public void setAutoApproved(boolean b) {
			this.autoApproved = b;
		}

		public void setOverride(boolean b) {
			this.override = b;
		}


		
		
		public I_GetConceptData getReferencedComponent() {
			try {
				return Terms.get().getConcept(getReferencedComponentId());
			} catch (Exception e) {
		    	AceLog.getAppLog().log(Level.SEVERE, "Unable to set WorkflowHistoryRefset's refCompId");
			}
			
			return null;
		}
		
		public UUID getReferencedComponentUid() {
			return getReferencedComponentId();
		}
		
		public UUID getReleaseDescriptionUid() {
			return getReferencedComponentUid();
		}
		
		public UUID getConceptUid() {
			return concept;
		}
		
		public UUID getWorkflowUid() {
			return workflowId;
		}

		public UUID getPathUid() {
			return path;
		}
		
		public UUID getModelerUid() {
			return modeler;
		}

		public UUID getActionUid() {
			return action;
		}

		public UUID getStateUid() {
			return state;
		}

		public String getFSN() {
			return fsn;
		}

		public Long getWorkflowTime() {
			return workflowTime;
		}

		public Long getEffectiveTime() {
			return effectiveTime;
		}
		public boolean getAutoApproved() {
			return autoApproved;
		}

		public boolean getOverride() {
			return override;
		}
		
		public String toString() {
			try { 
				I_TermFactory tf = Terms.get();

				return "\nReferenced Component Id(Concept) = " + getReferencedComponent().getInitialText() + 
				   "\nConcept = " + tf.getConcept(concept).getInitialText() +
				   "\nWorkflow Uid = " + workflowId.toString() +
				   "\nPath = " + tf.getConcept(path).getInitialText() +
				   "\nModeler = " + tf.getConcept(modeler).getInitialText() +
				   "\nAction = " + tf.getConcept(action).getInitialText() +
				   "\nState = " + tf.getConcept(state).getInitialText() +
				   "\nWorkflow Timestamp = " + workflowTime +
				   "\nEffectiveTime = " + effectiveTime +
				   "\nAutoApproved = " + autoApproved + 
				   "\nOverridden = " + override + 
				   "\nFSN = " + fsn;
			} catch (Exception io) {
				return "Failed to identify referencedComponentId for StressTest" + 
					   "\nError msg: " + io.getMessage();
			}
		}

		@Override
		public void cleanValues() {
			setReferencedComponentUid(null);
			concept = null;
			workflowId = null;
			path = null;
			modeler = null;
			action = null;
			state = null;
			fsn = null;
			workflowTime = null;
			effectiveTime = null;
		}


		@Override
		public boolean valuesExist() {
			boolean retVal =  getReferencedComponentUid() != null &&
							  concept != null &&
							  workflowId != null && 
							  path != null &&
							  modeler != null && 
							  action != null &&
							  state != null && 
							  workflowTime != null & 
							  effectiveTime != null & 
							  fsn != null && fsn.length() > 0;
								
			if (!retVal)
			{
				StringBuffer str = new StringBuffer();
				str.append("\nError in adding to Workflow History Refset");
				str.append("\nReferencedComponentId:" + getReferencedComponentId());
				str.append("\nconceptId:" + concept);
				str.append("\nworkflowId:" + workflowId);
				str.append("\npath:" + path);
				str.append("\nmodeler:" + modeler);
				str.append("\naction:" + action);
				str.append("\nstate:" + state);
				str.append("\nfsn:" + fsn);
				str.append("\noverride:" + override);
				str.append("\nautoApproved:" + autoApproved);
				str.append("\nworkflowTime:" + workflowTime);
				str.append("\neffectiveTime:" + effectiveTime);
				AceLog.getAppLog().log(Level.WARNING, "Failure in updating Workflow History Refset for concept: " + str.toString());
			}
			
			return retVal;
		}
	}

	@Override
	public String fieldsToRefsetString() throws IOException {
		return "<properties>\n" +
				   	"<property>" +
				   		"<key>concept</key>" +
				   		"<value>" + getConceptUid() + "</value>" +
				   	"</property>" + 
				   	"<property>" +
				   		"<key>workflowId</key>" +
				   		"<value>" + getWorkflowUid() + "</value>" +
				   	"</property>" + 
			   		"<property>" +
			   			"<key>path</key>" +
			   			"<value>" + getPathUid() + "</value>" +
			   		"</property>" + 
			   		"<property>" +
			   			"<key>modeler</key>" +
			   			"<value>" + getModelerUid() + "</value>" +
			   		"</property>" + 
			   		"<property>" +
			   			"<key>state</key>" +
			   			"<value>" + getStateUid() + "</value>" +
		   			"</property>" + 
				   	"<property>" +
						"<key>action</key>" +
						"<value>" + getActionUid() + "</value>" +
			   		"</property>" + 
			   		"<property>" +
			   			"<key>workflowTime</key>" +
			   			"<value>" + getWorkflowTime() + "</value>" +
			   		"</property>" + 
			   		"<property>" +
			   			"<key>fsn</key>" +
			   			"<value>" + getFSN() + "</value>" +
			   		"</property>" + 
			   		"<property>" +
		   			"<key>autoApproved</key>" +
		   			"<value>" + getAutoApproved() + "</value>" +
		   		"</property>" + 
		   		"<property>" +
	   			"<key>overridden</key>" +
	   			"<value>" + getOverride() + "</value>" +
	   		"</property>" + 
			"</properties>"; 
	}
	
	public void updateWorkflowHistory(WorkflowHistoryJavaBean update) throws Exception
	{
		setReleaseDescriptionUid(update.getReleaseDescription());
		setConceptUid(update.getConcept());
    	setWorkflowUid(update.getWorkflowId());
    	setActionUid(update.getAction());
    	setFSN(update.getFSN());
    	setModelerUid(update.getModeler());
    	setPathUid(update.getPath());
    	setStateUid(update.getState());
    	setAutoApproved(update.getAutoApproved());
    	setOverride(update.getOverridden());
    	
    	java.util.Date today = new java.util.Date();
        setEffectiveTime(Long.MAX_VALUE);
        // Add new timestamp for new version (in case this row is retired for WFid)
		setWorkflowTime(today.getTime());
        
        WorkflowHistoryRefsetWriter.lockMutex();
        addMember();
        Terms.get().addUncommitted(this.getRefsetConcept());
        Terms.get().commit();
		WorkflowHistoryRefsetWriter.unLockMutex();
	}
}