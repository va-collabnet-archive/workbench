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

import java.io.File;
import java.io.Serializable;
import java.util.UUID;

/**
 * The Class WfAction.
 */
public class WfAction implements Serializable{

	/** The name. */
	private String name;
	
	/** The id. */
	private UUID id;
	
	/** The consequence. */
	private WfState consequence;
	
	/** The business process. */
	private File businessProcess;
	
	/**
	 * Instantiates a new wf action.
	 *
	 * @param name the name
	 */
	public WfAction(String name) {
		super();
		this.name = name;
	}
	
	/**
	 * Instantiates a new wf action.
	 *
	 * @param name the name
	 * @param id the id
	 * @param consequence the consequence
	 * @param businessProcess the business process
	 */
	public WfAction(String name, UUID id, WfState consequence,
			File businessProcess) {
		super();
		this.name = name;
		this.id = id;
		this.consequence = consequence;
		this.businessProcess = businessProcess;
	}

	/**
	 * Instantiates a new wf action.
	 */
	public WfAction() {
		super();
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
	 * Gets the id.
	 *
	 * @return the id
	 */
	public UUID getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(UUID id) {
		this.id = id;
	}

	/**
	 * Gets the consequence.
	 *
	 * @return the consequence
	 */
	public WfState getConsequence() {
		return consequence;
	}

	/**
	 * Sets the consequence.
	 *
	 * @param consequence the new consequence
	 */
	public void setConsequence(WfState consequence) {
		this.consequence = consequence;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return name;
	}

	/**
	 * Gets the business process.
	 *
	 * @return the business process
	 */
	public File getBusinessProcess() {
		return businessProcess;
	}

	/**
	 * Sets the business process.
	 *
	 * @param businessProcess the new business process
	 */
	public void setBusinessProcess(File businessProcess) {
		this.businessProcess = businessProcess;
	}

}
