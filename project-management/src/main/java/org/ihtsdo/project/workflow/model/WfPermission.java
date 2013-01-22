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

/**
 * The Class WfPermission.
 */
public class WfPermission implements Serializable {
	
	/** The id. */
	private UUID id;
	
	/** The role. */
	private WfRole role;
	
	/** The hiearchy id. */
	private UUID hiearchyId;
	
	/**
	 * Instantiates a new wf permission.
	 */
	public WfPermission() {
		super();
	}
	
	/**
	 * Instantiates a new wf permission.
	 *
	 * @param id the id
	 * @param role the role
	 * @param hiearchyId the hiearchy id
	 */
	public WfPermission(UUID id, WfRole role, UUID hiearchyId) {
		super();
		this.id = id;
		this.role = role;
		this.hiearchyId = hiearchyId;
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
	 * Gets the hiearchy id.
	 *
	 * @return the hiearchy id
	 */
	public UUID getHiearchyId() {
		return hiearchyId;
	}
	
	/**
	 * Sets the hiearchy id.
	 *
	 * @param hiearchyId the new hiearchy id
	 */
	public void setHiearchyId(UUID hiearchyId) {
		this.hiearchyId = hiearchyId;
	}
	
	/**
	 * Gets the role name.
	 *
	 * @return the role name
	 */
	public String getRoleName() {
		return role.getName();
	}

	/**
	 * Gets the role.
	 *
	 * @return the role
	 */
	public WfRole getRole() {
		return role;
	}

	/**
	 * Sets the role.
	 *
	 * @param role the new role
	 */
	public void setRole(WfRole role) {
		this.role = role;
	}

}
