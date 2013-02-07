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

}
