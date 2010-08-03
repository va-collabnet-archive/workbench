package org.ihtsdo.workflow.refset.history;

import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.cement.WorkflowAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.time.TimeUtil;
import org.ihtsdo.workflow.refset.RefsetFields;
import org.ihtsdo.workflow.refset.utilities.RefsetWriterUtility;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetHelper;



/* 
* @author Jesse Efron
* 
*/
public class WorkflowHistoryRefsetWriter extends RefsetWriterUtility {
	private static boolean inUse = false;
	
	public WorkflowHistoryRefsetWriter() throws IOException, TerminologyException {
		WorkflowHistoryRefset refset = new WorkflowHistoryRefset();
		setRefsetName(refset.getRefsetName());
		setRefsetId(refset.getRefsetId());
		fields = new WorkflowHistoryRSFields();
	}
	
	/*
	WF Action (RefCompId)
	WFID - UUID 
	ConId - UUID
	Use Case - SCT 
	Path - SCT
	Modeler - SCT
	Action - SCT
	State - SCT
	FSN - String
	TimeStamp -  ?
	*/
	
	public void setWorkflowId(UUID uid) {
			((WorkflowHistoryRSFields)fields).setWorkflowId(uid);
	}

 	
	public void setConceptId(UUID uid) {
			((WorkflowHistoryRSFields)fields).setConceptId(uid);
	}
	
	
	public void setUseCase(I_GetConceptData useCase) {
		((WorkflowHistoryRSFields)fields).setUseCase(useCase);
	}

	public void setPath(I_GetConceptData path) {
		((WorkflowHistoryRSFields)fields).setPath(path);
	}

	public void setModeler(I_GetConceptData modeler) {
		((WorkflowHistoryRSFields)fields).setModeler(modeler);	
	}

	public void setAction(I_GetConceptData action) {
		((WorkflowHistoryRSFields)fields).setAction(action);
	}

	public void setState(I_GetConceptData state) {
		((WorkflowHistoryRSFields)fields).setState(state);
	}

	public void setFSN(String fsn) {
		((WorkflowHistoryRSFields)fields).setFSN(fsn);
	}

	public void setTimeStamp(String timeStamp) {
		((WorkflowHistoryRSFields)fields).setTimeStamp(timeStamp);
	}

	public UUID getWorkflowId() {
		return ((WorkflowHistoryRSFields)fields).getWorkflowId();
	}
		
	public UUID getConceptId() {
		return ((WorkflowHistoryRSFields)fields).getConceptId();
	}
	
	public I_GetConceptData getUseCase() {
		return ((WorkflowHistoryRSFields)fields).getUseCase();
	}
	
	public I_GetConceptData getPath() {
		return ((WorkflowHistoryRSFields)fields).getPath();
	}
	
	public I_GetConceptData getModeler() {
		return ((WorkflowHistoryRSFields)fields).getModeler();	
	}
	
	public I_GetConceptData getAction() {
		return ((WorkflowHistoryRSFields)fields).getAction();
	}
	
	public I_GetConceptData getState() {
		return ((WorkflowHistoryRSFields)fields).getState();
	}
	
	public String getFSN() {
		return ((WorkflowHistoryRSFields)fields).getFSN();
	}
	
	public String getTimeStamp() {
		return ((WorkflowHistoryRSFields)fields).getTimeStamp();
	}

	public void lockMutex() {
		inUse = true;
	}
	
	public boolean isInUse() {
		return inUse;
	}
	
	public void unLockMutex() {
		inUse = false;
	}
	
	
	
	public class WorkflowHistoryRSFields extends RefsetFields{
	
		
		public UUID workflowId = null;
		public UUID conceptId = null;
		public I_GetConceptData useCase = null;
		public I_GetConceptData path = null;
		public I_GetConceptData modeler = null;
		public I_GetConceptData action = null;
		public I_GetConceptData state = null;
		public String fsn = null;
		public String timeStamp = null;
		 		
		public WorkflowHistoryRSFields() {
			try {
				setReferencedComponentId(Terms.get().getConcept(WorkflowAuxiliary.Concept.WORKFLOW_HISTORY_INFORMATION.getUids()));
			} catch (Exception e) {
				
			}
		}
		
		public void setWorkflowId(UUID uid) {
			workflowId = uid;
		}
		
		public void setConceptId(UUID uid) {
			conceptId = uid;
		}

		public void setUseCase(I_GetConceptData uc) {
			useCase = uc;
		}

		public void setPath(I_GetConceptData p) {
			path = p;
		}
		
		public void setModeler(I_GetConceptData mod) {
			modeler = mod;
		}

