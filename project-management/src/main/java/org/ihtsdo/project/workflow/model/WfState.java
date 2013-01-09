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
import java.util.UUID;

import org.ihtsdo.project.workflow2.WfStateBI;

/**
 * The Class WfState.
 */
public class WfState implements Serializable, WfStateBI {

	/** The name. */
	private String name;
	
	/** The id. */
	private UUID id;

	/**
	 * Instantiates a new wf state.
	 */
	public WfState() {
		super();
	}

	/**
	 * Instantiates a new wf state.
	 *
	 * @param name the name
	 * @param id the id
	 */
	public WfState(String name, UUID id) {
		super();
		this.name = name;
		this.id = id;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof WfState) {
			WfState wfState = (WfState) obj;
			return this.id.equals(wfState.id);
		} else {
			return false;
		}
	}

	@Override
	public UUID getUuid() {
		return getId();
	}

}