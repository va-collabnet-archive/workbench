package org.ihtsdo.project.workflow.model;

import java.util.List;


public class WorkflowDefinition {
	
	private List<WfRole> roles;
	private List<WfState> states;
	private List<WfAction> actions;

	public WorkflowDefinition() {
		super();
	}

	public WorkflowDefinition(List<WfRole> roles, List<WfState> states,
			List<WfAction> actions) {
		super();
		this.roles = roles;
		this.states = states;
		this.actions = actions;
	}

	public List<WfRole> getRoles() {
		return roles;
	}

	public void setRoles(List<WfRole> roles) {
		this.roles = roles;
	}

	public List<WfState> getStates() {
		return states;
	}

	public void setStates(List<WfState> states) {
		this.states = states;
	}

	public List<WfAction> getActions() {
		return actions;
	}

	public void setActions(List<WfAction> actions) {
		this.actions = actions;
	}

}
