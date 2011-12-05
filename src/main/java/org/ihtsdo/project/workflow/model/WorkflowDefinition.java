package org.ihtsdo.project.workflow.model;

import java.util.List;
import java.util.Map;

import org.drools.KnowledgeBase;


public class WorkflowDefinition {
	
	private List<WfRole> roles;
	private List<WfState> states;
	private Map<String,WfAction> actions;
	private String stateTransitionKBFileName;
	private String name ;

	public WorkflowDefinition() {
		super();
	}

	public WorkflowDefinition(List<WfRole> roles, List<WfState> states,
			Map<String,WfAction> actions) {
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

	public Map<String,WfAction> getActions() {
		return actions;
	}

	public void setActions(Map<String,WfAction> actions) {
		this.actions = actions;
	}

	public String getStateTransitionKBFileName() {
		return stateTransitionKBFileName;
	}

	public void setStateTransitionKBFileName(String stateTransitionKBFileName) {
		this.stateTransitionKBFileName = stateTransitionKBFileName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
