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
package org.ihtsdo.project.model;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.project.workflow.model.WfMembership;

/**
 * The Class WorklistMetadata.
 */
public class WorklistMetadata implements Serializable{
	/** The name. */
	private String name;
	
	/** The uids. */
	private List<UUID> uids;
	
	/** The partition id. */
	private UUID partitionUUID;
	
	/** The workflow definition file name. */
	private String workflowDefinitionFileName;
	
	/** The workflow user roles. */
	private List<String> workflowUserRoles;

	/**
	 * Instantiates a new worklist metadata.
	 *
	 * @param name the name
	 * @param uids the uids
	 * @param partitionUUID the partition uuid
	 * @param workflowDefinitionFileName the workflow definition file name
	 * @param workflowUserRoles the workflow user roles
	 */
	public WorklistMetadata(String name, List<UUID> uids,
			UUID partitionUUID,
			String workflowDefinitionFileName,
			List<String> workflowUserRoles) {
		super();
		this.name = name;
		this.uids = uids;
		this.partitionUUID = partitionUUID;
		this.workflowDefinitionFileName = workflowDefinitionFileName;
		this.workflowUserRoles = workflowUserRoles;
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
	 * Gets the uids.
	 *
	 * @return the uids
	 */
	public List<UUID> getUids() {
		return uids;
	}

	/**
	 * Sets the uids.
	 *
	 * @param uids the new uids
	 */
	public void setUids(List<UUID> uids) {
		this.uids = uids;
	}

	/**
	 * Gets the partition uuid.
	 *
	 * @return the partition uuid
	 */
	public UUID getPartitionUUID() {
		return partitionUUID;
	}

	/**
	 * Sets the partition uuid.
	 *
	 * @param partitionUUID the new partition uuid
	 */
	public void setPartitionUUID(UUID partitionUUID) {
		this.partitionUUID = partitionUUID;
	}

	/**
	 * Gets the workflow definition file name.
	 *
	 * @return the workflow definition file name
	 */
	public String getWorkflowDefinitionFileName() {
		return workflowDefinitionFileName;
	}

	/**
	 * Sets the workflow definition file name.
	 *
	 * @param workflowDefinitionFileName the new workflow definition file name
	 */
	public void setWorkflowDefinitionFileName(String workflowDefinitionFileName) {
		this.workflowDefinitionFileName = workflowDefinitionFileName;
	}

	/**
	 * Gets the workflow user roles.
	 *
	 * @return the workflow user roles
	 */
	public List<String> getWorkflowUserRoles() {
		return workflowUserRoles;
	}

	/**
	 * Sets the workflow user roles.
	 *
	 * @param workflowUserRoles the new workflow user roles
	 */
	public void setWorkflowUserRoles(List<String> workflowUserRoles) {
		this.workflowUserRoles = workflowUserRoles;
	}
}
