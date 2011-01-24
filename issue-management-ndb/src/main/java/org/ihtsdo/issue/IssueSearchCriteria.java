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

import java.util.HashMap;

/**
 * The Class IssueSearchCriteria.
 */
public class IssueSearchCriteria {
	
	/** The user id. */
	private String userId;
	
	/** The download status. */
	private String downloadStatus;
	
	/** The workflow status. */
	private String workflowStatus;
	
	/** The priority. */
	private String priority;
	
	/** The role. */
	private String role;
	
	/** The queue name. */
	private String queueName;
	
	/** The map. */
	private HashMap<String,Object> map;
	
	/**
	 * Instantiates a new issue search criteria.
	 * 
	 * @param userId the user id
	 * @param downloadStatus the download status
	 * @param priority the priority
	 * @param role the role
	 * @param queueName the queue name
	 * @param workflowStatus the workflow status
	 * @param map the map
	 */
	public IssueSearchCriteria(String userId, String downloadStatus, String priority,
			String role, String queueName,String workflowStatus, HashMap<String, Object> map) {
		super();
		this.userId = userId;
		this.downloadStatus = downloadStatus;
		this.workflowStatus = workflowStatus;
		this.priority = priority;
		this.role=role;
		this.queueName=queueName;
		this.map = map;
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
	 * Gets the priority.
	 * 
	 * @return the priority
	 */
	public String getPriority() {
		return priority;
	}
	
	/**
	 * Sets the priority.
	 * 
	 * @param priority the new priority
	 */
	public void setPriority(String priority) {
		this.priority = priority;
	}
	
	/**
	 * Gets the map.
	 * 
	 * @return the map
	 */
	public HashMap<String, Object> getMap() {
		return map;
	}
	
	/**
	 * Sets the map.
	 * 
	 * @param map the map
	 */
	public void setMap(HashMap<String, Object> map) {
		this.map = map;
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
	 * Gets the queue name.
	 * 
	 * @return the queue name
	 */
	public String getQueueName() {
		return queueName;
	}

	/**
	 * Sets the queue name.
	 * 
	 * @param queueName the new queue name
	 */
	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	/**
	 * Gets the download status.
	 * 
	 * @return the download status
	 */
	public String getDownloadStatus() {
		return downloadStatus;
	}

	/**
	 * Sets the download status.
	 * 
	 * @param downloadStatus the new download status
	 */
	public void setDownloadStatus(String downloadStatus) {
		this.downloadStatus = downloadStatus;
	}

	/**
	 * Gets the workflow status.
	 * 
	 * @return the workflow status
	 */
	public String getWorkflowStatus() {
		return workflowStatus;
	}

	/**
	 * Sets the workflow status.
	 * 
	 * @param workflowStatus the new workflow status
	 */
	public void setWorkflowStatus(String workflowStatus) {
		this.workflowStatus = workflowStatus;
	}
}
