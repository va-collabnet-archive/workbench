package org.ihtsdo.workflow.refset.stateTrans;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.WorkflowRefsetFields;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetWriter;



/* 
* @author Jesse Efron
* 
*/
public class StateTransitionRefsetWriter extends WorkflowRefsetWriter 
{
	public StateTransitionRefsetWriter() throws IOException, TerminologyException {
		refset = new StateTransitionRefset();
		fields = new StateTransitionRSFields();
	
		setRefsetName(refset.getRefsetName());
		setRefsetId(refset.getRefsetId());
	}
	
	public void setReferencedComponentId(UUID uid) {
		((StateTransitionRSFields)fields).setReferencedComponentUid(uid);
	}
	
	public void setCategory(I_GetConceptData category) {
		setReferencedComponentId(category.getPrimUuid());
	}
	
	public void setWorkflowType(I_GetConceptData type) {
		((StateTransitionRSFields)fields).setWorkflowType(type);
	}

	public void setInitialState(I_GetConceptData state) {
		((StateTransitionRSFields)fields).setInitialState(state);
	}

	public void setAction(I_GetConceptData action) {
		((StateTransitionRSFields)fields).setAction(action);
	}

	public void setFinalState(I_GetConceptData state) {
		((StateTransitionRSFields)fields).setFinalState(state);
	}
	
	public UUID getReferencedComponentUid() {
		return ((StateTransitionRSFields)fields).getReferencedComponentId();
	}

	public I_GetConceptData getCategory() {
		try {
			return Terms.get().getConcept(getReferencedComponentUid());
		} catch (Exception e) {
	    	AceLog.getAppLog().log(Level.SEVERE, "Unable to get the Category (refCompId) from the StateTransitionRefset");
		}
		
		return null;
	}

	public I_GetConceptData getWorkflowType() {
		return ((StateTransitionRSFields)fields).getWorkflowType();
	}

	public I_GetConceptData getInitialState() {
		return ((StateTransitionRSFields)fields).getInitialState();
	}

	public I_GetConceptData getAction() {
		return ((StateTransitionRSFields)fields).getAction();
	}

	public I_GetConceptData getFinalState() {
		return ((StateTransitionRSFields)fields).getFinalState();
	}

	private class StateTransitionRSFields extends WorkflowRefsetFields {
		private I_GetConceptData workflowType = null;
		private I_GetConceptData initialState = null;
		private I_GetConceptData action = null;
		private I_GetConceptData finalState = null;
		 		
		public void setReferencedComponentUid(UUID uid) {
			try {
				setReferencedComponentId(uid);
			} catch (Exception e) {
		    	AceLog.getAppLog().log(Level.SEVERE, "Unable to set WorkflowHistoryRefset's refCompId: " + uid);
			}
		}
		
		public void setCategoryUid(UUID uid) {
			setReferencedComponentUid(uid);
		}
		

		private void setWorkflowType(I_GetConceptData type) {
			workflowType = type;
		}

		private void setInitialState(I_GetConceptData state) {
			initialState = state;
		}

		private void setAction(I_GetConceptData a) {
			action = a;
		}

		private void setFinalState(I_GetConceptData state) {
			finalState = state;
		}
		
		public I_GetConceptData getReferencedComponent() {
			try {
				return Terms.get().getConcept(getReferencedComponentId());
			} catch (Exception e) {
		    	AceLog.getAppLog().log(Level.SEVERE, "Unable to set WorkflowHistoryRefset's refCompId: ");
			}
			
			return null;
		}
		
		public UUID getReferencedComponentUid() {
			return getReferencedComponentId();
		}
		
		public I_GetConceptData getCategory() {
			return getReferencedComponent();
		}
		
		private I_GetConceptData getAction() {
			return action;
		}

		private I_GetConceptData getFinalState() {
			return finalState;
		}
		
		private I_GetConceptData getWorkflowType() {
			return workflowType;
		}

		private I_GetConceptData getInitialState() {
			return initialState;
		}


		public String toString() {
			try {
				I_GetConceptData con = Terms.get().getConcept(getReferencedComponentId());
				return "\nReferenced Component Id (Editor Category) = " + con.getInitialText() + 
					   "(" + con.getConceptNid() + ")" +
					   "\nWorkflow Type = " + workflowType.getInitialText() +
					   "\nInitial State = " + initialState.getInitialText() +
					   "\nAction = " + action.getInitialText() +
					   "\nFinal State= " + finalState.getInitialText();
			} catch (Exception e) {
		    	AceLog.getAppLog().log(Level.SEVERE, "Unable to identify the relCompId of the row");
				return ""; 
			}
		}

		@Override
		public void cleanValues() {
			setReferencedComponentId(null);
			initialState = null;
			action = null;
			finalState = null;
		}

		@Override
		public boolean valuesExist() {
			boolean retVal = ((getReferencedComponentId() != null) && 
					(initialState != null) &&  
					(action != null) &&  
					(finalState != null));
									
			if (!retVal)
			{
				StringBuffer str = new StringBuffer();
				str.append("\nError in adding to State Transition Refset");
				str.append("\nReferencedComponentId:" + getReferencedComponentId());
				str.append("\ninitialState:" + initialState);
				str.append("\naction:" + action);
				str.append("\nfinalState:" + finalState);
				AceLog.getAppLog().log(Level.WARNING, "Failure in updating State Transition Refset for concept: " + str.toString());
			}
			
			return retVal;
		}
	}

	@Override
	public String fieldsToRefsetString() throws IOException {
		return  "<properties>\n" +
				   	"<property>\n" +
				   		"<key>workflowType</key>" +
				   		"<value>" + getWorkflowType().getPrimUuid() + "</value>" +
				   	"</property>" + 
				   	"<property>" +
			   			"<key>initialState</key>" +
			   			"<value>" + getInitialState().getPrimUuid() + "</value>" +
			   		"</property>" + 
					   	"<property>" +
				   		"<key>action</key>" +
				   		"<value>" + getAction().getPrimUuid() + "</value>" +
				   	"</property>" + 
				   	"<property>" +
				   		"<key>finalState</key>" +
				   		"<value>" + getFinalState().getPrimUuid() + "</value>" +
				   	"</property>" +
				"</properties>"; 
	}
}
