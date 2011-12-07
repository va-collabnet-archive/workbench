package org.ihtsdo.project.workflow.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class WorkflowDefinition {
	
	private List<WfRole> roles;
	private List<WfState> states;
	private Map<String,? extends WfAction> actions;
	private List<String> xlsFileName;
	private List<String> drlFileName;
	private String name ;

	public WorkflowDefinition() {
		super();
		this.xlsFileName = new ArrayList<String>();
		this.drlFileName = new ArrayList<String>();
	}

	public WorkflowDefinition(List<WfRole> roles, List<WfState> states,
			Map<String,? extends WfAction> actions) {
		super();
		this.roles = roles;
		this.states = states;
		this.actions = actions;
		this.xlsFileName = new ArrayList<String>();
		this.drlFileName = new ArrayList<String>();
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

	public Map<String,? extends WfAction> getActions() {
		return actions;
	}

	public void setActions(Map<String,? extends WfAction> actions) {
		this.actions = actions;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getXlsFileName() {
		return xlsFileName;
	}

	public void setXlsFileName(List<String> xlsFileName) {
		this.xlsFileName = xlsFileName;
	}

	public List<String> getDrlFileName() {
		return drlFileName;
	}

	public void setDrlFileName(List<String> drlFileName) {
		this.drlFileName = drlFileName;
	}

}
