package org.ihtsdo.workflow.refset.history;

import java.io.IOException;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.cement.WorkflowAuxiliary;
import org.dwfa.tapi.TerminologyException;



/* 
* @author Jesse Efron
* 
*/
public class WorkflowHistoryJavaBean {
	private UUID workflowId = null;
	private UUID conceptId = null;
	private I_GetConceptData useCase = null;
	private I_GetConceptData path = null;
	private I_GetConceptData modeler = null;
	private I_GetConceptData action = null;
	private I_GetConceptData state = null;
	private String fsn = null;
	private String timeStamp = null;

	public void setWorkflowId(UUID id ) {
		workflowId = id;
	}

	public UUID getWorkflowId() {
		return workflowId;
	}

	public void setConceptId(UUID id ) {
		conceptId = id;
	}

	public UUID getConceptId() {
		return conceptId;
	}

	public void setUseCase(I_GetConceptData sctId ) {
		useCase = sctId;
	}

	public I_GetConceptData getUseCase() {
		return useCase;
	}

	public void setPath(I_GetConceptData sctId ) {
		path = sctId;
	}

	public I_GetConceptData getPath() {
		return path;
	}

	public void setModeler(I_GetConceptData sctId ) {
		modeler = sctId;
	}

	public	I_GetConceptData getModeler() {
		return modeler;
	}

	public void setState(I_GetConceptData sctId ) {
		state = sctId;
	}

	public I_GetConceptData getState() {
		return state;
	}

	public void setAction(I_GetConceptData sctId ) {
		action = sctId;
	}

	public I_GetConceptData getAction() {
		return action;
	}

	public void setFSN(String desc) {
		fsn = desc;
	}

	public String getFSN() {
		return fsn;
	}

	public void setTimeStamp(String stamp ) {
		timeStamp = stamp;
	}

	public String getTimeStamp() {
		return timeStamp;
	}
	
	public I_GetConceptData getReferencedComponent() throws TerminologyException, IOException {
		return Terms.get().getConcept(WorkflowAuxiliary.Concept.WORKFLOW_HISTORY_INFORMATION.getUids());
	}


	public String toString() {
		try {
			return "\nReferenced Component Id(Hard-Coded Workflow History Concept) = " + getReferencedComponent().getInitialText() + 
				   "(" + getReferencedComponent().getConceptNid() + ")" +
				   "\nWorkflow Id = " + workflowId +
				   "\nConcept Id = " + conceptId +
				   "\nUse Case = " + useCase.getInitialText() + 
				   "\nPath = " + path.getInitialText() +
				   "\nModeler = " + modeler.getInitialText() + 
				   "\nAction = " + action.getInitialText() +
				   "\nState = " + state.getInitialText() +
				   "\nFSN = " + fsn +
				   "\nTimestamp = " + timeStamp;
		} catch (IOException io) {
			return "Failed to identify referencedComponentId or WorkflowHistory" + 
				   "\nError msg: " + io.getMessage();
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}
}
