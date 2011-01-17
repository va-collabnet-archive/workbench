package org.ihtsdo.workflow.refset.stateTrans;

import java.io.IOException;

import org.dwfa.ace.api.I_GetConceptData;
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
	
	public I_GetConceptData getReferencedComponentId() {
		return getCategory();
	}
	
	public void setCategory(I_GetConceptData category) {
		((StateTransitionRSFields)fields).setCategory(category);
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

	public I_GetConceptData getCategory() {
		return ((StateTransitionRSFields)fields).getCategory();
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
		 		
		private void setCategory(I_GetConceptData category) {
			setReferencedComponentId(category);
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
		
		private I_GetConceptData getCategory() {
			return getReferencedComponentId();
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
				return "\nReferenced Component Id (Editor Category) = " + getReferencedComponentId().getInitialText() + 
					   "(" + getReferencedComponentId().getConceptNid() + ")" +
					   "\nWorkflow Type = " + workflowType.getInitialText() +
					   "\nInitial State = " + initialState.getInitialText() +
					   "\nAction = " + action.getInitialText() +
					   "\nFinal State= " + finalState.getInitialText();
			} catch (IOException io) {
				return "Failed to identify referencedComponentId" + 
					   "\nError msg: " + io.getMessage();
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
			return ((getReferencedComponentId() != null) && 
					(initialState != null) &&  
					(action != null) &&  
					(finalState != null));
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
