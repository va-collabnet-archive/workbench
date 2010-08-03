package org.ihtsdo.workflow.refset.strans;

import java.io.IOException;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.RefsetFields;
import org.ihtsdo.workflow.refset.utilities.RefsetWriterUtility;



/* 
* @author Jesse Efron
* 
*/
public class StateTransitionRefsetWriter extends RefsetWriterUtility {
	private final static int identifiedRefsetId = 0;
	private final static String identifiedRefsetName = "State Transition Refset";
	
	public StateTransitionRefsetWriter() throws IOException, TerminologyException {
		StateTransitionRefset refset = new StateTransitionRefset();
		setRefsetName(refset.getRefsetName());
		setRefsetId(refset.getRefsetId());
		fields = new StateTransitionRSFields();
	}
	
	public void setEditorCategory(I_GetConceptData category) {
		((StateTransitionRSFields)fields).setEditorCategory(category);
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

	public I_GetConceptData getEditorCategory() {
		return ((StateTransitionRSFields)fields).getReferencedComponentId();
	}

	public I_GetConceptData getWorkflowType() {
		return ((StateTransitionRSFields)fields).workflowType;
	}

	public I_GetConceptData getInitialState() {
		return ((StateTransitionRSFields)fields).initialState;
	}

	public I_GetConceptData getAction() {
		return ((StateTransitionRSFields)fields).action;
	}

	public I_GetConceptData getFinalState() {
		return ((StateTransitionRSFields)fields).finalState;
	}

	private class StateTransitionRSFields extends RefsetFields{
		private I_GetConceptData workflowType = null;
		private I_GetConceptData initialState = null;
		private I_GetConceptData action = null;
		private I_GetConceptData finalState = null;
		 		
		private StateTransitionRSFields() {

		}
		
		private void setEditorCategory(I_GetConceptData category) {
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
		
		public String toString() {
			try {
				return "\nReferenced Component Id (Editor Category) = " + getReferencedComponentId().getInitialText() + 
					   "(" + getReferencedComponentId().getConceptId() + ")" +
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
			// TODO Auto-generated method stub
			setReferencedComponentId(null);
			initialState = null;
			action = null;
			finalState = null;
		}

		@Override
		public boolean valuesExist() {
			// TODO Auto-generated method stub
			return ((getReferencedComponentId() != null) && 
					(workflowType != null) &&  
					(initialState != null) &&  
					(action != null) &&  
					(finalState != null));
		}
	}

	public String fieldsToRefsetString() throws IOException {
		return  "<properties>\n" +
				   	"<property>\n" +
				   		"<key>workflowType</key>" +
				   		"<value>" + getWorkflowType().getConceptId() + "</value>" +
				   	"</property>" + 
				   	"<property>" +
			   			"<key>initialState</key>" +
			   			"<value>" + getInitialState().getConceptId() + "</value>" +
			   		"</property>" + 
					   	"<property>" +
				   		"<key>action</key>" +
				   		"<value>" + getAction().getConceptId() + "</value>" +
				   	"</property>" + 
				   	"<property>" +
				   		"<key>finalState</key>" +
				   		"<value>" + getFinalState().getConceptId() + "</value>" +
				   	"</property>" +
				"</properties>"; 
	}
}
