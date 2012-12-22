/**
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
package org.ihtsdo.issue;

import java.io.Serializable;
import java.util.UUID;

/**
 * The Class IssueRepoRegistration.
 */
public class IssueRepoRegistration implements Serializable{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The Constant ISSUE_REPO_PROPERTY_NAME. */
	public static final String ISSUE_REPO_PROPERTY_NAME="RepositoryRegistrations";
	
	/** The uuid. */
	private UUID uuid;
	
	/** The repository uu id. */
	private UUID repositoryUUId;
	
	/** The user id. */
	private String userId;
	
	/** The password. */
	private String password;

	/** The role. */
	private String role;
	

	/**
	 * Instantiates a new issue repo registration.
	 */
	public IssueRepoRegistration(){}
	
	/**
	 * Instantiates a new issue repo registration.
	 * 
	 * @param repositoryUUId the repository uu id
	 * @param userId the user id
	 * @param password the password
	 * @param role the role
	 */
	public IssueRepoRegistration(UUID repositoryUUId, String userId,
			String password, String role) {
		super();
		this.repositoryUUId = repositoryUUId;
		this.userId = userId;
		this.password = password;
		this.role = role;
		this.uuid = UUID.randomUUID();
	}

	/**
	 * Gets the repository uu id.
	 * 
	 * @return the repository uu id
	 */
	public UUID getRepositoryUUId() {
		return repositoryUUId;
	}

	/**
	 * Sets the repository id.
	 * 
	 * @param repositoryUUId the new repository id
	 */
	public void setRepositoryId(UUID repositoryUUId) {
		this.repositoryUUId = repositoryUUId;
	}

	/**
	 * Gets the user id.
	 * 
	 * @return the user id
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * Sets the user id.
	 * 
	 * @param userId the new user id
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * Gets the password.
	 * 
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password.
	 * 
	 * @param password the new password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Gets the role.
	 * 
	 * @return the role
	 */
	public String getRole() {
		return role;
	}

	/**
	 * Sets the role.
	 * 
	 * @param role the new role
	 */
	public void setRole(String role) {
		this.role = role;
	}

	/**
	 * Gets the uuid.
	 * 
	 * @return the uuid
	 */
	public UUID getUuid() {
		return uuid;
	}

	/**
	 * Sets the uuid.
	 * 
	 * @param uuid the new uuid
	 */
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
}