		public void setAction(I_GetConceptData act) {
			action = act;
		}

		public void setState(I_GetConceptData s) {
			state = s;
		}

		public void setFSN(String desc) {
			fsn = desc;
		}

		public void setTimeStamp(String ts) {
			timeStamp = ts;
		}

		public UUID getWorkflowId() {
			return workflowId;
		}
		
		public UUID getConceptId() {
			return conceptId;
		}

		public I_GetConceptData getUseCase() {
			return useCase;
		}

		public I_GetConceptData getPath() {
			return path;
		}
		
		public I_GetConceptData getModeler() {
			return modeler;
		}

		public I_GetConceptData getAction() {
			return action;
		}

		public I_GetConceptData getState() {
			return state;
		}

		public String getFSN() {
			return fsn;
		}

		public String getTimeStamp() {
			return timeStamp;
		}

		
		public String toString() {
			try {
				return "\nReferenced Component Id(Hard-Coded Workflow History Concept) = " + getReferencedComponentId().getInitialText() + 
					   "(" + getReferencedComponentId().getConceptId() + ")" +
					   "\nWorkflow Id = " + workflowId +
					   "\nConcept Id = " + conceptId +
					   "\nUse Case = " + useCase.getConceptId() + 
					   "\nPath = " + path.getConceptId() +
					   "\nModeler = " + modeler.getConceptId() + 
					   "\nAction = " + action.getConceptId() +
					   "\nState = " + state.getConceptId() +
					   "\nFSN = " + fsn +
					   "\nTimestamp = " + timeStamp;
			} catch (IOException io) {
				return "Fai" +
						"led to identify referencedComponentId or WorkflowHistory" + 
					   "\nError msg: " + io.getMessage();
			}
		}

		@Override
		public void cleanValues() {
			workflowId = null;
			conceptId = null;
			useCase = null;
			path = null;
			modeler = null;
			action = null;
			state = null;
			fsn = null;
			timeStamp = null;
		}

		@Override
		public boolean valuesExist() {
			// TODO Auto-generated method stub
			return ((getReferencedComponentId() != null) && 
					(workflowId != null && conceptId != null) && (useCase != null) && 
					(path != null) && (modeler != null) && 
					(action != null) && (state != null) && 
					(fsn != null && fsn.length() > 0) &&
					(timeStamp != null && timeStamp.length() > 0));
		}
	}

	public String fieldsToRefsetString() {
		return "<properties>\n" +
				   	"<property>\n" +
				   		"<key>workflowId</key>" +
				   		"<value>" + getWorkflowId().toString() + "</value>" +
				   	"</property>" + 
				   	"<property>" +
				   		"<key>conceptId</key>" +
				   		"<value>" + getConceptId().toString() + "</value>" +
				   	"</property>" + 
				   	"<property>" +
			   			"<key>useCase</key>" +
			   			"<value>" + getUseCase().getConceptId() + "</value>" +
			   		"</property>" + 
			   		"<property>" +
			   			"<key>path</key>" +
			   			"<value>" + getPath().getConceptId() + "</value>" +
			   		"</property>" + 
			   		"<property>" +
			   			"<key>modeler</key>" +
			   			"<value>" + getModeler().getConceptId() + "</value>" +
			   		"</property>" + 
			   		"<property>" +
		   				"<key>action</key>" +
		   				"<value>" + getAction().getConceptId() + "</value>" +
		   			"</property>" + 
				   	"<property>" +
			   			"<key>state</key>" +
			   			"<value>" + getState().getConceptId() + "</value>" +
			   		"</property>" + 
			   		"<property>" +
			   			"<key>fsn</key>" +
			   			"<value>" + getFSN() + "</value>" +
			   		"</property>" + 
			   		"<property>" +
						"<key>timeStamp</key>" +
						"<value>" + getTimeStamp() + "</value>" +
					"</property>" + 
			"</properties>"; 
	}
	
	public void updateWorkflowHistory(WorkflowHistoryJavaBean update) throws Exception
	{
		setConceptId(update.getConceptId());
    	setWorkflowId(update.getWorkflowId());
    	setAction(update.getAction());
    	setFSN(update.getFSN());
    	setModeler(update.getModeler());
    	setPath(update.getPath());
    	setState(update.getState());
    	setUseCase(update.getUseCase());
    	
    	java.util.Date today = new java.util.Date();
        setTimeStamp(TimeUtil.getDateFormat().format(today));

        addMember();

        Terms.get().addUncommitted(Terms.get().getConcept(getRefsetId()));
		Terms.get().commit();
	}
}