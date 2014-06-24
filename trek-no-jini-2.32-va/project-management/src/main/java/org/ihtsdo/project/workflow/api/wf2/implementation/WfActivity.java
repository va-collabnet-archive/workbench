package org.ihtsdo.project.workflow.api.wf2.implementation;

import java.util.UUID;

import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.worker.MasterWorker;
import org.ihtsdo.project.workflow.api.WorkflowInterpreter;
import org.ihtsdo.project.workflow.model.WfAction;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.tk.workflow.api.WfActivityBI;
import org.ihtsdo.tk.workflow.api.WfProcessInstanceBI;

public class WfActivity implements WfActivityBI {
	
	private WfAction action;
	private boolean automatic = false;

	public WfActivity(WfAction action) {
		this.action = action;
	}

	@Override
	public String getName() {
		return action.getName();
	}

	@Override
	public UUID getUuid() {
		return action.getId();
	}

	@Override
	public Object getExecutable() {
		return action.getBusinessProcess();
	}

	@Override
	public void perform(WfProcessInstanceBI instance) throws Exception {
		MasterWorker worker = Terms.get().getActiveAceFrameConfig().getWorker();
		if (worker != null) {
			WorkflowInterpreter.doAction((WfInstance)instance, new WfRole(), action, worker);
		} else {
			WfInstance.updateInstanceState(((WfInstance)instance), action.getConsequence());
		}
	}
	
	public void performInBatch(WfProcessInstanceBI instance) throws Exception {
		MasterWorker worker = Terms.get().getActiveAceFrameConfig().getWorker();
		if (worker != null) {
			WorkflowInterpreter.doActionInBatch((WfInstance)instance, new WfRole(), action, worker);
		} else {
			WfInstance.updateInstanceState(((WfInstance)instance), action.getConsequence());
		}
	}

	@Override
	public boolean isAutomatic() {
		return automatic;
	}

	/**
	 * @return the action
	 */
	public WfAction getAction() {
		return action;
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(WfAction action) {
		this.action = action;
	}

	/**
	 * @param automatic the automatic to set
	 */
	public void setAutomatic(boolean automatic) {
		this.automatic = automatic;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return action.getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WfActivity other = (WfActivity) obj;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		return true;
	}
	
}
