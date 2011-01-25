package org.ihtsdo.workflow;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.workflow.WorkflowHistoryJavaBeanBI;



/* 
* @author Jesse Efron
* 
*/
public class WorkflowHistoryJavaBean implements WorkflowHistoryJavaBeanBI{
	private UUID workflowId = null;
	private UUID conceptId = null;
	private UUID useCase = null;
	private UUID path = null;
	private UUID modeler = null;
	private UUID action = null;
	private UUID state = null;
	private String fsn = null;
	private Long timeStamp = null;
	private boolean autoApproved;
	private boolean overridden;
	
	private Long refsetColumnTimeStamp = null;

	@Override
	public void setWorkflowId(UUID id ) {
		workflowId = id;
	}

	@Override
	public UUID getWorkflowId() {
		return workflowId;
	}

	@Override
	public void setConceptId(UUID id ) {
		conceptId = id;
	}

	@Override
	public UUID getConceptId() {
		return conceptId;
	}

	@Override
	public void setUseCase(UUID id ) {
		useCase = id;
	}

	@Override
	public UUID getUseCase() {
		return useCase;
	}

	@Override
	public void setPath(UUID id ) {
		path = id;
	}

	@Override
	public UUID getPath() {
		return path;
	}

	@Override
	public void setModeler(UUID id ) {
		modeler = id;
	}

	@Override
	public	UUID getModeler() {
		return modeler;
	}

	@Override
	public void setState(UUID id ) {
		state = id;
	}

	@Override
	public UUID getState() {
		return state;
	}

	@Override
	public void setAction(UUID id ) {
		action = id;
	}

	@Override
	public UUID getAction() {
		return action;
	}

	@Override
	public void setFSN(String desc) {
		fsn = desc;
	}

	@Override
	public String getFSN() {
		return fsn;
	}

	@Override
	public void setTimeStamp(Long t ) {
		timeStamp = t;
	}

	@Override
	public Long getTimeStamp() {
		return timeStamp;
	}

	public boolean getAutoApproved() {
		return autoApproved;
	}
	
	public boolean getOverridden() {
		return overridden;
	}

	public void setAutoApproved(boolean b) {
		autoApproved = b;
	}

	public void setOverridden(boolean b) {
		overridden = b;
	}

	
	
	@Override
	public void setRefsetColumnTimeStamp(Long t) {
		refsetColumnTimeStamp = t;
		
	}

	@Override
	public Long getRefsetColumnTimeStamp() {
		return refsetColumnTimeStamp;
	}

	public I_GetConceptData getReferencedComponent() throws TerminologyException, IOException {
		return Terms.get().getConcept(getConceptId());
	}

	public String toString() {
		try {
			I_TermFactory tf = Terms.get();
			
			return "\nReferenced Component Id(Concept) = " + getReferencedComponent().getInitialText() + 
				   "\nConcept Id = " + conceptId.toString() +
				   "\nWorkflow Id = " + workflowId.toString() +
				   "\nUse Case = " + tf.getConcept(useCase).getInitialText() + 
				   "\nPath = " + tf.getConcept(path).getInitialText() +
				   "\nModeler = " + tf.getConcept(modeler).getInitialText() + 
				   "\nAction = " + tf.getConcept(action).getInitialText() +
				   "\nState = " + tf.getConcept(state).getInitialText() +
				   "\nFSN = " + fsn +
				   "\nTimestamp = " + timeStamp + 
				   "\nAutoApproved = " + autoApproved + 
				   "\nOverridden = " + overridden + 
				   "\nrefsetColumnTimeStamp = " + refsetColumnTimeStamp +
				   "\nTimestamp = " + timeStamp;
		} catch (IOException io) {
			return "Failed to identify referencedComponentId or WorkflowHistory" + 
				   "\nError msg: " + io.getMessage();
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Failed to access fields of WorkflowHistoryJavaBean: " + this.toString(), e);
			return "";
		}
	}
	
	@Override
	public boolean equals(Object o1) {
		WorkflowHistoryJavaBean o2 = (WorkflowHistoryJavaBean)o1;
		
		if (this.getAction().equals(o2.getAction()) &&
			this.getConceptId().equals(o2.getConceptId()) &&
			this.getFSN().equals(o2.getFSN()) &&
			this.getModeler().equals(o2.getModeler()) &&
			this.getPath().equals(o2.getPath()) &&
			this.getState().equals(o2.getState()) &&
			this.getTimeStamp().equals(o2.getTimeStamp()) &&
			this.getRefsetColumnTimeStamp().equals(o2.getRefsetColumnTimeStamp()) &&
			this.getUseCase().equals(o2.getUseCase()) &&
			this.getWorkflowId().equals(o2.getWorkflowId()) &&
			this.getAutoApproved() == o2.getAutoApproved() && 
			this.getOverridden() == o2.getOverridden()

		) 
			
			return true;
		else
			return false;	
	}



}
