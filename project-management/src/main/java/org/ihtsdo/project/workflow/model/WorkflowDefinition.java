/*
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.project.workflow.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * The Class WorkflowDefinition.
 */
public class WorkflowDefinition implements Serializable {
	
	/** The roles. */
	private List<WfRole> roles;
	
	/** The states. */
	private List<WfState> states;
	
	/** The actions. */
	private Map<String,WfAction> actions;
	
	/** The xls file name. */
	private List<String> xlsFileName;
	
	/** The drl file name. */
	private List<String> drlFileName;
	
	/** The name. */
	private String name ;

	/**
	 * Instantiates a new workflow definition.
	 */
	public WorkflowDefinition() {
		super();
		this.xlsFileName = new ArrayList<String>();
		this.drlFileName = new ArrayList<String>();
	}

	/**
	 * Instantiates a new workflow definition.
	 *
	 * @param roles the roles
	 * @param states the states
	 * @param actions the actions
	 */
	public WorkflowDefinition(List<WfRole> roles, List<WfState> states,
			Map<String,WfAction> actions) {
		super();
		this.roles = roles;
		this.states = states;
		this.actions = actions;
		this.xlsFileName = new ArrayList<String>();
		this.drlFileName = new ArrayList<String>();
	}

	/**
	 * Gets the roles.
	 *
	 * @return the roles
	 */
	public List<WfRole> getRoles() {
		return roles;
	}

	/**
	 * Sets the roles.
	 *
	 * @param roles the new roles
	 */
	public void setRoles(List<WfRole> roles) {
		this.roles = roles;
	}

	/**
	 * Gets the states.
	 *
	 * @return the states
	 */
	public List<WfState> getStates() {
		return states;
	}

	/**
	 * Sets the states.
	 *
	 * @param states the new states
	 */
	public void setStates(List<WfState> states) {
		this.states = states;
	}

	/**
	 * Gets the actions.
	 *
	 * @return the actions
	 */
	public Map<String,WfAction> getActions() {
		return actions;
	}

	/**
	 * Sets the actions.
	 *
	 * @param actions the actions
	 */
	public void setActions(Map<String,WfAction> actions) {
		this.actions = actions;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the xls file name.
	 *
	 * @return the xls file name
	 */
	public List<String> getXlsFileName() {
		return xlsFileName;
	}

	/**
	 * Sets the xls file name.
	 *
	 * @param xlsFileName the new xls file name
	 */
	public void setXlsFileName(List<String> xlsFileName) {
		this.xlsFileName = xlsFileName;
	}

	/**
	 * Gets the drl file name.
	 *
	 * @return the drl file name
	 */
	public List<String> getDrlFileName() {
		return drlFileName;
	}

	/**
	 * Sets the drl file name.
	 *
	 * @param drlFileName the new drl file name
	 */
	public void setDrlFileName(List<String> drlFileName) {
		this.drlFileName = drlFileName;
	}

}
