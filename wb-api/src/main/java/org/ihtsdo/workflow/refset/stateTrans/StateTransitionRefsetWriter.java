package org.ihtsdo.workflow.refset.stateTrans;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.workflow.refset.WorkflowRefsetFields;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetWriter;



/* 
* @author Jesse Efron
* 
*/
public class StateTransitionRefsetWriter extends WorkflowRefsetWriter 
{
	public StateTransitionRefsetWriter() throws IOException, TerminologyException {
		super(stateTransitionConcept);
		fields = new StateTransitionRSFields();
	}
	
	public void setReferencedComponentId(UUID uid) {
		((StateTransitionRSFields)fields).setReferencedComponentUid(uid);
	}
	
	public void setCategory(I_GetConceptData category) {
		setReferencedComponentId(category.getPrimUuid());
	}

	public void setCategory(ConceptVersionBI category) {
		setReferencedComponentId(category.getPrimUuid());
	}

	public UUID getReferencedComponentUid() {
		return ((StateTransitionRSFields)fields).getReferencedComponentUid();
	}
	
	public UUID getCategoryUid() {
		return getReferencedComponentUid();
	}

	
	
	public void setWorkflowType(I_GetConceptData type) {
		((StateTransitionRSFields)fields).setWorkflowType(type.getPrimUuid());
	}

	public void setInitialState(I_GetConceptData state) {
		((StateTransitionRSFields)fields).setInitialState(state.getPrimUuid());
	}

	public void setAction(I_GetConceptData action) {
		((StateTransitionRSFields)fields).setAction(action.getPrimUuid());
	}

	public void setFinalState(I_GetConceptData state) {
		((StateTransitionRSFields)fields).setFinalState(state.getPrimUuid());
	}

	public void setWorkflowType(ConceptVersionBI type) {
		((StateTransitionRSFields)fields).setWorkflowType(type.getPrimUuid());
	}

	public void setInitialState(ConceptVersionBI state) {
		((StateTransitionRSFields)fields).setInitialState(state.getPrimUuid());
	}

	public void setAction(ConceptVersionBI action) {
		((StateTransitionRSFields)fields).setAction(action.getPrimUuid());
	}

	public void setFinalState(ConceptVersionBI state) {
		((StateTransitionRSFields)fields).setFinalState(state.getPrimUuid());
	}

	public UUID getWorkflowTypeUid() {
		return ((StateTransitionRSFields)fields).getWorkflowType();
	}

	public UUID getInitialStateUid() {
		return ((StateTransitionRSFields)fields).getInitialState();
	}

	public UUID getActionUid() {
		return ((StateTransitionRSFields)fields).getAction();
	}

	public UUID getFinalStateUid() {
		return ((StateTransitionRSFields)fields).getFinalState();
	}

	private class StateTransitionRSFields extends WorkflowRefsetFields {
		private UUID workflowType = null;
		private UUID initialState = null;
		private UUID action = null;
		private UUID finalState = null;
		 		
		public void setCategory(UUID uid) {
			setReferencedComponentUid(uid);
		}
		

		private void setWorkflowType(UUID type) {
			workflowType = type;
		}

		private void setInitialState(UUID state) {
			initialState = state;
		}

		private void setAction(UUID a) {
			action = a;
		}

		private void setFinalState(UUID state) {
			finalState = state;
		}

		public UUID getCategory() {
			return getReferencedComponentUid();
		}
		
		private UUID getAction() {
			return action;
		}

		private UUID getFinalState() {
			return finalState;
		}
		
		private UUID getWorkflowType() {
			return workflowType;
		}

		private UUID getInitialState() {
			return initialState;
		}


		public String toString() {
			try {
				I_GetConceptData con = Terms.get().getConcept(getReferencedComponentUid());
				return "\nReferenced Component Id (Editor Category) = " + con.getInitialText() + 
					   "(" + con.getConceptNid() + ")" +
					   "\nWorkflow Type = " + Terms.get().getConcept(workflowType).getInitialText() +
					   "\nInitial State = " + Terms.get().getConcept(initialState).getInitialText() +
					   "\nAction = " + Terms.get().getConcept(action).getInitialText() +
					   "\nFinal State= " + Terms.get().getConcept(finalState).getInitialText();
			} catch (Exception e) {
		    	AceLog.getAppLog().log(Level.SEVERE, "Unable to identify the relCompId of the row with error: " + e.getMessage());
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
			boolean retVal = ((getReferencedComponentUid() != null) && 
					(initialState != null) &&  
					(action != null) &&  
					(finalState != null));
									
			if (!retVal)
			{
				StringBuffer str = new StringBuffer();
				str.append("\nError in adding to State Transition Refset");
				str.append("\nReferencedComponentId:" + getReferencedComponentUid());
				str.append("\ninitialState:" + initialState);
				str.append("\naction:" + action);
				str.append("\nfinalState:" + finalState);
			}
			
			return retVal;
		}
	}

	@Override
	public String fieldsToRefsetString() throws IOException {
		return  "<properties>\n" +
				   	"<property>\n" +
				   		"<key>workflowType</key>" +
				   		"<value>" + getWorkflowTypeUid() + "</value>" +
				   	"</property>" + 
				   	"<property>" +
			   			"<key>initialState</key>" +
			   			"<value>" + getInitialStateUid() + "</value>" +
			   		"</property>" + 
					   	"<property>" +
				   		"<key>action</key>" +
				   		"<value>" + getActionUid() + "</value>" +
				   	"</property>" + 
				   	"<property>" +
				   		"<key>finalState</key>" +
				   		"<value>" + getFinalStateUid() + "</value>" +
				   	"</property>" +
				"</properties>"; 
	}
}
