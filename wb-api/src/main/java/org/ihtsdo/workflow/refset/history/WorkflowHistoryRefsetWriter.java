package org.ihtsdo.workflow.refset.history;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.RefsetAuxiliary;
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
	
	
	
	public void setWorkflowUid(UUID uid) {
			((WorkflowHistoryRSFields)fields).setWorkflowUid(uid);
	}

 	
	public void setConceptUid(UUID uid) {
			((WorkflowHistoryRSFields)fields).setConceptUid(uid);
	}
	
	
	public void setUseCaseUid(UUID useCase) {
		((WorkflowHistoryRSFields)fields).setUseCaseUid(useCase);
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

	public void setRefsetColumnTimeStamp(Long timeStamp) {
		((WorkflowHistoryRSFields)fields).setRefsetColumnTimeStamp(timeStamp);
	}

	public void setTimeStamp(Long timeStamp) {
		((WorkflowHistoryRSFields)fields).setTimeStamp(timeStamp);
	}

	public void setAutoApproved(boolean b) {
		((WorkflowHistoryRSFields)fields).setAutoApproved(b);
	}	
	
	public void setOverride(boolean b) {
		((WorkflowHistoryRSFields)fields).setOverride(b);
	}	
	
	public UUID getWorkflowUid() {
		return ((WorkflowHistoryRSFields)fields).getWorkflowUid();
	}
		
	public UUID getConceptUid() {
		return ((WorkflowHistoryRSFields)fields).getConceptUid();
	}
	
	public UUID getUseCaseUid() {
		return ((WorkflowHistoryRSFields)fields).getUseCaseUid();
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
	
	public Long getRefsetColumnTimeStamp() {
		return ((WorkflowHistoryRSFields)fields).getRefsetColumnTimeStamp();
	}

	public Long getTimeStamp() {
		return ((WorkflowHistoryRSFields)fields).getTimeStamp();
	}
	
	public boolean getAutoApproved() {
		return ((WorkflowHistoryRSFields)fields).getAutoApproved();
	}

	
	public boolean getOverride() {
		return ((WorkflowHistoryRSFields)fields).getOverride();
	}

	
	
	public static void lockMutex() {
		inUse = true; 
	}
	
	public static boolean isInUse() {
		return inUse;
	}
	
	public static void unLockMutex() {
		inUse = false;
	}
	
	
	
	public class WorkflowHistoryRSFields extends WorkflowRefsetFields {
	
		
		public UUID workflowId = null;
		public UUID conceptId = null;
		public UUID useCase = null;
		public UUID path = null;
		public UUID modeler = null;
		public UUID action = null;
		public UUID state = null;
		public String fsn = null;
		public Long refsetColumnTimeStamp = null;
		public Long timeStamp = null;
		public boolean autoApproved;
		public boolean override;
		
		public WorkflowHistoryRSFields() {
			try {
				setReferencedComponentId(Terms.get().getConcept(RefsetAuxiliary.Concept.WORKFLOW_HISTORY.getUids()));
			} catch (Exception e) {
				
			}
		}
		
		public void setWorkflowUid(UUID uid) {
			workflowId = uid;
		}
		
		public void setConceptUid(UUID uid) {
			try {
				setReferencedComponentId(Terms.get().getConcept(uid));
			} catch (Exception e) {
	        	AceLog.getAppLog().alertAndLog(Level.SEVERE, "Error getting concept from database: " + uid, e);
			}
		}

		public void setUseCaseUid(UUID uc) {
			useCase = uc;
		}

		public void setPathUid(UUID p) {
			path = p;
		}
		
		public void setModelerUid(UUID mod) {
			modeler = mod;
		}

		public void setActionUid(UUID act) {
			action = act;
		}

		public void setStateUid(UUID s) {
			state = s;
		}

		public void setFSN(String desc) {
			fsn = desc;
		}

		public void setRefsetColumnTimeStamp(Long ts) {
			refsetColumnTimeStamp = ts;
		}

		public void setTimeStamp(Long ts) {
			timeStamp = ts;
		}
		
		public void setAutoApproved(boolean b) {
			this.autoApproved = b;
		}

		public void setOverride(boolean b) {
			this.override = b;
		}


		public UUID getWorkflowUid() {
			return workflowId;
		}
		
		public UUID getConceptUid() {
			return getReferencedComponentId().getPrimUuid();
		}

		public UUID getUseCaseUid() {
			return useCase;
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

		public Long getRefsetColumnTimeStamp() {
			return refsetColumnTimeStamp;
		}

		public Long getTimeStamp() {
			return timeStamp;
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

				return "\nReferenced Component Id(Concept) = " + getReferencedComponentId().getInitialText() + 
				   "\nWorkflow Uid = " + workflowId.toString() +
				   "\nUseCase = " + tf.getConcept(useCase).getInitialText() +
				   "\nPath = " + tf.getConcept(path).getInitialText() +
				   "\nModeler = " + tf.getConcept(modeler).getInitialText() +
				   "\nAction = " + tf.getConcept(action).getInitialText() +
				   "\nState = " + tf.getConcept(state).getInitialText() +
				   "\ngetRefsetColumnTimeStamp = " + refsetColumnTimeStamp +
				   "\ntimestamp = " + timeStamp +
				   "\nFSN = " + fsn;
			} catch (Exception io) {
				return "Failed to identify referencedComponentId for StressTest" + 
					   "\nError msg: " + io.getMessage();
			}
		}

		@Override
		public void cleanValues() {
			workflowId = null;
			useCase = null;
			path = null;
			modeler = null;
			action = null;
			state = null;
			fsn = null;
			refsetColumnTimeStamp = null;
		}


		@Override
		public boolean valuesExist() {
			boolean retVal =  workflowId != null &&
							  useCase != null && path != null &&
							  modeler != null && action != null &&
							  state != null && refsetColumnTimeStamp != null & 
							  fsn != null && fsn.length() > 0;
								
			if (!retVal)
			{
				StringBuffer str = new StringBuffer();
				str.append("\nError in adding to Workflow History Refset");
				str.append("\nReferencedComponentId:" + getReferencedComponentId());
				str.append("\nworkflowId:" + workflowId);
				str.append("\nuseCase:" + useCase);
				str.append("\npath:" + path);
				str.append("\nmodeler:" + modeler);
				str.append("\naction:" + useCase);
				str.append("\nstate:" + state);
				str.append("\nrefsetColumnTimeStamp:" + refsetColumnTimeStamp);
				str.append("\nfsn:" + fsn);
	        	AceLog.getAppLog().alertAndLog(Level.SEVERE, str.toString(), new Exception("Failure in updating Workflow History Refset"));
			}
			
			return retVal;
		}
	}

	@Override
	public String fieldsToRefsetString() throws IOException {
		return "<properties>\n" +
				   	"<property>" +
				   		"<key>workflowId</key>" +
				   		"<value>" + getWorkflowUid() + "</value>" +
				   	"</property>" + 
				   	"<property>" +
			   			"<key>useCase</key>" +
			   			"<value>" + getUseCaseUid() + "</value>" +
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
			   			"<key>refsetColumnTimeStamp</key>" +
			   			"<value>" + getRefsetColumnTimeStamp() + "</value>" +
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
		setConceptUid(update.getConceptId());
    	setWorkflowUid(update.getWorkflowId());
    	setActionUid(update.getAction());
    	setFSN(update.getFSN());
    	setModelerUid(update.getModeler());
    	setPathUid(update.getPath());
    	setStateUid(update.getState());
    	setUseCaseUid(update.getUseCase());
    	setAutoApproved(update.getAutoApproved());
    	
    	java.util.Date today = new java.util.Date();
        setTimeStamp(Long.MAX_VALUE);
        // Add new timestamp for new version (in case this row is retired for WFid)
		setRefsetColumnTimeStamp(today.getTime());
        
        WorkflowHistoryRefsetWriter.lockMutex();
        addMember();
        Terms.get().addUncommitted(this.getRefsetConcept());
        Terms.get().commit();
		WorkflowHistoryRefsetWriter.unLockMutex();
	}
}