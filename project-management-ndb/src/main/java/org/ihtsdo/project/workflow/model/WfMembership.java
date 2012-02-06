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
 * The Class WfMembership.
 */
public class WfMembership implements Serializable{
	
	/** The id. */
	private UUID id;
	
	/** The user. */
	private WfUser user;
	
	/** The role. */
	private WfRole role;
	
	/** The default assignment. */
	private boolean defaultAssignment;
	
	/**
	 * Instantiates a new wf membership.
	 *
	 * @param id the id
	 * @param user the user
	 * @param role the role
	 * @param defaultAssignment the default assignment
	 */
	public WfMembership(UUID id, WfUser user, WfRole role, boolean defaultAssignment) {
		super();
		this.id = id;
		this.user = user;
		this.role = role;
		this.defaultAssignment = defaultAssignment;
	}

	/**
	 * Gets the user.
	 *
	 * @return the user
	 */
	public WfUser getUser() {
		return user;
	}

	/**
	 * Sets the user.
	 *
	 * @param user the new user
	 */
	public void setUser(WfUser user) {
		this.user = user;
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

	/**
	 * Checks if is default assignment.
	 *
	 * @return true, if is default assignment
	 */
	public boolean isDefaultAssignment() {
		return defaultAssignment;
	}

	/**
	 * Sets the default assignment.
	 *
	 * @param defaultAssignment the new default assignment
	 */
	public void setDefaultAssignment(boolean defaultAssignment) {
		this.defaultAssignment = defaultAssignment;
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
	
}
