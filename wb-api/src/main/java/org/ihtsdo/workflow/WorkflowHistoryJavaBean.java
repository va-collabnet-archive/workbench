package org.ihtsdo.workflow;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.tk.api.workflow.WorkflowHistoryJavaBeanBI;
import org.ihtsdo.tk.hash.Hashcode;



/* 
* @author Jesse Efron
* 
*/
public class WorkflowHistoryJavaBean implements WorkflowHistoryJavaBeanBI{
	private UUID workflowId = null;
	private UUID concept = null;
	private UUID path = null;
	private UUID modeler = null;
	private UUID action = null;
	private UUID state = null;
	private String fsn = null;
	private Long workflowTime = null;
	private Long effectiveTime = null;
	private boolean autoApproved;
	private boolean overridden;
	private int memberId = 0;

	@Override
	public void setWorkflowId(UUID id ) {
		workflowId = id;
	}

	@Override
	public UUID getWorkflowId() {
		return workflowId;
	}

	@Override
	public void setConcept(UUID id ) {
		concept = id;
	}

	@Override
	public UUID getConcept() {
		return concept;
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
	public void setEffectiveTime(Long t ) {
		effectiveTime = t;
	}

	@Override
	public Long getEffectiveTime() {
		return effectiveTime;
	}

	@Override
	public void setWorkflowTime(Long t ) {
		workflowTime = t;
	}

	@Override
	public Long getWorkflowTime() {
		return workflowTime;
	}
	
	@Override
	public boolean getAutoApproved() {
		return autoApproved;
	}
	
	@Override
	public int getRxMemberId() {
		return memberId;
	}
	
	@Override
	public boolean getOverridden() {
		return overridden;
	}

	@Override
	public void setAutoApproved(boolean b) {
		autoApproved = b;
	}

	@Override
	public void setRxMemberId(int id) {
		memberId = id;
	}

	@Override
	public void setOverridden(boolean b) {
		overridden = b;
	}

	@Override
	public String toString() {
		try {
			I_TermFactory tf = Terms.get();
			
			return "\nConcept (Referenced Component Id) = " + tf.getConcept(concept).getInitialText() +
				   "\nWorkflow Id = " + workflowId.toString() +
				   "\nPath = " + tf.getConcept(path).getInitialText() +
				   "\nModeler = " + tf.getConcept(modeler).getInitialText() + 
				   "\nAction = " + tf.getConcept(action).getInitialText() +
				   "\nState = " + tf.getConcept(state).getInitialText() +
				   "\nFSN = " + fsn +
				   "\nEffectiveTimestamp = " + effectiveTime +
				   "\nWorkflow Time = " + workflowTime + 
				   "\nAutoApproved = " + autoApproved + 
				   "\nOverridden = " + overridden +
				   "\nRxMemberId = " + memberId;
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
			this.getConcept().equals(o2.getConcept()) &&
			this.getFSN().equals(o2.getFSN()) &&
			this.getModeler().equals(o2.getModeler()) &&
			this.getPath().equals(o2.getPath()) &&
			this.getState().equals(o2.getState()) &&
			this.getEffectiveTime().equals(o2.getEffectiveTime()) &&
			this.getWorkflowTime().equals(o2.getWorkflowTime()) &&
			this.getWorkflowId().equals(o2.getWorkflowId()) &&
			this.getAutoApproved() == o2.getAutoApproved() && 
			this.getRxMemberId() == o2.getRxMemberId() && 
			this.getOverridden() == o2.getOverridden()
		) {
			return true;
		} else {
			return false;	
	}
	}

    @Override
    public int hashCode() {
    	Integer autoHashCode = (autoApproved) ? Integer.MAX_VALUE : Integer.MIN_VALUE; 
    	Integer overHashCode = (overridden) ? Integer.MAX_VALUE : Integer.MIN_VALUE;
    	
        return Hashcode.compute(new int[]{action.hashCode(), concept.hashCode(), fsn.hashCode(), modeler.hashCode(),
        								  path.hashCode(), state.hashCode(), effectiveTime.hashCode(), workflowTime.hashCode(), 
        								  memberId, autoHashCode, overHashCode});
    }

}
