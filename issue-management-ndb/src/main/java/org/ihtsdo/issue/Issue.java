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
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

/**
 * The Class Issue.
 */
public class Issue implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Id. */
	private int Id;

	/** The UU id. */
	private UUID UUId;
	
	/** The external id. */
	private String externalId;
	
	/** The title. */
	private String title;
	
	/** The component. */
	private String component;
	
	/** The component id. */
	private String componentId;
	
	/** The description. */
	private String description;

	/** The priority. */
	private String priority;
	
	/** The user. */
	private String user;

	/** The role. */
	private String role="";
	
	/** The queue name. */
	private String queueName="";
	
	/** The external user. */
	private String externalUser="";
	
	/** The category. */
	private String category;
	
	/** The workflow status. */
	private String workflowStatus="";

	/** The download status. */
	private String downloadStatus="";
	
	/** The project id. */
	private String projectId;
	
	/** The repository uu id. */
	private UUID repositoryUUId;
	
	/** The comments for update. */
	private String commentsForUpdate="";
	
	/** The field map att id. */
	private String fieldMapAttId="";
	
	private long lastModifiedDate;
	
	/** The field map. */
	private HashMap<String,Object> fieldMap;
	
	/**
	 * Instantiates a new issue.
	 */
	public Issue() {
		this.fieldMap = new HashMap<String,Object>();
		this.lastModifiedDate=new Date().getTime();
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public int getId() {
		return Id;
	}

	/**
	 * Sets the id.
	 * 
	 * @param id the new id
	 */
	public void setId(int id) {
		Id = id;
	}
	
	/**
	 * The Enum CATEGORY.
	 */
	public enum CATEGORY{
		  
  		/** The Source_ error. */
  		Source_Error
	}
	
	/**
	 * The Enum STATUS.
	 */
	public enum STATUS {
		
		/** The Open. */
		Open,
/** The Closed. */
Closed,
/** The Pending. */
Pending
	}
	
	/**
	 * The Enum PRIORITY.
	 */
	public enum PRIORITY{
		
		/** The DEFAULT. */
		DEFAULT, 
 /** The EMERGENCY. */
 EMERGENCY
	}
	
	/**
	 * Gets the title.
	 * 
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the title.
	 * 
	 * @param title the new title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Gets the external id.
	 * 
	 * @return the external id
	 */
	public String getExternalId() {
		return externalId;
	}

	/**
	 * Sets the external id.
	 * 
	 * @param externalId the new external id
	 */
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	/**
	 * Gets the component.
	 * 
	 * @return the component
	 */
	public String getComponent() {
		return component;
	}

	/**
	 * Sets the component.
	 * 
	 * @param component the new component
	 */
	public void setComponent(String component) {
		this.component = component;
	}

	/**
	 * Gets the component id.
	 * 
	 * @return the component id
	 */
	public String getComponentId() {
		return componentId;
	}

	/**
	 * Sets the component id.
	 * 
	 * @param componentId the new component id
	 */
	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}

	/**
	 * Gets the description.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 * 
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
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
	 * Gets the user.
	 * 
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Sets the external user.
	 * 
	 * @param user the new external user
	 */
	public void setExternalUser(String user) {
		this.externalUser = user;
	}

	/**
	 * Gets the external user.
	 * 
	 * @return the external user
	 */
	public String getExternalUser() {
		return externalUser;
	}

	/**
	 * Sets the user.
	 * 
	 * @param user the new user
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * Gets the category.
	 * 
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * Sets the category.
	 * 
	 * @param category the new category
	 */
	public void setCategory(String category) {
		this.category = category;
	}


	/**
	 * Gets the project id.
	 * 
	 * @return the project id
	 */
	public String getProjectId() {
		return projectId;
	}

	/**
	 * Sets the project id.
	 * 
	 * @param projectId the new project id
	 */
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	/**
	 * Gets the comments for update.
	 * 
	 * @return the comments for update
	 */
	public String getCommentsForUpdate() {
		return commentsForUpdate;
	}

	/**
	 * Sets the comments for update.
	 * 
	 * @param commentsForUpdate the new comments for update
	 */
	public void setCommentsForUpdate(String commentsForUpdate) {
		this.commentsForUpdate = commentsForUpdate;
	}

	/**
	 * Gets the field map.
	 * 
	 * @return the field map
	 */
	public HashMap<String, Object> getFieldMap() {
		return fieldMap;
	}

	/**
	 * Sets the field map.
	 * 
	 * @param fieldMap the field map
	 */
	public void setFieldMap(HashMap<String, Object> fieldMap) {
		this.fieldMap = fieldMap;
	}

	/**
	 * Gets the field map att id.
	 * 
	 * @return the field map att id
	 */
	public String getFieldMapAttId() {
		return fieldMapAttId;
	}

	/**
	 * Sets the field map att id.
	 * 
	 * @param fieldMapAttId the new field map att id
	 */
	public void setFieldMapAttId(String fieldMapAttId) {
		this.fieldMapAttId = fieldMapAttId;
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
	 * Sets the repository uu id.
	 * 
	 * @param repositoryUUId the new repository uu id
	 */
	public void setRepositoryUUId(UUID repositoryUUId) {
		this.repositoryUUId = repositoryUUId;
	}

	/**
	 * Gets the uU id.
	 * 
	 * @return the uU id
	 */
	public UUID getUUId() {
		return UUId;
	}

	/**
	 * Sets the uU id.
	 * 
	 * @param id the new uU id
	 */
	public void setUUId(UUID id) {
		UUId = id;
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

	public long getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(long lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
}
