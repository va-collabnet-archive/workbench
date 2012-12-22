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
import java.util.UUID;

/**
 * The Class WfUser.
 */
public class WfUser implements Serializable{

	/** The username. */
	private String username;

	/** The id. */
	private UUID id;

	/** The permissions. */
	private List<WfPermission> permissions;

	/**
	 * Instantiates a new wf user.
	 */
	public WfUser() {
		super();
	}

	/**
	 * Instantiates a new wf user.
	 * 
	 * @param username
	 *            the username
	 * @param id
	 *            the id
	 */
	public WfUser(String username, UUID id) {
		super();
		this.username = username;
		this.id = id;
		this.permissions = new ArrayList<WfPermission>();
	}

	/**
	 * Instantiates a new wf user.
	 * 
	 * @param username
	 *            the username
	 * @param id
	 *            the id
	 * @param permissions
	 *            the permissions
	 */
	public WfUser(String username, UUID id, List<WfPermission> permissions) {
		super();
		this.username = username;
		this.id = id;
		this.permissions = permissions;
	}

	/**
	 * Gets the username.
	 * 
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Sets the username.
	 * 
	 * @param username
	 *            the new username
	 */
	public void setUsername(String username) {
		this.username = username;
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
	 * @param id
	 *            the new id
	 */
	public void setId(UUID id) {
		this.id = id;
	}

	/**
	 * Gets the permissions.
	 * 
	 * @return the permissions
	 */
	public List<WfPermission> getPermissions() {
		return permissions;
	}

	/**
	 * Sets the permissions.
	 * 
	 * @param permissions
	 *            the new permissions
	 */
	public void setPermissions(List<WfPermission> permissions) {
		this.permissions = permissions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return username;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof WfUser) {
			WfUser user = (WfUser) obj;
			return user.getId().equals(id);
		} else {
			return false;
		}
	}

}