package org.ihtsdo.project.workflow.api.wf2.implementation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.project.workflow.model.WfAction;
import org.ihtsdo.project.workflow.model.WorkflowDefinition;
import org.ihtsdo.tk.workflow.api.WfActivityBI;
import org.ihtsdo.tk.workflow.api.WfProcessDefinitionBI;
import org.ihtsdo.tk.workflow.api.WfRoleBI;
import org.ihtsdo.tk.workflow.api.WfStateBI;

public class WfProcessDefinition implements WfProcessDefinitionBI {
	
	WorkflowDefinition definition;

	public WfProcessDefinition(WorkflowDefinition definition) {
		this.definition = definition;
	}

	@Override
	public String getName() {
		return definition.getName();
	}

	@Override
	public UUID getUuid() throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<WfRoleBI> getRoles() {
		List<WfRoleBI> roles = new ArrayList<WfRoleBI>();
		roles.addAll(definition.getRoles());
		return roles;
	}

	@Override
	public Collection<WfStateBI> getStates() {
		List<WfStateBI> states = new ArrayList<WfStateBI>();
		states.addAll(definition.getStates());
		return states;
	}

	@Override
	public Collection<WfActivityBI> getActivities() {
		List<WfActivityBI> activities = new ArrayList<WfActivityBI>();
		for (WfAction loopAction : definition.getActions().values()) {
			activities.add(new WfActivity(loopAction));
		}
		return activities;
	}

	@Override
	public Object getLogic() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean logicIsValid() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isCompleteState(WfStateBI state) {
		if (state.getName().startsWith("Approved") || state.getName().startsWith("Cancel")) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean isPromoteState(WfStateBI state) {
		if (state.getName().startsWith("Approved")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @return the definition
	 */
	public WorkflowDefinition getDefinition() {
		return definition;
	}

	/**
	 * @param definition the definition to set
	 */
	public void setDefinition(WorkflowDefinition definition) {
		this.definition = definition;
	}

}
