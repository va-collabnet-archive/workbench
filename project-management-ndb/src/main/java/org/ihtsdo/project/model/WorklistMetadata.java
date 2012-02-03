package org.ihtsdo.project.model;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.project.workflow.model.WfMembership;
import org.ihtsdo.project.workflow.model.WorkflowDefinition;

public class WorklistMetadata implements Serializable{
	/** The name. */
	private String name;
	
	/** The uids. */
	private List<UUID> uids;
	
	/** The partition id. */
	private UUID partitionUUID;
	
	private String workflowDefinitionFileName;
	
	private List<WfMembership> workflowUserRoles;

	public WorklistMetadata(String name, List<UUID> uids,
			UUID partitionUUID,
			String workflowDefinitionFileName,
			List<WfMembership> workflowUserRoles) {
		super();
		this.name = name;
		this.uids = uids;
		this.partitionUUID = partitionUUID;
		this.workflowDefinitionFileName = workflowDefinitionFileName;
		this.workflowUserRoles = workflowUserRoles;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<UUID> getUids() {
		return uids;
	}

	public void setUids(List<UUID> uids) {
		this.uids = uids;
	}

	public UUID getPartitionUUID() {
		return partitionUUID;
	}

	public void setPartitionUUID(UUID partitionUUID) {
		this.partitionUUID = partitionUUID;
	}

	public String getWorkflowDefinitionFileName() {
		return workflowDefinitionFileName;
	}

	public void setWorkflowDefinitionFileName(String workflowDefinitionFileName) {
		this.workflowDefinitionFileName = workflowDefinitionFileName;
	}

	public List<WfMembership> getWorkflowUserRoles() {
		return workflowUserRoles;
	}

	public void setWorkflowUserRoles(List<WfMembership> workflowUserRoles) {
		this.workflowUserRoles = workflowUserRoles;
	}
}
